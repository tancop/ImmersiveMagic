package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.recipes.SacrificeRecipe
import dev.tancop.immersivemagic.recipes.SacrificeRecipeInput
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.util.RecipeMatcher

object SacrificeMechanics {
    fun handleEntityDeath(level: Level, pos: BlockPos, deadEntity: LivingEntity, player: Player) {
        val corePos = if (level.getBlockState(pos).`is`(Blocks.GOLD_BLOCK)) {
            pos.takeIf { isValidAltarCore(level, pos) }
        } else {
            findPossibleCores(level, pos).firstOrNull { isValidAltarCore(level, it) }
        }

        if (corePos == null) return

        val start = corePos.above().north().west()
        val end = corePos.above().south().east()

        val area = AABB(
            Vec3(start.x.toDouble(), start.y - 0.5, start.z.toDouble()),
            Vec3(end.x + 1.0, end.y + 1.0, end.z + 1.0)
        )

        @Suppress("UNCHECKED_CAST") // all returned entities are items
        val droppedItems = level.getEntities(null, area) {
            it.type == EntityType.ITEM
        } as List<ItemEntity>

        val stacks = droppedItems.map { it.item }

        val matchResult = tryGetMatchingRecipe(level, player, deadEntity, stacks, droppedItems)

        val recipe = matchResult?.recipe
        if (recipe == null) return

        val result = recipe.result

        val spawnPos = corePos.above()
        (0..3).forEach { _ ->
            level.addFreshEntity(LightningBolt(EntityType.LIGHTNING_BOLT, level).apply {
                setVisualOnly(true)
                setPos(
                    spawnPos.center
                )
            })
        }

        level.addFreshEntity(
            ItemEntity(
                level, spawnPos.x.toDouble(),
                spawnPos.y.toDouble(), spawnPos.z.toDouble(), result.copy()
            )
        )

        player.giveExperiencePoints(-recipe.xpCost)

        if (recipe.items == null) return

        val matches = RecipeMatcher.findMatches(matchResult.stacks, recipe.items) ?: return
        shrinkStacks(recipe, matchResult.stacks, matches, matchResult.droppedItems, level)
    }

    fun shrinkStacks(
        recipe: SacrificeRecipe,
        stacks: List<ItemStack>,
        matches: IntArray,
        droppedItems: List<ItemEntity>,
        level: Level
    ) {
        if (recipe.items == null) return

        for (i in 0..<matches.size) {
            val match = matches[i]
            val ingredient = recipe.items[match]
            val stack = stacks[i]

            for (item in ingredient.items) {
                if (item.item == stack.item) {
                    stack.shrink(item.count)

                    val itemEntity = droppedItems[i]
                    val itemPos = itemEntity.position()
                    val yRotate = itemEntity.yRot
                    itemEntity.kill()

                    level.addFreshEntity(
                        ItemEntity(
                            level, itemPos.x, itemPos.y, itemPos.z, stack, 0.0, 0.0, 0.0
                        ).apply {
                            yRot = yRotate
                        }
                    )
                }
            }
        }
    }

    class MatchResult(val recipe: SacrificeRecipe, val stacks: List<ItemStack>, val droppedItems: List<ItemEntity>)

    fun tryGetMatchingRecipe(
        level: Level,
        player: Player,
        deadEntity: LivingEntity,
        stacks: List<ItemStack>,
        droppedItems: List<ItemEntity>
    ): MatchResult? {
        val recipes = level.recipeManager

        for (holder in recipes.getAllRecipesFor(ImmersiveMagic.SACRIFICE.get())) {
            println("checking recipe ${holder.id}")
            val recipe = holder.value
            // Ignore unrelated items and pass an empty list if the recipe doesn't need any
            val filteredStacks = stacks.filter { stack -> recipe.items?.any { it.test(stack) } ?: false }
            val filteredEntities =
                droppedItems.filterIndexed { idx, _ -> recipe.items?.any { it.test(stacks[idx]) } ?: false }

            val input = SacrificeRecipeInput(
                player,
                deadEntity,
                filteredStacks
            )

            if (recipe.matches(input, level)) {
                return MatchResult(recipe, filteredStacks, filteredEntities)
            }
        }

        return null
    }

    // Finds all possible cores around a slab
    fun findPossibleCores(level: Level, pos: BlockPos): Set<BlockPos> {
        val cores = mutableSetOf<BlockPos>()
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -2..0) {
                    val checkPos = pos.east(x).north(y).above(z)
                    if (level.getBlockState(checkPos).`is`(Blocks.GOLD_BLOCK)) {
                        cores.add(checkPos)
                    }
                }
            }
        }
        return cores
    }

    // Checks if all blocks around the core gold block are polished diorite "marble" slabs.
    // Please don't try and draw the check pattern (unless you're Kanye)
    fun isValidAltarCore(level: Level, goldPos: BlockPos): Boolean =
        level.getBlockState(goldPos).`is`(Blocks.GOLD_BLOCK)
                && level.getBlockState(goldPos.north()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.north().east()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.east()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.east().south()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.south()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.south().west()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.west()).`is`(Blocks.POLISHED_DIORITE_SLAB)
                && level.getBlockState(goldPos.west().north()).`is`(Blocks.POLISHED_DIORITE_SLAB)
}
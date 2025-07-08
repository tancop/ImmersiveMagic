package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.recipes.SacrificeRecipeInput
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.util.RecipeMatcher
import kotlin.jvm.optionals.getOrNull

object SacrificeMechanics {
    fun handleEntityDeath(level: Level, pos: BlockPos, deadEntity: LivingEntity, player: Player) {
        val corePos = if (level.getBlockState(pos).`is`(Blocks.GOLD_BLOCK)) {
            pos.takeIf { isValidAltarCore(level, pos) }
        } else if (level.getBlockState(pos).`is`(Blocks.POLISHED_DIORITE_SLAB)) {
            findPossibleCores(level, pos).firstOrNull { isValidAltarCore(level, it) }
        } else {
            null
        }

        if (corePos != null) {
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

            val recipes = level.recipeManager
            val input = SacrificeRecipeInput(player, deadEntity, stacks)

            val recipe = recipes.getRecipeFor(ImmersiveMagic.SACRIFICE.get(), input, level)
                .getOrNull()?.value

            if (recipe != null) {
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

                if (recipe.items != null) {
                    RecipeMatcher.findMatches(stacks, recipe.items)?.let { matches ->
                        for (i in 0..<matches.size) {
                            val match = matches[i]
                            val ingredient = recipe.items[match]
                            val stack = stacks[i]

                            for (item in ingredient.items) {
                                if (item.item == stack.item) {
                                    stack.shrink(item.count)

                                    val itemEntity = droppedItems[i]
                                    val itemPos = itemEntity.position()
                                    itemEntity.kill()

                                    level.addFreshEntity(
                                        ItemEntity(
                                            level, itemPos.x, itemPos.y, itemPos.z, stack
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Finds all possible cores around a slab
    fun findPossibleCores(level: Level, pos: BlockPos): Set<BlockPos> {
        val cores = mutableSetOf<BlockPos>()
        for (x in -1..1) {
            for (y in -1..1) {
                val checkPos = pos.east(x).north(y)
                if (level.getBlockState(checkPos).`is`(Blocks.GOLD_BLOCK)) {
                    cores.add(checkPos)
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
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
            val recipes = level.recipeManager
            val input = SacrificeRecipeInput(player, deadEntity)

            val recipe = recipes.getRecipeFor(ImmersiveMagic.SACRIFICE.get(), input, level)
                .getOrNull()?.value
            val result = recipe?.result

            if (result != null) {
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
                        spawnPos.y.toDouble(), spawnPos.z.toDouble(), result
                    )
                )

                player.giveExperiencePoints(-recipe.xpCost)
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
package dev.tancop.immersivemagic

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks

object SacrificeMechanics {
    fun handleEntityDeath(level: Level, pos: BlockPos, deadEntity: LivingEntity) {
        val corePos = if (level.getBlockState(pos).`is`(Blocks.GOLD_BLOCK)) {
            if (isValidAltarCore(level, pos)) {
                pos
            } else {
                null
            }
        } else if (level.getBlockState(pos).`is`(Blocks.POLISHED_DIORITE_SLAB)) {
            findPossibleCores(level, pos).firstOrNull { isValidAltarCore(level, it) }
        } else {
            null
        }

        if (corePos != null) {
            println("Entity died on altar")
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
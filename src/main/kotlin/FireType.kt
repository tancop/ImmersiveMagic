package dev.tancop.immersivemagic

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.SoulFireBlock

enum class FireType : Comparable<FireType> {
    NONE, NORMAL, SOUL, BLAZE;

    companion object {
        val BLAZE_BURNER: ResourceLocation = ResourceLocation.fromNamespaceAndPath("create", "blaze_burner")

        fun getFromBlock(level: Level, blockPos: BlockPos): FireType {
            val state = level.getBlockState(blockPos)
            val block = state.block

            val fireType = when (block) {
                is CampfireBlock -> NORMAL
                is FireBlock -> NORMAL
                is SoulFireBlock -> SOUL
                else -> NONE
            }

            if (BuiltInRegistries.BLOCK.containsKey(BLAZE_BURNER)) {
                val burnerBlock = BuiltInRegistries.BLOCK.get(BLAZE_BURNER)

                if (state.`is`(burnerBlock)) {
                    return BLAZE
                }
            }

            // Unlit campfires don't work
            if (block is CampfireBlock && !state.getValue(CampfireBlock.LIT)) return NONE
            // Set type to soul if it's a soul fire
            if (state.`is`(Blocks.SOUL_CAMPFIRE)) return SOUL

            return fireType
        }
    }
}
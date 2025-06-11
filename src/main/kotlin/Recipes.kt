package dev.tancop.immersivemagic

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
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

object Recipes {
    val recipes: Map<Set<Item>, Pair<FireType, PotionRef>> = mapOf(
        setOf(Items.MAGMA_CREAM) to Pair(FireType.NORMAL, PotionRef.of(Potions.FIRE_RESISTANCE)),
        setOf(Items.LIGHT_BLUE_DYE, Items.SUGAR) to Pair(FireType.NORMAL, PotionRef.of(Potions.SWIFTNESS)),
        setOf(Items.WITHER_ROSE) to Pair(
            FireType.SOUL, PotionRef.of(
                "potion.immersivemagic.decay_potion",
                listOf(
                    MobEffectInstance(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156)
            )
        ),
        setOf(Items.WITHER_ROSE, Items.GUNPOWDER) to Pair(
            FireType.SOUL, PotionRef.of(
                "potion.immersivemagic.splash_decay_potion",
                listOf(
                    MobEffectInstance(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156),
                PotionType.SPLASH
            )
        ),
    )

    val acceptedItems = recipes.keys.reduce { acc, set -> acc union set }

    fun tryGetPotion(items: Set<Item>, fireType: FireType): PotionRef? {
        val recipe = recipes[items] ?: return null
        if (fireType >= recipe.first) {
            return recipe.second
        }
        return null
    }
}
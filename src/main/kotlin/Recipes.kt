package dev.tancop.immersivemagic

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

enum class FireType : Comparable<FireType> {
    NORMAL, SOUL, BLAZE,
}

object Recipes {
    val recipes = mapOf(
        setOf(Items.MAGMA_CREAM) to Pair(FireType.NORMAL, PotionRef.of(Potions.FIRE_RESISTANCE)),
        setOf(Items.LIGHT_BLUE_DYE, Items.SUGAR) to Pair(FireType.NORMAL, PotionRef.of(Potions.SWIFTNESS)),
        setOf(Items.WITHER_ROSE) to Pair(
            FireType.SOUL, PotionRef.of(
                "potion.immersivemagic.decay_potion",
                listOf(
                    MobEffectInstance(MobEffects.WITHER, 800)
                ),
                0x736156
            )
        ),
    )

    val acceptedItems = recipes.keys.reduce { acc, set -> acc union set }
}
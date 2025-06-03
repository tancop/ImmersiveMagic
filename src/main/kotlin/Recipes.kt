package dev.tancop.immersivemagic

import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items

object Recipes {
    val recipes = mapOf(
        setOf(Items.MAGMA_CREAM) to MobEffects.FIRE_RESISTANCE,
        setOf(Items.LIGHT_BLUE_DYE, Items.SUGAR) to MobEffects.MOVEMENT_SPEED,
    )

    val acceptedItems = recipes.keys.reduce { acc, set -> acc union set }
}
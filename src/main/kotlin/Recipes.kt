package dev.tancop.immersivemagic

import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

enum class FireType {
    NORMAL, SOUL, BLAZE,
}

object Recipes {
    val recipes = mapOf(
        setOf(Items.MAGMA_CREAM) to Pair(FireType.NORMAL, Potions.FIRE_RESISTANCE),
        setOf(Items.LIGHT_BLUE_DYE, Items.SUGAR) to Pair(FireType.NORMAL, Potions.SWIFTNESS),
    )

    val acceptedItems = recipes.keys.reduce { acc, set -> acc union set }
}
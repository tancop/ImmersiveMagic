package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionEffect
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.util.FastColor
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient
import java.util.concurrent.CompletableFuture

class BrewingRecipeProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    RecipeProvider(output, registries) {

    override fun buildRecipes(output: RecipeOutput) {
        addWitherPotions(output)

        addVanillaPotion(output, "healing", Potions.HEALING, Items.GLISTERING_MELON_SLICE)
        addVanillaPotion(
            output,
            "strong_healing",
            Potions.STRONG_HEALING,
            Items.GLISTERING_MELON_SLICE,
            Items.GLOWSTONE_DUST
        )
        
        addVanillaPotion(output, "fire_resistance", Potions.FIRE_RESISTANCE, Items.MAGMA_CREAM)
        addVanillaPotion(
            output,
            "long_fire_resistance",
            Potions.LONG_FIRE_RESISTANCE,
            Items.MAGMA_CREAM,
            Items.REDSTONE
        )

        addVanillaPotion(output, "poison", Potions.POISON, Items.SPIDER_EYE)
    }

    fun addWitherPotions(output: RecipeOutput) {
        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.WITHER_ROSE)),
            FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_potion",
                listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156)
            )
        ).save(output, "decay_potion")

        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.WITHER_ROSE), Ingredient.of(Items.GUNPOWDER)),
            FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_splash_potion",
                listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156),
                type = PotionRef.PotionType.SPLASH
            )
        ).save(output, "decay_splash_potion")

        BrewingRecipeBuilder(
            listOf(
                Ingredient.of(Items.WITHER_ROSE),
                Ingredient.of(Items.GUNPOWDER),
                Ingredient.of(Items.DRAGON_BREATH)
            ),
            FireType.SOUL,
            PotionRef.of(
                "potion.immersivemagic.decay_lingering_potion",
                listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156),
                type = PotionRef.PotionType.LINGERING
            )
        ).save(output, "decay_lingering_potion")
    }

    fun addVanillaPotion(output: RecipeOutput, id: String, result: Holder<Potion>, vararg items: Item) {
        BrewingRecipeBuilder(
            items.map { Ingredient.of(it) },
            FireType.SOUL,
            PotionRef.of(result)
        ).save(output, "${id}_potion")

        BrewingRecipeBuilder(
            items.map { Ingredient.of(it) } + Ingredient.of(Items.GUNPOWDER),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.SPLASH)
        ).save(output, "${id}_splash_potion")

        BrewingRecipeBuilder(
            items.map { Ingredient.of(it) } + Ingredient.of(Items.GUNPOWDER) + Ingredient.of(Items.DRAGON_BREATH),
            FireType.SOUL,
            PotionRef.of(result, PotionRef.PotionType.LINGERING)
        ).save(output, "${id}_lingering_potion")
    }
}
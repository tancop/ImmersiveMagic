package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionEffect
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.util.FastColor
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient
import java.util.concurrent.CompletableFuture

class BrewingRecipeProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    RecipeProvider(output, registries) {

    override fun buildRecipes(output: RecipeOutput) {
        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.WITHER_ROSE)),
            FireType.NORMAL,
            PotionRef.Companion.of(
                "potion.immersivemagic.decay_potion",
                listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156)
            )
        ).save(output, "decay_potion")

        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.WITHER_ROSE), Ingredient.of(Items.GUNPOWDER)),
            FireType.NORMAL,
            PotionRef.Companion.of(
                "potion.immersivemagic.splash_decay_potion",
                listOf(
                    PotionEffect(MobEffects.WITHER, 800)
                ),
                FastColor.ARGB32.opaque(0x736156),
                type = PotionRef.PotionType.SPLASH
            )
        ).save(output, "splash_decay_potion")

        BrewingRecipeBuilder(
            listOf(Ingredient.of(Items.SUGAR), Ingredient.of(Items.LIGHT_BLUE_DYE)),
            FireType.NORMAL,
            PotionRef.Companion.of(Potions.SWIFTNESS)
        ).save(output, "meth")
    }
}
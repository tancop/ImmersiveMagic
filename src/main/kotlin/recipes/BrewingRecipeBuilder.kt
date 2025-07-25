package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient


class BrewingRecipeBuilder(
    val ingredients: List<Ingredient>,
    val fireType: FireType,
    val result: PotionRef
) {
    fun save(output: RecipeOutput, id: ResourceLocation) {
        val recipe = BrewingRecipe(ingredients, fireType, result)
        output.accept(id, recipe, null)
    }

    fun save(output: RecipeOutput, id: String) = save(output, ResourceLocation.parse(id))
}
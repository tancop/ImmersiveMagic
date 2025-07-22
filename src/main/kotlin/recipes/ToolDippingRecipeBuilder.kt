package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

class ToolDippingRecipeBuilder(
    val potion: PotionRef,
    val item: Ingredient,
    val components: DataComponentPatch?,
    val serverComponents: DataComponentPatch?,
    val bottlesUsed: Int = 1
) {
    fun save(output: RecipeOutput, id: ResourceLocation) {
        val recipe = ToolDippingRecipe(potion, item, components, serverComponents)
        output.accept(id, recipe, null)
    }

    fun save(output: RecipeOutput, id: String) = save(output, ResourceLocation.parse(id))
}
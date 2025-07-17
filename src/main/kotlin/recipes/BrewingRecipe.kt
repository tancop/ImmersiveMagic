package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.ImmersiveMagic
import dev.tancop.immersivemagic.MaybeSerializable
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class BrewingRecipe(
    val ingredients: List<Ingredient>,
    val fireType: FireType,
    val result: PotionRef
) :
    Recipe<BrewingRecipeInput>, MaybeSerializable {
    override fun matches(
        input: BrewingRecipeInput,
        level: Level
    ): Boolean {
        val worldFireType = FireType.getFromBlock(level, input.entity.blockPos.below())
        if (worldFireType < fireType) return false

        val usedItems = mutableListOf<ItemStack>()

        for (ingredient in ingredients) {
            var found = false
            for (stack in input.entity.items) {
                if (usedItems.contains(stack)) {
                    continue
                }
                if (ingredient.test(stack)) {
                    found = true
                    usedItems.add(stack)
                    break
                }
            }
            if (!found) return false
        }

        return true
    }

    override fun assemble(input: BrewingRecipeInput, registries: HolderLookup.Provider): ItemStack = result.getStack()

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.BREWING_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.BREWING.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = result.getStack()

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false

    companion object {
        private var cachedItems: Set<Ingredient>? = null

        fun getAcceptedIngredients(level: Level): Set<Ingredient> {
            if (cachedItems != null) return cachedItems!!

            val recipes = level.recipeManager.getAllRecipesFor(ImmersiveMagic.BREWING.get())

            val items = mutableSetOf<Ingredient>()
            for (recipe in recipes) {
                items.addAll(recipe.value.ingredients)
            }

            cachedItems = items
            return items
        }
    }
}
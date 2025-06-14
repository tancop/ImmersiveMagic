package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.*
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class BrewingRecipe(val ingredients: List<Ingredient>, val fireType: FireType, val result: PotionRef) :
    Recipe<BrewingRecipeInput>, MaybeSerializable {
    override fun matches(
        input: BrewingRecipeInput,
        level: Level
    ): Boolean {
        val worldFireType = FireType.Companion.getFromBlock(level, input.entity.blockPos.below())
        if (worldFireType < fireType) return false

        val inputCounts = input.entity.items.groupingBy { it }.eachCount()
        val recipeCounts = ingredients.groupingBy { it }.eachCount()

        return inputCounts == recipeCounts
    }

    override fun assemble(input: BrewingRecipeInput, registries: HolderLookup.Provider): ItemStack = result.getStack()

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.Companion.BREWING_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.Companion.BREWING.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = result.getStack()

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false
}
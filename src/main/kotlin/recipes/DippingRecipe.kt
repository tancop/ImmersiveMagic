package dev.tancop.immersivemagic.recipes

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
import net.minecraft.world.level.block.LayeredCauldronBlock

class DippingRecipe(
    val potion: PotionRef,
    val item: Ingredient,
    val result: ItemStack,
    override val bottlesUsed: Int = 1
) :
    Recipe<DippingRecipeInput>, MaybeSerializable, DippingRecipeInterface {
    override fun matches(
        input: DippingRecipeInput,
        level: Level
    ): Boolean = input.entity.storedPotion?.let {
        it == potion
                && item.test(input.item)
                && input.entity.blockState.getValue(LayeredCauldronBlock.LEVEL) >= bottlesUsed
    } ?: false

    override fun assemble(input: DippingRecipeInput, registries: HolderLookup.Provider): ItemStack = result.copy()

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.DIPPING_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.DIPPING.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = result.copy()

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false
}
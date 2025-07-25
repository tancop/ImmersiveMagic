package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.LayeredCauldronBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class BrewingRecipeInput(val entity: LayeredCauldronBlockEntity) :
    RecipeInput {
    override fun getItem(index: Int): ItemStack {
        return entity.items.elementAt(index)
    }

    override fun size(): Int = entity.items.size
}
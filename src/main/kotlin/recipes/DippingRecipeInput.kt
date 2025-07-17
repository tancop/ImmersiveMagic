package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.LayeredCauldronBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class DippingRecipeInput(val entity: LayeredCauldronBlockEntity, val item: ItemStack) : RecipeInput {
    override fun getItem(index: Int): ItemStack {
        if (index == entity.items.size) {
            return item.copy()
        }

        return entity.items.elementAt(index)
    }

    override fun size(): Int = entity.items.size + 1
}
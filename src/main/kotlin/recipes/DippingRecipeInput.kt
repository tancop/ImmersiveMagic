package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.LayeredCauldronBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class DippingRecipeInput(val entity: LayeredCauldronBlockEntity, val container: ItemStack) : RecipeInput {
    override fun getItem(index: Int): ItemStack {
        if (index == entity.items.size) {
            return container.copy()
        }
        
        return entity.items.elementAt(index)
    }

    override fun size(): Int = entity.items.size + 1
}
package dev.tancop.immersivemagic.recipes

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class SacrificeRecipeInput(val player: Player, val entity: Entity, val items: List<ItemStack>) : RecipeInput {
    // No items in this recipe type!
    override fun getItem(index: Int): ItemStack = ItemStack.EMPTY.copy()

    override fun size(): Int = 0

    // The default implementations returns true because no items. This is wrong
    // for item-less recipes and means no input can ever match a recipe
    override fun isEmpty(): Boolean = false
}
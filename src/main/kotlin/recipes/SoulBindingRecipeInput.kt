package dev.tancop.immersivemagic.recipes

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class SoulBindingRecipeInput(val player: Player, val killedEntity: Entity) : RecipeInput {
    // No items in this recipe type!
    override fun getItem(index: Int): ItemStack = ItemStack.EMPTY.copy()

    override fun size(): Int = 0

    override fun isEmpty(): Boolean = false
}
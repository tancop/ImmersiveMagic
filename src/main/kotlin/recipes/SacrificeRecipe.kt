package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.ImmersiveMagic
import dev.tancop.immersivemagic.MaybeSerializable
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class SacrificeRecipe(
    val entity: Holder<EntityType<*>>,
    val xpCost: Int,
    val result: ItemStack,
) :
    Recipe<SacrificeRecipeInput>, MaybeSerializable {
    override fun matches(
        input: SacrificeRecipeInput,
        level: Level
    ): Boolean = input.entity.type == entity.value() && input.player.totalExperience >= xpCost

    override fun assemble(input: SacrificeRecipeInput, registries: HolderLookup.Provider): ItemStack = result.copy()

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.Companion.SACRIFICE_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.Companion.SACRIFICE.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = result.copy()

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false
}
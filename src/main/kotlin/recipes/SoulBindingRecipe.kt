package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.ImmersiveMagic
import dev.tancop.immersivemagic.ImmersiveMagic.Companion.EMPTY_SCROLL
import dev.tancop.immersivemagic.MaybeSerializable
import dev.tancop.immersivemagic.ServerComponentMap
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class SoulBindingRecipe(
    val entity: Holder<EntityType<*>>,
    val xpCost: Int,
    val result: ItemStack
) :
    Recipe<SoulBindingRecipeInput>, MaybeSerializable {
    override fun matches(
        input: SoulBindingRecipeInput,
        level: Level
    ): Boolean =
        input.killedEntity.type == entity.value()
                && input.player.totalExperience >= xpCost
                && canReceiveSpell(input.player.getItemInHand(InteractionHand.OFF_HAND))

    fun canReceiveSpell(stack: ItemStack): Boolean {
        val map = ServerComponentMap.fromStack(stack)
        if (map.has(EMPTY_SCROLL.get())) return true
        return map.allKeys.any { ImmersiveMagic.SPELL_COMPONENTS_REGISTRY.containsKey(ResourceLocation.parse(it)) }
    }

    override fun assemble(input: SoulBindingRecipeInput, registries: HolderLookup.Provider): ItemStack = result.copy()

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.SOUL_BINDING_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.SOUL_BINDING.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = result.copy()

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false
}
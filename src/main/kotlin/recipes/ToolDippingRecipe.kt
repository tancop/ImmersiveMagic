package dev.tancop.immersivemagic.recipes

import dev.tancop.immersivemagic.ImmersiveMagic
import dev.tancop.immersivemagic.MaybeSerializable
import dev.tancop.immersivemagic.PotionRef
import dev.tancop.immersivemagic.ServerComponentMap
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LayeredCauldronBlock

class ToolDippingRecipe(
    val potion: PotionRef,
    val item: Ingredient,
    val components: DataComponentPatch?,
    val serverComponents: DataComponentPatch?,
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

    override fun assemble(input: DippingRecipeInput, registries: HolderLookup.Provider): ItemStack {
        val stack = input.item.copy()

        if (components != null) {
            stack.applyComponents(components)
        }

        if (serverComponents != null) {
            val serverMap = ServerComponentMap.fromStack(stack)

            for ((key, value) in serverComponents.entrySet()) {
                if (value.isPresent) {
                    @Suppress("UNCHECKED_CAST") // Keys are never null so casting to `Any` always works
                    serverMap.set(key as DataComponentType<Any>, value.get())
                } else {
                    // Empty optional means remove
                    serverMap.remove(key)
                }
            }

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(serverMap.encode()))
        }

        return stack
    }

    // No way to make these work with recipe books
    override fun isSpecial(): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = ImmersiveMagic.TOOL_DIPPING_SERIALIZER.get()

    override fun getType(): RecipeType<*> = ImmersiveMagic.TOOL_DIPPING.get()

    // No dimension requirements
    override fun canCraftInDimensions(x: Int, y: Int): Boolean = true

    // I don't know what to put in this one, these recipes return a different stack every time
    override fun getResultItem(p0: HolderLookup.Provider): ItemStack = ItemStack(Items.AIR)

    // This is a server-side mod, syncing recipes to the client can disconnect it
    override fun immersiveMagic_isSerializable(): Boolean = false
}
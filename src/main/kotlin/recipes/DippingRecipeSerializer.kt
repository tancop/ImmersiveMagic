package dev.tancop.immersivemagic.recipes

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer

class DippingRecipeSerializer : RecipeSerializer<DippingRecipe> {
    override fun codec(): MapCodec<DippingRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, DippingRecipe> =
        throw NotImplementedError("DippingRecipe is not stream serializable")

    companion object {
        val CODEC: MapCodec<DippingRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PotionRef.CODEC.fieldOf("potion").forGetter { it.potion },
                Ingredient.CODEC.fieldOf("container").forGetter(DippingRecipe::container),
                ItemStack.CODEC.fieldOf("result").forGetter(DippingRecipe::result),
            ).apply(instance) { potion, container, result ->
                DippingRecipe(
                    potion ?: throw IllegalStateException("Recipe result is null"),
                    container,
                    result
                )
            }
        }
    }
}

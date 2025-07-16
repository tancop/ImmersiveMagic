package dev.tancop.immersivemagic.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import java.util.*

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
                Codec.INT.optionalFieldOf("bottles_used").forGetter { Optional.of(it.bottlesUsed) }
            ).apply(instance) { potion, container, result, bottlesUsed ->
                DippingRecipe(
                    potion ?: throw IllegalStateException("Recipe result is null"),
                    container,
                    result,
                    bottlesUsed.orElse(1),
                )
            }
        }
    }
}

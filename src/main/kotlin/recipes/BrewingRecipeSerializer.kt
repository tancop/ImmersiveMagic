package dev.tancop.immersivemagic.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.tancop.immersivemagic.FireType
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer

class BrewingRecipeSerializer : RecipeSerializer<BrewingRecipe> {
    override fun codec(): MapCodec<BrewingRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> =
        throw NotImplementedError("BrewingRecipe is not stream serializable")

    companion object {
        val CODEC: MapCodec<BrewingRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(BrewingRecipe::ingredients),
                Codec.STRING.fieldOf("fireType").forGetter { it.fireType.name },
                PotionRef.Companion.CODEC.fieldOf("result").forGetter { DataResult.success(it.result) }
            ).apply(instance) { items, fireType, result ->
                BrewingRecipe(
                    items,
                    FireType.valueOf(fireType),
                    result.getOrThrow { IllegalStateException("Empty result in recipe: ${result.error()}") },
                )
            }
        }
    }
}

package dev.tancop.immersivemagic.recipes

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.tancop.immersivemagic.PotionRef
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ToolDippingRecipeSerializer : RecipeSerializer<ToolDippingRecipe> {
    override fun codec(): MapCodec<ToolDippingRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, ToolDippingRecipe> =
        throw NotImplementedError("ToolDippingRecipe is not stream serializable")

    companion object {
        val CODEC: MapCodec<ToolDippingRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PotionRef.CODEC.fieldOf("potion").forGetter { it.potion },
                Ingredient.CODEC.fieldOf("item").forGetter(ToolDippingRecipe::item),
                DataComponentPatch.CODEC.optionalFieldOf("components").forGetter { Optional.ofNullable(it.components) },
                DataComponentPatch.CODEC.optionalFieldOf("server_components")
                    .forGetter { Optional.ofNullable(it.serverComponents) },
            ).apply(instance) { potion, item, components, serverComponents ->
                ToolDippingRecipe(
                    potion ?: throw IllegalStateException("Recipe result is null"),
                    item,
                    components.getOrNull(),
                    serverComponents.getOrNull(),
                )
            }
        }
    }
}
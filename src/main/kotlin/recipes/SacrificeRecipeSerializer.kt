package dev.tancop.immersivemagic.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeSerializer

class SacrificeRecipeSerializer : RecipeSerializer<SacrificeRecipe> {
    override fun codec(): MapCodec<SacrificeRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, SacrificeRecipe> =
        throw NotImplementedError("SacrificeRecipe is not stream serializable")

    companion object {
        val CODEC: MapCodec<SacrificeRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("entity").forGetter { it.entity.key!!.location() },
                Codec.INT.fieldOf("xp_cost").forGetter(SacrificeRecipe::xpCost),
                ItemStack.CODEC.fieldOf("result").forGetter(SacrificeRecipe::result),
            ).apply(instance) { entity, xpCost, result ->
                SacrificeRecipe(
                    BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(BuiltInRegistries.ENTITY_TYPE.get(entity)),
                    xpCost,
                    result
                )
            }
        }
    }
}

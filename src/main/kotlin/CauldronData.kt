package dev.tancop.immersivemagic

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack

data class CauldronData(var items: List<ItemStack>) {
    companion object {
        private val LIST_CODEC: Codec<List<ItemStack>> = ItemStack.CODEC.listOf()
        val CODEC: Codec<CauldronData> =
            RecordCodecBuilder.create<CauldronData> { instance ->
                instance.group(
                    LIST_CODEC.fieldOf("i").forGetter(CauldronData::items),
                ).apply(instance) { CauldronData(it) }
            }
    }
}

data class ChunkData(var items: List<Pair<BlockPos, CauldronData>>) {
    companion object {
        private val PAIR_CODEC: Codec<Pair<BlockPos, CauldronData>> =
            Codec.pair(BlockPos.CODEC.fieldOf("left").codec(), CauldronData.CODEC.fieldOf("right").codec())
        private val LIST_CODEC: Codec<List<Pair<BlockPos, CauldronData>>> =
            PAIR_CODEC.listOf()
        val CODEC: Codec<ChunkData> =
            RecordCodecBuilder.create<ChunkData> { instance ->
                instance.group(
                    LIST_CODEC.fieldOf("i").forGetter(ChunkData::items),
                ).apply(instance) { ChunkData(it) }
            }
    }
}
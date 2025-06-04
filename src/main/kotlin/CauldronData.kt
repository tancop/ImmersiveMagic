package dev.tancop.immersivemagic

import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

data class CauldronData(var items: MutableList<ItemStack>) {
    companion object {
        private val LIST_CODEC: Codec<List<ItemStack>> = ItemStack.CODEC.listOf()
        val CODEC: Codec<CauldronData> =
            RecordCodecBuilder.create<CauldronData> { instance ->
                instance.group(
                    LIST_CODEC.fieldOf("i").forGetter(CauldronData::items),
                ).apply(instance) { CauldronData(it.toMutableList()) }
            }
    }
}

data class ChunkData(var items: MutableMap<BlockPos, CauldronData>) {
    companion object {
        private val MAP_CODEC = Codec.unboundedMap(Codec.STRING, CauldronData.CODEC)
        val CODEC: Codec<ChunkData> =
            RecordCodecBuilder.create<ChunkData> { instance ->
                instance.group(
                    MAP_CODEC.fieldOf("i").forGetter {
                        it.items.mapKeys { (key, value) ->
                            BlockPos.CODEC.encodeStart(JsonOps.INSTANCE, key).result().getOrNull()?.toString()
                        }
                    },
                ).apply(instance) {
                    ChunkData(it.mapKeys { (key, value) ->
                        val tree = ImmersiveMagic.GSON.toJsonTree(key)
                        println("Json tree: $tree")
                        BlockPos.CODEC.decode(JsonOps.INSTANCE, tree)
                            .also { res ->
                                println("decode result: $res")
                            }
                            .result().getOrNull()!!.first
                    }.toMutableMap())
                }
            }
    }
}
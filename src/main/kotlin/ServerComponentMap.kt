package dev.tancop.immersivemagic

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.Util
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

@JvmInline
value class ServerComponentMap(private val compoundTag: CompoundTag) {
    fun has(key: DataComponentType<*>): Boolean =
        compoundTag.contains(Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, key))

    fun <T> get(key: DataComponentType<T>): T? {
        val codec = key.codec() ?: return null
        val name = Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, key)

        val tag = compoundTag.get(name)

        return codec.decode(NbtOps.INSTANCE, tag).result().getOrNull()?.first
            ?: if (tag is StringTag) {
                // Might be JSON, like when reading from a recipe
                codec.decode(
                    JsonOps.INSTANCE,
                    JsonParser.parseString(tag.asString)
                ).result().getOrNull()?.first
            } else {
                null
            }
    }

    fun <T> set(key: DataComponentType<T>, value: T) {
        val codec = key.codec() ?: return
        val name = Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, key)

        val encoded = codec.encodeStart(NbtOps.INSTANCE, value).result().getOrNull()

        if (encoded != null) {
            compoundTag.put(name, encoded)
        }
    }

    fun <T> remove(key: DataComponentType<T>) {
        compoundTag.remove(Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, key))
    }

    fun encode(): CompoundTag = compoundTag.copy()

    val allKeys: Set<String>
        get() = compoundTag.allKeys

    companion object {
        fun fromStack(stack: ItemStack): ServerComponentMap {
            val compoundTag = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: CompoundTag()

            return ServerComponentMap(compoundTag)
        }
    }
}
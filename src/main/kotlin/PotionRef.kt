package dev.tancop.immersivemagic

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import kotlin.jvm.optionals.getOrNull

sealed class PotionRef {
    enum class PotionType {
        NORMAL, SPLASH, LINGERING
    }

    data class Vanilla(val potion: Holder<Potion>, val type: PotionType) : PotionRef() {
        override fun getStack(registries: RegistryAccess): ItemStack {
            val contents = PotionContents(potion)
            val stack = ItemStack(
                when (type) {
                    PotionType.NORMAL -> Items.POTION
                    PotionType.SPLASH -> Items.SPLASH_POTION
                    PotionType.LINGERING -> Items.LINGERING_POTION
                }, 1
            )
            stack.set(DataComponents.POTION_CONTENTS, contents)
            return stack
        }

        override fun getEffectColor(registries: RegistryAccess): Int = PotionContents.getColor(potion)

        companion object {
            val CODEC: Codec<Vanilla> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("potion").forGetter { it.potion.key!!.location() },
                    Codec.STRING.fieldOf("type").forGetter { it.type.name },
                ).apply(instance) { potion, type ->
                    Vanilla(
                        BuiltInRegistries.POTION.wrapAsHolder(
                            BuiltInRegistries.POTION.get(potion)
                                ?: throw IllegalArgumentException("Invalid potion: $potion")
                        ),
                        PotionType.valueOf(type)
                    )
                }
            }
        }
    }

    data class Custom(
        val potion: ResourceLocation,
        val type: PotionType,
    ) : PotionRef() {
        var potionHolder: Holder<CustomPotion>? = null

        fun loadHolder(registries: RegistryAccess) {
            if (potionHolder == null) {
                val registry = registries.registry(ImmersiveMagic.CUSTOM_POTIONS_REGISTRY_KEY).get()

                val holder = registry.wrapAsHolder(
                    registry.get(potion) ?: throw IllegalArgumentException("Unknown potion: $potion")
                )
                potionHolder = holder
            }
        }

        override fun getStack(registries: RegistryAccess): ItemStack {
            loadHolder(registries)
            return potionHolder!!.value().getStack(type)
        }

        override fun getEffectColor(registries: RegistryAccess): Int {
            loadHolder(registries)
            return potionHolder!!.value().color
        }

        companion object {
            val CODEC: Codec<Custom> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("custom_potion").forGetter { it.potion },
                    Codec.STRING.fieldOf("type").forGetter { it.type.name },
                ).apply(instance) { potion, type ->
                    Custom(potion, PotionType.valueOf(type))
                }
            }
        }
    }

    abstract fun getStack(registries: RegistryAccess): ItemStack
    abstract fun getEffectColor(registries: RegistryAccess): Int

    companion object {
        fun of(potion: Holder<Potion>, type: PotionType = PotionType.NORMAL): PotionRef =
            Vanilla(potion, type)

        val CODEC: Codec<PotionRef?> =
            Codec.xor(Vanilla.CODEC, Custom.CODEC).xmap({ either ->
                val left = either.left().getOrNull()
                if (left != null) {
                    return@xmap left
                }
                val right = either.right().getOrNull()
                if (right != null) {
                    return@xmap right
                }
                null
            }, { res ->
                when (res) {
                    is Vanilla -> Either.left(res)
                    is Custom -> Either.right(res)
                    else -> null
                }
            })
    }
}
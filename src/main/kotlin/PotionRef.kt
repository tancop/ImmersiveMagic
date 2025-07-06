package dev.tancop.immersivemagic

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import java.util.*
import kotlin.jvm.optionals.getOrNull

sealed class PotionRef {
    enum class PotionType {
        NORMAL, SPLASH, LINGERING
    }

    data class GamePotion(val potion: Holder<Potion>, val potionType: PotionType) : PotionRef() {
        override fun getStack(): ItemStack {
            val contents = PotionContents(potion)
            val stack = ItemStack(
                when (potionType) {
                    PotionType.NORMAL -> Items.POTION
                    PotionType.SPLASH -> Items.SPLASH_POTION
                    PotionType.LINGERING -> Items.LINGERING_POTION
                }, 1
            )
            stack.set(DataComponents.POTION_CONTENTS, contents)
            return stack
        }

        override fun getEffectColor(): Int = PotionContents.getColor(potion)

        companion object {
            val CODEC: Codec<GamePotion> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("potion").forGetter { it.potion.key!!.location() },
                    Codec.STRING.fieldOf("type").forGetter { it.potionType.name },
                ).apply(instance) { potion, type ->
                    GamePotion(
                        BuiltInRegistries.POTION.wrapAsHolder(
                            BuiltInRegistries.POTION.get(potion)!!
                        ),
                        PotionType.valueOf(type)
                    )
                }
            }
        }
    }

    data class CustomPotion(
        val name: String,
        val effects: List<PotionEffect>,
        val color: Int,
        val type: PotionType
    ) : PotionRef() {
        override fun getStack(): ItemStack {
            val contents = PotionContents(
                Optional.of(Potions.WATER),
                Optional.of(color),
                effects.map { it.toInstance() }
            )
            val stack = ItemStack(
                when (type) {
                    PotionType.NORMAL -> Items.POTION
                    PotionType.SPLASH -> Items.SPLASH_POTION
                    PotionType.LINGERING -> Items.LINGERING_POTION
                }, 1
            )
            stack.set(DataComponents.POTION_CONTENTS, contents)
            stack.set(DataComponents.ITEM_NAME, Component.translatable(name))
            return stack
        }

        override fun getEffectColor(): Int = color

        companion object {
            val CODEC: Codec<CustomPotion> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CustomPotion::name),
                    PotionEffect.CODEC.listOf().fieldOf("effects").forGetter(CustomPotion::effects),
                    ColorCodecs.RGB.fieldOf("color").forGetter(CustomPotion::color),
                    Codec.STRING.fieldOf("type").forGetter { it.type.name },
                ).apply(instance) { name, effects, color, type ->
                    CustomPotion(name, effects, color, PotionType.valueOf(type))
                }
            }
        }
    }

    data class CustomItem(
        val item: ItemStack,
        val color: Int,
    ) : PotionRef() {
        override fun getStack(): ItemStack {
            return item.copy()
        }

        override fun getEffectColor(): Int = color

        companion object {
            val CODEC: Codec<CustomItem> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(CustomItem::item),
                    ColorCodecs.RGB.fieldOf("color").forGetter(CustomItem::color),
                ).apply(instance) { item, color -> CustomItem(item, color) }
            }
        }
    }

    abstract fun getStack(): ItemStack
    abstract fun getEffectColor(): Int

    companion object {
        fun of(potion: Holder<Potion>, type: PotionType = PotionType.NORMAL): PotionRef =
            GamePotion(potion, type)

        fun of(
            name: String,
            effects: List<PotionEffect>,
            color: Int,
            type: PotionType = PotionType.NORMAL
        ): PotionRef =
            CustomPotion(name, effects, color, type)

        fun of(item: ItemStack, color: Int): PotionRef =
            CustomItem(item, color)

        val CODEC: Codec<DataResult<out PotionRef>> =
            Codec.xor(GamePotion.CODEC, Codec.xor(CustomPotion.CODEC, CustomItem.CODEC)).xmap({ either ->
                val left = either.left().getOrNull()
                if (left != null) {
                    return@xmap DataResult.success(left)
                }
                val right = either.right().getOrNull()
                if (right != null) {
                    val innerLeft = right.left().getOrNull()
                    if (innerLeft != null) {
                        return@xmap DataResult.success(innerLeft)
                    }
                    val innerRight = right.right().getOrNull()
                    if (innerRight != null) {
                        return@xmap DataResult.success(innerRight)
                    }
                }
                DataResult.error { "Failed to deserialize PotionRef" }
            }, { res ->
                res.mapOrElse({
                    when (it) {
                        is GamePotion -> Either.left(it)
                        is CustomPotion -> Either.right(Either.left(it))
                        is CustomItem -> Either.right(Either.right(it))
                    }
                }, { Either.left(null) })
            })
    }
}
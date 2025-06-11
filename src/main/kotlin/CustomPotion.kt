package dev.tancop.immersivemagic

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import java.util.*

enum class PotionType {
    NORMAL, SPLASH, LINGERING
}

sealed class PotionRef {
    data class GamePotion(val potion: Holder<Potion>) : PotionRef() {
        override fun getStack(): ItemStack {
            val contents = PotionContents(potion)
            val stack = ItemStack(Items.POTION, 1)
            stack.set(DataComponents.POTION_CONTENTS, contents)
            return stack
        }

        override fun getEffectColor(): Int = PotionContents.getColor(potion)
    }

    data class CustomPotion(
        val name: String,
        val effects: List<MobEffectInstance>,
        val color: Int,
        val type: PotionType
    ) : PotionRef() {
        override fun getStack(): ItemStack {
            val contents = PotionContents(
                Optional.of(Potions.WATER),
                Optional.of(color),
                effects
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
    }

    data class CustomItem(
        val item: ItemStack,
        val color: Int,
    ) : PotionRef() {
        override fun getStack(): ItemStack {
            return item.copy()
        }

        override fun getEffectColor(): Int = color
    }

    abstract fun getStack(): ItemStack
    abstract fun getEffectColor(): Int

    companion object {
        fun of(potion: Holder<Potion>): PotionRef =
            GamePotion(potion)

        fun of(
            name: String,
            effects: List<MobEffectInstance>,
            color: Int,
            type: PotionType = PotionType.NORMAL
        ): PotionRef =
            CustomPotion(name, effects, color, type)

        fun of(item: ItemStack, color: Int): PotionRef =
            CustomItem(item, color)
    }
}
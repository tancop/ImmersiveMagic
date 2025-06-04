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

sealed class PotionRef {
    data class GamePotion(val potion: Holder<Potion>) : PotionRef() {
        override fun getStack(): ItemStack {
            val contents = PotionContents(potion)
            val stack = ItemStack(Items.POTION, 1)
            stack.set(DataComponents.POTION_CONTENTS, contents)
            return stack
        }
    }

    data class CustomPotion(val name: String, val effects: List<MobEffectInstance>, val color: Int) : PotionRef() {
        constructor(name: String, effects: List<MobEffectInstance>, color: Color) : this(
            name,
            effects,
            color.toPacked()
        )

        override fun getStack(): ItemStack {
            val contents = PotionContents(
                Optional.of(Potions.WATER),
                Optional.of(color),
                effects
            )
            val stack = ItemStack(Items.POTION, 1)
            stack.set(DataComponents.POTION_CONTENTS, contents)
            stack.set(DataComponents.ITEM_NAME, Component.translatable(name))
            return stack
        }
    }

    abstract fun getStack(): ItemStack

    companion object {
        fun of(potion: Holder<Potion>): PotionRef =
            GamePotion(potion)

        fun of(name: String, effects: List<MobEffectInstance>, color: Color): PotionRef =
            CustomPotion(name, effects, color)

        fun of(name: String, effects: List<MobEffectInstance>, color: Int): PotionRef =
            CustomPotion(name, effects, color)
    }
}
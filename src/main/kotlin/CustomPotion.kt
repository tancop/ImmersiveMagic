package dev.tancop.immersivemagic

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import java.util.*

data class CustomPotion(
    val name: String,
    val effects: List<PotionEffect>,
    val color: Int
) {
    val id = name.lowercase().replace(" ", "_")

    fun getStack(type: PotionRef.PotionType): ItemStack {
        val contents = PotionContents(
            Optional.of(Potions.WATER),
            Optional.of(color),
            effects.map { it.toInstance() }
        )
        val stack = ItemStack(
            when (type) {
                PotionRef.PotionType.NORMAL -> Items.POTION
                PotionRef.PotionType.SPLASH -> Items.SPLASH_POTION
                PotionRef.PotionType.LINGERING -> Items.LINGERING_POTION
            }, 1
        )
        stack[DataComponents.POTION_CONTENTS] = contents
        stack[DataComponents.ITEM_NAME] = when (type) {
            PotionRef.PotionType.NORMAL -> Component.translatableWithFallback(
                "item.immersivemagic.potion.effect.$id",
                "Potion of $name"
            )

            PotionRef.PotionType.SPLASH -> Component.translatableWithFallback(
                "item.immersivemagic.splash_potion.effect.$id",
                "Splash Potion of $name"
            )

            PotionRef.PotionType.LINGERING -> Component.translatableWithFallback(
                "item.immersivemagic.lingering_potion.effect.$id",
                "Lingering Potion of $name"
            )
        }
        return stack
    }

    companion object {
        val CODEC: Codec<CustomPotion> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter { it.name },
                PotionEffect.CODEC.listOf().fieldOf("effects").forGetter(CustomPotion::effects),
                ColorCodecs.RGB.fieldOf("color").forGetter(CustomPotion::color),
            ).apply(instance) { name, effects, color ->
                CustomPotion(name, effects, color)
            }
        }
    }
}
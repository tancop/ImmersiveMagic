package dev.tancop.immersivemagic

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.EitherCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import kotlin.jvm.optionals.getOrNull

data class DippedWeaponComponent(val charges: Int, val effect: Either<Holder<Potion>, List<PotionEffect>>) :
    LoreProvider {

    fun formatComponent(effects: List<MobEffectInstance>): List<Component> {
        if (effects.isEmpty()) return emptyList()

        val lines = mutableListOf<Component>(
            Component.translatableWithFallback("ui.immersivemagic.dipped_weapon.on_hit", "Applies on hit:")
                .withStyle(TEXT_STYLE)
        )

        PotionContents.addPotionTooltip(
            effects,
            {
                lines.add(
                    MutableComponent.create(it.contents)
                        .withStyle(it.style.withItalic(false))
                )
            },
            1.0f,
            20f
        )

        lines.add(
            Component.literal("($charges ").withStyle(TEXT_STYLE)
                .append(Component.translatableWithFallback("ui.immersivemagic.spell_charges", "charges"))
                .append(Component.literal(")"))
        )

        return lines
    }

    fun withLowerCharge(): DippedWeaponComponent = DippedWeaponComponent(charges - 1, effect)

    fun applyEffects(target: LivingEntity) {
        val holder = effect.left().getOrNull()
        if (holder != null) {
            val potion = holder.value()

            for (effect in potion.effects) {
                target.addEffect(MobEffectInstance(effect))
            }
        }
        val effects = effect.right().getOrNull()
        if (effects != null) {
            val effects = effects.map { it.toInstance() }

            for (effect in effects) {
                target.addEffect(effect)
            }
        }
    }

    override fun getLore(): List<Component> {
        val holder = effect.left().getOrNull()
        if (holder != null) {
            val potion = holder.value()

            return formatComponent(potion.effects)
        }
        val effects = effect.right().getOrNull()
        if (effects != null) {
            val effects = effects.map { it.toInstance() }

            return formatComponent(effects)
        }

        return emptyList()
    }

    companion object {
        val CODEC: MapCodec<DippedWeaponComponent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("charges").forGetter(DippedWeaponComponent::charges),
                EitherCodec(Potion.CODEC, PotionEffect.CODEC.listOf()).fieldOf("effect").forGetter { it.effect },
            ).apply(instance) { charges, effect ->
                DippedWeaponComponent(charges, effect)
            }
        }

        val TEXT_STYLE: Style = Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false)
    }
}
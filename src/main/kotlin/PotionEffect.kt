package dev.tancop.immersivemagic

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance

class PotionEffect(
    val effect: Holder<MobEffect>,
    val duration: Int = 0,
    val amplifier: Int = 0,
    var ambient: Boolean = false,
    val visible: Boolean = true,
    val showIcon: Boolean = visible,
) {
    fun toInstance(): MobEffectInstance =
        MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon)

    companion object {
        val CODEC: Codec<PotionEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("effect").forGetter { it.effect.key!!.location() },
                Codec.INT.orElse(0).fieldOf("duration").forGetter { it.duration },
                Codec.INT.orElse(0).fieldOf("amplifier").forGetter { it.amplifier },
                Codec.BOOL.orElse(false).fieldOf("ambient").forGetter { it.ambient },
                Codec.BOOL.orElse(true).fieldOf("visible").forGetter { it.visible },
                Codec.BOOL.orElse(true).fieldOf("showIcon").forGetter { it.showIcon }
            ).apply(instance) { effect, duration, amplifier, ambient, visible, showIcon ->
                PotionEffect(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                        BuiltInRegistries.MOB_EFFECT.get(effect)!!
                    ), duration, amplifier, ambient, visible, showIcon
                )
            }
        }

        val STREAM_CODEC: StreamCodec<ByteBuf, PotionEffect> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, { it.effect.key!!.location() },
            ByteBufCodecs.INT, { it.duration },
            ByteBufCodecs.INT, { it.amplifier },
            ByteBufCodecs.BOOL, { it.ambient },
            ByteBufCodecs.BOOL, { it.visible },
            ByteBufCodecs.BOOL, { it.showIcon },
            { effect, duration, amplifier, ambient, visible, showIcon ->
                PotionEffect(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                        BuiltInRegistries.MOB_EFFECT.get(effect)!!
                    ),
                    duration, amplifier, ambient, visible, showIcon
                )
            }
        )
    }
}
package dev.tancop.immersivemagic

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.FastColor

object ColorCodecs {
    val RGB: Codec<Int> = RecordCodecBuilder.create { instance ->
        instance.group(
            Codec.INT.fieldOf("red").forGetter { FastColor.ARGB32.red(it) },
            Codec.INT.fieldOf("green").forGetter { FastColor.ARGB32.green(it) },
            Codec.INT.fieldOf("blue").forGetter { FastColor.ARGB32.blue(it) },
        ).apply(instance) { red, green, blue ->
            FastColor.ARGB32.color(red, green, blue)
        }
    }
}
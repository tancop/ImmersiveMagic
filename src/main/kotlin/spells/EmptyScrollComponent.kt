package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec

data class EmptyScrollComponent(val enabled: Boolean = true) {
    companion object {
        val CODEC: Codec<EmptyScrollComponent> = Codec.unit(EmptyScrollComponent())
    }
}
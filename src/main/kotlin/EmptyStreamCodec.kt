package dev.tancop.immersivemagic

import net.minecraft.network.codec.StreamCodec

// Just like `StreamCodec#unit` but doesn't check encoded values
@Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE") // Kotlin wants us to add `& Any` for some reason
class EmptyStreamCodec<B, V>(val returnValue: V) : StreamCodec<B, V> {
    override fun decode(buffer: B): V = returnValue

    override fun encode(buffer: B, value: V) {}
}
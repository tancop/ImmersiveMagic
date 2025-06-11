package dev.tancop.immersivemagic

// Block entities can mark themselves as not serializable for client-bound chunk packets
interface BlockEntityExt {
    @Suppress("FunctionName")
    // Prefix to avoid collision with other mods
    fun immersiveMagic_isSerializable(): Boolean
}
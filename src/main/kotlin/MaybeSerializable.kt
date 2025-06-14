package dev.tancop.immersivemagic

// Used to mark block entities and recipes as not serializable for client-bound chunk packets
interface MaybeSerializable {
    @Suppress("FunctionName")
    // Prefix to avoid collision with other mods
    fun immersiveMagic_isSerializable(): Boolean = true
}
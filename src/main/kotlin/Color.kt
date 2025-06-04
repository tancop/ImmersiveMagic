package dev.tancop.immersivemagic

import net.minecraft.util.FastColor

data class Color(val red: Int, val green: Int, val blue: Int) {
    fun toPacked() = FastColor.ARGB32.color(red, green, blue)
}
package dev.tancop.immersivemagic

import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

interface LoreProvider {
    fun getLore(): Component?
}

fun ItemStack.applyServerComponentLore() {
    val map = ServerComponentMap.fromStack(this)

    for (key in map.allKeys) {
        val type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(key))
        if (type == null) continue

        val component = map.get(type)

        if (component is LoreProvider) {
            val lore = component.getLore()

            if (lore != null) {
                set(DataComponents.LORE, ItemLore(listOf(lore)))
                return
            }
        }
    }
}
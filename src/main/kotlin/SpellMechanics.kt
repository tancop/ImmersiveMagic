package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.spells.SpellComponent
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.component.CustomData
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

fun <T : PlayerInteractEvent> handleSpellCast(
    event: T,
    castFunction: SpellComponent.(T) -> InteractionResult,
    onSuccess: (T) -> Unit
) {
    val heldItem = event.entity.getItemInHand(event.hand)

    val map = ServerComponentMap.fromStack(heldItem) ?: return

    val key = map.allKeys
        .firstOrNull { ImmersiveMagic.SPELL_COMPONENTS_REGISTRY.containsKey(ResourceLocation.parse(it)) }
        ?: return
    val resourceLocation = ResourceLocation.parse(key)

    val componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(resourceLocation) ?: return
    val component = map.get(componentType)

    if (component is SpellComponent) {
        if (component.charges > 0) {
            val result = component.castFunction(event)
            if (result == InteractionResult.SUCCESS) {
                val newComponent = component.withLowerCharge()

                @Suppress("UNCHECKED_CAST") // We already checked the component's type
                map.set(componentType as DataComponentType<SpellComponent>, newComponent)

                heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(map.encode()))
                heldItem.set(DataComponents.LORE, newComponent.getItemLore())

                onSuccess(event)
                return
            }
        }
    }
}
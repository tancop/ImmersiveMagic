package dev.tancop.immersivemagic.spells

import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.component.ItemLore
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

abstract class SpellComponent {
    abstract fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult
    abstract fun castOnBlock(event: PlayerInteractEvent.RightClickBlock): InteractionResult
    abstract fun castOnEntity(event: PlayerInteractEvent.EntityInteractSpecific): InteractionResult

    abstract val charges: Int
    abstract val maxCharges: Int
    abstract fun withLowerCharge(): SpellComponent

    fun getItemLore(): ItemLore = ItemLore(
        listOf(
            Component.literal("${charges}/${maxCharges} "),
            Component.translatable("ui.immersivemagic.spell_charges")
        )
    )
}
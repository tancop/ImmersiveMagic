package dev.tancop.immersivemagic.spells

import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.component.ItemLore
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

abstract class SpellComponent {
    open fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult = InteractionResult.PASS
    open fun castOnBlock(event: PlayerInteractEvent.RightClickBlock): InteractionResult = InteractionResult.PASS
    open fun castOnEntity(event: PlayerInteractEvent.EntityInteractSpecific): InteractionResult = InteractionResult.PASS

    abstract val charges: Int
    abstract val maxCharges: Int
    abstract fun withLowerCharge(): SpellComponent

    fun getItemLore(): ItemLore = ItemLore(
        listOf(
            Component.literal("${charges}/${maxCharges} ").append(
                Component.translatable("ui.immersivemagic.spell_charges")
            ),
        )
    )
}
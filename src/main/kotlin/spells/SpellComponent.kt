package dev.tancop.immersivemagic.spells

import dev.tancop.immersivemagic.LoreProvider
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.InteractionResult
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

abstract class SpellComponent : LoreProvider {
    open fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult = InteractionResult.PASS
    open fun castOnBlock(event: PlayerInteractEvent.RightClickBlock): InteractionResult = InteractionResult.PASS
    open fun castOnEntity(event: PlayerInteractEvent.EntityInteractSpecific): InteractionResult = InteractionResult.PASS

    abstract val charges: Int
    abstract val maxCharges: Int
    abstract fun withLowerCharge(): SpellComponent

    override fun getLore(): List<Component> =
        if (maxCharges == -1) {
            listOf(Component.translatableWithFallback("ui.immersivemagic.infinite_spell_charges", "Infinite charges"))
        } else {
            listOf(
                Component.literal("${charges}/${maxCharges} ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.translatableWithFallback("ui.immersivemagic.spell_charges", "charges"))
            )
        }
}
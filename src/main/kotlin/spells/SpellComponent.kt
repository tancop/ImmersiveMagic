package dev.tancop.immersivemagic.spells

import net.minecraft.world.InteractionResult
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

abstract class SpellComponent {
    abstract fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult
    abstract fun castOnBlock(event: PlayerInteractEvent.RightClickBlock): InteractionResult
    abstract fun castOnEntity(event: PlayerInteractEvent.EntityInteractSpecific): InteractionResult
    abstract val charges: Int
}
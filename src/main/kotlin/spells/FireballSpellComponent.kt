package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.SmallFireball
import net.minecraft.world.level.Level
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

data class FireballSpellComponent(override val charges: Int, val maxCharges: Int = 3) : SpellComponent() {
    fun cast(player: Player, level: Level): InteractionResult {
        val lookAngle = player.lookAngle

        val fireball = SmallFireball(level, player, lookAngle)
        level.addFreshEntity(fireball)

        return InteractionResult.SUCCESS
    }

    override fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult {
        return cast(event.entity, event.level)
    }

    override fun castOnBlock(event: PlayerInteractEvent.RightClickBlock): InteractionResult {
        return cast(event.entity, event.level)
    }

    override fun castOnEntity(event: PlayerInteractEvent.EntityInteractSpecific): InteractionResult {
        return cast(event.entity, event.level)
    }

    companion object {
        val CODEC: MapCodec<FireballSpellComponent?> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("charges").forGetter(FireballSpellComponent::charges),
                Codec.INT.fieldOf("max_charges").forGetter(FireballSpellComponent::maxCharges),
            ).apply(instance) { charges, maxCharges ->
                FireballSpellComponent(charges, maxCharges)
            }
        }
    }
}
package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.projectile.LargeFireball
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import kotlin.jvm.optionals.getOrNull

data class FireballSpellComponent(override val charges: Int, override val maxCharges: Int) : SpellComponent() {
    override fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult {
        val player = event.entity
        val level = event.level

        val lookAngle = player.lookAngle

        val fireball = LargeFireball(level, player, lookAngle, 1).apply {
            setPos(player.eyePosition)
        }
        level.addFreshEntity(fireball)

        return InteractionResult.SUCCESS
    }

    override fun withLowerCharge(): SpellComponent = FireballSpellComponent(charges - 1, maxCharges)

    override fun encodeNbt(): Tag? =
        CODEC.codec().encodeStart(NbtOps.INSTANCE, this).result().getOrNull()

    companion object {
        val CODEC: MapCodec<FireballSpellComponent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("charges").forGetter(FireballSpellComponent::charges),
                Codec.INT.fieldOf("max_charges").forGetter(FireballSpellComponent::maxCharges),
            ).apply(instance) { charges, maxCharges ->
                FireballSpellComponent(charges, maxCharges)
            }
        }
    }
}
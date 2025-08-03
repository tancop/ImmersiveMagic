package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import kotlin.math.PI
import kotlin.math.abs

data class ForcePushSpellComponent(override val charges: Int, override val maxCharges: Int) : SpellComponent() {
    override fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult {
        val player = event.entity

        val yLookAngle = Mth.atan2(player.lookAngle.z, player.lookAngle.x)

        val start = player.eyePosition.add(RANGE, -VERTICAL_RANGE, RANGE)
        val end = player.eyePosition.add(-RANGE, VERTICAL_RANGE, -RANGE)

        val aabb = AABB(start, end)
        val entities = event.level.getEntities(null, aabb)

        for (entity in entities) {
            if (entity is LivingEntity) {
                val delta = entity.position().subtract(player.eyePosition).normalize()

                val yEntityAngle = Mth.atan2(delta.z, delta.x)

                val angleDiff = abs(yEntityAngle - yLookAngle)
                if (angleDiff < MAX_ANGLE_DIFF) {
                    entity.knockback(
                        ((DISTANCE_STRENGTH_FACTOR / entity.distanceTo(player)) - angleDiff * ANGLE_STRENGTH_FACTOR)
                            .coerceIn(MIN_PUSH_STRENGTH..MAX_PUSH_STRENGTH),
                        -delta.x,
                        -delta.z
                    )
                }
            }
        }

        val flatLookAngle = Vec3(player.lookAngle.x, 0.0, player.lookAngle.z).normalize()
        val sideVector = flatLookAngle.cross(Vec3(0.0, 1.0, 0.0)).normalize()

        for (i in -2..2) {
            val offset = flatLookAngle.scale(PARTICLE_DISTANCE - PARTICLE_OFFSET * abs(i))

            val particlePos = player.eyePosition
                .add(offset)
                .add(sideVector.scale(i.toDouble()))

            (event.level as ServerLevel).sendParticles(
                ParticleTypes.GUST,
                particlePos.x,
                particlePos.y - 0.75,
                particlePos.z,
                1,
                0.0,
                0.0,
                0.0,
                0.2
            )

            event.level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.WIND_CHARGE_THROW,
                SoundSource.PLAYERS,
                0.8F,
                0.5F
            )
        }

        return InteractionResult.SUCCESS
    }

    override fun withLowerCharge(): SpellComponent = ForcePushSpellComponent(charges - 1, maxCharges)

    companion object {
        const val RANGE = 15.0
        const val VERTICAL_RANGE = 2.0

        const val DISTANCE_STRENGTH_FACTOR = 12.0
        const val MIN_PUSH_STRENGTH = 0.5
        const val MAX_PUSH_STRENGTH = 2.5

        const val ANGLE_STRENGTH_FACTOR = 5.0
        const val MAX_ANGLE_DIFF = PI / 4

        const val PARTICLE_OFFSET = 0.6
        const val PARTICLE_DISTANCE = 4.0

        val CODEC: MapCodec<ForcePushSpellComponent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("charges").forGetter(ForcePushSpellComponent::charges),
                Codec.INT.fieldOf("max_charges").forGetter(ForcePushSpellComponent::maxCharges),
            ).apply(instance) { charges, maxCharges ->
                ForcePushSpellComponent(charges, maxCharges)
            }
        }
    }
}
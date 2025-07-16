package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.projectile.EvokerFangs
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

data class EvokerFangsSpellComponent(override val charges: Int, override val maxCharges: Int) : SpellComponent() {
    override fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult {
        val player = event.entity
        val level = event.level

        // Keep track of how many fangs we spawned
        var spawnedFangs = 0

        val direction = Vec3(player.lookAngle.x, 0.0, player.lookAngle.z)

        for (i in 0..FANG_COUNT) {
            val fangPos = player.position().add(direction.scale(OFFSET * i))
            val blockPos = BlockPos.containing(fangPos)

            // Try different Y positions to find a valid one
            for (offset in (-MAX_Y_DIFF..MAX_Y_DIFF).reversed()) {
                val checkPos = blockPos.above(offset)
                val lowerPos = checkPos.below()

                // Spawn if supported by a solid block
                if (level.getBlockState(lowerPos).isFaceSturdy(level, lowerPos, Direction.UP)) {
                    // Offset the fangs if spawning on a non-solid block
                    val modelOffset = if (!level.isEmptyBlock(checkPos)) {
                        val lowerState = level.getBlockState(lowerPos)
                        val shape = lowerState.getShape(level, lowerPos)

                        if (!shape.isEmpty) {
                            shape.max(Direction.Axis.Y)
                        } else {
                            0.0
                        }
                    } else {
                        0.0
                    }

                    level.addFreshEntity(
                        EvokerFangs(
                            level,
                            fangPos.x, fangPos.y + offset + modelOffset, fangPos.z,
                            Mth.atan2(direction.z, direction.x).toFloat(), SPAWN_DELAY_TICKS * i, player
                        )
                    )

                    spawnedFangs++
                    break
                }
            }
        }

        // No fangs spawned, don't use a spell charge for balance
        if (spawnedFangs == 0) return InteractionResult.FAIL

        return InteractionResult.SUCCESS
    }

    override fun withLowerCharge(): SpellComponent = EvokerFangsSpellComponent(charges - 1, maxCharges)

    companion object {
        // Distance between fangs
        const val OFFSET = 1.5
        const val FANG_COUNT = 8

        // Delay between fang spawns
        const val SPAWN_DELAY_TICKS = 2

        // Maximum Y difference between player and spawned fangs
        const val MAX_Y_DIFF = 3

        val CODEC: MapCodec<EvokerFangsSpellComponent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("charges").forGetter(EvokerFangsSpellComponent::charges),
                Codec.INT.fieldOf("max_charges").forGetter(EvokerFangsSpellComponent::maxCharges),
            ).apply(instance) { charges, maxCharges ->
                EvokerFangsSpellComponent(charges, maxCharges)
            }
        }
    }
}
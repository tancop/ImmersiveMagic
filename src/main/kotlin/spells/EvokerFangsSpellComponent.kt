package dev.tancop.immersivemagic.spells

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.projectile.EvokerFangs
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import kotlin.jvm.optionals.getOrNull

data class EvokerFangsSpellComponent(override val charges: Int, override val maxCharges: Int) : SpellComponent() {
    override fun cast(event: PlayerInteractEvent.RightClickItem): InteractionResult {
        val player = event.entity
        val level = event.level

        val direction = Vec3(player.lookAngle.x, 0.0, player.lookAngle.z)

        for (i in 0..FANG_COUNT) {
            val fangPos = player.position().add(direction.scale(OFFSET * i))

            level.addFreshEntity(
                EvokerFangs(
                    level,
                    fangPos.x, fangPos.y, fangPos.z,
                    0.0F, SPAWN_DELAY * i, player
                )
            )
        }

        return InteractionResult.SUCCESS
    }

    override fun withLowerCharge(): SpellComponent = EvokerFangsSpellComponent(charges - 1, maxCharges)

    override fun encodeNbt(): Tag? =
        CODEC.codec().encodeStart(NbtOps.INSTANCE, this).result().getOrNull()

    companion object {
        const val OFFSET = 1.0
        const val FANG_COUNT = 8
        const val SPAWN_DELAY = 5

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
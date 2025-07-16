package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.ImmersiveMagic.Companion.WATER_CAULDRON_BLOCK_ENTITY
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

// Stores potion ingredients added to a cauldron
class LayeredCauldronBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(WATER_CAULDRON_BLOCK_ENTITY.get(), pos, state), MaybeSerializable {

    var items: MutableList<ItemStack> = mutableListOf()
    var storedPotion: PotionRef? = null
    var ticksToNextSpray = 0

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        val listTag = ListTag()

        for (i in 0..<items.size) {
            listTag.add(
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, items[i]).result().get()
            )
        }

        tag.put("items", listTag)

        if (storedPotion != null) {
            val potionTag = PotionRef.CODEC.encodeStart(NbtOps.INSTANCE, storedPotion).result().getOrNull()

            if (potionTag != null) {
                tag.put("storedPotion", potionTag)
            }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        val listTag = tag.getList("items", Tag.TAG_COMPOUND.toInt())

        items.clear()
        for (item in listTag) {
            val stack = ItemStack.CODEC.decode(NbtOps.INSTANCE, item).result().get().first
            items.add(stack)
        }

        val potionTag = tag.get("storedPotion")

        storedPotion =
            PotionRef.CODEC.decode(NbtOps.INSTANCE, potionTag).result().getOrNull()
                ?.first
    }

    // Server-only block entity, if we send it to clients without this mod installed
    // they will fail to deserialize the chunk packet
    override fun immersiveMagic_isSerializable(): Boolean {
        return false
    }

    // Spawn effect particles based on the potion inside
    fun spawnParticles(level: Level, pos: BlockPos, count: Int) {
        // No particles for pure water
        if (items.isEmpty()) return

        val color = storedPotion?.getEffectColor() ?: FastColor.ARGB32.color(255, 255, 255, 255)

        val particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color)
        (level as ServerLevel).sendParticles(
            particle,
            pos.x + 0.5,
            pos.y + 1.0,
            pos.z + 0.5,
            count,
            0.0,
            0.05,
            0.0,
            0.2
        )
    }

    companion object {
        @Suppress("unused") // `state` is never actually used but it's part of the block entity interface
        fun tick(level: Level, pos: BlockPos, state: BlockState, instance: BlockEntity) {
            (instance as? LayeredCauldronBlockEntity)?.let {
                if (instance.ticksToNextSpray == 0) {
                    instance.spawnParticles(level, pos, Random.nextInt(5, 15))
                    instance.ticksToNextSpray = Random.nextInt(20, 100)
                } else {
                    instance.ticksToNextSpray--
                }
            }
        }
    }
}
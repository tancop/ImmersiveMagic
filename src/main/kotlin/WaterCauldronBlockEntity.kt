package dev.tancop.immersivemagic

import dev.tancop.immersivemagic.ImmersiveMagic.Companion.WATER_CAULDRON_BLOCK_ENTITY
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class WaterCauldronBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(WATER_CAULDRON_BLOCK_ENTITY.get(), pos, state) {

    var items: MutableSet<ItemStack> = mutableSetOf()

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        val itemList = items.toList()
        val listTag = ListTag()

        for (i in 0..<itemList.size) {
            val item = itemList[i]
            listTag.add(
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, item).result().get()
            )
        }

        tag.put("items", listTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        val listTag = tag.getList("items", Tag.TAG_COMPOUND.toInt())

        items.clear()
        for (item in listTag) {
            val stack = ItemStack.CODEC.decode(NbtOps.INSTANCE, item).result().get().first
            items.add(stack)
        }
    }
}
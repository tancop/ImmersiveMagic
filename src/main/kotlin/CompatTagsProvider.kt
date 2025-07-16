package dev.tancop.immersivemagic

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.world.level.block.Blocks
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CompatTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider?>,
    existingFileHelper: ExistingFileHelper?
) : BlockTagsProvider(output, lookupProvider, ImmersiveMagic.MOD_ID, existingFileHelper) {

    override fun addTags(lookupProvider: HolderLookup.Provider) {
        tag(ImmersiveMagic.PISTON_BEHAVIOR_NORMAL)
            .add(Blocks.WATER_CAULDRON)
    }
}
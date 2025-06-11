package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.BlockEntityExt;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

// Skips serializing block entities with `immersiveMagic_isSerializable` returning false
@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {
    @Shadow
    @Final
    private List<Object> blockEntitiesData;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At("TAIL"))
    void addEntityToList(LevelChunk levelChunk, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException {
        this.blockEntitiesData.clear();

        // Static method in a private inner class. No way to do this without reflection
        Method create = Class.forName("net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData$BlockEntityInfo")
                .getDeclaredMethod("create", BlockEntity.class);
        create.setAccessible(true);

        for (Map.Entry<BlockPos, BlockEntity> entry : levelChunk.getBlockEntities().entrySet()) {
            if (((BlockEntityExt) entry.getValue()).immersiveMagic_isSerializable()) {
                Object blockEntityInfo = create.invoke(null, entry.getValue());

                this.blockEntitiesData.add(blockEntityInfo);
            }
        }
    }
}

package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.MaybeSerializable;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements MaybeSerializable {
    @Override
    @Unique
    public boolean immersiveMagic_isSerializable() {
        return true;
    }
}

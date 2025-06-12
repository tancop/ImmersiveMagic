package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.ImmersiveMagic;
import dev.tancop.immersivemagic.LayeredCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LayeredCauldronBlock.class)
@Implements(@Interface(iface = EntityBlock.class, prefix = "entityBlock$"))
public class LayeredCauldronBlockMixin {
    public BlockEntity entityBlock$newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LayeredCauldronBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> entityBlock$getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                              @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return type == ImmersiveMagic.Companion.getWATER_CAULDRON_BLOCK_ENTITY().get() ? LayeredCauldronBlockEntity.Companion::tick : null;
    }
}

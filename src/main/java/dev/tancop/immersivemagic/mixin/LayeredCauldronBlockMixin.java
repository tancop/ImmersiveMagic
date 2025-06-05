package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.WaterCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LayeredCauldronBlock.class)
@Implements(@Interface(iface = EntityBlock.class, prefix = "entityBlock$"))
public class LayeredCauldronBlockMixin {
    public BlockEntity entityBlock$newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WaterCauldronBlockEntity(pos, state);
    }
}

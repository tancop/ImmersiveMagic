package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.CauldronHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {
    @Final
    @Shadow
    protected CauldronInteraction.InteractionMap interactions;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                           BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        Item insertedItem = stack.getItem();

        CauldronInteraction.WATER.map().put(Items.GLASS_BOTTLE, CauldronHandlers.INSTANCE.getBottleInteraction());
        CauldronInteraction.WATER.map().put(Items.WATER_BUCKET, CauldronHandlers.INSTANCE.getWaterBucketInteraction());
        CauldronInteraction.WATER.map().put(Items.POTION, CauldronHandlers.INSTANCE.getPotionInteraction());
        CauldronInteraction.WATER.map().put(Items.AIR, CauldronHandlers.INSTANCE.getStirInteraction());

        CauldronInteraction interaction = this.interactions.map().get(insertedItem);
        ItemInteractionResult result = interaction.interact(state, level, pos, player, hand, stack);

        if (result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && !level.isClientSide) {
            result = CauldronHandlers.INSTANCE.fallbackInteract(stack, state, level, pos, player, hand);
        }

        cir.setReturnValue(result);
    }
}

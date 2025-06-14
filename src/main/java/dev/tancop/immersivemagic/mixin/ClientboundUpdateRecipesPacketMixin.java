package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.MaybeSerializable;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Prevent non-serializable recipes getting synced to the client
@Mixin(ClientboundUpdateRecipesPacket.class)
public abstract class ClientboundUpdateRecipesPacketMixin {
    @Final
    @Shadow
    @Mutable
    private List<RecipeHolder<?>> recipes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        recipes = recipes.stream().filter((holder) -> {
            if (holder.value() instanceof MaybeSerializable) {
                return ((MaybeSerializable) holder.value()).immersiveMagic_isSerializable();
            }
            return true;
        }).toList();
    }
}

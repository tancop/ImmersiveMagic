package dev.tancop.immersivemagic.mixin;

import dev.tancop.immersivemagic.Config;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {
    @Inject(method = "addVanillaMixes", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/item/alchemy/PotionBrewing$Builder;addStartMix" + "(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)V"), cancellable = true)
    private static void overrideVanillaMixes(PotionBrewing.Builder builder, CallbackInfo ci) {
        if (Config.INSTANCE.getDisableVanillaBrewing()) {
            ci.cancel();
        }
    }
}

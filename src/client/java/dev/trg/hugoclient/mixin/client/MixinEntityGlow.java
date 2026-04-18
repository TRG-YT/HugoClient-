package dev.trg.hugoclient.mixin.client;

import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.feature.ClientFeature;
import dev.trg.hugoclient.client.feature.OpGlowManager;
import dev.trg.hugoclient.client.util.ServerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntityGlow {

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void hugoclient_overrideIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ItemEntity item)) return;
        if (!ServerUtil.isAllowedEnvironment()) return;
        if (!HugoClientConfig.isEnabled(ClientFeature.OP_GLOW)) return;
        if (Boolean.TRUE.equals(cir.getReturnValue())) return;

        ItemStack stack = item.getStack();
        if (OpGlowManager.isHighlightedItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void hugoclient_overrideGlowColor(CallbackInfoReturnable<Integer> cir) {
        if (!((Object) this instanceof ItemEntity item)) return;
        if (!ServerUtil.isAllowedEnvironment()) return;
        if (!HugoClientConfig.isEnabled(ClientFeature.OP_GLOW)) return;

        ItemStack stack = item.getStack();
        if (OpGlowManager.isElytraItem(stack)) {
            cir.setReturnValue(OpGlowManager.GLOW_COLOR_ELYTRA);
        } else if (OpGlowManager.isOpItem(stack)) {
            cir.setReturnValue(OpGlowManager.GLOW_COLOR_GOLD);
        }
    }
}

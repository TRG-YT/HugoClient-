package dev.trg.hugoclient.mixin.client;

import dev.trg.hugoclient.client.combat.CombatLogger;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hugoclient_interceptCombatMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null) return;
        if (!CombatLogger.shouldInterceptOverlay()) return;

        String plain = message.getString();
        if (plain.contains("Du befindest dich im Kampf")) {
            CombatLogger.triggerCombat();
            ci.cancel();
        }
    }
}

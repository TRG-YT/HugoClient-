package dev.trg.hugoclient.mixin.client;

import dev.trg.hugoclient.client.combat.CombatLogger;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fängt die Server-Action-Bar-Nachricht „Du befindest dich im Kampf" ab,
 * bevor sie gerendert wird.
 *
 * Statt dem Original-Text startet/verlängert CombatLogger den 20-s-Timer
 * und zeigt per Tick-Handler „Combat: Xs" an.
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hugoclient_interceptCombatMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null) return;
        String plain = message.getString();
        if (plain.contains("Du befindest dich im Kampf")) {
            CombatLogger.triggerCombat();
            ci.cancel(); // Original-Nachricht wird NICHT angezeigt
        }
    }
}

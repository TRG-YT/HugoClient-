package dev.trg.hugoclient.mixin.client;

import dev.trg.hugoclient.client.feature.AutoAfkModule;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Versionstoleranter Mouse-Mixin für 1.21.4 bis 1.21.11.
 *
 * <p>Die Signatur von Mouse#onMouseButton hat sich zwischen den Patch-Versionen
 * geändert. Deshalb werden hier bewusst keine Zielparameter übernommen, sondern
 * nur das Callback selbst. So bleibt der Inject kompatibel, solange der
 * Methodenname gleich bleibt.
 */
@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseButton", at = @At("HEAD"), require = 0)
    private void hugoclient_trackMouseButton(CallbackInfo ci) {
        AutoAfkModule.notifyActivity();
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), require = 0)
    private void hugoclient_trackMouseMove(CallbackInfo ci) {
        AutoAfkModule.notifyActivity();
    }
}

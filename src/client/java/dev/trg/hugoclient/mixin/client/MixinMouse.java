package dev.trg.hugoclient.mixin.client;

import dev.trg.hugoclient.client.feature.AutoAfkModule;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


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

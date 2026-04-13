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

/**
 * Mixin auf {@link Entity}, das für Boden-Items (ItemEntity) den Glow-Effekt
 * und die Umrissfarbe überschreibt, sofern OP-Glow aktiviert ist UND die
 * Umgebung erlaubt ist ({@link ServerUtil#isAllowedEnvironment()}).
 *
 * <p>Farben:
 * <ul>
 *   <li>Elytra → leuchtendes Rot  ({@code 0xFFFF2200})</li>
 *   <li>OP-Item → leuchtendes Gold ({@code 0xFFFFAA00})</li>
 * </ul>
 *
 * <p>Kompatibel mit Minecraft 1.21.4 – 1.21.11.
 */
@Mixin(Entity.class)
public abstract class MixinEntityGlow {

    /**
     * Gibt {@code true} zurück, wenn das Entity ein hervorgehobenes OP-Item ist,
     * das Feature aktiv ist und die Umgebung erlaubt ist.
     */
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void hugoclient_overrideIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        // Nur für ItemEntities
        if (!((Object) this instanceof ItemEntity item)) return;
        // Umgebungs- und Feature-Guard
        if (!ServerUtil.isAllowedEnvironment()) return;
        if (!HugoClientConfig.isEnabled(ClientFeature.OP_GLOW)) return;
        // Bereits glühend → nichts tun
        if (Boolean.TRUE.equals(cir.getReturnValue())) return;

        ItemStack stack = item.getStack();
        if (OpGlowManager.isHighlightedItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Überschreibt die Outline-Farbe für OP-Items.
     *
     * <p>Der Rückgabewert ist ein ARGB-Integer (0xAARRGGBB), den
     * {@code WorldRenderer} über {@code ColorHelper.Argb.getRed/Green/Blue()}
     * auseinandernimmt und an den Outline-Shader übergibt.
     */
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

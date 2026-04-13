package dev.trg.hugoclient.client.combat;

import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.feature.ClientFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class CombatLogger {

    private static long  combatEndMs     = -1L;
    private static int   lastSecondShown = -1;
    private static float lastHealth      = -1f;

    private static void resetCombatTimer() {
        combatEndMs = System.currentTimeMillis() + 20_000L;
        lastSecondShown = -1;
    }

    public static void triggerCombat() {
        if (!isFeatureEnabled()) {
            return;
        }

        if (!isInCombat()) {
            resetCombatTimer();
        }
    }

    public static boolean isInCombat() {
        return System.currentTimeMillis() < combatEndMs;
    }

    public static boolean shouldInterceptOverlay() {
        return isFeatureEnabled();
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!isFeatureEnabled()) {
                return ActionResult.PASS;
            }

            if (player != null && entity instanceof PlayerEntity) {
                resetCombatTimer();
            }

            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;

            if (!isFeatureEnabled()) {
                clearCombatDisplay(player);
                if (player != null) {
                    lastHealth = player.getHealth();
                }
                return;
            }

            if (player != null) {
                float currentHealth = player.getHealth();
                if (isInCombat() && lastHealth >= 0f && currentHealth < lastHealth) {
                    resetCombatTimer();
                }
                lastHealth = currentHealth;
            }

            if (!isInCombat()) {
                clearCombatDisplay(player);
                return;
            }

            if (player == null) return;

            long remainingMs = combatEndMs - System.currentTimeMillis();
            int seconds = (int) Math.ceil(remainingMs / 1000.0);
            seconds = Math.max(seconds, 1);

            if (seconds != lastSecondShown) {
                lastSecondShown = seconds;
                player.sendMessage(
                        Text.literal("§cCombat: §f" + seconds + "s"),
                        true
                );
            }
        });
    }

    private static boolean isFeatureEnabled() {
        return HugoClientConfig.isEnabled(ClientFeature.COMBAT_LOG_COUNTER);
    }

    private static void clearCombatDisplay(ClientPlayerEntity player) {
        combatEndMs = -1L;
        if (lastSecondShown != 0) {
            lastSecondShown = 0;
            if (player != null) {
                player.sendMessage(Text.literal(""), true);
            }
        }
    }
}

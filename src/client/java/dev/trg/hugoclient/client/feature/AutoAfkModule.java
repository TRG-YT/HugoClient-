package dev.trg.hugoclient.client.feature;

import dev.trg.hugoclient.client.CommandUtil;
import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.util.ServerUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class AutoAfkModule {
    private static final long AFK_TIMEOUT_MS = 4L * 60L * 1000L;
    private static volatile long lastActivityMs = System.currentTimeMillis();
    private static volatile boolean afkSent = false;
    private static Vec3d lastPos = null;
    private AutoAfkModule() {}
    public static void notifyActivity() {
        lastActivityMs = System.currentTimeMillis();
        afkSent = false;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AutoAfkModule::onEndTick);
    }
    private static void onEndTick(MinecraftClient client) {
        if (!ServerUtil.isAllowedEnvironment()) {
            resetTimer();
            return;
        }

        if (!HugoClientConfig.isEnabled(ClientFeature.AUTO_AFK)) {
            resetTimer();
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null) {
            resetTimer();
            return;
        }

        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        if (lastPos == null || !pos.equals(lastPos)) {
            notifyActivity();
        }
        lastPos = pos;

        if (isAnyGameKeyPressed(client.options)) {
            notifyActivity();
        }

        if (client.currentScreen != null) {
            notifyActivity();
        }

        long idleMs = System.currentTimeMillis() - lastActivityMs;
        if (idleMs >= AFK_TIMEOUT_MS && !afkSent) {
            afkSent = true;
            CommandUtil.send(client, "afk");
            player.sendMessage(
                    Text.literal("§7[HugoClient] §eAutoAFK: §f/afk wurde gesendet."),
                    false
            );
        }
    }

    private static boolean isAnyGameKeyPressed(GameOptions opts) {
        return opts.forwardKey.isPressed()
                || opts.backKey.isPressed()
                || opts.leftKey.isPressed()
                || opts.rightKey.isPressed()
                || opts.jumpKey.isPressed()
                || opts.sneakKey.isPressed()
                || opts.sprintKey.isPressed()
                || opts.attackKey.isPressed()
                || opts.useKey.isPressed()
                || opts.pickItemKey.isPressed()
                || opts.dropKey.isPressed()
                || opts.swapHandsKey.isPressed()
                || opts.inventoryKey.isPressed()
                || opts.chatKey.isPressed();
    }

    private static void resetTimer() {
        lastActivityMs = System.currentTimeMillis();
        afkSent = false;
        lastPos = null;
    }
}

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

/**
 * AutoAFK – sendet {@code /afk} automatisch, wenn der Spieler
 * {@value #AFK_TIMEOUT_MS} ms (= 4 Minuten) inaktiv war.
 *
 * <h2>Erkannte Aktivitäten</h2>
 * <ul>
 *   <li>Positions­änderung des Spielers (jeder Tick)</li>
 *   <li>Gedrückte Bewegungs-/Aktions-Tasten</li>
 *   <li>Geöffneter Screen (Chat, Inventar …)</li>
 *   <li>Maus-Button-Klick (via {@code MixinMouse})</li>
 *   <li>Maus-Cursor-Bewegung (via {@code MixinMouse})</li>
 * </ul>
 *
 * <h2>Spam-Schutz</h2>
 * <p>{@code /afk} wird nur einmal pro Inaktivitäts­phase gesendet.
 * Erst wenn der Spieler wieder aktiv wird, kann der nächste Trigger
 * ausgelöst werden.
 *
 * <h2>Sicherheitsbedingung</h2>
 * <p>Nur aktiv, wenn {@link ServerUtil#isAllowedEnvironment()} true zurückgibt.
 *
 * <p>Kompatibel mit Minecraft 1.21.4 – 1.21.11.
 */
public final class AutoAfkModule {

    /** Timeout in Millisekunden (4 Minuten). */
    private static final long AFK_TIMEOUT_MS = 4L * 60L * 1000L;

    /** Zeitstempel der letzten erkannten Aktivität. */
    private static volatile long lastActivityMs = System.currentTimeMillis();

    /** true, wenn /afk in dieser Inaktivitätsphase bereits gesendet wurde. */
    private static volatile boolean afkSent = false;

    /** Letzte bekannte Spieler-Position – zur Änderungs­erkennung. */
    private static Vec3d lastPos = null;

    private AutoAfkModule() {}

    // -----------------------------------------------------------------------
    // Öffentliche API (auch vom MixinMouse aufgerufen)
    // -----------------------------------------------------------------------

    /**
     * Muss aufgerufen werden, sobald irgendeine Aktivität erkannt wird.
     * Setzt den AFK-Timer zurück und erlaubt einen neuen Trigger-Zyklus.
     */
    public static void notifyActivity() {
        lastActivityMs = System.currentTimeMillis();
        afkSent = false;
    }

    // -----------------------------------------------------------------------
    // Registrierung
    // -----------------------------------------------------------------------

    /** Einmalig beim Mod-Start aufrufen. */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AutoAfkModule::onEndTick);
    }

    // -----------------------------------------------------------------------
    // Tick-Logik
    // -----------------------------------------------------------------------

    private static void onEndTick(MinecraftClient client) {
        // Umgebungs-Guard: nur auf HugoSMP.net oder Singleplayer-Survival
        if (!ServerUtil.isAllowedEnvironment()) {
            resetTimer(); // Timer sauber halten, damit kein sofortiger Trigger
            return;
        }

        // Feature-Toggle
        if (!HugoClientConfig.isEnabled(ClientFeature.AUTO_AFK)) {
            resetTimer();
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null) {
            resetTimer();
            return;
        }

        // --- Aktivitätserkennung ---

        // 1) Positionsänderung
        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        if (lastPos == null || !pos.equals(lastPos)) {
            notifyActivity();
        }
        lastPos = pos;

        // 2) Spielrelevante Tasten gedrückt
        if (isAnyGameKeyPressed(client.options)) {
            notifyActivity();
        }

        // 3) Screen geöffnet (Chat, Inventar, Pause …)
        if (client.currentScreen != null) {
            notifyActivity();
        }

        // Mausereignisse werden über MixinMouse → notifyActivity() gemeldet.

        // --- AFK-Check ---
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

    /**
     * Prüft, ob irgendeine spielrelevante Taste gerade gehalten wird.
     * Nur Minecraft-interne Bindings – kein nativer GLFW-Aufruf.
     */
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

    /** Setzt Timer und afkSent zurück, ohne eine Nachricht zu senden. */
    private static void resetTimer() {
        lastActivityMs = System.currentTimeMillis();
        afkSent = false;
        lastPos = null;
    }
}

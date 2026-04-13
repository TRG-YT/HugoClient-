package dev.trg.hugoclient.client.chat;

import dev.trg.hugoclient.client.BotExecutor;
import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.config.PearlBotConfig;
import dev.trg.hugoclient.client.feature.ClientFeature;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.time.Instant;

public class ChatListener {

    public static void register() {

        ClientReceiveMessageEvents.CHAT.register(
                (Text message,
                 net.minecraft.network.message.SignedMessage signedMessage,
                 com.mojang.authlib.GameProfile sender,
                 net.minecraft.network.message.MessageType.Parameters params,
                 Instant timestamp) -> {

                    handleMessage(message.getString());
                }
        );

        ClientReceiveMessageEvents.GAME.register(
                (Text message, boolean overlay) -> {

                    handleMessage(message.getString());
                }
        );
    }

    private static void handleMessage(String raw) {
        if (!HugoClientConfig.isEnabled(ClientFeature.PEARLBOT)) return;
        if (PearlBotConfig.getAll().isEmpty()) return;

        String full = raw.trim();

        if (!full.toLowerCase().endsWith(": t")) return;

        int colonIndex = full.lastIndexOf(':');
        if (colonIndex <= 0) return;

        String beforeColon = full.substring(0, colonIndex).trim();
        String[] parts = beforeColon.split("\\s+");
        if (parts.length == 0) return;

        String playerName = parts[parts.length - 1];

        if (PearlBotConfig.get(playerName) != null) {
            BotExecutor.execute(playerName);
        }
    }
}

package dev.trg.hugoclient.client;

import net.minecraft.client.MinecraftClient;

public final class CommandUtil {
    private CommandUtil() {}

    public static void send(MinecraftClient client, String cmdWithoutSlash) {
        if (client == null || client.getNetworkHandler() == null) return;

        try {
            client.getNetworkHandler().sendChatCommand(cmdWithoutSlash);
            return;
        } catch (Throwable ignored) {}

        client.getNetworkHandler().sendChatMessage("/" + cmdWithoutSlash);
    }
}
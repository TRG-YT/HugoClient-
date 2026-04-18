
package dev.trg.hugoclient.client.util;

import net.minecraft.client.MinecraftClient;

public final class ServerUtil {

    private ServerUtil() {}

    public static boolean isAllowedEnvironment() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null;
    }
}
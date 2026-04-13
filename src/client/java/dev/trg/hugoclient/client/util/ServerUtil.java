
package dev.trg.hugoclient.client.util;

import net.minecraft.client.MinecraftClient;

public final class ServerUtil {

    private ServerUtil() {}

    public static boolean isAllowedEnvironment() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null;
    }
}













/*package dev.trg.hugoclient.client.util;




import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.net.InetSocketAddress;
import java.util.Locale;



public final class ServerUtil {

    private static final String ALLOWED_SERVER = "hugosmp.net";

    private ServerUtil() {}


    public static boolean isAllowedEnvironment() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;

        if (client.isInSingleplayer()) {
            return isSurvivalMode(client);
        }

        return isAllowedMultiplayer(client);
    }

    private static boolean isSurvivalMode(MinecraftClient client) {
        if (client.player == null) return false;
        // creativeMode ist true in Creative; Spectator hat allowFlying + flying
        boolean isCreative  = client.player.getAbilities().creativeMode;
        boolean isSpectator = client.player.isSpectator();
        return !isCreative && !isSpectator;
    }

    private static boolean isAllowedMultiplayer(MinecraftClient client) {
        ServerInfo info = client.getCurrentServerEntry();
        if (info != null) {
            String address = info.address.toLowerCase(Locale.ROOT).trim();
            address = stripPort(address);
            return address.equals(ALLOWED_SERVER);
        }

        if (client.getNetworkHandler() != null) {
            var connection = client.getNetworkHandler().getConnection();
            if (connection != null
                    && connection.getAddress() instanceof InetSocketAddress inetAddr) {
                String host = inetAddr.getHostString().toLowerCase(Locale.ROOT).trim();
                return host.equals(ALLOWED_SERVER);
            }
        }

        return false;
    }

    private static String stripPort(String address) {
        if (address.startsWith("[")) {
            int closingBracket = address.lastIndexOf(']');
            if (closingBracket >= 0 && closingBracket < address.length() - 1) {
                return address.substring(0, closingBracket + 1);
            }
            return address;
        }

        int lastColon = address.lastIndexOf(':');
        if (lastColon > 0) {
            String portCandidate = address.substring(lastColon + 1);
            try {
                Integer.parseInt(portCandidate);
                return address.substring(0, lastColon);
            } catch (NumberFormatException ignored) {
            }
        }

        return address;
    }
}
*/
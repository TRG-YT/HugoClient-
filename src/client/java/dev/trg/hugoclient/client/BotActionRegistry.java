package dev.trg.hugoclient.client;

import java.util.HashMap;
import java.util.Map;

public class BotActionRegistry {

    private static final Map<String, BotAction> ACTIONS = new HashMap<>();

    static {
        ACTIONS.put(
                "StreamerOne",
                new BotAction(135.0f, 12.0f, 2)
        );

        ACTIONS.put(
                "StreamerTwo",
                new BotAction(92.0f, 18.5f, 3)
        );
    }

    public static BotAction get(String senderName) {
        return ACTIONS.get(senderName);
    }
}

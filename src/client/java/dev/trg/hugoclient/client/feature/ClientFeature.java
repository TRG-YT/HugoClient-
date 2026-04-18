package dev.trg.hugoclient.client.feature;

import net.minecraft.text.Text;

public enum ClientFeature {

    PEARLBOT(
            "pearlbot",
            "feature.hugoclient.pearlbot",
            true
    ),
    COMBAT_LOG_COUNTER(
            "combat_log_counter",
            "feature.hugoclient.combat_log_counter",
            true
    ),
    SHIFT_RIGHT_CLICK_GUI(
            "shift_right_click_gui",
            "feature.hugoclient.shift_right_click_gui",
            true
    ),

    OP_GLOW(
            "op_glow",
            "feature.hugoclient.op_glow",
            true
    ),

    ITEM_DELAY(
            "item_delay",
            "feature.hugoclient.item_delay",
            true
    ),

    AUTO_AFK(
            "auto_afk",
            "feature.hugoclient.auto_afk",
            false
    );

    private final String id;
    private final String translationKey;
    private final boolean defaultEnabled;

    ClientFeature(String id, String translationKey, boolean defaultEnabled) {
        this.id = id;
        this.translationKey = translationKey;
        this.defaultEnabled = defaultEnabled;
    }

    public String id() {
        return id;
    }

    public Text title() {
        return Text.translatable(translationKey);
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }
}

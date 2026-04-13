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

    /**
     * OP Glow: Hebt wertvolle Boden-Items (Netherite, Diamond, Elytra usw.)
     * mit einem farbigen Outline-Effekt hervor.
     * Elytra → leuchtendes Rot, alle anderen OP-Items → Gold.
     * Nur aktiv auf HugoSMP.net oder in Singleplayer-Survival.
     */
    OP_GLOW(
            "op_glow",
            "feature.hugoclient.op_glow",
            true
    ),

    /**
     * Item Delay / Inventory Rescue: Wenn ein wertvolles Boden-Item unter dem
     * Spieler liegt, aber das Inventar voll ist, werden automatisch zuerst
     * Müll-Items und bei Bedarf als Fallback Totems gedroppt.
     * Nur aktiv auf HugoSMP.net oder in Singleplayer-Survival.
     */
    ITEM_DELAY(
            "item_delay",
            "feature.hugoclient.item_delay",
            true
    ),

    /**
     * AutoAFK: Sendet automatisch /afk, wenn der Spieler 4 Minuten lang
     * keinerlei Eingaben macht (Bewegung, Klick, Tasten, Maus).
     * Standardmäßig deaktiviert – muss bewusst eingeschaltet werden.
     * Nur aktiv auf HugoSMP.net oder in Singleplayer-Survival.
     */
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

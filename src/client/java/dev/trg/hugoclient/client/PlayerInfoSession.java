package dev.trg.hugoclient.client;

import java.util.UUID;

public final class PlayerInfoSession {
    public final String targetName;
    public final UUID targetUuid;

    public String rank = null;
    public String kills = null;
    public String deaths = null;

    public String clanName = null;
    public String clanTag = null;
    public String clanMembers = null;
    public String clanLeader = null;
    public boolean noClan = false;

    public boolean statsDone = false;
    public boolean clanDone = false;

    public long lastUpdateMs = System.currentTimeMillis();

    public PlayerInfoSession(String targetName, UUID targetUuid) {
        this.targetName = targetName;
        this.targetUuid = targetUuid;
    }

    public String kdString() {
        try {
            if (kills == null || deaths == null) return "—";
            double k = Double.parseDouble(kills);
            double d = Double.parseDouble(deaths);
            if (d == 0.0D) return String.format("%.2f", k);
            return String.format("%.2f", k / d);
        } catch (Exception e) {
            return "—";
        }
    }
}
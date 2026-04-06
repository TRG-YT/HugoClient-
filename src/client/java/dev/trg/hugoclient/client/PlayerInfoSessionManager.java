package dev.trg.hugoclient.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerInfoSessionManager {

    private PlayerInfoSessionManager() {}

    private static PlayerInfoSession current = null;
    private static boolean initialized = false;

    private static final Pattern STATS_HEADER =
            Pattern.compile("^Stats von (\\S+) \\(All-Time\\)$");
    private static final Pattern STATS_RANK =
            Pattern.compile("^Position im Ranking: #(\\d+)$");
    private static final Pattern STATS_KILLS =
            Pattern.compile("^Kills: (\\d+)$");
    private static final Pattern STATS_DEATHS =
            Pattern.compile("^Tode: (\\d+)$");

    private static final Pattern CLAN_HEADER =
            Pattern.compile("^Clan Informationen$");
    private static final Pattern CLAN_NAME =
            Pattern.compile("^Name: (.+)$");
    private static final Pattern CLAN_TAG =
            Pattern.compile("^Kürzel: (.+)$");
    private static final Pattern CLAN_LEADER =
            Pattern.compile("^Leader: (.+)$");
    private static final Pattern CLAN_MEMBERS =
            Pattern.compile("^Mitglieder \\((\\d+)\\).*$");
    private static final Pattern NO_CLAN =
            Pattern.compile(".*(kein Clan|keinen Clan|nicht in einem Clan).*", Pattern.CASE_INSENSITIVE);

    private static boolean inStatsBlock = false;
    private static boolean statsHeaderSeen = false;

    private static boolean inClanBlock = false;
    private static boolean clanHeaderSeen = false;

    public static void initChatListener() {
        if (initialized) return;
        initialized = true;

        ClientReceiveMessageEvents.GAME.register((Text message, boolean overlay) -> {
            if (current == null) return;

            String s = message.getString().trim();
            if (s.isEmpty()) return;

            if (NO_CLAN.matcher(s).matches()) {
                current.noClan = true;
                current.clanName = "Kein Clan";
                current.clanTag = "—";
                current.clanMembers = "0";
                current.clanLeader = "—";
                current.clanDone = true;
                current.lastUpdateMs = System.currentTimeMillis();

                inClanBlock = false;
                clanHeaderSeen = false;
                return;
            }

            if (inStatsBlock && statsHeaderSeen && s.equals("--------------------")) {
                inStatsBlock = false;
                current.statsDone = true;
                current.lastUpdateMs = System.currentTimeMillis();
                return;
            }

            if (inClanBlock && clanHeaderSeen && s.equals("--------------------")) {
                inClanBlock = false;
                current.clanDone = true;
                current.lastUpdateMs = System.currentTimeMillis();
                return;
            }

            Matcher statsHeader = STATS_HEADER.matcher(s);
            if (statsHeader.matches()) {
                String name = statsHeader.group(1);
                if (name.equalsIgnoreCase(current.targetName)) {
                    inStatsBlock = true;
                    statsHeaderSeen = true;

                    current.rank = null;
                    current.kills = null;
                    current.deaths = null;
                    current.statsDone = false;
                    current.lastUpdateMs = System.currentTimeMillis();
                }
                return;
            }

            Matcher clanHeader = CLAN_HEADER.matcher(s);
            if (clanHeader.matches()) {
                inClanBlock = true;
                clanHeaderSeen = true;

                current.noClan = false;
                current.clanName = null;
                current.clanTag = null;
                current.clanMembers = null;
                current.clanLeader = null;
                current.clanDone = false;
                current.lastUpdateMs = System.currentTimeMillis();
                return;
            }

            if (inStatsBlock && statsHeaderSeen) {
                Matcher rank = STATS_RANK.matcher(s);
                if (rank.matches()) {
                    current.rank = rank.group(1);
                    current.lastUpdateMs = System.currentTimeMillis();
                    return;
                }

                Matcher kills = STATS_KILLS.matcher(s);
                if (kills.matches()) {
                    current.kills = kills.group(1);
                    current.lastUpdateMs = System.currentTimeMillis();
                    return;
                }

                Matcher deaths = STATS_DEATHS.matcher(s);
                if (deaths.matches()) {
                    current.deaths = deaths.group(1);
                    current.lastUpdateMs = System.currentTimeMillis();
                    return;
                }
            }

            if (inClanBlock && clanHeaderSeen) {
                Matcher name = CLAN_NAME.matcher(s);
                if (name.matches()) {
                    current.clanName = name.group(1).trim();
                    current.lastUpdateMs = System.currentTimeMillis();
                    checkClanDone();
                    return;
                }

                Matcher tag = CLAN_TAG.matcher(s);
                if (tag.matches()) {
                    current.clanTag = tag.group(1).trim();
                    current.lastUpdateMs = System.currentTimeMillis();
                    checkClanDone();
                    return;
                }

                Matcher leader = CLAN_LEADER.matcher(s);
                if (leader.matches()) {
                    current.clanLeader = leader.group(1).trim();
                    current.lastUpdateMs = System.currentTimeMillis();
                    checkClanDone();
                    return;
                }

                Matcher members = CLAN_MEMBERS.matcher(s);
                if (members.matches()) {
                    current.clanMembers = members.group(1).trim();
                    current.lastUpdateMs = System.currentTimeMillis();
                    checkClanDone();
                }
            }
        });
    }

    private static void checkClanDone() {
        if (current == null) return;

        if (current.clanName != null
                && current.clanTag != null
                && current.clanMembers != null
                && current.clanLeader != null) {
            current.clanDone = true;
        }
    }

    public static PlayerInfoSession start(String targetName, UUID uuid) {
        current = new PlayerInfoSession(targetName, uuid);

        inStatsBlock = false;
        statsHeaderSeen = false;

        inClanBlock = false;
        clanHeaderSeen = false;

        return current;
    }

    public static PlayerInfoSession getCurrent() {
        return current;
    }
}
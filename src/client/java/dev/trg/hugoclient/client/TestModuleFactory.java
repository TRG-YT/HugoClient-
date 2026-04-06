package dev.trg.hugoclient.client;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestModuleFactory {

    private TestModuleFactory() {}

    public static PlayerInfoSession createRandomSession() {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        String[] names   = {"Steve", "Alex", "Doofbuohner", "Scop3xPX2", "Jonas9803", "naniEXE", "hk8i"};
        String[] clans   = {"Aether", "Chaos", "Velocity", "Oblivion", "Frost", "NoClan"};
        String[] tags    = {"ATH",    "CHS",   "VLT",       "OBV",      "FRST", "—"};
        String[] leaders = {"Steve",  "Alex",  "CommanderX","Ghostyy",  "Nyr0x","—"};

        String name      = names[r.nextInt(names.length)];
        int    clanIndex = r.nextInt(clans.length);

        PlayerInfoSession session = PlayerInfoSessionManager.start(name, UUID.randomUUID());

        session.rank   = String.valueOf(r.nextInt(1, 99_999));
        session.kills  = String.valueOf(r.nextInt(0, 25_000));
        session.deaths = String.valueOf(r.nextInt(1, 12_000));
        session.statsDone = true;

        if ("NoClan".equals(clans[clanIndex])) {
            session.noClan      = true;
            session.clanName    = "Kein Clan";
            session.clanTag     = "—";
            session.clanMembers = "0";
            session.clanLeader  = "—";
            session.clanDone    = true; // <-- was missing
        } else {
            session.noClan      = false;
            session.clanName    = clans[clanIndex];
            session.clanTag     = tags[clanIndex];
            session.clanMembers = String.valueOf(r.nextInt(3, 40));
            session.clanLeader  = leaders[clanIndex];
            session.clanDone    = true;
        }

        session.lastUpdateMs = System.currentTimeMillis();
        return session;
    }
}

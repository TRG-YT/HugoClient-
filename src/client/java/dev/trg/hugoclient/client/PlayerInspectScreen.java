package dev.trg.hugoclient.client;

import dev.trg.hugoclient.client.util.DrawCompat;
import dev.trg.hugoclient.client.util.MatrixCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PlayerInspectScreen extends Screen {

    private static final int PREF_W = 860;
    private static final int PREF_H = 460;
    private static final int MIN_W = 120;
    private static final int MIN_H = 80;
    private static final int MARGIN = 8;
    private static final int PAD = 9;
    private static final int GAP = 7;
    private static final int MID_GAP = 0;

    private static final int BG_OVERLAY = 0xC0000000;
    private static final int PANEL_BG = 0xFF0D1117;
    private static final int CARD_BG = 0xFF161B22;
    private static final int CARD_BORDER = 0xFF21262D;
    private static final int INNER_BG = 0xFF0A0F14;

    private static final int TEXT_PRIMARY = 0xFFE6EDF3;
    private static final int TEXT_MUTED = 0xFF8B949E;
    private static final int TEXT_DIM = 0xFF3D444D;

    private static final int BLUE = 0xFF58A6FF;
    private static final int GREEN = 0xFF3FB950;
    private static final int AMBER = 0xFFD29922;
    private static final int RED = 0xFFF85149;
    private static final int PURPLE = 0xFFBC8CFF;
    private static final int STRIPE = 0xFF1F6FEB;

    private final PlayerInfoSession session;
    private final PlayerEntity targetEntity;
    private final long openedAtMs;

    public PlayerInspectScreen(PlayerInfoSession session, PlayerEntity targetEntity) {
        super(Text.literal("Player Inspect"));
        this.session = session;
        this.targetEntity = targetEntity;
        this.openedAtMs = System.currentTimeMillis();
    }

    private int pw() {
        return clamp(this.width - MARGIN * 2, MIN_W, PREF_W);
    }

    private int ph() {
        return clamp(this.height - MARGIN * 2, MIN_H, PREF_H);
    }

    private int px() {
        return (this.width - pw()) / 2;
    }

    private int py() {
        return (this.height - ph()) / 2;
    }

    private int leftW() {
        return clamp(pw() * 30 / 100, 55, 220);
    }

    private int rightW() {
        return Math.max(0, pw() - leftW() - MID_GAP);
    }

    private int footerH() {
        return clamp(ph() * 12 / 100, 34, 56);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private String safeName() {
        return (session != null && session.targetName != null && !session.targetName.isBlank())
                ? session.targetName
                : "Unknown";
    }

    @Override
    protected void init() {
        this.clearChildren();

        int rw = rightW();
        int fh = footerH();

        int btnAreaW = rw - PAD * 2;
        if (btnAreaW < 30) {
            return;
        }

        int totalW = Math.min(340, btnAreaW);
        int btnW = Math.max(10, (totalW - GAP) / 2);
        int btnH = Math.max(14, fh - PAD * 2);
        int footerY = py() + ph() - fh;
        int btnY = footerY + (fh - btnH) / 2;

        int btnStartX = px() + leftW() + MID_GAP + PAD + Math.max(0, (btnAreaW - totalW) / 2);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Inventory"),
                b -> CommandUtil.send(MinecraftClient.getInstance(), "invsee " + safeName())
        ).dimensions(btnStartX, btnY, btnW, btnH).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Enderchest"),
                b -> CommandUtil.send(MinecraftClient.getInstance(), "ecsee " + safeName())
        ).dimensions(btnStartX + btnW + GAP, btnY, btnW, btnH).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        final int px = px();
        final int py = py();
        final int pw = pw();
        final int ph = ph();
        final int lw = leftW();
        final int rw = rightW();

        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);

        ctx.fill(px + 3, py + 3, px + pw + 3, py + ph + 3, 0x38000000);
        ctx.fill(px, py, px + pw, py + ph, PANEL_BG);
        border(ctx, px, py, pw, ph, CARD_BORDER);
        stripe(ctx, px, py, pw);

        renderLeftBase(ctx, px, py, lw, ph);
        if (rw > 0) {
            renderRightBase(ctx, px + lw + MID_GAP, py, rw, ph);
        }

        super.render(ctx, mx, my, delta);

        renderLeftForeground(ctx, px, py, lw, ph, mx, my);
        if (rw > 0) {
            renderRightForeground(ctx, px + lw + MID_GAP, py, rw, ph);
        }
    }

    private void renderLeftBase(DrawContext ctx, int x, int y, int w, int h) {
        int curY = y + PAD;
        curY += 12;
        curY += 16;
        curY += 4;

        final int bot = y + h;
        final int statusReserve = 55;
        final int portW = Math.max(0, w - PAD * 2);
        final int portH = clamp(h / 2, 20, Math.max(20, bot - curY - statusReserve));

        if (portW > 2 && portH > 2) {
            final int portX = x + PAD;
            final int portY = curY;

            ctx.fill(portX, portY, portX + portW, portY + portH, INNER_BG);
            border(ctx, portX, portY, portW, portH, CARD_BORDER);
            stripe(ctx, portX, portY, portW);
        }
    }

    private void renderLeftForeground(DrawContext ctx, int x, int y, int w, int h, int mx, int my) {
        scissor(ctx, x, y, w, h, () -> {
            int curY = y + PAD;
            final int bot = y + h;

            curY = textLine(ctx, "PROFIL", x + PAD, curY, bot, TEXT_DIM, false);
            curY = textLine(ctx, fitText(safeName(), w - PAD * 2), x + PAD, curY + 2, bot, TEXT_PRIMARY, true);
            curY += 4;

            final int statusReserve = 55;
            final int portW = Math.max(0, w - PAD * 2);
            final int portH = clamp(h / 2, 20, Math.max(20, bot - curY - statusReserve));

            if (portW > 2 && portH > 2) {
                final int portX = x + PAD;
                final int portY = curY;

                if (targetEntity != null && portW > 4 && portH > 8) {
                    final int size = clamp(Math.min(portW, portH) / 3, 10, 64);
                    final int cx = portX + portW / 2;
                    final int cy = portY + portH / 2;

                    scissor(ctx, portX + 1, portY + 3, portW - 2, portH - 4, () -> {
                        MatrixCompat.push(ctx);
                        try {
                            InventoryScreen.drawEntity(
                                    ctx,
                                    cx - size, portY + 4,
                                    cx + size, portY + portH - 4,
                                    size,
                                    0.0F,
                                   // (float) (cy - my),
                                    //(float) (cx - mx),
                                    (float) (mx),
                                    (float) (my),
                                    targetEntity
                            );
                        } finally {
                            MatrixCompat.pop(ctx);
                        }
                    });
                }

                curY += portH + PAD;
            }

            curY = textLine(ctx, "STATUS", x + PAD, curY, bot, TEXT_DIM, false);
            curY = textLine(ctx, "Online", x + PAD, curY, bot, GREEN, true);
            curY += 3;
            curY = textLine(ctx, "SESSION TIME", x + PAD, curY, bot, TEXT_DIM, false);
            textLine(ctx, formatElapsed(System.currentTimeMillis() - openedAtMs), x + PAD, curY, bot, TEXT_PRIMARY, true);
        });
    }

    private void renderRightBase(DrawContext ctx, int x, int y, int w, int h) {
        final int fh = footerH();
        final int g = GAP;

        final int contentY = y;
        final int contentH = Math.max(0, h - fh - g);
        final int footerY = y + h - fh;

        final int statsH = contentH * 45 / 100;
        final int clanH = Math.max(0, contentH - statsH - g);

        if (statsH > 0) {
            card(ctx, x, contentY, w, statsH, AMBER);
        }
        if (clanH > 0) {
            card(ctx, x, contentY + statsH + g, w, clanH, PURPLE);
        }
        if (fh > 0) {
            ctx.fill(x, footerY, x + w, footerY + fh, CARD_BG);
            border(ctx, x, footerY, w, fh, CARD_BORDER);
        }
    }

    private void renderRightForeground(DrawContext ctx, int x, int y, int w, int h) {
        final int fh = footerH();
        final int g = GAP;

        final int contentY = y;
        final int contentH = Math.max(0, h - fh - g);

        final int statsH = contentH * 45 / 100;
        final int clanH = Math.max(0, contentH - statsH - g);

        if (statsH > 0) {
            drawStatsForeground(ctx, x, contentY, w, statsH);
        }
        if (clanH > 0) {
            drawClanForeground(ctx, x, contentY + statsH + g, w, clanH);
        }
    }

    private void drawStatsForeground(DrawContext ctx, int x, int y, int w, int h) {
        scissor(ctx, x + 1, y + 1, w - 2, h - 2, () -> {
            final int labelY = y + PAD;
            if (h > 12) {
                DrawCompat.drawTextWithShadow(ctx, textRenderer, "STATISTIKEN", x + PAD, labelY, AMBER);
            }

            final int rowsTop = labelY + 13;
            final int rowH = Math.max(0, y + h - rowsTop);
            final int row1 = rowsTop + rowH / 4;
            final int row2 = rowsTop + rowH * 3 / 4;
            final int col1 = x + PAD;
            final int col2 = x + w / 2;
            final int maxValW = w / 2 - PAD - 2;

            if (rowH >= 14) {
                stat(ctx, col1, row1, "Ranking", rankVal(), AMBER, maxValW);
                stat(ctx, col2, row1, "K/D", kdVal(), BLUE, maxValW);
                stat(ctx, col1, row2, "Kills", killsVal(), GREEN, maxValW);
                stat(ctx, col2, row2, "Tode", deathsVal(), RED, maxValW);
            }
        });
    }

    private void drawClanForeground(DrawContext ctx, int x, int y, int w, int h) {
        scissor(ctx, x + 1, y + 1, w - 2, h - 2, () -> {
            final int labelY = y + PAD;
            if (h > 12) {
                DrawCompat.drawTextWithShadow(ctx, textRenderer, "CLAN", x + PAD, labelY, PURPLE);
            }

            final int rowsTop = labelY + 13;
            final int rowH = Math.max(0, y + h - rowsTop);
            final int row1 = rowsTop + rowH / 4;
            final int row2 = rowsTop + rowH * 3 / 4;
            final int col1 = x + PAD;
            final int col2 = x + w / 2;
            final int maxValW = w / 2 - PAD - 2;

            if (rowH >= 14) {
                stat(ctx, col1, row1, "Name", clanNameVal(), TEXT_PRIMARY, maxValW);
                stat(ctx, col2, row1, "Kürzel", clanTagVal(), TEXT_PRIMARY, maxValW);
                stat(ctx, col1, row2, "Mitglieder", clanMembersVal(), TEXT_PRIMARY, maxValW);
                stat(ctx, col2, row2, "Leader", clanLeaderVal(), TEXT_PRIMARY, maxValW);
            }
        });
    }

    private String rankVal() {
        return orLoad(session.rank != null ? "#" + session.rank : null, session.statsDone);
    }

    private String killsVal() {
        return orLoad(session.kills, session.statsDone);
    }

    private String deathsVal() {
        return orLoad(session.deaths, session.statsDone);
    }

    private String kdVal() {
        return session.statsDone ? safeKd() : "lädt...";
    }

    private String clanNameVal() {
        return orLoad(session.clanName, session.clanDone);
    }

    private String clanTagVal() {
        return orLoad(session.clanTag, session.clanDone);
    }

    private String clanMembersVal() {
        return orLoad(session.clanMembers, session.clanDone);
    }

    private String clanLeaderVal() {
        return orLoad(session.clanLeader, session.clanDone);
    }

    private String orLoad(String v, boolean done) {
        if (v != null && !v.isBlank()) {
            return v;
        }
        return done ? "—" : "lädt...";
    }

    private String safeKd() {
        try {
            return session.kdString();
        } catch (Throwable t) {
            return "—";
        }
    }

    private void card(DrawContext ctx, int x, int y, int w, int h, int stripeColor) {
        if (w <= 0 || h <= 0) {
            return;
        }

        ctx.fill(x, y, x + w, y + h, CARD_BG);
        border(ctx, x, y, w, h, CARD_BORDER);

        if (w > 2 && h > 3) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + 3, stripeColor);
        }
    }

    private void stat(DrawContext ctx, int x, int y, String label, String value, int valColor, int maxValW) {
        DrawCompat.drawText(ctx, textRenderer, label, x, y, TEXT_MUTED, false);
        DrawCompat.drawTextWithShadow(
                ctx,
                textRenderer,
                fitText(value, Math.max(0, maxValW)),
                x,
                y + 11,
                valColor
        );
    }

    private int textLine(DrawContext ctx, String text, int x, int y, int yMax, int color, boolean shadow) {
        if (y + 9 <= yMax && !text.isEmpty()) {
            DrawCompat.drawText(ctx, textRenderer, text, x, y, color, shadow);
        }
        return y + (shadow ? 14 : 12);
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }

        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void stripe(DrawContext ctx, int x, int y, int w) {
        if (w > 2) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + 3, STRIPE);
        }
    }

    private void scissor(DrawContext ctx, int x, int y, int w, int h, Runnable action) {
        if (w <= 0 || h <= 0) {
            return;
        }

        ctx.enableScissor(x, y, x + w, y + h);
        try {
            action.run();
        } finally {
            ctx.disableScissor();
        }
    }

    private String fitText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (maxWidth <= 0) {
            return "";
        }

        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }

        final int ellipsisWidth = textRenderer.getWidth("...");
        if (ellipsisWidth >= maxWidth) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (textRenderer.getWidth(builder.toString() + c) + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(c);
        }

        return builder + "...";
    }

    private String formatElapsed(long ms) {
        long s = Math.max(0L, ms / 1000L);
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
package dev.trg.hugoclient.client.gui;

import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.feature.ClientFeature;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.Map;

public class HugoClickGuiScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final Screen parent;
    private final Map<ClientFeature, ButtonWidget> featureButtons =
            new EnumMap<>(ClientFeature.class);

    private int panelX;
    private int panelY;
    private int panelHeight;

    public HugoClickGuiScreen(Screen parent) {
        super(Text.translatable("screen.hugoclient.click_gui.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        panelHeight = 74 + (ClientFeature.values().length * BUTTON_SPACING);
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - panelHeight) / 2;

        featureButtons.clear();

        int buttonX = panelX + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = panelY + 42;

        for (ClientFeature feature : ClientFeature.values()) {
            ButtonWidget button = ButtonWidget.builder(
                            buildFeatureText(feature),
                            widget -> {
                                HugoClientConfig.toggle(feature);
                                refreshFeatureButtons();
                            }
                    )
                    .dimensions(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            addDrawableChild(button);
            featureButtons.put(feature, button);
            buttonY += BUTTON_SPACING;
        }

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(buttonX, panelY + panelHeight - 28, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );

        refreshFeatureButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawScreenOverlay(context);
        drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawScreenOverlay(DrawContext context) {
        context.fill(0, 0, width, height, 0xA0101010);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void refreshFeatureButtons() {
        for (Map.Entry<ClientFeature, ButtonWidget> entry : featureButtons.entrySet()) {
            entry.getValue().setMessage(buildFeatureText(entry.getKey()));
        }
    }

    private MutableText buildFeatureText(ClientFeature feature) {
        MutableText state = HugoClientConfig.isEnabled(feature)
                ? ScreenTexts.ON.copy()
                : ScreenTexts.OFF.copy();

        return feature.title().copy()
                .append(Text.literal(": "))
                .append(state);
    }

    private void drawPanel(DrawContext context) {
        int left = panelX;
        int top = panelY;
        int right = panelX + PANEL_WIDTH;
        int bottom = panelY + panelHeight;

        context.fill(left, top, right, bottom, 0xE01B1B1B);
        context.fill(left, top, right, top + 1, 0xFF5A5A5A);
        context.fill(left, bottom - 1, right, bottom, 0xFF5A5A5A);
        context.fill(left, top, left + 1, bottom, 0xFF5A5A5A);
        context.fill(right - 1, top, right, bottom, 0xFF5A5A5A);

        context.drawCenteredTextWithShadow(
                textRenderer,
                title,
                width / 2,
                panelY + 12,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.translatable("screen.hugoclient.click_gui.subtitle"),
                width / 2,
                panelY + 24,
                0xBEBEBE
        );
    }
}
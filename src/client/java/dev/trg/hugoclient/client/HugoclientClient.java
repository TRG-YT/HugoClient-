package dev.trg.hugoclient.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.trg.hugoclient.client.chat.ChatListener;
import dev.trg.hugoclient.client.command.PearlBotCommand;
import dev.trg.hugoclient.client.config.PearlBotConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HugoclientClient implements ClientModInitializer {

    private static volatile Runnable pendingScreen = null;

    private static long lastOpenMs = 0L;

    @Override
    public void onInitializeClient() {
        PearlBotConfig.load();
        ChatListener.register();
        PearlBotCommand.register();

        PlayerInfoSessionManager.initChatListener();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Runnable action = pendingScreen;
            if (action != null) {
                pendingScreen = null;
                action.run();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(literal("info")
                    .then(argument("name", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "name");
                                pendingScreen = () -> openInfoScreen(name);
                                return 1;
                            })
                    )
            );

            dispatcher.register(literal("testmodule")
                    .executes(ctx -> {
                        pendingScreen = HugoclientClient::openTestModule;
                        return 1;
                    })
            );
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || !mc.options.sneakKey.isPressed()) return ActionResult.PASS;
            if (!(entity instanceof PlayerEntity target)) return ActionResult.PASS;
            long now = System.currentTimeMillis();
            if (now - lastOpenMs < 300L) return ActionResult.SUCCESS;
            lastOpenMs = now;

            openInfoScreen(target);
            return ActionResult.SUCCESS;
        });

    }

    private static void openTestModule() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = (mc != null) ? mc.player : null;
        if (mc == null || me == null) return;

        PlayerInfoSession session = TestModuleFactory.createRandomSession();
        me.sendMessage(
                Text.literal("§7[HugoClient] §fTestmodul geöffnet für §e" + session.targetName),
                true
        );
        mc.setScreen(new PlayerInspectScreen(session, me));
    }

    private static void openInfoScreen(String targetName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = (mc != null) ? mc.player : null;
        if (mc == null || me == null || mc.world == null) return;

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p.getName().getString().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            me.sendMessage(
                    Text.literal("§cSpieler '" + targetName + "' ist nicht in deiner Nähe / nicht geladen."),
                    false
            );
            return;
        }

        openInfoScreen(target);
    }

    private static void openInfoScreen(PlayerEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = (mc != null) ? mc.player : null;
        if (mc == null || me == null || target == null) return;

        String name = target.getName().getString();
        UUID   uuid = target.getUuid();

        PlayerInfoSession session = PlayerInfoSessionManager.start(name, uuid);
        CommandUtil.send(mc, "statsall " + session.targetName);
        
        CommandUtil.send(mc, "clan uinfo " + session.targetName);

        me.sendMessage(
                Text.literal("§7[HugoClient] §fÖffne Info für §e" + name),
                true
        );
        mc.setScreen(new PlayerInspectScreen(session, target));
    }
}

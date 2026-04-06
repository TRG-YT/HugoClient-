package dev.trg.hugoclient.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.trg.hugoclient.client.config.PearlBotConfig;
import dev.trg.hugoclient.client.config.PearlData;
import dev.trg.hugoclient.client.util.InventoryCompat;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PearlBotCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        literal("pearlbot")
                                .then(literal("save")
                                        .then(argument("name", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    MinecraftClient client = MinecraftClient.getInstance();

                                                    if (client.player == null) {
                                                        return 0;
                                                    }

                                                    String name = StringArgumentType.getString(ctx, "name");

                                                    PearlData data = new PearlData(
                                                            client.player.getYaw(),
                                                            client.player.getPitch(),
                                                            InventoryCompat.getSelectedSlot(client.player.getInventory())
                                                    );

                                                    PearlBotConfig.put(name, data);
                                                    PearlBotConfig.save();

                                                    client.player.sendMessage(
                                                            Text.literal("§aSaved pearl for " + name),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                )
        );
    }
}
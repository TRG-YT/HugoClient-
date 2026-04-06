package dev.trg.hugoclient.client;

import dev.trg.hugoclient.client.config.PearlBotConfig;
import dev.trg.hugoclient.client.config.PearlData;
import dev.trg.hugoclient.client.util.InventoryCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class BotExecutor {

    private static final double REACH = 5.0D;

    public static void execute(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        PearlData data = PearlBotConfig.get(name);
        if (data == null) {
            return;
        }

        client.player.setYaw(data.yaw());
        client.player.setPitch(data.pitch());

        InventoryCompat.setSelectedSlot(client.player.getInventory(), data.slot());

        Vec3d start = client.player.getCameraPosVec(1.0F);
        Vec3d dir = Vec3d.fromPolar(client.player.getPitch(), client.player.getYaw());
        Vec3d end = start.add(dir.multiply(REACH));

        HitResult hit = client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState state = client.world.getBlockState(blockHit.getBlockPos());

        if (!(state.getBlock() instanceof TrapdoorBlock)) {
            return;
        }

        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
        client.player.swingHand(Hand.MAIN_HAND);
    }
}
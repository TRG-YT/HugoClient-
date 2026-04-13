package dev.trg.hugoclient.client.feature;

import dev.trg.hugoclient.client.config.HugoClientConfig;
import dev.trg.hugoclient.client.util.InventoryCompat;
import dev.trg.hugoclient.client.util.ServerUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.List;

public final class ItemDelayModule {
    private static final double GROUND_CHECK_RADIUS = 1.35;
    private static final int RETRY_DELAY_TICKS = 6;
    private static final int ACTION_TIMEOUT_TICKS = 8;
    private static final int EMPTY_SPACE_SLOT = -999;

    private static PendingDrop pendingDrop;
    private static int retryCooldown;

    private ItemDelayModule() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ItemDelayModule::onTick);
    }

    private static void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            resetState();
            return;
        }

        if (!HugoClientConfig.isEnabled(ClientFeature.ITEM_DELAY)) {
            resetState();
            return;
        }
        if (!ServerUtil.isAllowedEnvironment()) {
            resetState();
            return;
        }
        if (player.getAbilities().creativeMode || player.isSpectator()) {
            resetState();
            return;
        }
        if (player.currentScreenHandler != player.playerScreenHandler) {
            resetState();
            return;
        }
        if (player.isUsingItem()) {
            return;
        }

        if (retryCooldown > 0) {
            retryCooldown--;
        }

        if (pendingDrop != null) {
            tickPendingDrop(client, player);
            return;
        }

        if (retryCooldown > 0) {
            return;
        }

        int valuableGroundItems = countValuableGroundItemsUnderPlayer(player);
        if (valuableGroundItems <= 0) return;

        if (countFreeInventorySlots(player) > 0) {
            return;
        }

        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }

        Slot slotToDrop = findDropSlot(player);
        if (slotToDrop == null) {
            return;
        }

        ItemStack expected = slotToDrop.getStack().copy();
        if (expected.isEmpty()) {
            return;
        }

        client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                slotToDrop.id,
                0,
                SlotActionType.PICKUP,
                player
        );

        pendingDrop = new PendingDrop(slotToDrop.id, slotToDrop.getIndex(), expected, PendingStage.WAIT_CURSOR_PICKUP, ACTION_TIMEOUT_TICKS);
    }

    private static void tickPendingDrop(MinecraftClient client, ClientPlayerEntity player) {
        if (pendingDrop == null) {
            return;
        }

        if (--pendingDrop.timeoutTicks < 0) {
            failPendingDrop();
            return;
        }

        if (countValuableGroundItemsUnderPlayer(player) <= 0) {
            resetState();
            return;
        }

        Slot originSlot = getPlayerInventorySlotById(player, pendingDrop.slotId);
        ItemStack cursor = player.currentScreenHandler.getCursorStack();

        switch (pendingDrop.stage) {
            case WAIT_CURSOR_PICKUP -> {
                if (cursor.isEmpty()) {
                    return;
                }

                if (!ItemStack.areItemsAndComponentsEqual(cursor, pendingDrop.expectedStack)) {
                    failPendingDrop();
                    return;
                }

                client.interactionManager.clickSlot(
                        player.playerScreenHandler.syncId,
                        EMPTY_SPACE_SLOT,
                        0,
                        SlotActionType.PICKUP,
                        player
                );

                pendingDrop.stage = PendingStage.WAIT_CURSOR_DROP;
                pendingDrop.timeoutTicks = ACTION_TIMEOUT_TICKS;
            }

            case WAIT_CURSOR_DROP -> {
                if (!cursor.isEmpty()) {
                    return;
                }

                if (originSlot != null && !originSlot.getStack().isEmpty()) {
                    // Server hat die Änderung noch nicht bestätigt oder den Klick zurückgerollt.
                    return;
                }

                announceDrop(player, pendingDrop.expectedStack);
                resetState();
            }
        }
    }

    private static void failPendingDrop() {
        resetState();
        retryCooldown = RETRY_DELAY_TICKS;
    }

    private static void resetState() {
        pendingDrop = null;
        retryCooldown = 0;
    }

    private static int countValuableGroundItemsUnderPlayer(ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return 0;

        Box box = player.getBoundingBox().expand(GROUND_CHECK_RADIUS, 0.35, GROUND_CHECK_RADIUS);
        List<ItemEntity> items = client.world.getEntitiesByClass(
                ItemEntity.class,
                box,
                entity -> !entity.getStack().isEmpty() && OpGlowManager.isHighlightedItem(entity.getStack())
        );

        return items.size();
    }

    private static int countFreeInventorySlots(ClientPlayerEntity player) {
        int free = 0;
        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) continue;

            int invIndex = slot.getIndex();
            if (!isMainInventorySlot(invIndex)) continue;

            if (!slot.hasStack()) {
                free++;
            }
        }
        return free;
    }

    private static Slot findDropSlot(ClientPlayerEntity player) {
        Slot slot = findPotionSlot(player, false);
        if (slot != null) return slot;

        slot = findUtilityBlockSlot(player, false);
        if (slot != null) return slot;

        slot = findTotemSlot(player, false);
        if (slot != null) return slot;

        // Multiplayer-Fallback: wenn nur die Hotbar disposable Items enthält,
        // darf auch dort gedroppt werden – bevorzugt aber nie der aktuell aktive Slot.
        slot = findPotionSlot(player, true);
        if (slot != null) return slot;

        slot = findUtilityBlockSlot(player, true);
        if (slot != null) return slot;

        return findTotemSlot(player, true);
    }

    private static Slot findPotionSlot(ClientPlayerEntity player, boolean includeHotbar) {
        Slot best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) continue;

            int invIndex = slot.getIndex();
            if (!isCandidateInventorySlot(player, invIndex, includeHotbar)) continue;

            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (!isPotionItem(stack)) continue;

            int score = stack.getCount();
            if (stack.getItem() == Items.LINGERING_POTION) score += 30;
            if (stack.getItem() == Items.SPLASH_POTION) score += 20;
            if (stack.getItem() == Items.POTION) score += 10;
            if (isHotbarSlot(invIndex)) score -= 1000;

            if (best == null || score > bestScore) {
                best = slot;
                bestScore = score;
            }
        }

        return best;
    }

    private static Slot findUtilityBlockSlot(ClientPlayerEntity player, boolean includeHotbar) {
        Slot best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) continue;

            int invIndex = slot.getIndex();
            if (!isCandidateInventorySlot(player, invIndex, includeHotbar)) continue;

            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (item != Items.OBSIDIAN && item != Items.COBWEB) continue;

            int score = stack.getCount();
            if (item == Items.OBSIDIAN) score += 20;
            if (item == Items.COBWEB) score += 10;
            if (isHotbarSlot(invIndex)) score -= 1000;

            if (best == null || score > bestScore) {
                best = slot;
                bestScore = score;
            }
        }

        return best;
    }

    private static Slot findTotemSlot(ClientPlayerEntity player, boolean includeHotbar) {
        if (countAllTotems(player) <= 1) {
            return null;
        }

        Slot best = null;
        int bestCount = Integer.MIN_VALUE;

        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) continue;

            int invIndex = slot.getIndex();
            if (!isCandidateInventorySlot(player, invIndex, includeHotbar)) continue;

            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.getItem() != Items.TOTEM_OF_UNDYING) continue;

            int score = stack.getCount();
            if (isHotbarSlot(invIndex)) score -= 1000;

            if (best == null || score > bestCount) {
                best = slot;
                bestCount = score;
            }
        }

        return best;
    }

    private static int countAllTotems(ClientPlayerEntity player) {
        int total = 0;

        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) continue;

            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                total += stack.getCount();
            }
        }

        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            total += offhand.getCount();
        }

        return total;
    }

    private static Slot getPlayerInventorySlotById(ClientPlayerEntity player, int slotId) {
        for (Slot slot : player.playerScreenHandler.slots) {
            if (slot.id == slotId && slot.inventory == player.getInventory()) {
                return slot;
            }
        }
        return null;
    }

    private static void announceDrop(ClientPlayerEntity player, ItemStack droppedStack) {
        String prefix;
        Item item = droppedStack.getItem();

        if (isPotionItem(droppedStack)) {
            prefix = "§dPotion gedroppt";
        } else if (item == Items.OBSIDIAN) {
            prefix = "§5Obsidian gedroppt";
        } else if (item == Items.COBWEB) {
            prefix = "§fCobweb gedroppt";
        } else if (item == Items.TOTEM_OF_UNDYING) {
            prefix = "§cTotem gedroppt";
        } else {
            prefix = "§7Item gedroppt";
        }

        player.sendMessage(
                Text.literal("§6[Inventory Rescue] " + prefix + " §7→ §f" + droppedStack.getName().getString()),
                true
        );
    }

    private static boolean isMainInventorySlot(int invIndex) {
        return invIndex >= 0 && invIndex < 36;
    }

    private static boolean isHotbarSlot(int invIndex) {
        return invIndex >= 0 && invIndex < 9;
    }

    private static boolean isUpperInventorySlot(int invIndex) {
        return invIndex >= 9 && invIndex < 36;
    }

    private static boolean isCandidateInventorySlot(ClientPlayerEntity player, int invIndex, boolean includeHotbar) {
        if (isUpperInventorySlot(invIndex)) {
            return true;
        }

        if (!includeHotbar || !isHotbarSlot(invIndex)) {
            return false;
        }

        int selectedSlot = InventoryCompat.getSelectedSlot(player.getInventory());
        return invIndex != selectedSlot;
    }

    private static boolean isPotionItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }

    private enum PendingStage {
        WAIT_CURSOR_PICKUP,
        WAIT_CURSOR_DROP
    }

    private static final class PendingDrop {
        private final int slotId;
        private final int inventoryIndex;
        private final ItemStack expectedStack;
        private PendingStage stage;
        private int timeoutTicks;

        private PendingDrop(int slotId, int inventoryIndex, ItemStack expectedStack, PendingStage stage, int timeoutTicks) {
            this.slotId = slotId;
            this.inventoryIndex = inventoryIndex;
            this.expectedStack = expectedStack;
            this.stage = stage;
            this.timeoutTicks = timeoutTicks;
        }
    }
}

package dev.trg.hugoclient.client.feature;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Set;

/**
 * Zentrale Verwaltung für alle Items, die vom OP-Glow-Feature hervorgehoben
 * oder von Inventory Rescue geschützt werden.
 */
public final class OpGlowManager {

    public static final int GLOW_COLOR_GOLD = 0xFFFFAA00;
    public static final int GLOW_COLOR_ELYTRA = 0xFFFF2200;

    private static final Set<Item> ELYTRA_ITEMS = Set.of(
            Items.ELYTRA
    );

    private static final Set<Item> OP_ITEMS = Set.of(
            Items.NETHERITE_SWORD,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_HOE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_AXE,
            Items.DIAMOND_SHOVEL,
            Items.DIAMOND_HOE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,
            Items.MACE,
            Items.TRIDENT,
            Items.END_CRYSTAL,
            Items.NETHERITE_INGOT,
            Items.NETHERITE_SCRAP,
            Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
            Items.ANCIENT_DEBRIS,
            Items.NETHERITE_BLOCK,
            Items.DIAMOND,
            Items.DIAMOND_BLOCK,
            Items.DIAMOND_ORE,
            Items.DEEPSLATE_DIAMOND_ORE,
            Items.GILDED_BLACKSTONE,
            Items.SKELETON_SKULL,
            Items.WITHER_SKELETON_SKULL,
            Items.ZOMBIE_HEAD,
            Items.PLAYER_HEAD,
            Items.CREEPER_HEAD,
            Items.DRAGON_HEAD,
            Items.PIGLIN_HEAD
    );

    private static final Set<Item> FIGHT_ITEMS = Set.of(
            Items.END_CRYSTAL,
            Items.MACE,
            Items.TRIDENT,
            Items.BOW,
            Items.CROSSBOW,
            Items.SHIELD,
            Items.ARROW,
            Items.SPECTRAL_ARROW,
            Items.TIPPED_ARROW,
            Items.ENDER_PEARL,
            Items.TOTEM_OF_UNDYING,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.EXPERIENCE_BOTTLE,
            Items.WIND_CHARGE
    );

    private OpGlowManager() {}

    public static boolean isElytraItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && ELYTRA_ITEMS.contains(stack.getItem());
    }

    public static boolean isOpItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && OP_ITEMS.contains(stack.getItem());
    }

    public static boolean isHighlightedItem(ItemStack stack) {
        return isElytraItem(stack) || isOpItem(stack);
    }

    public static int getGlowColor(ItemStack stack) {
        if (isElytraItem(stack)) return GLOW_COLOR_ELYTRA;
        if (isOpItem(stack)) return GLOW_COLOR_GOLD;
        return 0xFFFFFFFF;
    }

    public static int getPriority(ItemStack stack) {
        if (isElytraItem(stack)) return 0;
        if (isOpItem(stack)) return 1;
        return Integer.MAX_VALUE;
    }

    public static boolean isFightItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && FIGHT_ITEMS.contains(stack.getItem());
    }

    public static boolean isProtectedInventoryItem(ItemStack stack) {
        return isHighlightedItem(stack) || isFightItem(stack);
    }

    public static boolean isDisposableTrash(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() == Items.TOTEM_OF_UNDYING) return false;
        return !isProtectedInventoryItem(stack);
    }
}

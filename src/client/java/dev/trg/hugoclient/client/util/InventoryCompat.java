package dev.trg.hugoclient.client.util;

import net.minecraft.entity.player.PlayerInventory;

import java.lang.reflect.Field;

public final class InventoryCompat {

    private static final String[] SLOT_FIELD_NAMES = {
            "selectedSlot", // named/dev
            "field_7545"    // intermediary/runtime
    };

    private static volatile Field selectedSlotField;

    private InventoryCompat() {
    }

    public static int getSelectedSlot(PlayerInventory inventory) {
        try {
            Field field = resolveSelectedSlotField(inventory.getClass());
            return field.getInt(inventory);
        } catch (Throwable t) {
            throw new RuntimeException("Could not read selected slot from PlayerInventory", t);
        }
    }

    public static void setSelectedSlot(PlayerInventory inventory, int slot) {
        try {
            Field field = resolveSelectedSlotField(inventory.getClass());
            field.setInt(inventory, slot);
        } catch (Throwable t) {
            throw new RuntimeException("Could not write selected slot to PlayerInventory", t);
        }
    }

    private static Field resolveSelectedSlotField(Class<?> inventoryClass) throws NoSuchFieldException {
        Field cached = selectedSlotField;
        if (cached != null) {
            return cached;
        }

        synchronized (InventoryCompat.class) {
            cached = selectedSlotField;
            if (cached != null) {
                return cached;
            }

            for (String name : SLOT_FIELD_NAMES) {
                Field found = findFieldRecursive(inventoryClass, name);
                if (found != null) {
                    makeAccessible(found);
                    selectedSlotField = found;
                    return found;
                }
            }

            throw new NoSuchFieldException("selected slot field not found");
        }
    }

    private static Field findFieldRecursive(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static void makeAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (Throwable ignored) {
            try {
                field.trySetAccessible();
            } catch (Throwable ignoredAgain) {
            }
        }
    }
}
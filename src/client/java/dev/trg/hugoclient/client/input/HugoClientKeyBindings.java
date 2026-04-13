package dev.trg.hugoclient.client.input;

import dev.trg.hugoclient.client.gui.HugoClickGuiScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public final class HugoClientKeyBindings {

    private static final String KEY_TRANSLATION = "key.hugoclient.open_click_gui";
    private static final String LEGACY_CATEGORY_TRANSLATION = "key.categories.hugoclient";
    private static final String MODERN_CATEGORY_NAMESPACE = "hugoclient";
    private static final String MODERN_CATEGORY_PATH = "click_gui";

    private static final KeyBinding OPEN_CLICK_GUI = registerOpenClickGuiKey();

    private HugoClientKeyBindings() {
    }

    public static void register() {
        // Stellt sicher, dass die statische Initialisierung beim Start ausgeführt wird.
    }

    public static void handleEndTick(MinecraftClient client) {
        while (OPEN_CLICK_GUI.wasPressed()) {
            if (client.currentScreen instanceof HugoClickGuiScreen) {
                continue;
            }

            client.setScreen(new HugoClickGuiScreen(client.currentScreen));
        }
    }

    private static KeyBinding registerOpenClickGuiKey() {
        return KeyBindingHelper.registerKeyBinding(
                createCompatibleKeyBinding(
                        KEY_TRANSLATION,
                        InputUtil.Type.KEYSYM,
                        InputUtil.UNKNOWN_KEY.getCode()
                )
        );
    }

    private static KeyBinding createCompatibleKeyBinding(String translationKey, InputUtil.Type type, int code) {
        RuntimeException lastFailure = null;

        for (Constructor<?> constructor : getSortedKeyBindingConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (!isSupportedKeyBindingConstructor(parameterTypes)) {
                continue;
            }

            try {
                Object categoryArgument = createCategoryArgument(parameterTypes[3]);
                constructor.setAccessible(true);
                return (KeyBinding) constructor.newInstance(translationKey, type, code, categoryArgument);
            } catch (Exception e) {
                lastFailure = new RuntimeException(
                        "Failed with constructor " + describeConstructor(constructor),
                        e
                );
            }
        }

        throw new RuntimeException(
                "Failed to create compatible key binding for HugoClient. Constructors="
                        + describeAvailableConstructors(),
                lastFailure
        );
    }

    private static Constructor<?>[] getSortedKeyBindingConstructors() {
        Constructor<?>[] constructors = KeyBinding.class.getConstructors();
        Arrays.sort(constructors, Comparator.comparingInt((Constructor<?> c) -> categoryPriority(c.getParameterTypes())).reversed());
        return constructors;
    }

    private static int categoryPriority(Class<?>[] parameterTypes) {
        if (!isSupportedKeyBindingConstructor(parameterTypes)) {
            return -1;
        }

        return parameterTypes[3] == String.class ? 0 : 1;
    }

    private static boolean isSupportedKeyBindingConstructor(Class<?>[] parameterTypes) {
        return parameterTypes.length == 4
                && parameterTypes[0] == String.class
                && parameterTypes[1] == InputUtil.Type.class
                && (parameterTypes[2] == int.class || parameterTypes[2] == Integer.TYPE);
    }

    private static Object createCategoryArgument(Class<?> categoryType) throws ReflectiveOperationException {
        if (categoryType == String.class) {
            return LEGACY_CATEGORY_TRANSLATION;
        }

        Identifier identifier = Identifier.of(MODERN_CATEGORY_NAMESPACE, MODERN_CATEGORY_PATH);

        Object category = tryCreateCategory(categoryType, identifier, KeyBinding.class.getDeclaredClasses());
        if (category != null) {
            return category;
        }

        category = tryCreateCategory(categoryType, identifier, new Class<?>[]{categoryType});
        if (category != null) {
            return category;
        }

        category = tryInvokeFactoryMethods(KeyBinding.class, categoryType, identifier);
        if (category != null) {
            return category;
        }

        throw new NoSuchMethodException("No compatible keybinding category factory for " + categoryType.getName());
    }

    private static Object tryCreateCategory(Class<?> expectedType, Identifier identifier, Class<?>[] candidateClasses)
            throws ReflectiveOperationException {
        for (Class<?> candidate : candidateClasses) {
            if (!expectedType.isAssignableFrom(candidate) && !candidate.isAssignableFrom(expectedType)) {
                continue;
            }

            Object category = tryInvokeFactoryMethods(candidate, expectedType, identifier);
            if (category != null) {
                return category;
            }

            category = tryInvokeConstructors(candidate, expectedType, identifier);
            if (category != null) {
                return category;
            }
        }

        return null;
    }

    private static Object tryInvokeFactoryMethods(Class<?> owner, Class<?> expectedType, Identifier identifier)
            throws ReflectiveOperationException {
        for (Method method : owner.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!expectedType.isAssignableFrom(method.getReturnType())) {
                continue;
            }

            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == Identifier.class) {
                return method.invoke(null, identifier);
            }
            if (params.length == 2 && params[0] == String.class && params[1] == String.class) {
                return method.invoke(null, MODERN_CATEGORY_NAMESPACE, MODERN_CATEGORY_PATH);
            }
        }

        return null;
    }

    private static Object tryInvokeConstructors(Class<?> candidate, Class<?> expectedType, Identifier identifier)
            throws ReflectiveOperationException {
        for (Constructor<?> constructor : candidate.getConstructors()) {
            Class<?>[] params = constructor.getParameterTypes();
            Object created = null;

            if (params.length == 1 && params[0] == Identifier.class) {
                created = constructor.newInstance(identifier);
            } else if (params.length == 2 && params[0] == String.class && params[1] == String.class) {
                created = constructor.newInstance(MODERN_CATEGORY_NAMESPACE, MODERN_CATEGORY_PATH);
            }

            if (created != null && expectedType.isInstance(created)) {
                return created;
            }
        }

        return null;
    }

    private static String describeAvailableConstructors() {
        return Arrays.stream(KeyBinding.class.getConstructors())
                .map(HugoClientKeyBindings::describeConstructor)
                .filter(Objects::nonNull)
                .reduce((left, right) -> left + "; " + right)
                .orElse("<none>");
    }

    private static String describeConstructor(Constructor<?> constructor) {
        return constructor.getName() + Arrays.toString(constructor.getParameterTypes());
    }
}
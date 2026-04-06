package dev.trg.hugoclient.client.util;

import net.minecraft.client.gui.DrawContext;

import java.lang.reflect.Method;

public final class MatrixCompat {

    private static volatile Method getMatricesMethod;
    private static volatile Method pushMethod;
    private static volatile Method popMethod;
    private static volatile Class<?> stackClass;

    private MatrixCompat() {
    }

    public static void push(DrawContext ctx) {
        try {
            Object matrices = getMatricesObject(ctx);
            Method push = resolvePushMethod(matrices.getClass());
            push.invoke(matrices);
        } catch (Throwable t) {
            throw new RuntimeException("Could not push DrawContext matrices compatibly", t);
        }
    }

    public static void pop(DrawContext ctx) {
        try {
            Object matrices = getMatricesObject(ctx);
            Method pop = resolvePopMethod(matrices.getClass());
            pop.invoke(matrices);
        } catch (Throwable t) {
            throw new RuntimeException("Could not pop DrawContext matrices compatibly", t);
        }
    }

    private static Object getMatricesObject(DrawContext ctx) throws Exception {
        Method method = resolveGetMatricesMethod(ctx.getClass());
        return method.invoke(ctx);
    }

    private static Method resolveGetMatricesMethod(Class<?> drawContextClass) {
        Method cached = getMatricesMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (MatrixCompat.class) {
            cached = getMatricesMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(drawContextClass, new String[]{"getMatrices", "method_51448"});
            if (found == null) {
                throw new RuntimeException("No compatible DrawContext.getMatrices method found");
            }

            makeAccessible(found);
            getMatricesMethod = found;
            return found;
        }
    }

    private static Method resolvePushMethod(Class<?> matricesClass) {
        Method cached = pushMethod;
        Class<?> cachedClass = stackClass;

        if (cached != null && cachedClass == matricesClass) {
            return cached;
        }

        synchronized (MatrixCompat.class) {
            cached = pushMethod;
            cachedClass = stackClass;

            if (cached != null && cachedClass == matricesClass) {
                return cached;
            }

            Method found = findMethodRecursive(matricesClass, new String[]{"push", "pushMatrix"});
            if (found == null) {
                throw new RuntimeException("No compatible matrix push method found on " + matricesClass.getName());
            }

            makeAccessible(found);
            pushMethod = found;
            stackClass = matricesClass;
            return found;
        }
    }

    private static Method resolvePopMethod(Class<?> matricesClass) {
        Method cached = popMethod;
        Class<?> cachedClass = stackClass;

        if (cached != null && cachedClass == matricesClass) {
            return cached;
        }

        synchronized (MatrixCompat.class) {
            cached = popMethod;
            cachedClass = stackClass;

            if (cached != null && cachedClass == matricesClass) {
                return cached;
            }

            Method found = findMethodRecursive(matricesClass, new String[]{"pop", "popMatrix"});
            if (found == null) {
                throw new RuntimeException("No compatible matrix pop method found on " + matricesClass.getName());
            }

            makeAccessible(found);
            popMethod = found;
            stackClass = matricesClass;
            return found;
        }
    }

    private static Method findMethodRecursive(Class<?> owner, String[] names, Class<?>... parameterTypes) {
        Class<?> current = owner;
        while (current != null) {
            for (String name : names) {
                try {
                    return current.getMethod(name, parameterTypes);
                } catch (NoSuchMethodException ignored) {
                }

                try {
                    return current.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static void makeAccessible(Method method) {
        try {
            method.setAccessible(true);
        } catch (Throwable ignored) {
            try {
                method.trySetAccessible();
            } catch (Throwable ignoredAgain) {
            }
        }
    }
}
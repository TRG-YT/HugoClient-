package dev.trg.hugoclient.client.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.lang.reflect.Method;

public final class DrawCompat {

    private static volatile Method drawTextStringMethod;
    private static volatile Method drawTextOrderedMethod;
    private static volatile Method drawTextTextMethod;

    private static volatile Method drawTextShadowStringMethod;
    private static volatile Method drawTextShadowOrderedMethod;
    private static volatile Method drawTextShadowTextMethod;

    private DrawCompat() {
    }

    public static int drawText(DrawContext ctx, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            text = "";
        }

        if (shadow) {
            return drawTextWithShadow(ctx, renderer, text, x, y, color);
        }

        try {
            Method method = resolveDrawTextString(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, text, x, y, color, false);
            }

            OrderedText ordered = Text.literal(text).asOrderedText();

            method = resolveDrawTextOrdered(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, ordered, x, y, color, false);
            }

            method = resolveDrawTextText(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, Text.literal(text), x, y, color, false);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Could not draw text compatibly", t);
        }

        throw new RuntimeException("No compatible DrawContext.drawText overload found");
    }

    public static int drawTextWithShadow(DrawContext ctx, TextRenderer renderer, String text, int x, int y, int color) {
        if (text == null) {
            text = "";
        }

        try {
            Method method = resolveDrawTextShadowString(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, text, x, y, color);
            }

            OrderedText ordered = Text.literal(text).asOrderedText();

            method = resolveDrawTextShadowOrdered(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, ordered, x, y, color);
            }

            method = resolveDrawTextShadowText(ctx.getClass());
            if (method != null) {
                return invokeTextMethod(method, ctx, renderer, Text.literal(text), x, y, color);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Could not draw shadow text compatibly", t);
        }

        throw new RuntimeException("No compatible DrawContext.drawTextWithShadow overload found");
    }

    private static int invokeTextMethod(Method method, Object target, Object... args) throws Exception {
        Object result = method.invoke(target, args);

        if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            return 0;
        }

        if (result instanceof Number number) {
            return number.intValue();
        }

        return 0;
    }

    private static Method resolveDrawTextString(Class<?> owner) {
        Method cached = drawTextStringMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextStringMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawText", "method_51433"},
                    TextRenderer.class, String.class, int.class, int.class, int.class, boolean.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextStringMethod = found;
            }
            return found;
        }
    }

    private static Method resolveDrawTextOrdered(Class<?> owner) {
        Method cached = drawTextOrderedMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextOrderedMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawText", "method_51430"},
                    TextRenderer.class, OrderedText.class, int.class, int.class, int.class, boolean.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextOrderedMethod = found;
            }
            return found;
        }
    }

    private static Method resolveDrawTextText(Class<?> owner) {
        Method cached = drawTextTextMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextTextMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawText", "method_51439"},
                    TextRenderer.class, Text.class, int.class, int.class, int.class, boolean.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextTextMethod = found;
            }
            return found;
        }
    }

    private static Method resolveDrawTextShadowString(Class<?> owner) {
        Method cached = drawTextShadowStringMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextShadowStringMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawTextWithShadow", "method_25303"},
                    TextRenderer.class, String.class, int.class, int.class, int.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextShadowStringMethod = found;
            }
            return found;
        }
    }

    private static Method resolveDrawTextShadowOrdered(Class<?> owner) {
        Method cached = drawTextShadowOrderedMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextShadowOrderedMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawTextWithShadow", "method_35720"},
                    TextRenderer.class, OrderedText.class, int.class, int.class, int.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextShadowOrderedMethod = found;
            }
            return found;
        }
    }

    private static Method resolveDrawTextShadowText(Class<?> owner) {
        Method cached = drawTextShadowTextMethod;
        if (cached != null) {
            return cached;
        }

        synchronized (DrawCompat.class) {
            cached = drawTextShadowTextMethod;
            if (cached != null) {
                return cached;
            }

            Method found = findMethodRecursive(
                    owner,
                    new String[]{"drawTextWithShadow", "method_27535"},
                    TextRenderer.class, Text.class, int.class, int.class, int.class
            );
            if (found != null) {
                makeAccessible(found);
                drawTextShadowTextMethod = found;
            }
            return found;
        }
    }

    private static Method findMethodRecursive(Class<?> owner, String[] possibleNames, Class<?>... parameterTypes) {
        Class<?> current = owner;
        while (current != null) {
            for (String name : possibleNames) {
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
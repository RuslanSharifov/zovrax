package az.zovrax.inventorypickup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Isolated HUD rendering. Preview and game HUD share the exact drawing logic. */
public final class PickupHudRenderer {
    public static final int WIDTH = 194;
    public static final int HEIGHT = 32;
    private static final int GAP = 6;
    private final List<PickupToast> active = new ArrayList<>();

    public static int scaledWidth(PickupConfig config) {
        return Math.round(WIDTH * config.notificationScale / 100f);
    }
    public static int scaledHeight(PickupConfig config) {
        return Math.round(HEIGHT * config.notificationScale / 100f);
    }
    public static int pixelX(PickupConfig config, int screenWidth) {
        return Math.round((float) config.hudX * Math.max(0, screenWidth - scaledWidth(config)));
    }
    public static int pixelY(PickupConfig config, int screenHeight) {
        return Math.round((float) config.hudY * Math.max(0, screenHeight - scaledHeight(config)));
    }

    public void enqueue(ItemStack stack, int amount, int total, PickupConfig config) {
        if (amount <= 0 || stack.isEmpty()) return;
        long now = System.currentTimeMillis();
        if (config.mergeRepeated) {
            for (PickupToast toast : active) {
                if (ItemStack.areItemsAndComponentsEqual(toast.stack, stack)
                        && now - toast.createdAtMillis < config.durationMs) {
                    toast.accumulate(amount, total, now);
                    return;
                }
            }
        }
        active.add(0, new PickupToast(stack, amount, total, now));
        while (active.size() > config.maxVisible) active.remove(active.size() - 1);
    }

    public void prune(PickupConfig config) {
        long now = System.currentTimeMillis();
        for (Iterator<PickupToast> iterator = active.iterator(); iterator.hasNext();) {
            if (now - iterator.next().createdAtMillis >= config.durationMs) iterator.remove();
        }
        while (active.size() > config.maxVisible) active.remove(active.size() - 1);
    }

    public void render(DrawContext context, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PickupConfig config = InventoryPickupClient.config();
        if (!config.enabled || client.player == null ||
                (client.currentScreen != null && !config.allowOverScreens)) return;

        config.ensurePosition(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        long now = System.currentTimeMillis();
        int x = pixelX(config, context.getScaledWindowWidth());
        int y = pixelY(config, context.getScaledWindowHeight());
        for (int i = 0; i < Math.min(config.maxVisible, active.size()); i++) {
            PickupToast toast = active.get(i);
            long elapsed = now - toast.createdAtMillis;
            if (elapsed < 0 || elapsed >= config.durationMs) continue;
            float opacity = envelope(elapsed, config.durationMs, config.animationMs);
            float eased = ease(opacity);
            float shift = (1f - eased) * 32f;
            int dx = config.hudX > 0.5 ? Math.round(shift) : -Math.round(shift);
            int dy = y + (config.hudY > 0.5 ? -i : i) *
                    (scaledHeight(config) + Math.round(GAP * config.notificationScale / 100f));
            // Anchor remains on-screen; slide motion is relative to its chosen position.
            drawNotification(context, client, toast.stack, toast.gain, toast.total,
                    x + dx, dy, opacity, config);
        }
    }

    public static void drawPreview(DrawContext ctx, PickupConfig config, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        drawNotification(ctx, client, new ItemStack(net.minecraft.item.Items.DIAMOND), 4, 9,
                x, y, 1f, config);
    }

    private static void drawNotification(DrawContext ctx, MinecraftClient client,
                                         ItemStack stack, int gain, int total, int x, int y,
                                         float opacity, PickupConfig config) {
        float scale = config.notificationScale / 100f;
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(x, y);
            ctx.getMatrices().scale(scale, scale);
            int border = argb(opacity * 0.8f, 148, 255, 135);
            int background = argb(opacity * config.backgroundOpacity / 100f, 10, 24, 16);
            ctx.fill(0, 0, WIDTH, HEIGHT, background);
            ctx.fill(0, 0, 2, HEIGHT, border);
            ctx.fill(0, 0, WIDTH, 1, argb(opacity * .35f, 170, 220, 153));
            if (opacity > .18f) ctx.drawItem(stack, 8, 8);
            String name = stack.getName().getString();
            if (name.length() > 25) name = name.substring(0, 24) + "…";
            ctx.drawTextWithShadow(client.textRenderer, name, 31, 6, argb(opacity, 247, 255, 242));
            StringBuilder info = new StringBuilder();
            if (config.showGain) info.append("+").append(gain);
            if (config.showTotal) {
                if (!info.isEmpty()) info.append("   ");
                info.append("Total: ").append(total);
            }
            ctx.drawTextWithShadow(client.textRenderer, info.toString(), 31, 18,
                    argb(opacity, 173, 242, 157));
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }

    static float envelope(long elapsed, int duration, int animation) {
        if (elapsed < 0 || elapsed >= duration) return 0f;
        return Math.max(0f, Math.min(1f,
                Math.min(elapsed / (float) animation, (duration - elapsed) / (float) animation)));
    }
    private static float ease(float value) { return value * value * (3f - 2f * value); }
    private static int argb(float opacity, int red, int green, int blue) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255f)));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

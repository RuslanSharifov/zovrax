package az.zovrax.inventorypickup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Independent, additive HUD layer: no vanilla mixins or global GL state changes.
 * The background/text fade 0->1->0 while the notification slides in and out.
 */
public final class PickupHudRenderer {
    private static final int WIDTH = 194;
    private static final int HEIGHT = 32;
    private static final int GAP = 6;
    private final List<PickupToast> active = new ArrayList<>();

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
        while (active.size() > config.maxVisible) {
            active.remove(active.size() - 1);
        }
    }

    public void prune(PickupConfig config) {
        long now = System.currentTimeMillis();
        for (Iterator<PickupToast> iterator = active.iterator(); iterator.hasNext();) {
            PickupToast toast = iterator.next();
            if (now - toast.createdAtMillis >= config.durationMs) iterator.remove();
        }
        while (active.size() > config.maxVisible) active.remove(active.size() - 1);
    }

    public void render(DrawContext context, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PickupConfig config = InventoryPickupClient.config();
        if (!config.enabled || client.player == null
                || (client.currentScreen != null && !config.allowOverScreens)) {
            return;
        }

        long now = System.currentTimeMillis();
        int visible = Math.min(config.maxVisible, active.size());
        for (int i = 0; i < visible; i++) {
            PickupToast toast = active.get(i);
            long elapsed = now - toast.createdAtMillis;
            if (elapsed < 0 || elapsed > config.durationMs) continue;

            float opacity = envelope(elapsed, config.durationMs, config.animationMs);
            float eased = ease(opacity);
            int offscreenShift = Math.round((1f - eased) * 32f);
            int baseX = ((config.position & 1) == 0)
                    ? context.getScaledWindowWidth() - config.margin - WIDTH
                    : config.margin;
            int x = ((config.position & 1) == 0)
                    ? baseX + offscreenShift : baseX - offscreenShift;
            int y = config.position < 2
                    ? config.margin + i * (HEIGHT + GAP)
                    : context.getScaledWindowHeight() - config.margin - HEIGHT - i * (HEIGHT + GAP);

            paintToast(context, client, toast, x, y, opacity, config);
        }
    }

    private void paintToast(DrawContext ctx, MinecraftClient client, PickupToast toast,
                            int x, int y, float opacity, PickupConfig config) {
        int border = argb(opacity * 0.8f, 148, 255, 135);
        int background = argb(opacity * config.backgroundOpacity / 100f, 10, 24, 16);
        ctx.fill(x, y, x + WIDTH, y + HEIGHT, background);
        ctx.fill(x, y, x + 2, y + HEIGHT, border);
        ctx.fill(x, y, x + WIDTH, y + 1, argb(opacity * 0.35f, 170, 220, 153));

        // Vanilla item rendering doesn't expose per-icon alpha; all other elements
        // fade continuously. Wait for the first frames before showing the item.
        if (opacity > 0.18f) ctx.drawItem(toast.stack, x + 8, y + 8);

        String displayName = toast.stack.getName().getString();
        if (displayName.length() > 25) displayName = displayName.substring(0, 24) + "…";
        ctx.drawTextWithShadow(client.textRenderer, displayName, x + 31, y + 6,
                argb(opacity, 247, 255, 242));

        StringBuilder info = new StringBuilder();
        if (config.showGain) info.append("+").append(toast.gain);
        if (config.showTotal) {
            if (!info.isEmpty()) info.append("   ");
            info.append("Total: ").append(toast.total);
        }
        ctx.drawTextWithShadow(client.textRenderer, info.toString(), x + 31, y + 18,
                argb(opacity, 173, 242, 157));
    }

    /** Smooth, symmetric opacity envelope. */
    static float envelope(long elapsed, int duration, int animation) {
        if (elapsed < 0 || elapsed >= duration) return 0f;
        float in = Math.min(1f, elapsed / (float) animation);
        float out = Math.min(1f, (duration - elapsed) / (float) animation);
        return Math.max(0f, Math.min(in, out));
    }

    private static float ease(float progress) {
        return progress * progress * (3f - 2f * progress);
    }

    private static int argb(float opacity, int red, int green, int blue) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255f)));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

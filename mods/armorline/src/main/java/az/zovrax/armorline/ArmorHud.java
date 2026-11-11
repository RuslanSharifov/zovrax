package az.zovrax.armorline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** Four armor items rendered as one movable HUD, with independent display layers. */
public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] EMPTY_LABELS = { "H", "C", "L", "B" };
    private static final int ROW = 31;
    private static final int V_WIDTH = 116;
    private static final int V_HEIGHT = 128;
    private static final int H_WIDTH = 316;
    private static final int H_HEIGHT = 35;

    private ArmorHud() { }

    public static int width(ArmorConfig c) {
        return Math.round((c.vertical ? V_WIDTH : H_WIDTH) * c.scale / 100f);
    }

    public static int height(ArmorConfig c) {
        return Math.round((c.vertical ? V_HEIGHT : H_HEIGHT) * c.scale / 100f);
    }

    public static int x(ArmorConfig c, int screenWidth) {
        return Math.round((float) c.x * Math.max(0, screenWidth - width(c)));
    }

    public static int y(ArmorConfig c, int screenHeight) {
        return Math.round((float) c.y * Math.max(0, screenHeight - height(c)));
    }

    public static void hud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ArmorConfig config = ArmorLineClient.config();
        if (!config.enabled || client.player == null || client.currentScreen != null) return;
        config.ensurePosition(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        draw(ctx, client, config, x(config, ctx.getScaledWindowWidth()),
            y(config, ctx.getScaledWindowHeight()), false);
    }

    public static void drawPreview(DrawContext ctx, ArmorConfig config, int px, int py) {
        draw(ctx, MinecraftClient.getInstance(), config, px, py, true);
    }

    private static int argb(int rgb, float alpha) {
        return (Math.clamp(Math.round(alpha * 255), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static void rectangle(DrawContext ctx, int x1, int y1, int x2, int y2,
                                  int rgb, float alpha) {
        if (alpha > 0.001f) ctx.fill(x1, y1, x2, y2, argb(rgb, alpha));
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, ArmorConfig c,
                             int x, int y, boolean preview) {
        if (!preview && !c.enabled) return;
        float combined = c.opacity / 100f;
        if (combined <= 0f) return;
        float background = combined * c.backgroundOpacity / 100f;
        float icons = combined * c.iconsOpacity / 100f;
        float labels = combined * c.textOpacity / 100f;
        int w = c.vertical ? V_WIDTH : H_WIDTH;
        int h = c.vertical ? V_HEIGHT : H_HEIGHT;

        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(x, y);
            ctx.getMatrices().scale(c.scale / 100f, c.scale / 100f);

            rectangle(ctx, 0, 0, w, h, 0x132422, background * .88f);
            rectangle(ctx, 0, 0, w, 1, 0x76E5C0, background);
            rectangle(ctx, 0, h - 1, w, h, 0x76E5C0, background);
            // Continuous spine: the armor pieces form a single connected widget.
            if (c.vertical) rectangle(ctx, w / 2 - 1, 14, w / 2 + 1, h - 14, 0x7DE7C5, background * .7f);
            else rectangle(ctx, 14, h / 2 - 1, w - 14, h / 2 + 1, 0x7DE7C5, background * .7f);

            for (int i = 0; i < SLOTS.length; i++) {
                int left = c.vertical ? 3 : 3 + i * 78;
                int top = c.vertical ? 3 + i * ROW : 3;
                int right = c.vertical ? w - 3 : left + 76;
                int bottom = c.vertical ? top + ROW - 1 : h - 3;
                rectangle(ctx, left, top, right, bottom, 0x244039, background * .68f);

                int side = c.indicatorSide == 0 ? (c.x > .5d ? 2 : 1) : c.indicatorSide;
                boolean leftSide = side == 2;
                boolean indicators = side != 3;
                // Image stays inside its slot. The side choice changes where the
                // durability bar/numbers appear relative to the equipped armor.
                int itemX = leftSide && indicators ? right - 23 : left + 6;
                int iconY = top + 6;
                ItemStack stack = mc.player == null ? ItemStack.EMPTY : mc.player.getEquippedStack(SLOTS[i]);

                if (icons > 0f) {
                    rectangle(ctx, itemX - 1, top + 5, itemX + 17, top + 24,
                        stack.isEmpty() ? 0x3E5B52 : 0x335149, background * .65f);
                    if (stack.isEmpty()) {
                        ctx.drawTextWithShadow(mc.textRenderer, EMPTY_LABELS[i],
                            itemX + 5, top + 9, argb(0xAAC7BB, icons * .7f));
                    } else {
                        // The item renderer is opaque in Minecraft 1.21.10. Apply a
                        // surface-tinted veil to attenuate it, without changing
                        // global shader state or other mods' HUD rendering.
                        ctx.drawItem(stack, itemX, iconY);
                        if (icons < 1f) rectangle(ctx, itemX, iconY, itemX + 16, iconY + 16,
                            0x152825, (1f - icons) * .95f);
                    }
                }

                if (!indicators || stack.isEmpty() || !stack.isDamageable()) continue;
                int max = Math.max(1, stack.getMaxDamage());
                int remaining = Math.clamp(max - stack.getDamage(), 0, max);
                float ratio = remaining / (float) max;
                int barX = leftSide ? left + 4 : left + 27;
                int barW = c.vertical ? 81 : 46;
                int textX = barX;
                int barY = bottom - 7;
                if (c.durabilityMode == 0 || c.durabilityMode == 1) {
                    rectangle(ctx, barX, barY, barX + barW, barY + 3, 0x192A26, background);
                    rectangle(ctx, barX, barY, barX + Math.round(barW * ratio), barY + 3,
                        ratio > .5f ? 0x76E39D : ratio > .25f ? 0xE6C36B : 0xF27C70, labels);
                }
                if ((c.durabilityMode == 0 || c.durabilityMode == 2) && labels > 0f) {
                    ctx.getMatrices().pushMatrix();
                    try {
                        ctx.getMatrices().translate(textX, top + 5);
                        ctx.getMatrices().scale(c.vertical ? .82f : .58f, c.vertical ? .82f : .58f);
                        ctx.drawTextWithShadow(mc.textRenderer, remaining + "/" + max, 0, 0,
                            argb(0xEAFBF3, labels));
                    } finally {
                        ctx.getMatrices().popMatrix();
                    }
                }
            }
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }
}

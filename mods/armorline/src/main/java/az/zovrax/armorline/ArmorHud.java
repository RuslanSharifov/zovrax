package az.zovrax.armorline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** A single connected armor silhouette, ordered HEAD → CHEST → LEGS → FEET. */
public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int V_WIDTH = 67;
    private static final int V_HEIGHT = 130;
    private static final int H_WIDTH = 234;
    private static final int H_HEIGHT = 36;

    public static int width(ArmorConfig c) {
        return Math.round((c.vertical ? V_WIDTH : H_WIDTH) * c.scale / 100f);
    }

    public static int height(ArmorConfig c) {
        return Math.round((c.vertical ? V_HEIGHT : H_HEIGHT) * c.scale / 100f);
    }

    public static int x(ArmorConfig c, int screenW) {
        return Math.round((float) c.x * Math.max(0, screenW - width(c)));
    }

    public static int y(ArmorConfig c, int screenH) {
        return Math.round((float) c.y * Math.max(0, screenH - height(c)));
    }

    public static void hud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ArmorConfig c = ArmorLineClient.config();
        if (!c.enabled || client.player == null || client.currentScreen != null) return;
        c.ensurePosition(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        draw(ctx, client, c, x(c, ctx.getScaledWindowWidth()),
            y(c, ctx.getScaledWindowHeight()), false);
    }

    public static void drawPreview(DrawContext ctx, ArmorConfig c, int px, int py) {
        draw(ctx, MinecraftClient.getInstance(), c, px, py, true);
    }

    private static int color(int rgb, float alpha) {
        return (Math.clamp(Math.round(alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, ArmorConfig c,
                             int px, int py, boolean preview) {
        if (!preview && !c.enabled) return;
        float alpha = c.opacity / 100f;
        if (alpha <= 0) return;
        float scale = c.scale / 100f;
        int w = c.vertical ? V_WIDTH : H_WIDTH;
        int h = c.vertical ? V_HEIGHT : H_HEIGHT;
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(px, py);
            ctx.getMatrices().scale(scale, scale);
            // One continuous background with four linked silhouette segments.
            ctx.fill(0, 0, w, h, color(0x101E20, alpha * 0.75f));
            ctx.fill(0, 0, w, 1, color(0x7DE7C5, alpha * 0.7f));
            ctx.fill(0, h - 1, w, h, color(0x7DE7C5, alpha * 0.7f));
            if (c.vertical) {
                ctx.fill(12, 13, 14, 116, color(0x7DE7C5, alpha * 0.45f));
            } else {
                ctx.fill(20, 12, 214, 14, color(0x7DE7C5, alpha * 0.45f));
            }
            for (int index = 0; index < SLOTS.length; index++) {
                int left = c.vertical ? 3 : 3 + index * 58;
                int top = c.vertical ? 3 + index * 31 : 3;
                int right = c.vertical ? w - 3 : left + 54;
                int bottom = c.vertical ? top + 30 : h - 3;
                if (index > 0) {
                    if (c.vertical) ctx.fill(26, top - 2, 39, top, color(0x7DE7C5, alpha * 0.7f));
                    else ctx.fill(left - 4, 16, left, 18, color(0x7DE7C5, alpha * 0.7f));
                }
                ctx.fill(left, top, right, bottom, color(0x243734, alpha * 0.76f));
                ItemStack stack = mc.player == null ? ItemStack.EMPTY : mc.player.getEquippedStack(SLOTS[index]);
                // The editing preview shows an outline even for an empty armor slot.
                ctx.fill(left + 5, top + 5, left + 23, top + 23,
                    color(stack.isEmpty() ? 0x405954 : 0x3B534B, alpha * 0.5f));
                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, left + 6, top + 6);
                    // Do not mutate Minecraft's global shader state. A translucent
                    // overlay attenuates icons together with the connected widget.
                    if (alpha < 1f) {
                        ctx.fill(left + 5, top + 5, left + 23, top + 23,
                            color(0x243734, (1f - alpha) * 0.92f));
                    }
                } else {
                    // Helmet / chest / leggings / boots glyph markers.
                    String marker = new String[]{"H", "C", "L", "B"}[index];
                    ctx.drawTextWithShadow(mc.textRenderer, marker,
                        left + 11, top + 9, color(0x91A9A0, alpha * .8f));
                }
                if (!stack.isEmpty() && stack.isDamageable()) {
                    int max = Math.max(1, stack.getMaxDamage());
                    int remaining = Math.clamp(max - stack.getDamage(), 0, max);
                    if (c.showDurabilityBar) {
                        float ratio = remaining / (float) max;
                        int barLeft = left + 26;
                        int barRight = right - 4;
                        int barBottom = bottom - 5;
                        ctx.fill(barLeft, barBottom, barRight, barBottom + 3, color(0x152421, alpha));
                        ctx.fill(barLeft, barBottom,
                            barLeft + Math.round((barRight - barLeft) * ratio), barBottom + 3,
                            color(ratio > .5f ? 0x71E397 : ratio > .25f ? 0xE5C36C : 0xF27D72, alpha));
                    }
                    if (c.showDurabilityNumbers) {
                        // Text remains exact (remaining/max), rather than assuming all armor has 100 durability.
                        ctx.getMatrices().pushMatrix();
                        try {
                            ctx.getMatrices().translate(left + 26, top + 5);
                            ctx.getMatrices().scale(.58f, .58f);
                            ctx.drawTextWithShadow(mc.textRenderer, remaining + "/" + max, 0, 0,
                                color(0xE8F9F0, alpha));
                        } finally { ctx.getMatrices().popMatrix(); }
                    }
                }
            }
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }
}

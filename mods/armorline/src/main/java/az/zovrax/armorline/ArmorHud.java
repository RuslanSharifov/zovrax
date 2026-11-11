package az.zovrax.armorline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Screenshot-inspired equipment display: helmet, chestplate, leggings, boots
 * centered in one vertical group, remaining durability to the left, equipped
 * main hand tool and offhand (shield/totem/other item) in a right-hand column.
 */
public final class ArmorHud {
    private static final EquipmentSlot[] ARMOR = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int ARMOR_X = 56;
    private static final int HAND_X = 112;
    private static final int V_WIDTH = 172;
    private static final int V_HEIGHT = 96;
    private static final int H_WIDTH = 280;
    private static final int H_HEIGHT = 61;
    private ArmorHud() {}

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
        return (Math.clamp(Math.round(alpha * 255f), 0, 255) << 24) | (rgb & 0xffffff);
    }

    private static void fill(DrawContext ctx, int x1, int y1, int x2, int y2,
                             int rgb, float alpha) {
        if (alpha > 0f) ctx.fill(x1, y1, x2, y2, color(rgb, alpha));
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, ArmorConfig c,
                             int x, int y, boolean preview) {
        if (!preview && !c.enabled) return;
        float overall = c.opacity / 100f;
        if (overall <= 0f) return;
        float background = overall * c.backgroundOpacity / 100f;
        float iconAlpha = overall * c.iconsOpacity / 100f;
        float labelAlpha = overall * c.textOpacity / 100f;
        int w = c.vertical ? V_WIDTH : H_WIDTH;
        int h = c.vertical ? V_HEIGHT : H_HEIGHT;

        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(x, y);
            ctx.getMatrices().scale(c.scale / 100f, c.scale / 100f);
            // At 0% background opacity, only Minecraft items and white
            // durability numbers remain: no forced slots, borders or spines.
            fill(ctx, 0, 0, w, h, 0x10231f, background * .75f);

            for (int i = 0; i < ARMOR.length; i++) {
                int armorX = c.vertical ? ARMOR_X : 17 + i * 53;
                int armorY = c.vertical ? 2 + i * 23 : 7;
                ItemStack armor = mc.player == null ? ItemStack.EMPTY : mc.player.getEquippedStack(ARMOR[i]);
                drawEquipment(ctx, mc, c, armor, armorX, armorY, 40, iconAlpha, labelAlpha, background, false);
            }

            if (mc.player != null) {
                // Main hand shows the actual selected item, including pickaxe,
                // axe, sword or bow; offhand shows actual shield/totem if present.
                if (c.showMainHand) {
                    ItemStack main = mc.player.getMainHandStack();
                    if (!main.isEmpty()) drawEquipment(ctx, mc, c, main,
                        c.vertical ? HAND_X : 18, c.vertical ? 27 : 34,
                        c.vertical ? 34 : 44, iconAlpha, labelAlpha, background, true);
                }
                if (c.showOffHand) {
                    ItemStack offhand = mc.player.getOffHandStack();
                    if (!offhand.isEmpty()) drawEquipment(ctx, mc, c, offhand,
                        c.vertical ? HAND_X : 220, c.vertical ? 52 : 34,
                        c.vertical ? 34 : 46, iconAlpha, labelAlpha, background, true);
                }
            }
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }

    /**
     * For non-damageable items (e.g. Totem of Undying), show the item without
     * inventing a durability number. Stack count is shown only when >1.
     */
    private static void drawEquipment(DrawContext ctx, MinecraftClient mc, ArmorConfig c,
                                      ItemStack stack, int iconX, int iconY, int numberWidth,
                                      float iconAlpha, float labelAlpha, float background,
                                      boolean hand) {
        if (stack.isEmpty()) return;
        if (iconAlpha > 0f) {
            fill(ctx, iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0x254238, background * .4f);
            ctx.drawItem(stack, iconX, iconY);
            // 1.21.10 item drawing doesn't accept an overall alpha parameter.
            // Tint veil approximates intermediate alpha without affecting other HUDs.
            if (iconAlpha < 1f) fill(ctx, iconX, iconY, iconX + 16, iconY + 16,
                0x17312a, (1f - iconAlpha) * .96f);
        }

        int side = c.indicatorSide == 0 ? (hand ? 1 : 2) : c.indicatorSide;
        if (side == 3 || labelAlpha <= 0f) return;

        boolean numbers = c.durabilityMode == 0 || c.durabilityMode == 2;
        boolean bars = c.durabilityMode == 0 || c.durabilityMode == 1;
        if (!numbers && !bars) return;
        if (!stack.isDamageable()) {
            if (numbers && stack.getCount() > 1) ctx.drawTextWithShadow(mc.textRenderer,
                Integer.toString(stack.getCount()), side == 2 ? iconX - 16 : iconX + 19,
                iconY + 4, color(0xffffff, labelAlpha));
            return;
        }

        int max = Math.max(1, stack.getMaxDamage());
        int remaining = Math.clamp(max - stack.getDamage(), 0, max);
        float ratio = remaining / (float) max;
        String label = Integer.toString(remaining); // screenshot-like compact numeric durability
        int labelX = side == 2 ? iconX - mc.textRenderer.getWidth(label) - 7 : iconX + 22;
        if (numbers) ctx.drawTextWithShadow(mc.textRenderer, label,
            labelX, iconY + 4, color(0xffffff, labelAlpha));
        if (bars) {
            int bx = side == 2 ? iconX - numberWidth : iconX + 22;
            int by = iconY + (numbers ? 16 : 10);
            int bw = Math.max(12, numberWidth - 8);
            fill(ctx, bx, by, bx + bw, by + 2, 0x081f17, background * .9f);
            fill(ctx, bx, by, bx + Math.round(bw * ratio), by + 2,
                ratio > .5f ? 0x6ff19c : ratio > .25f ? 0xeac677 : 0xf37872, labelAlpha);
        }
    }
}

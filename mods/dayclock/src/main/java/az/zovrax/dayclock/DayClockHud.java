package az.zovrax.dayclock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;

/** Compact text HUD; draws the sun or moon with pixels, no font glyph dependencies. */
public final class DayClockHud {
    private static final int PADDING = 7;
    private static final int ROW_HEIGHT = 12;
    private DayClockHud() { }

    public static boolean sleepWindow(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) return false;
        // Respect whether beds work in this dimension. This is a time eligibility
        // indicator; obstacles, nearby monsters and bed occupancy are not checked.
        return mc.world.getDimension().bedWorks() && (!mc.world.isDay() || mc.world.isThundering());
    }

    private static String prettify(String value) {
        String name = value.replace('_', ' ');
        if (name.isEmpty()) return "Unknown";
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private static List<String> lines(MinecraftClient mc, DayClockConfig cfg, boolean preview) {
        ArrayList<String> values = new ArrayList<>();
        if (mc.world == null || mc.player == null) {
            if (preview) {
                if (cfg.showDay) values.add("Day 1");
                if (cfg.showTime) values.add("Clock 06:00");
                if (cfg.showCoordinates) values.add("XYZ 0, 64, 0");
                if (cfg.showBiome) values.add("Biome Plains");
                if (cfg.showDimension) values.add("Dimension Overworld");
                if (cfg.showFacing) values.add("Facing North");
                if (cfg.showWeather) values.add("Weather Clear");
                if (cfg.showLight) values.add("Light 15");
            }
            return values;
        }
        BlockPos pos = mc.player.getBlockPos();
        if (cfg.showDay) values.add("Day " + ClockMath.displayedDay(mc.world.getTimeOfDay()));
        if (cfg.showTime) values.add("Clock " + ClockMath.clock(mc.world.getTimeOfDay()));
        if (cfg.showCoordinates) values.add("XYZ " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        if (cfg.showBiome) {
            String biome = mc.world.getBiome(pos).getKey().map(key -> key.getValue().getPath()).orElse("unknown");
            values.add("Biome " + prettify(biome));
        }
        if (cfg.showDimension) values.add("Dimension " + prettify(mc.world.getRegistryKey().getValue().getPath()));
        if (cfg.showFacing) values.add("Facing " + prettify(mc.player.getHorizontalFacing().asString()));
        if (cfg.showWeather) values.add("Weather " + (mc.world.isThundering() ? "Thunder" : mc.world.isRaining() ? "Rain" : "Clear"));
        if (cfg.showLight) values.add("Light " + mc.world.getLightLevel(pos));
        return values;
    }

    private static int contentWidth(MinecraftClient mc, List<String> lines, DayClockConfig cfg) {
        int text = 52;
        for (String line : lines) text = Math.max(text, mc.textRenderer.getWidth(line));
        return PADDING * 2 + text + (cfg.showTime ? 17 : 0);
    }

    private static int contentHeight(List<String> lines) {
        return PADDING * 2 + lines.size() * ROW_HEIGHT;
    }

    public static int width(DayClockConfig cfg, MinecraftClient mc) {
        return Math.round(contentWidth(mc, lines(mc, cfg, true), cfg) * cfg.scale / 100f);
    }

    public static int height(DayClockConfig cfg, MinecraftClient mc) {
        return Math.round(contentHeight(lines(mc, cfg, true)) * cfg.scale / 100f);
    }

    public static int x(DayClockConfig cfg, MinecraftClient mc, int screenWidth) {
        return (int) Math.round(cfg.x * Math.max(0, screenWidth - width(cfg, mc)));
    }

    public static int y(DayClockConfig cfg, MinecraftClient mc, int screenHeight) {
        return (int) Math.round(cfg.y * Math.max(0, screenHeight - height(cfg, mc)));
    }

    public static void render(DrawContext ctx, RenderTickCounter ignored) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DayClockConfig cfg = DayClockClient.config();
        if (!cfg.enabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.options.hudHidden) return;
        draw(ctx, mc, cfg, x(cfg, mc, ctx.getScaledWindowWidth()),
            y(cfg, mc, ctx.getScaledWindowHeight()), false);
    }

    public static void preview(DrawContext ctx, DayClockConfig cfg, int x, int y) {
        draw(ctx, MinecraftClient.getInstance(), cfg, x, y, true);
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, DayClockConfig cfg, int px, int py, boolean preview) {
        List<String> values = lines(mc, cfg, preview);
        if (values.isEmpty() || cfg.opacity == 0) return;
        int w = contentWidth(mc, values, cfg);
        int h = contentHeight(values);
        int alpha = Math.clamp(cfg.opacity * 255 / 100, 0, 255);
        int bg = Math.clamp(cfg.backgroundOpacity * alpha / 100, 0, 255);
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(px, py);
            ctx.getMatrices().scale(cfg.scale / 100f, cfg.scale / 100f);
            if (bg > 0) ctx.fill(0, 0, w, h, (bg << 24) | 0x0C1722);
            int lineIndex = 0;
            for (String value : values) {
                int ty = PADDING + lineIndex * ROW_HEIGHT + 1;
                int textColor = (alpha << 24) | 0xF7F9FE;
                ctx.drawTextWithShadow(mc.textRenderer, value, PADDING, ty, textColor);
                if (cfg.showTime && value.startsWith("Clock ")) {
                    drawSkyIcon(ctx, w - PADDING - 11, ty - 1, sleepWindow(mc), alpha);
                }
                lineIndex++;
            }
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }

    private static void drawSkyIcon(DrawContext ctx, int x, int y, boolean sleepAllowed, int alpha) {
        if (sleepAllowed) {
            // Crescent rendered directly from transparent pixels; no opaque
            // cutout that would obscure the game on a transparent overlay.
            final int moon = (alpha << 24) | 0xB6CFFF;
            int[][] rows = {
                {4, 6}, {2, 5}, {1, 4}, {0, 3}, {0, 3},
                {0, 3}, {1, 4}, {2, 5}, {4, 6}
            };
            for (int dy = 0; dy < rows.length; dy++) {
                ctx.fill(x + rows[dy][0], y + dy,
                    x + rows[dy][1] + 1, y + dy + 1, moon);
            }
        } else {
            final int sun = (alpha << 24) | 0xFFD66B;
            ctx.fill(x+4,y,x+6,y+2,sun);
            ctx.fill(x+4,y+8,x+6,y+10,sun);
            ctx.fill(x,y+4,x+2,y+6,sun);
            ctx.fill(x+8,y+4,x+10,y+6,sun);
            ctx.fill(x+3,y+3,x+7,y+7,sun);
            ctx.fill(x+2,y+2,x+3,y+3,sun);
            ctx.fill(x+7,y+7,x+8,y+8,sun);
            ctx.fill(x+7,y+2,x+8,y+3,sun);
            ctx.fill(x+2,y+7,x+3,y+8,sun);
        }
    }
}

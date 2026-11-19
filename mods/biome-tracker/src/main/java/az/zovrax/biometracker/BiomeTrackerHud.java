package az.zovrax.biometracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import java.util.Locale;

public final class BiomeTrackerHud {
    private static final int HUD_HEIGHT = 24;

    private BiomeTrackerHud() {}

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        BiomeTrackerConfig cfg = BiomeTrackerClient.config();
        if (!cfg.hudEnabled || cfg.opacity == 0 || client.player == null || client.world == null ||
            client.currentScreen != null || client.options.hudHidden) return;

        int width = contentWidth(client);
        renderAt(context, client, drawX(client, context.getScaledWindowWidth(), width),
            drawY(client, context.getScaledWindowHeight()), false);
    }

    /** Draws the live HUD as an editor preview, even while a screen is open or HUD is disabled. */
    public static void renderPreview(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = contentWidth(client);
        renderAt(context, client, drawX(client, context.getScaledWindowWidth(), width),
            drawY(client, context.getScaledWindowHeight()), true);
    }

    public static int contentWidth(MinecraftClient client) {
        return client.textRenderer.getWidth(displayLine(client)) + 18;
    }

    public static int renderedWidth(MinecraftClient client) {
        return Math.round(contentWidth(client) * BiomeTrackerClient.config().scale / 100f);
    }

    public static int renderedHeight() {
        return Math.round(HUD_HEIGHT * BiomeTrackerClient.config().scale / 100f);
    }

    public static int drawX(MinecraftClient client, int screenWidth, int contentWidth) {
        int available = Math.max(0, screenWidth - Math.round(contentWidth * BiomeTrackerClient.config().scale / 100f));
        return Math.round((float) (BiomeTrackerClient.config().x * available));
    }

    public static int drawY(MinecraftClient client, int screenHeight) {
        int available = Math.max(0, screenHeight - renderedHeight());
        return Math.round((float) (BiomeTrackerClient.config().y * available));
    }

    private static String displayLine(MinecraftClient client) {
        if (client.player == null || client.world == null) return "BIOME TRACKER";
        BlockPos pos = client.player.getBlockPos();
        String biomeId = client.world.getBiome(pos).getKey()
            .map(key -> key.getValue().toString()).orElse("minecraft:unknown");
        String path = biomeId.substring(biomeId.indexOf(':') + 1);
        String name = path.replace('_', ' ').toUpperCase(Locale.ROOT);
        String time = BiomeStats.formatTicks(BiomeTrackerClient.stats().currentSessionTicks());
        return BiomeTrackerClient.config().showCurrentBiomeTime ? name + "  •  " + time : name;
    }

    private static void renderAt(DrawContext context, MinecraftClient client, int drawX, int drawY, boolean preview) {
        BiomeTrackerConfig cfg = BiomeTrackerClient.config();
        int width = contentWidth(client);
        int alpha = cfg.opacity * 255 / 100;
        if (preview && (!cfg.hudEnabled || alpha == 0)) alpha = 180;
        int bgAlpha = cfg.backgroundOpacity * alpha / 100;

        context.getMatrices().pushMatrix();
        try {
            context.getMatrices().translate(drawX, drawY);
            context.getMatrices().scale(cfg.scale / 100f, cfg.scale / 100f);
            if (bgAlpha > 0) context.fill(0, 0, width, HUD_HEIGHT, (bgAlpha << 24) | 0x101A26);
            context.drawTextWithShadow(client.textRenderer, displayLine(client), 9, 8, (alpha << 24) | 0xFFFFFF);
            if (preview) {
                context.fill(0, 0, width, 1, 0xCCB6F36B);
                context.fill(0, HUD_HEIGHT - 1, width, HUD_HEIGHT, 0xCCB6F36B);
                context.fill(0, 0, 1, HUD_HEIGHT, 0xCCB6F36B);
                context.fill(width - 1, 0, width, HUD_HEIGHT, 0xCCB6F36B);
            }
        } finally {
            context.getMatrices().popMatrix();
        }
    }
}

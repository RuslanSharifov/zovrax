package az.zovrax.biometracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import java.util.Locale;

public final class BiomeTrackerHud {
    private BiomeTrackerHud() {}

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        BiomeTrackerConfig cfg = BiomeTrackerClient.config();
        if (!cfg.hudEnabled || cfg.opacity == 0 || client.player == null || client.world == null ||
            client.currentScreen != null || client.options.hudHidden) return;

        BlockPos pos = client.player.getBlockPos();
        String biomeId = client.world.getBiome(pos).getKey()
            .map(key -> key.getValue().toString()).orElse("minecraft:unknown");
        String path = biomeId.substring(biomeId.indexOf(':') + 1);
        String name = path.replace('_', ' ').toUpperCase(Locale.ROOT);
        String time = BiomeStats.formatTicks(BiomeTrackerClient.stats().currentSessionTicks());
        String line = cfg.showCurrentBiomeTime ? name + "  •  " + time : name;

        int textWidth = client.textRenderer.getWidth(line);
        int width = textWidth + 18;
        int height = 24;
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();
        int drawX = (int)(cfg.x * Math.max(0, scaledWidth - width * cfg.scale / 100));
        int drawY = (int)(cfg.y * Math.max(0, scaledHeight - height * cfg.scale / 100));
        int alpha = cfg.opacity * 255 / 100;
        int bgAlpha = cfg.backgroundOpacity * alpha / 100;

        context.getMatrices().pushMatrix();
        try {
            context.getMatrices().translate(drawX, drawY);
            context.getMatrices().scale(cfg.scale / 100f, cfg.scale / 100f);
            if (bgAlpha > 0) context.fill(0, 0, width, height, (bgAlpha << 24) | 0x101A26);
            context.drawTextWithShadow(client.textRenderer, line, 9, 8, (alpha << 24) | 0xFFFFFF);
        } finally {
            context.getMatrices().popMatrix();
        }
    }
}

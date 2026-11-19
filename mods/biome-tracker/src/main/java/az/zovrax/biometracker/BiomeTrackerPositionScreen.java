package az.zovrax.biometracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BiomeTrackerPositionScreen extends Screen {
    private final Screen parent;
    private final BiomeTrackerConfig cfg = BiomeTrackerClient.config();
    private boolean dragging;
    private boolean dragMoved;
    private double dragOffsetX;
    private double dragOffsetY;
    private double pressX;
    private double pressY;

    public BiomeTrackerPositionScreen(Screen parent) {
        super(Text.literal("Move Biome HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonY = height - 34;
        int buttonWidth = 100;
        int gap = 8;
        int totalWidth = buttonWidth * 2 + gap;
        int x = (width - totalWidth) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Center"), b -> {
            cfg.x = 0.5;
            cfg.y = 0.04;
            cfg.save();
        }).dimensions(x, buttonY, buttonWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(x + buttonWidth + gap, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        BiomeTrackerHud.renderPreview(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Hold and drag the HUD to move it. Click the HUD to open settings."),
            width / 2, 28, 0xFFE0E8F0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideHud(mouseX, mouseY)) {
            MinecraftClient client = MinecraftClient.getInstance();
            int hudX = BiomeTrackerHud.drawX(client, width, BiomeTrackerHud.contentWidth(client));
            int hudY = BiomeTrackerHud.drawY(client, height);
            dragging = true;
            dragMoved = false;
            pressX = mouseX;
            pressY = mouseY;
            dragOffsetX = mouseX - hudX;
            dragOffsetY = mouseY - hudY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            if (Math.abs(mouseX - pressX) > 2 || Math.abs(mouseY - pressY) > 2) dragMoved = true;
            MinecraftClient client = MinecraftClient.getInstance();
            int maxX = Math.max(0, width - BiomeTrackerHud.renderedWidth(client));
            int maxY = Math.max(0, height - BiomeTrackerHud.renderedHeight());
            cfg.x = maxX == 0 ? 0 : Math.clamp((mouseX - dragOffsetX) / maxX, 0.0, 1.0);
            cfg.y = maxY == 0 ? 0 : Math.clamp((mouseY - dragOffsetY) / maxY, 0.0, 1.0);
            cfg.save();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            cfg.save();
            if (!dragMoved && client != null) client.setScreen(parent);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInsideHud(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int x = BiomeTrackerHud.drawX(client, width, BiomeTrackerHud.contentWidth(client));
        int y = BiomeTrackerHud.drawY(client, height);
        return mouseX >= x && mouseX <= x + BiomeTrackerHud.renderedWidth(client)
            && mouseY >= y && mouseY <= y + BiomeTrackerHud.renderedHeight();
    }

    @Override
    public void close() {
        cfg.save();
        if (client != null) client.setScreen(parent);
    }
}

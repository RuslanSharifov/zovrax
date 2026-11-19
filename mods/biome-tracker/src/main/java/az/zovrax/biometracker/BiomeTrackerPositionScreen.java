package az.zovrax.biometracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class BiomeTrackerPositionScreen extends Screen {
    private final Screen parent;
    private final BiomeTrackerConfig cfg = BiomeTrackerClient.config();
    private boolean dragging;
    private boolean dragMoved;

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
            Text.literal("Left-click and drag the HUD to move it. Click without dragging to open settings."),
            width / 2, 28, 0xFFE0E8F0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Let buttons (especially Done) handle their own clicks before starting a HUD drag.
        if (super.mouseClicked(click, doubled)) return true;
        if (click.buttonInfo().button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInsideHud(click.x(), click.y())) {
            dragging = true;
            dragMoved = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging) {
            if (Math.abs(deltaX) > 0 || Math.abs(deltaY) > 0) dragMoved = true;
            MinecraftClient client = MinecraftClient.getInstance();
            int maxX = Math.max(1, width - BiomeTrackerHud.renderedWidth(client));
            int maxY = Math.max(1, height - BiomeTrackerHud.renderedHeight());
            cfg.x = Math.clamp(cfg.x + deltaX / maxX, 0.0, 1.0);
            cfg.y = Math.clamp(cfg.y + deltaY / maxY, 0.0, 1.0);
            cfg.save();
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            cfg.save();
            // A click without movement exits the editor back to the full settings screen.
            if (!dragMoved && client != null) client.setScreen(parent);
            return true;
        }
        return super.mouseReleased(click);
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

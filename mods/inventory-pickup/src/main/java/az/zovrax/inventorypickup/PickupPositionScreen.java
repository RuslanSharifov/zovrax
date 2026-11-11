package az.zovrax.inventorypickup;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag the actual HUD notification anywhere. Click it to edit size and opacity. */
public final class PickupPositionScreen extends Screen {
    private final Screen parent;
    private final PickupConfig config = InventoryPickupClient.config();
    private PickupValueSlider scaleSlider;
    private PickupValueSlider opacitySlider;
    private boolean popup;
    private boolean dragging;
    private boolean dragged;

    public PickupPositionScreen(Screen parent) {
        super(Text.literal("Move Inventory Pickup notification"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        config.ensurePosition(width, height);
        int w = Math.min(202, Math.max(110, width - 24));
        scaleSlider = addDrawableChild(new PickupValueSlider(12, 12, w,
                "Notification size", "%", 50, 200, 5,
                () -> config.notificationScale, v -> config.notificationScale = v));
        opacitySlider = addDrawableChild(new PickupValueSlider(12, 35, w,
                "Notification opacity", "%", 0, 100, 1,
                () -> config.notificationOpacity, v -> config.notificationOpacity = v));
        scaleSlider.visible = false;
        opacitySlider.visible = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 55, height - 27, 110, 20).build());
    }

    private int previewX() { return PickupHudRenderer.pixelX(config, width); }
    private int previewY() { return PickupHudRenderer.pixelY(config, height); }
    private int previewW() { return PickupHudRenderer.scaledWidth(config); }
    private int previewH() { return PickupHudRenderer.scaledHeight(config); }
    private boolean onPreview(double mx, double my) {
        return mx >= previewX() && mx <= previewX() + previewW()
                && my >= previewY() && my <= previewY() + previewH();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.buttonInfo().button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && onPreview(click.x(), click.y())) {
            dragging = true;
            dragged = false;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging) {
            // Use deltas from the mouse event so resizing is smooth and deterministic.
            if (Math.abs(offsetX) > 0 || Math.abs(offsetY) > 0) dragged = true;
            int availableX = Math.max(1, width - previewW());
            int availableY = Math.max(1, height - previewH());
            config.hudX = clamp(config.hudX + offsetX / availableX);
            config.hudY = clamp(config.hudY + offsetY / availableY);
            config.save();
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            if (!dragged) popup = !popup;
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    private static double clamp(double v) { return Math.max(0d, Math.min(1d, v)); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        // Always preview actual size and overall alpha, even while settings change.
        PickupHudRenderer.drawPreview(ctx, config, previewX(), previewY());
        // Keep a selectable outline even when the user sets 0% opacity.
        int px = previewX(), py = previewY(), pw = previewW(), ph = previewH();
        ctx.fill(px, py, px + pw, py + 1, 0xA0FFFFFF);
        ctx.fill(px, py + ph - 1, px + pw, py + ph, 0xA0FFFFFF);
        ctx.fill(px, py, px + 1, py + ph, 0xA0FFFFFF);
        ctx.fill(px + pw - 1, py, px + pw, py + ph, 0xA0FFFFFF);

        if (popup) {
            int w = Math.min(202, Math.max(110, width - 24));
            int left = Math.min(width - w - 8, Math.max(8, previewX()));
            // Panel follows the item: below near the top, above near the bottom.
            int top = previewY() < height / 2
                    ? previewY() + previewH() + 7
                    : previewY() - 56;
            top = Math.max(8, Math.min(height - 62, top));
            scaleSlider.setPosition(left, top);
            opacitySlider.setPosition(left, top + 23);
        }
        scaleSlider.visible = popup;
        opacitySlider.visible = popup;
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Drag the notification to move it • Click to edit size / opacity"),
                width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

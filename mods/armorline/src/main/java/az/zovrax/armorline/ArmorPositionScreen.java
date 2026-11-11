package az.zovrax.armorline;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag the whole armor silhouette; click once to edit size, opacity and layout live. */
public final class ArmorPositionScreen extends Screen {
    private final Screen parent;
    private final ArmorConfig config = ArmorLineClient.config();
    private ArmorSlider sizeSlider;
    private ArmorSlider opacitySlider;
    private ButtonWidget layoutButton;
    private boolean popup;
    private boolean dragging;
    private boolean dragged;

    public ArmorPositionScreen(Screen parent) {
        super(Text.literal("ArmorLine HUD editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        config.ensurePosition(width, height);
        int w = Math.min(215, Math.max(100, width - 20));
        sizeSlider = addDrawableChild(new ArmorSlider(8, 8, w, "Size", "%", 50, 200, 5,
            () -> config.scale, v -> config.scale = v));
        opacitySlider = addDrawableChild(new ArmorSlider(8, 31, w, "Opacity", "%", 0, 100, 1,
            () -> config.opacity, v -> config.opacity = v));
        layoutButton = addDrawableChild(ButtonWidget.builder(Text.literal("Layout: " +
            (config.vertical ? "Vertical" : "Horizontal")), b -> {
            config.vertical = !config.vertical;
            config.save();
            b.setMessage(Text.literal("Layout: " + (config.vertical ? "Vertical" : "Horizontal")));
        }).dimensions(8, 54, w, 20).build());
        sizeSlider.visible = false;
        opacitySlider.visible = false;
        layoutButton.visible = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width / 2 - 55, height - 28, 110, 20).build());
    }

    private int px() { return ArmorHud.x(config, width); }
    private int py() { return ArmorHud.y(config, height); }
    private int pw() { return ArmorHud.width(config); }
    private int ph() { return ArmorHud.height(config); }
    private boolean hit(double x, double y) {
        return x >= px() && x <= px() + pw() && y >= py() && y <= py() + ph();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.buttonInfo().button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && hit(click.x(), click.y())) {
            dragging = true;
            dragged = false;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (!dragging) return super.mouseDragged(click, deltaX, deltaY);
        if (deltaX != 0 || deltaY != 0) dragged = true;
        config.x = Math.clamp(config.x + deltaX / Math.max(1, width - pw()), 0d, 1d);
        config.y = Math.clamp(config.y + deltaY / Math.max(1, height - ph()), 0d, 1d);
        config.save();
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (!dragging) return super.mouseReleased(click);
        dragging = false;
        if (!dragged) popup = !popup;
        config.save();
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ArmorHud.drawPreview(ctx, config, px(), py());
        // Always show a drag outline even with opacity set to zero or empty slots.
        int x = px(), y = py(), w = pw(), h = ph();
        ctx.fill(x, y, x + w, y + 1, 0xB5FFFFFF);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xB5FFFFFF);
        ctx.fill(x, y, x + 1, y + h, 0xB5FFFFFF);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xB5FFFFFF);
        if (popup) {
            int panelW = Math.min(215, Math.max(100, width - 20));
            int left = Math.clamp(px(), 5, Math.max(5, width - panelW - 5));
            int top = py() < height / 2 ? py() + ph() + 8 : py() - 82;
            top = Math.clamp(top, 6, Math.max(6, height - 88));
            sizeSlider.setPosition(left, top);
            opacitySlider.setPosition(left, top + 23);
            layoutButton.setPosition(left, top + 46);
        }
        sizeSlider.visible = popup;
        opacitySlider.visible = popup;
        layoutButton.visible = popup;
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Drag armor HUD to move • Click to adjust"), width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

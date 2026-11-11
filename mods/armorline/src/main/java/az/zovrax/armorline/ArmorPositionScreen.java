package az.zovrax.armorline;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Move the whole widget; context controls always get first chance at pointer events. */
public final class ArmorPositionScreen extends Screen {
    private static final String[] MODES = { "Bars + numbers", "Bars only", "Numbers only", "Hidden" };
    private static final String[] SIDES = { "Auto", "Right", "Left", "Hidden" };

    private final Screen parent;
    private final ArmorConfig config = ArmorLineClient.config();
    private ArmorSlider scale;
    private ArmorSlider overall;
    private ArmorSlider background;
    private ArmorSlider icons;
    private ArmorSlider text;
    private ButtonWidget layoutButton;
    private ButtonWidget durabilityButton;
    private ButtonWidget sideButton;
    private boolean popup;
    private boolean dragging;
    private boolean moved;

    public ArmorPositionScreen(Screen parent) {
        super(Text.literal("ArmorLine HUD editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        config.ensurePosition(width, height);
        int w = Math.min(220, Math.max(110, width - 16));
        scale = addDrawableChild(new ArmorSlider(6, 6, w, "Size", "%", 50, 200, 5,
            () -> config.scale, v -> config.scale = v));
        overall = addDrawableChild(new ArmorSlider(6, 29, w, "Overall opacity", "%", 0, 100, 1,
            () -> config.opacity, v -> config.opacity = v));
        background = addDrawableChild(new ArmorSlider(6, 52, w, "Background", "%", 0, 100, 1,
            () -> config.backgroundOpacity, v -> config.backgroundOpacity = v));
        icons = addDrawableChild(new ArmorSlider(6, 75, w, "Armor icons", "%", 0, 100, 1,
            () -> config.iconsOpacity, v -> config.iconsOpacity = v));
        text = addDrawableChild(new ArmorSlider(6, 98, w, "Numbers / bars", "%", 0, 100, 1,
            () -> config.textOpacity, v -> config.textOpacity = v));
        layoutButton = addDrawableChild(ButtonWidget.builder(Text.literal(layoutLabel()), b -> {
            int oldX = px(), oldY = py();
            config.vertical = !config.vertical;
            reanchor(oldX, oldY);
            config.save();
            b.setMessage(Text.literal(layoutLabel()));
        }).dimensions(6, 121, w, 20).build());
        durabilityButton = addDrawableChild(ButtonWidget.builder(Text.literal(modeLabel()), b -> {
            config.durabilityMode = (config.durabilityMode + 1) % MODES.length;
            config.save();
            b.setMessage(Text.literal(modeLabel()));
        }).dimensions(6, 144, w, 20).build());
        sideButton = addDrawableChild(ButtonWidget.builder(Text.literal(sideLabel()), b -> {
            config.indicatorSide = (config.indicatorSide + 1) % SIDES.length;
            config.save();
            b.setMessage(Text.literal(sideLabel()));
        }).dimensions(6, 167, w, 20).build());
        popupVisibility(false);
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width / 2 - 55, height - 27, 110, 20).build());
    }

    private void popupVisibility(boolean visible) {
        scale.visible = visible;
        overall.visible = visible;
        background.visible = visible;
        icons.visible = visible;
        text.visible = visible;
        layoutButton.visible = visible;
        durabilityButton.visible = visible;
        sideButton.visible = visible;
    }

    private String layoutLabel() { return "Layout: " + (config.vertical ? "Vertical" : "Horizontal"); }
    private String modeLabel() { return "Durability: " + MODES[config.durabilityMode]; }
    private String sideLabel() { return "Indicator side: " + SIDES[config.indicatorSide]; }

    private int px() { return ArmorHud.x(config, width); }
    private int py() { return ArmorHud.y(config, height); }
    private int pw() { return ArmorHud.width(config); }
    private int ph() { return ArmorHud.height(config); }

    private boolean hit(double x, double y) {
        return x >= px() && x <= px() + pw() && y >= py() && y <= py() + ph();
    }

    private void reanchor(int oldX, int oldY) {
        config.x = Math.clamp(oldX / (double) Math.max(1, width - pw()), 0d, 1d);
        config.y = Math.clamp(oldY / (double) Math.max(1, height - ph()), 0d, 1d);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Critical bug fix: when the contextual controls overlap the HUD preview,
        // process children before beginning a drag so Done and layout always work.
        if (super.mouseClicked(click, doubled)) return true;
        if (click.buttonInfo().button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && hit(click.x(), click.y())) {
            dragging = true;
            moved = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging) {
            if (deltaX != 0 || deltaY != 0) moved = true;
            config.x = Math.clamp(config.x + deltaX / Math.max(1, width - pw()), 0d, 1d);
            config.y = Math.clamp(config.y + deltaY / Math.max(1, height - ph()), 0d, 1d);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            if (!moved) popup = !popup;
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ArmorHud.drawPreview(ctx, config, px(), py());
        // Always selectable even when all layers have 0% opacity.
        int x = px(), y = py(), w = pw(), h = ph();
        ctx.fill(x, y, x + w, y + 1, 0xB0FFFFFF);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xB0FFFFFF);
        ctx.fill(x, y, x + 1, y + h, 0xB0FFFFFF);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xB0FFFFFF);

        if (popup) {
            int pwid = Math.min(220, Math.max(110, width - 16));
            int left = Math.clamp(px(), 6, Math.max(6, width - pwid - 6));
            int top = py() + ph() + 8;
            if (top + 190 > height - 30) top = py() - 198;
            top = Math.clamp(top, 6, Math.max(6, height - 200));
            scale.setPosition(left, top);
            overall.setPosition(left, top + 23);
            background.setPosition(left, top + 46);
            icons.setPosition(left, top + 69);
            text.setPosition(left, top + 92);
            layoutButton.setPosition(left, top + 115);
            durabilityButton.setPosition(left, top + 138);
            sideButton.setPosition(left, top + 161);
        }
        popupVisibility(popup);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Drag to move  |  Click to edit"), width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

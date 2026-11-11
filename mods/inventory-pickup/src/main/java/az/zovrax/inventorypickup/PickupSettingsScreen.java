package az.zovrax.inventorypickup;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Minimal native Minecraft settings; the ordinary blurred backdrop is enough. */
public final class PickupSettingsScreen extends Screen {
    private final Screen parent;
    private final PickupConfig config = InventoryPickupClient.config();

    public PickupSettingsScreen(Screen parent) {
        super(Text.literal("Inventory Pickup settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(290, width - 32);
        int x = (width - w) / 2;
        int y = Math.max(36, (height - 276) / 2);
        int gap = 26;

        toggle(x, y, w, "Mod enabled", () -> config.enabled,
                () -> config.enabled = !config.enabled);
        toggle(x, y + gap, w, "Show gain (+N)", () -> config.showGain,
                () -> config.showGain = !config.showGain);
        toggle(x, y + gap * 2, w, "Show inventory total", () -> config.showTotal,
                () -> config.showTotal = !config.showTotal);

        addDrawableChild(new PickupValueSlider(x, y + gap * 3, w,
                "Duration", " ms", 800, 10000, 100, () -> config.durationMs,
                v -> config.durationMs = v));
        addDrawableChild(new PickupValueSlider(x, y + gap * 4, w,
                "Fade / slide speed", " ms", 100, 900, 50, () -> config.animationMs,
                v -> config.animationMs = v));
        addDrawableChild(new PickupValueSlider(x, y + gap * 5, w,
                "Background opacity", "%", 0, 100, 1, () -> config.backgroundOpacity,
                v -> config.backgroundOpacity = v));
        addDrawableChild(new PickupValueSlider(x, y + gap * 6, w,
                "Max notifications", "", 1, 6, 1, () -> config.maxVisible,
                v -> config.maxVisible = v));

        addDrawableChild(ButtonWidget.builder(Text.literal("Customize position & appearance"),
                button -> {
                    if (client != null) client.setScreen(new PickupPositionScreen(this));
                }).dimensions(x, y + gap * 7, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(x, y + gap * 8, w, 20).build());
    }

    private interface Flag { boolean get(); }
    private void toggle(int x, int y, int w, String label, Flag value, Runnable action) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label + ": " + (value.get() ? "ON" : "OFF")), button -> {
                    action.run();
                    config.save();
                    button.setMessage(Text.literal(label + ": " + (value.get() ? "ON" : "OFF")));
                }).dimensions(x, y, w, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2,
                Math.max(12, (height - 276) / 2 - 18), 0xFFFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

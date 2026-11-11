package az.zovrax.armorline;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Minecraft-native buttons, no intrusive opaque backdrop. */
public final class ArmorSettingsScreen extends Screen {
    private final Screen parent;
    private final ArmorConfig config = ArmorLineClient.config();

    public ArmorSettingsScreen(Screen parent) {
        super(Text.literal("ArmorLine settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(285, width - 28);
        int x = (width - w) / 2;
        int y = Math.max(30, (height - 242) / 2);
        toggle(x, y, w, "Armor HUD enabled", () -> config.enabled, () -> config.enabled = !config.enabled);
        toggle(x, y + 25, w, "Durability bars",
            () -> config.showDurabilityBar, () -> config.showDurabilityBar = !config.showDurabilityBar);
        toggle(x, y + 50, w, "Durability numbers",
            () -> config.showDurabilityNumbers, () -> config.showDurabilityNumbers = !config.showDurabilityNumbers);
        toggle(x, y + 75, w, "Layout: vertical",
            () -> config.vertical, () -> config.vertical = !config.vertical);
        addDrawableChild(new ArmorSlider(x, y + 100, w, "HUD size", "%", 50, 200, 5,
            () -> config.scale, v -> config.scale = v));
        addDrawableChild(new ArmorSlider(x, y + 125, w, "HUD opacity", "%", 0, 100, 1,
            () -> config.opacity, v -> config.opacity = v));
        addDrawableChild(ButtonWidget.builder(Text.literal("Customize HUD position"),
            b -> { if (client != null) client.setScreen(new ArmorPositionScreen(this)); })
            .dimensions(x, y + 150, w, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(x, y + 175, w, 20).build());
    }

    private interface Flag { boolean get(); }

    private void toggle(int x, int y, int w, String name, Flag flag, Runnable callback) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + (flag.get() ? "ON" : "OFF")), b -> {
            callback.run();
            config.save();
            b.setMessage(Text.literal(name + ": " + (flag.get() ? "ON" : "OFF")));
        }).dimensions(x, y, w, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title,
            width / 2, Math.max(10, (height - 242) / 2 + 3), 0xFFFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

package az.zovrax.armorline;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Two compact native Minecraft settings pages; each value saves immediately. */
public final class ArmorSettingsScreen extends Screen {
    private static final String[] MODES = { "Bars + numbers", "Bars only", "Numbers only", "Hidden" };
    private static final String[] SIDES = { "Auto (screen side)", "Right", "Left", "Hidden" };

    private final Screen parent;
    private final ArmorConfig config = ArmorLineClient.config();
    private boolean appearancePage;

    public ArmorSettingsScreen(Screen parent) {
        super(Text.literal("ArmorLine settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(300, Math.max(150, width - 24));
        int x = (width - w) / 2;
        int y = Math.max(26, (height - 220) / 2);
        int gap = 24;

        if (!appearancePage) {
            addDrawableChild(ButtonWidget.builder(Text.literal(
                "Armor HUD: " + (config.enabled ? "ON" : "OFF")), b -> {
                    config.enabled = !config.enabled;
                    config.save();
                    b.setMessage(Text.literal("Armor HUD: " + (config.enabled ? "ON" : "OFF")));
                }).dimensions(x, y, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal(layoutLabel()), b -> {
                config.vertical = !config.vertical;
                config.save();
                b.setMessage(Text.literal(layoutLabel()));
            }).dimensions(x, y + gap, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal(modeLabel()), b -> {
                config.durabilityMode = (config.durabilityMode + 1) % MODES.length;
                config.save();
                b.setMessage(Text.literal(modeLabel()));
            }).dimensions(x, y + gap * 2, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal(sideLabel()), b -> {
                config.indicatorSide = (config.indicatorSide + 1) % SIDES.length;
                config.save();
                b.setMessage(Text.literal(sideLabel()));
            }).dimensions(x, y + gap * 3, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Customize HUD position"), b -> openEditor())
                .dimensions(x, y + gap * 4, w, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Appearance and transparency"), b -> {
                appearancePage = true;
                clearAndInit();
            }).dimensions(x, y + gap * 5, w, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x, y + gap * 6, w, 20).build());
        } else {
            addDrawableChild(new ArmorSlider(x, y, w, "HUD size", "%", 50, 200, 5,
                () -> config.scale, v -> config.scale = v));
            addDrawableChild(new ArmorSlider(x, y + gap, w, "Overall opacity", "%", 0, 100, 1,
                () -> config.opacity, v -> config.opacity = v));
            addDrawableChild(new ArmorSlider(x, y + gap * 2, w, "Background opacity", "%", 0, 100, 1,
                () -> config.backgroundOpacity, v -> config.backgroundOpacity = v));
            addDrawableChild(new ArmorSlider(x, y + gap * 3, w, "Armor icons opacity", "%", 0, 100, 1,
                () -> config.iconsOpacity, v -> config.iconsOpacity = v));
            addDrawableChild(new ArmorSlider(x, y + gap * 4, w, "Numbers / bars opacity", "%", 0, 100, 1,
                () -> config.textOpacity, v -> config.textOpacity = v));
            addDrawableChild(ButtonWidget.builder(Text.literal("Customize HUD position"), b -> openEditor())
                .dimensions(x, y + gap * 5, w, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Back to armor settings"), b -> {
                appearancePage = false;
                clearAndInit();
            }).dimensions(x, y + gap * 6, w, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x, y + gap * 7, w, 20).build());
        }
    }

    private String layoutLabel() {
        return "Layout: " + (config.vertical ? "Vertical" : "Horizontal");
    }

    private String modeLabel() {
        return "Durability: " + MODES[config.durabilityMode];
    }

    private String sideLabel() {
        return "Indicators: " + SIDES[config.indicatorSide];
    }

    private void openEditor() {
        config.save();
        if (client != null) client.setScreen(new ArmorPositionScreen(this));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

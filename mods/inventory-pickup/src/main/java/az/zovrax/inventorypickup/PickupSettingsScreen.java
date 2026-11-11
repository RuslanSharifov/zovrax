package az.zovrax.inventorypickup;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * In-game settings panel, opened by the configurable key binding (default O).
 * Every adjustment is saved immediately in Minecraft's config/ directory.
 */
public final class PickupSettingsScreen extends Screen {
    private final Screen parent;
    private final PickupConfig config = InventoryPickupClient.config();

    public PickupSettingsScreen(Screen parent) {
        super(Text.literal("Zovrax • Inventory Pickup"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int gap = 10;
        int controlWidth = Math.min(216, Math.max(120, (width - 56) / 2));
        int left = width / 2 - controlWidth - gap / 2;
        int right = width / 2 + gap / 2;
        int y = 82;
        int step = 27;

        option(left, y, controlWidth, () -> "HUD: " + onOff(config.enabled),
                () -> config.enabled = !config.enabled);
        option(right, y, controlWidth, () -> "Position: " + positionName(),
                () -> config.position = (config.position + 1) % 4);

        option(left, y + step, controlWidth, () -> "Gain (+N): " + onOff(config.showGain),
                () -> config.showGain = !config.showGain);
        option(right, y + step, controlWidth, () -> "Inventory total: " + onOff(config.showTotal),
                () -> config.showTotal = !config.showTotal);

        option(left, y + step * 2, controlWidth,
                () -> "Duration: " + config.durationMs + " ms",
                () -> config.durationMs = config.durationMs >= 6000 ? 1000 : config.durationMs + 500);
        option(right, y + step * 2, controlWidth,
                () -> "Animation: " + config.animationMs + " ms",
                () -> config.animationMs = config.animationMs >= 800 ? 150 : config.animationMs + 100);

        option(left, y + step * 3, controlWidth,
                () -> "Background: " + config.backgroundOpacity + "%",
                () -> config.backgroundOpacity = config.backgroundOpacity >= 100 ? 20 : config.backgroundOpacity + 10);
        option(right, y + step * 3, controlWidth,
                () -> "Max notifications: " + config.maxVisible,
                () -> config.maxVisible = config.maxVisible >= 6 ? 1 : config.maxVisible + 1);

        option(left, y + step * 4, controlWidth,
                () -> "Merge repeats: " + onOff(config.mergeRepeated),
                () -> config.mergeRepeated = !config.mergeRepeated);
        option(right, y + step * 4, controlWidth,
                () -> "Show over menus: " + onOff(config.allowOverScreens),
                () -> config.allowOverScreens = !config.allowOverScreens);

        option(left, y + step * 5, controlWidth,
                () -> "Screen padding: " + config.margin + " px",
                () -> config.margin = config.margin >= 64 ? 8 : config.margin + 8);
        addDrawableChild(ButtonWidget.builder(Text.literal("Preview notification"), b -> {
            InventoryPickupClient.preview();
            if (client != null) client.setScreen(parent);
        }).dimensions(right, y + step * 5, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done / Back"), b -> close())
                .dimensions(width / 2 - 90, y + step * 6 + 6, 180, 20).build());
    }

    private interface Caption {
        String get();
    }

    private void option(int x, int y, int width, Caption caption, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(caption.get()), button -> {
            action.run();
            config.save();
            button.setMessage(Text.literal(caption.get()));
        }).dimensions(x, y, width, 20).build());
    }

    private String positionName() {
        return switch (config.position) {
            case 1 -> "Top left";
            case 2 -> "Bottom right";
            case 3 -> "Bottom left";
            default -> "Top right";
        };
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int left = width / 2 - Math.min(248, width / 2 - 12);
        int right = width / 2 + Math.min(248, width / 2 - 12);
        ctx.fill(left, 20, right, Math.min(height - 12, 299), 0xD9102017);
        ctx.fill(left, 20, right, 22, 0xFFB5F58A);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("ZOVRAX  /  INVENTORY PICKUP"),
                width / 2, 37, 0xFFC7FF9F);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("ANIMATED NOTIFICATIONS  •  CLIENT ONLY"),
                width / 2, 54, 0xFFB5C6B6);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Key binding: Options → Controls → Key Binds (default O)"),
                width / 2, Math.min(height - 25, 293), 0xFFA5B5A4);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

package az.zovrax.smoothdoors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** In-game toggles and sliders; all options are persisted immediately. */
public final class SmoothDoorsSettingsScreen extends Screen {
    private final Screen parent;
    private final MotionConfig config = SmoothDoorsClient.config();

    public SmoothDoorsSettingsScreen(Screen parent) {
        super(Component.literal("Zovrax Smooth Doors"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int widthPx = Math.min(300, width - 28);
        int x = (width - widthPx) / 2;
        int top = Math.max(38, (height - 170) / 2);
        toggle(x, top, widthPx, "Animation", () -> config.enabled, v -> config.enabled = v);
        toggle(x, top + 24, widthPx, "Doors (including double doors)", () -> config.doors, v -> config.doors = v);
        toggle(x, top + 48, widthPx, "Trapdoors / shutters", () -> config.trapdoors, v -> config.trapdoors = v);
        addRenderableWidget(new DurationSlider(x, top + 78, widthPx, true));
        addRenderableWidget(new DurationSlider(x, top + 102, widthPx, false));
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(x, top + 134, widthPx, 20).build());
    }

    private void toggle(int x, int y, int w, String title, BooleanSupplier getter, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(
                Component.literal(title + ": " + (getter.getAsBoolean() ? "ON" : "OFF")), button -> {
                    setter.accept(!getter.getAsBoolean());
                    config.save();
                    button.setMessage(Component.literal(title + ": " + (getter.getAsBoolean() ? "ON" : "OFF")));
                }).bounds(x, y, w, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    private final class DurationSlider extends AbstractSliderButton {
        private final boolean opening;

        private DurationSlider(int x, int y, int w, boolean opening) {
            super(x, y, w, 20, Component.empty(),
                    ((opening ? config.openingMs : config.closingMs) - 100) / 1900.0);
            this.opening = opening;
            updateMessage();
        }

        private int milliseconds() {
            return (int) Math.round((100 + value * 1900) / 50.0) * 50;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal((opening ? "Opening" : "Closing") + " duration: "
                    + milliseconds() + " ms"));
        }

        @Override
        protected void applyValue() {
            if (opening) config.openingMs = milliseconds();
            else config.closingMs = milliseconds();
            updateMessage();
            config.save();
        }
    }
}

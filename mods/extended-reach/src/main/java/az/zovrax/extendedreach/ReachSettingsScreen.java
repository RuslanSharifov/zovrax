package az.zovrax.extendedreach;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/** Vanilla UI; works even when optional Mod Menu is not installed. */
public final class ReachSettingsScreen extends Screen {
    private final Screen parent;

    public ReachSettingsScreen(Screen parent) {
        super(Text.translatable("screen.zovrax_extended_reach.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(300, width - 32);
        int x = (width - w) / 2;
        int y = height / 2 - 20;
        addDrawableChild(new ReachSlider(x, y, w));
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.zovrax_extended_reach.done"),
            button -> close()
        ).dimensions(x, y + 34, w, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 78, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.translatable("screen.zovrax_extended_reach.help"),
            width / 2, height / 2 - 54, 0xFFBFC7D5);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.translatable("screen.zovrax_extended_reach.server"),
            width / 2, height / 2 + 60, 0xFFAEB7C6);
    }

    @Override
    public void close() {
        ExtendedReachClient.config().save();
        if (client != null) client.setScreen(parent);
    }

    private static final class ReachSlider extends SliderWidget {
        private ReachSlider(int x, int y, int width) {
            super(x, y, width, 20, Text.empty(),
                (ExtendedReachClient.config().blocks - ReachLimits.MIN)
                    / (double) (ReachLimits.MAX - ReachLimits.MIN));
            updateMessage();
        }

        private int selected() {
            return ReachLimits.clamp(ReachLimits.MIN
                + (int) Math.round(value * (ReachLimits.MAX - ReachLimits.MIN)));
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable("screen.zovrax_extended_reach.distance", selected()));
        }

        @Override
        protected void applyValue() {
            ExtendedReachClient.setBlocks(selected());
            updateMessage();
        }
    }
}

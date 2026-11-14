package az.zovrax.hub;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class HubMessageScreen extends Screen {
    private final Screen parent;
    private final String description;

    public HubMessageScreen(Screen parent, String title, String description) {
        super(Text.literal(title));
        this.parent = parent;
        this.description = description;
    }

    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Back to Hub"), b -> close())
            .dimensions(width / 2 - 75, height / 2 + 24, 150, 20).build());
    }

    @Override public void render(DrawContext ctx,int mx,int my,float delta) {
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 40, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, description,
            width / 2, height / 2 - 12, 0xFFAED2DF);
        super.render(ctx,mx,my,delta);
    }

    @Override public void close() {
        if (client != null) client.setScreen(parent);
    }
}

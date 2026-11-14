package az.zovrax.hub;

import java.net.URI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/** Explicit consent before opening the official HTTPS site or JAR in the system browser. */
public final class HubLinkScreen extends Screen {
    private final Screen parent;
    private final String url;
    private final String action;

    public HubLinkScreen(Screen parent, String url, String action) {
        super(Text.literal("Open official ZOVRAX link?"));
        this.parent = parent;
        this.url = url;
        this.action = action;
    }

    @Override protected void init() {
        int center = width / 2, top = height / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Open browser"), b -> {
            try {
                URI uri = URI.create(url);
                if (!"https".equals(uri.getScheme())
                    || !"ruslansharifov.github.io".equals(uri.getHost())
                    || !uri.getPath().startsWith("/zovrax/"))
                    throw new IllegalArgumentException("Untrusted destination");
                Util.getOperatingSystem().open(uri);
            } catch (Exception ex) {
                if (client != null)
                    client.setScreen(new HubMessageScreen(parent, "Cannot open link", "Link rejected or unsupported."));
                return;
            }
            close();
        }).dimensions(center - 104, top + 14, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
            .dimensions(center + 4, top + 14, 100, 20).build());
    }

    @Override public void render(DrawContext ctx,int mx,int my,float delta) {
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 39, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, action, width / 2, height / 2 - 20, 0xFFAAEDE4);
        super.render(ctx,mx,my,delta);
    }

    @Override public void close() {
        if (client != null) client.setScreen(parent);
    }
}

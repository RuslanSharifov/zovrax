package az.zovrax.biometracker;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BiomeTrackerSettingsScreen extends Screen {
    private final Screen parent;
    private final BiomeTrackerConfig cfg = BiomeTrackerClient.config();

    public BiomeTrackerSettingsScreen(Screen parent) {
        super(Text.translatable("screen.zovrax_biome_tracker.settings"));
        this.parent = parent;
    }

    @Override protected void init() {
        int w = Math.min(340, width - 24);
        int x = (width - w) / 2;
        int y = Math.max(20, (height - 10 * 24) / 2);
        toggle(x,y,w,"HUD",()->cfg.hudEnabled,v->cfg.hudEnabled=v);
        toggle(x,y+24,w,"Biome change notifications",()->cfg.notifications,v->cfg.notifications=v);
        toggle(x,y+48,w,"Notification sound",()->cfg.notificationSound,v->cfg.notificationSound=v);
        toggle(x,y+72,w,"Biome boundary markers",()->cfg.showBoundaries,v->cfg.showBoundaries=v);
        toggle(x,y+96,w,"Show current-biome time",()->cfg.showCurrentBiomeTime,v->cfg.showCurrentBiomeTime=v);
        adjustRow(x,y+120,w,"HUD size",()->cfg.scale,v->cfg.scale=v,60,160,10,"%");
        adjustRow(x,y+144,w,"Text opacity",()->cfg.opacity,v->cfg.opacity=v,0,100,10,"%");
        adjustRow(x,y+168,w,"Panel opacity",()->cfg.backgroundOpacity,v->cfg.backgroundOpacity=v,0,100,10,"%");
        addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD / position"),b->{if(client!=null)client.setScreen(new BiomeTrackerPositionScreen(this));}).dimensions(x,y+192,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Biome statistics"),b->{if(client!=null)client.setScreen(new BiomeTrackerStatsScreen(this));}).dimensions(x,y+216,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,y+240,w,20).build());
    }

    private void toggle(int x,int y,int w,String label,BooleanSupplier get,Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label+": "+(get.getAsBoolean()?"ON":"OFF")),b->{set.accept(!get.getAsBoolean());cfg.save();b.setMessage(Text.literal(label+": "+(get.getAsBoolean()?"ON":"OFF")));}).dimensions(x,y,w,20).build());
    }

    private void adjustRow(int x,int y,int w,String label,java.util.function.IntSupplier get,Consumer<Integer> set,int min,int max,int step,String suffix) {
        int buttonW=24;
        addDrawableChild(ButtonWidget.builder(Text.literal("−"),b->{set.accept(Math.max(min,get.getAsInt()-step));cfg.save();}).dimensions(x,y,buttonW,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(label+": "+get.getAsInt()+suffix),b->{set.accept(get.getAsInt());cfg.save();b.setMessage(Text.literal(label+": "+get.getAsInt()+suffix));}).dimensions(x+buttonW+4,y,w-2*buttonW-8,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"),b->{set.accept(Math.min(max,get.getAsInt()+step));cfg.save();}).dimensions(x+w-buttonW,y,buttonW,20).build());
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,8,0xFFFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

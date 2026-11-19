package az.zovrax.biometracker;

import java.util.Comparator;
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
        int w = Math.min(330, width - 24);
        int x = (width - w) / 2;
        int y = Math.max(28, (height - 9 * 25) / 2);
        toggle(x,y,w,"HUD",()->cfg.hudEnabled,v->cfg.hudEnabled=v);
        toggle(x,y+25,w,"Biome change notifications",()->cfg.notifications,v->cfg.notifications=v);
        toggle(x,y+50,w,"Notification sound",()->cfg.notificationSound,v->cfg.notificationSound=v);
        toggle(x,y+75,w,"Biome boundary particles",()->cfg.showBoundaries,v->cfg.showBoundaries=v);
        toggle(x,y+100,w,"Show time in current biome",()->cfg.showCurrentBiomeTime,v->cfg.showCurrentBiomeTime=v);
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD size: "+cfg.scale+"%  (− / +)"),b->{cfg.scale=cfg.scale>=150?60:cfg.scale+10;cfg.save();b.setMessage(Text.literal("HUD size: "+cfg.scale+"%  (− / +)"));}).dimensions(x,y+125,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD opacity: "+cfg.opacity+"%  (− / +)"),b->{cfg.opacity=cfg.opacity>=100?0:cfg.opacity+10;cfg.save();b.setMessage(Text.literal("HUD opacity: "+cfg.opacity+"%  (− / +)"));}).dimensions(x,y+150,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Background opacity: "+cfg.backgroundOpacity+"%"),b->{cfg.backgroundOpacity=cfg.backgroundOpacity>=100?0:cfg.backgroundOpacity+10;cfg.save();b.setMessage(Text.literal("Background opacity: "+cfg.backgroundOpacity+"%"));}).dimensions(x,y+175,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD / position"),b->{if(client!=null)client.setScreen(new BiomeTrackerPositionScreen(this));}).dimensions(x,y+200,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Biome statistics"),b->{if(client!=null)client.setScreen(new BiomeTrackerStatsScreen(this));}).dimensions(x,y+225,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,y+250,w,20).build());
    }

    private void toggle(int x,int y,int w,String label,java.util.function.BooleanSupplier get,java.util.function.Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label+": "+(get.getAsBoolean()?"ON":"OFF")),b->{set.accept(!get.getAsBoolean());cfg.save();b.setMessage(Text.literal(label+": "+(get.getAsBoolean()?"ON":"OFF")));}).dimensions(x,y,w,20).build());
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

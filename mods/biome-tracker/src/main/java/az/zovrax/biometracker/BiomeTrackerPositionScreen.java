package az.zovrax.biometracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BiomeTrackerPositionScreen extends Screen {
    private final Screen parent;
    private final BiomeTrackerConfig cfg = BiomeTrackerClient.config();
    public BiomeTrackerPositionScreen(Screen parent){super(Text.literal("Move Biome HUD"));this.parent=parent;}

    @Override protected void init(){
        int w=190,x=(width-w)/2,y=height/2-35;
        addDrawableChild(ButtonWidget.builder(Text.literal("←"),b->{cfg.x=Math.max(0,cfg.x-0.025);cfg.save();}).dimensions(x,y+28,40,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("→"),b->{cfg.x=Math.min(1,cfg.x+0.025);cfg.save();}).dimensions(x+150,y+28,40,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("↑"),b->{cfg.y=Math.max(0,cfg.y-0.025);cfg.save();}).dimensions(x+75,y,40,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("↓"),b->{cfg.y=Math.min(1,cfg.y+0.025);cfg.save();}).dimensions(x+75,y+56,40,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Center"),b->{cfg.x=0.5;cfg.y=0.04;cfg.save();}).dimensions(x+45,y+28,100,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,y+90,w,20).build());
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){renderBackground(ctx,mx,my,delta);ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,12,0xFFFFFFFF);super.render(ctx,mx,my,delta);}
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

package az.zovrax.dayclock;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Settings are local to the client and each toggle persists immediately. */
public final class DayClockSettingsScreen extends Screen {
    private final Screen parent;
    private final DayClockConfig config = DayClockClient.config();
    private int page = 0;

    public DayClockSettingsScreen(Screen parent) {
        super(Text.literal("DayClock HUD settings"));
        this.parent = parent;
    }

    @Override protected void init() {
        int w = Math.min(290, Math.max(150, width-28));
        int x = (width-w)/2;
        int gap = 23;
        int top = Math.max(22, (height-9*gap-15)/2);
        if (page == 0) {
            toggle(x,top,w,"HUD",()->config.enabled,v->config.enabled=v);
            toggle(x,top+gap,w,"World day",()->config.showDay,v->config.showDay=v);
            toggle(x,top+2*gap,w,"Game time + sleep icon",()->config.showTime,v->config.showTime=v);
            toggle(x,top+3*gap,w,"XYZ coordinates",()->config.showCoordinates,v->config.showCoordinates=v);
            toggle(x,top+4*gap,w,"Biome",()->config.showBiome,v->config.showBiome=v);
            toggle(x,top+5*gap,w,"Dimension",()->config.showDimension,v->config.showDimension=v);
            toggle(x,top+6*gap,w,"Facing direction",()->config.showFacing,v->config.showFacing=v);
            addDrawableChild(ButtonWidget.builder(Text.literal("More options >"), b -> goTo(1)).dimensions(x,top+7*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x,top+8*gap,w,20).build());
        } else {
            toggle(x,top,w,"Weather",()->config.showWeather,v->config.showWeather=v);
            toggle(x,top+gap,w,"Light level",()->config.showLight,v->config.showLight=v);
            addDrawableChild(new DayClockSlider(x,top+2*gap,w,"HUD size",50,200,5,()->config.scale,v->config.scale=v));
            addDrawableChild(new DayClockSlider(x,top+3*gap,w,"HUD opacity",0,100,5,()->config.opacity,v->config.opacity=v));
            addDrawableChild(new DayClockSlider(x,top+4*gap,w,"Background",0,100,5,()->config.backgroundOpacity,v->config.backgroundOpacity=v));
            addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD on screen"), b -> {
                config.save();
                if (client != null) client.setScreen(new DayClockPositionScreen(this));
            }).dimensions(x,top+5*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("< Back to visibility"), b -> goTo(0)).dimensions(x,top+7*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x,top+8*gap,w,20).build());
        }
    }

    private void toggle(int x,int y,int w,String label,BooleanSupplier getter,Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label+": "+(getter.getAsBoolean()?"ON":"OFF")), b -> {
            setter.accept(!getter.getAsBoolean()); config.save();
            b.setMessage(Text.literal(label+": "+(getter.getAsBoolean()?"ON":"OFF")));
        }).dimensions(x,y,w,20).build());
    }

    private void goTo(int n) { page=n; clearAndInit(); }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,8,0xFFFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
    }

    @Override public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

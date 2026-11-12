package az.zovrax.smartreturn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Ten effect styles, size, opacity, density, and see-through-walls option. */
public final class SmartReturnVisualSettings extends Screen {
    private final Screen parent;
    private final SmartReturnConfig cfg=SmartReturnClient.config();
    public SmartReturnVisualSettings(Screen parent){
        super(Text.translatable("smartreturn.settings.visuals.title"));
        this.parent=parent;
    }
    @Override protected void init(){
        int w=Math.min(300,width-34),x=(width-w)/2;
        int y=Math.max(25,(height-230)/2);
        addDrawableChild(new SmartReturnSlider(x,y,w,0,9,cfg.effectStyle,
            value->Text.translatable("smartreturn.settings.style",
                value+1,Text.translatable("smartreturn.style."+value)),
            value->{cfg.effectStyle=value;cfg.save();}));
        addDrawableChild(ButtonWidget.builder(wallLabel(),b->{
            cfg.throughWalls=!cfg.throughWalls;
            b.setMessage(wallLabel());cfg.save();
        }).dimensions(x,y+28,w,20).build());
        addDrawableChild(new SmartReturnSlider(x,y+56,w,60,180,cfg.compassSize,
            value->Text.translatable("smartreturn.settings.size",value),
            value->{cfg.compassSize=value;cfg.save();}));
        addDrawableChild(new SmartReturnSlider(x,y+84,w,15,100,cfg.compassOpacity,
            value->Text.translatable("smartreturn.settings.opacity",value),
            value->{cfg.compassOpacity=value;cfg.save();}));
        addDrawableChild(new SmartReturnSlider(x,y+112,w,25,100,cfg.effectDensity,
            value->Text.translatable("smartreturn.settings.density",value),
            value->{cfg.effectDensity=value;cfg.save();}));
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.position"),b->{
            if(client!=null)client.setScreen(new SmartReturnPositionScreen(this));
        }).dimensions(x,y+145,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.done"),b->close())
            .dimensions(x,y+179,w,20).build());
    }
    private Text wallLabel(){
        return Text.translatable("smartreturn.settings.walls",
            Text.translatable(cfg.throughWalls?"smartreturn.on":"smartreturn.off"));
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,
            Math.max(8,(height-230)/2+8),0xffffffff);
        super.render(ctx,mx,my,delta);
    }
    @Override public void close(){
        cfg.save();
        if(client!=null)client.setScreen(parent);
    }
}

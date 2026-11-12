package az.zovrax.smartreturn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * X/Y sliders allow exact placement anywhere on-screen, regardless of window
 * size or GUI scale. A live animated vector arrow previews the final position.
 */
public final class SmartReturnPositionScreen extends Screen {
    private final Screen parent;
    private final SmartReturnConfig cfg=SmartReturnClient.config();

    public SmartReturnPositionScreen(Screen parent){
        super(Text.translatable("smartreturn.position.title"));
        this.parent=parent;
    }
    @Override protected void init(){
        int w=Math.min(290,width-32),x=(width-w)/2;
        int y=Math.max(40,height-100);
        addDrawableChild(new SmartReturnSlider(x,y,w,0,100,
            Math.round(cfg.compassX*100),
            value->Text.translatable("smartreturn.position.x",value),
            value->{cfg.compassX=value/100f;cfg.save();}));
        addDrawableChild(new SmartReturnSlider(x,y+24,w,0,100,
            Math.round(cfg.compassY*100),
            value->Text.translatable("smartreturn.position.y",value),
            value->{cfg.compassY=value/100f;cfg.save();}));
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.done"),b->close())
            .dimensions(x,y+50,w,20).build());
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,12,0xffffffff);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.translatable("smartreturn.position.help"),width/2,25,0xffb6d4c9);
        float phase=(float)(Math.sin(System.nanoTime()/800000000.0)*50);
        if(client!=null)
            SmartReturnHud.drawCompass(ctx,client,cfg,phase,
                Text.translatable("smartreturn.hud.return").getString());
        super.render(ctx,mx,my,delta);
    }
    @Override public void close(){
        cfg.save();
        if(client!=null)client.setScreen(parent);
    }
}

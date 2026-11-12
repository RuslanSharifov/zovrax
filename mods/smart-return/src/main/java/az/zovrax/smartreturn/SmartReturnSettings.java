package az.zovrax.smartreturn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Native controls screen; visual adjustments live on a separate page. */
public final class SmartReturnSettings extends Screen {
    private final Screen parent;
    private final SmartReturnConfig cfg=SmartReturnClient.config();

    public SmartReturnSettings(Screen parent){
        super(Text.translatable("smartreturn.settings.title"));
        this.parent=parent;
    }
    private Text status(String key,boolean flag){
        return Text.translatable(key,Text.translatable(flag?"smartreturn.on":"smartreturn.off"));
    }
    private Text mode(){
        return Text.translatable("smartreturn.settings.display",
            Text.translatable("smartreturn.mode."+cfg.displayMode));
    }
    @Override protected void init(){
        int w=Math.min(292,width-34),x=(width-w)/2;
        int top=Math.max(28,(height-225)/2);
        addDrawableChild(ButtonWidget.builder(status("smartreturn.settings.record",cfg.enabled),b->{
            cfg.enabled=!cfg.enabled;
            b.setMessage(status("smartreturn.settings.record",cfg.enabled));cfg.save();
        }).dimensions(x,top,w,20).build());
        addDrawableChild(ButtonWidget.builder(mode(),b->{
            cfg.displayMode=(cfg.displayMode+1)%3;b.setMessage(mode());cfg.save();
        }).dimensions(x,top+25,w,20).build());
        addDrawableChild(ButtonWidget.builder(status("smartreturn.settings.reminder",cfg.reminders),b->{
            cfg.reminders=!cfg.reminders;
            b.setMessage(status("smartreturn.settings.reminder",cfg.reminders));cfg.save();
        }).dimensions(x,top+50,w,20).build());
        addDrawableChild(new SmartReturnSlider(x,top+75,w,1,10,cfg.reminderMinutes,
            v->Text.translatable("smartreturn.settings.minutes",v),
            v->{cfg.reminderMinutes=v;cfg.save();}));
        addDrawableChild(ButtonWidget.builder(status("smartreturn.settings.distance",cfg.showDistance),b->{
            cfg.showDistance=!cfg.showDistance;
            b.setMessage(status("smartreturn.settings.distance",cfg.showDistance));cfg.save();
        }).dimensions(x,top+100,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.visuals"),b->{
            if(client!=null)client.setScreen(new SmartReturnVisualSettings(this));
        }).dimensions(x,top+125,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.forget"),b->{
            if(client!=null)SmartReturnClient.routes().forget(client);
        }).dimensions(x,top+150,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("smartreturn.settings.done"),b->close())
            .dimensions(x,top+180,w,20).build());
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,
            Math.max(8,(height-225)/2+8),0xffffffff);
        super.render(ctx,mx,my,delta);
    }
    @Override public void close(){
        cfg.save();
        if(client!=null)client.setScreen(parent);
    }
}

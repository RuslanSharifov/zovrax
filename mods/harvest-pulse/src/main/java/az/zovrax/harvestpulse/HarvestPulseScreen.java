package az.zovrax.harvestpulse;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Farm list and independently saved alerts / size preferences. */
public final class HarvestPulseScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private int page;
    private boolean options;
    public HarvestPulseScreen(Screen parent) {super(Text.literal("HarvestPulse - farm tracker"));this.parent=parent;}

    @Override protected void init() {
        int w=Math.min(288,Math.max(160,width-20));
        int x=(width-w)/2;
        int top=Math.max(26,(height-258)/2);
        if(!options) {
            addDrawableChild(ButtonWidget.builder(Text.literal(HarvestPulseClient.isSelecting()?
                "Cancel selection (H)":"Track new farm • choose 2 corners"),b->{
                if(HarvestPulseClient.isSelecting()) {
                    HarvestPulseClient.cancelSelection(client);
                    clearAndInit();
                } else HarvestPulseClient.beginSelection(client);
            }).dimensions(x,top,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Area limit: "+cfg.selectionLimit+" x "+cfg.selectionLimit),b->{
                int next=0;
                for(int i=0;i<FieldMath.SIZES.length;i++)if(FieldMath.SIZES[i]==cfg.selectionLimit)next=(i+1)%FieldMath.SIZES.length;
                cfg.selectionLimit=FieldMath.SIZES[next];cfg.save();
                b.setMessage(Text.literal("Area limit: "+cfg.selectionLimit+" x "+cfg.selectionLimit));
            }).dimensions(x,top+24,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("HUD: "+(cfg.enabled?"ON":"OFF")),b->{
                cfg.enabled=!cfg.enabled;cfg.save();b.setMessage(Text.literal("HUD: "+(cfg.enabled?"ON":"OFF")));
            }).dimensions(x,top+48,w,20).build());
            int visibleIndex=0;
            for(HarvestPulseConfig.Field f:cfg.fields) {
                if(!FieldTracker.worldKey(client).equals(f.worldKey)) continue;
                if(visibleIndex>=page*4 && visibleIndex<page*4+4) {
                    int row=visibleIndex-page*4;
                    addDrawableChild(ButtonWidget.builder(Text.literal((visibleIndex+1)+". "+f.name+"  • Edit"),b->{
                        if(client!=null)client.setScreen(new HarvestPulseFieldScreen(this,f.id));
                    }).dimensions(x,top+76+row*23,w,20).build());
                }
                visibleIndex++;
            }
            addDrawableChild(ButtonWidget.builder(Text.literal("< Previous"),b->{page--;clearAndInit();})
                .dimensions(x,top+171,(w-4)/2,20).build()).active=page>0;
            addDrawableChild(ButtonWidget.builder(Text.literal("Next >"),b->{page++;clearAndInit();})
                .dimensions(x+(w+4)/2,top+171,(w-4)/2,20).build()).active=(page+1)*4<visibleIndex;
            addDrawableChild(ButtonWidget.builder(Text.literal("Alerts & appearance >"),b->{options=true;clearAndInit();})
                .dimensions(x,top+195,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD on screen"),b->{
                if(client!=null)client.setScreen(new HarvestPulsePositionScreen(this));
            }).dimensions(x,top+219,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close())
                .dimensions(x,top+243,w,20).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal(soundText()),b->{
                cfg.soundAlerts=!cfg.soundAlerts;cfg.save();b.setMessage(Text.literal(soundText()));
            }).dimensions(x,top,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(toastText()),b->{
                cfg.toastAlerts=!cfg.toastAlerts;cfg.save();b.setMessage(Text.literal(toastText()));
            }).dimensions(x,top+24,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(animationText()),b->{
                cfg.animateAlerts=!cfg.animateAlerts;cfg.save();b.setMessage(Text.literal(animationText()));
            }).dimensions(x,top+48,w,20).build());
            addDrawableChild(new HarvestPulseSlider(x,top+72,w,"Scale",50,200,5,()->cfg.scale,v->cfg.scale=v));
            addDrawableChild(new HarvestPulseSlider(x,top+96,w,"HUD opacity",0,100,5,()->cfg.opacity,v->cfg.opacity=v));
            addDrawableChild(new HarvestPulseSlider(x,top+120,w,"Background",0,100,5,()->cfg.backgroundOpacity,v->cfg.backgroundOpacity=v));
            addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD on screen"),b->{
                if(client!=null)client.setScreen(new HarvestPulsePositionScreen(this));
            }).dimensions(x,top+171,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("< Back to farms"),b->{options=false;clearAndInit();})
                .dimensions(x,top+219,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close())
                .dimensions(x,top+243,w,20).build());
        }
    }
    private String soundText() {return "Harvest bell sound: "+(cfg.soundAlerts?"ON":"OFF");}
    private String toastText() {return "Top-right notification: "+(cfg.toastAlerts?"ON":"OFF");}
    private String animationText() {return "Slide + fade animation: "+(cfg.animateAlerts?"ON":"OFF");}
    @Override public void render(DrawContext ctx,int mx,int my,float delta) {
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,8,0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("16 / 32 / 64 / 128 blocks  •  Up to 8 fields per world"),width/2,20,0xFFAEDEC0);
        super.render(ctx,mx,my,delta);
    }
    @Override public void close() {cfg.save();if(client!=null)client.setScreen(parent);}
}

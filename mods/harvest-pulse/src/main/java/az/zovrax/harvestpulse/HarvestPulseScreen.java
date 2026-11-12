package az.zovrax.harvestpulse;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Field list and safe corner-selection controls. UI never directly edits server fields. */
public final class HarvestPulseScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private int page;
    private boolean preferences;
    public HarvestPulseScreen(Screen parent) {super(Text.literal("HarvestPulse - Farm Monitor"));this.parent=parent;}

    @Override protected void init() {
        int w=Math.min(285,Math.max(145,width-24));
        int x=(width-w)/2;
        int gap=23;
        int top=Math.max(29,(height-9*gap-24)/2);
        if (!preferences) {
            addDrawableChild(ButtonWidget.builder(Text.literal(
                HarvestPulseClient.selecting()?"Resume choosing corners":"Select field (2 corners)"), b->{
                    if (HarvestPulseClient.selecting()) client.setScreen(null);
                    else HarvestPulseClient.beginSelection(client);
                }).dimensions(x,top,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Max selection: "+cfg.selectionLimit+" x "+cfg.selectionLimit+" blocks"), b->{
                cfg.selectionLimit=FieldMath.nextLimit(cfg.selectionLimit);cfg.save();
                b.setMessage(Text.literal("Max selection: "+cfg.selectionLimit+" x "+cfg.selectionLimit+" blocks"));
            }).dimensions(x,top+gap,w,20).build());
            for (int row=0; row<4; row++) {
                int index=page*4+row;
                if(index>=cfg.fields.size()) break;
                HarvestPulseConfig.Field field=cfg.fields.get(index);
                addDrawableChild(ButtonWidget.builder(Text.literal((index+1)+". "+field.name+"  [Edit]"), b->{
                    if(client!=null)client.setScreen(new HarvestPulseFieldScreen(this,field.id));
                }).dimensions(x,top+gap*(row+2),w,20).build());
            }
            ButtonWidget prev=addDrawableChild(ButtonWidget.builder(Text.literal("< Previous"),b->{page--;clearAndInit();})
                .dimensions(x,top+gap*6,(w-4)/2,20).build()); prev.active=page>0;
            ButtonWidget next=addDrawableChild(ButtonWidget.builder(Text.literal("Next >"),b->{page++;clearAndInit();})
                .dimensions(x+(w+4)/2,top+gap*6,(w-4)/2,20).build());
            next.active=(page+1)*4<cfg.fields.size();
            addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD / inline appearance editor"),b->{
                if(client!=null)client.setScreen(new HarvestPulsePositionScreen(this));
            }).dimensions(x,top+gap*7,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Notifications & appearance"),b->{preferences=true;clearAndInit();})
                .dimensions(x,top+gap*8,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(HarvestPulseClient.selecting()?"Cancel selection":"Done"),b->{
                if(HarvestPulseClient.selecting()){HarvestPulseClient.cancelSelection();clearAndInit();}
                else close();
            }).dimensions(x,top+gap*9,w,20).build());
        } else {
            toggle(x,top,w,"Crop-ready sound",cfg.notifySound,()->{cfg.notifySound=!cfg.notifySound;});
            toggle(x,top+gap,w,"Top-right notification",cfg.notifyToast,()->{cfg.notifyToast=!cfg.notifyToast;});
            toggle(x,top+gap*2,w,"Show existing fields in selection",cfg.showExistingRegions,()->{cfg.showExistingRegions=!cfg.showExistingRegions;});
            toggle(x,top+gap*3,w,"Progress HUD",cfg.enabled,()->{cfg.enabled=!cfg.enabled;});
            addDrawableChild(new HarvestPulseSlider(x,top+gap*4,w,"HUD opacity",0,100,5,()->cfg.opacity,v->cfg.opacity=v));
            addDrawableChild(new HarvestPulseSlider(x,top+gap*5,w,"Background opacity",0,100,5,()->cfg.backgroundOpacity,v->cfg.backgroundOpacity=v));
            addDrawableChild(new HarvestPulseSlider(x,top+gap*6,w,"HUD size",50,200,5,()->cfg.scale,v->cfg.scale=v));
            addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD directly on screen"),b->{
                if(client!=null)client.setScreen(new HarvestPulsePositionScreen(this));
            }).dimensions(x,top+gap*7,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("< Back to fields"),b->{preferences=false;clearAndInit();})
                .dimensions(x,top+gap*9,w,20).build());
        }
    }
    private void toggle(int x,int y,int w,String label,boolean current,Runnable updater) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label+": "+(current?"ON":"OFF")),b->{
            updater.run();cfg.save();clearAndInit();
        }).dimensions(x,y,w,20).build());
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("GREEN valid / RED invalid / CYAN already registered  |  8 fields/player"),
            width/2,21,0xFFA8E3BE);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

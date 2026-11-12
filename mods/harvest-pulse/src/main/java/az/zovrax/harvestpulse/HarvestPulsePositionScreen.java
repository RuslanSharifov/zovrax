package az.zovrax.harvestpulse;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Click HUD for inline controls; hold and drag, or double click+drag, to reposition. */
public final class HarvestPulsePositionScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private boolean pressed, dragging, doublePress, editorOpen;
    private long pressedAt;
    private double downX,downY;
    private HarvestPulseSlider opacity, background, scale;
    private ButtonWidget sound, toast, exit;
    public HarvestPulsePositionScreen(Screen parent) {
        super(Text.literal("HarvestPulse - live HUD editor"));this.parent=parent;
    }
    @Override protected void init() {
        exit=addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close())
            .dimensions(width/2-55,height-27,110,20).build());
        int w=Math.min(215,Math.max(120,width-16));
        opacity=addDrawableChild(new HarvestPulseSlider(5,5,w,"HUD opacity",0,100,5,
            ()->cfg.opacity,v->cfg.opacity=v));
        background=addDrawableChild(new HarvestPulseSlider(5,28,w,"Background",0,100,5,
            ()->cfg.backgroundOpacity,v->cfg.backgroundOpacity=v));
        scale=addDrawableChild(new HarvestPulseSlider(5,51,w,"Scale",50,200,5,
            ()->cfg.scale,v->cfg.scale=v));
        sound=addDrawableChild(ButtonWidget.builder(Text.literal(soundLabel()),b->{
            cfg.notifySound=!cfg.notifySound;cfg.save();b.setMessage(Text.literal(soundLabel()));
        }).dimensions(5,74,w,20).build());
        toast=addDrawableChild(ButtonWidget.builder(Text.literal(toastLabel()),b->{
            cfg.notifyToast=!cfg.notifyToast;cfg.save();b.setMessage(Text.literal(toastLabel()));
        }).dimensions(5,97,w,20).build());
        visibility(false);
    }
    private String soundLabel(){return "Ready sound: "+(cfg.notifySound?"ON":"OFF");}
    private String toastLabel(){return "Top-right notification: "+(cfg.notifyToast?"ON":"OFF");}
    private void visibility(boolean visible){
        opacity.visible=visible;background.visible=visible;scale.visible=visible;
        sound.visible=visible;toast.visible=visible;
    }
    private int px(){return HarvestPulseHud.x(cfg,width);}
    private int py(){return HarvestPulseHud.y(cfg,client,height,true);}
    private int pw(){return HarvestPulseHud.width(cfg);}
    private int ph(){return HarvestPulseHud.height(cfg,client,true);}
    private boolean hit(double x,double y) {
        return x>=px()&&x<=px()+pw()&&y>=py()&&y<=py()+ph();
    }
    @Override public boolean mouseClicked(Click click,boolean doubled) {
        // Native sliders/buttons take priority even if the popup overlaps the HUD.
        if(super.mouseClicked(click,doubled))return true;
        if(click.buttonInfo().button()==GLFW.GLFW_MOUSE_BUTTON_LEFT&&hit(click.x(),click.y())) {
            pressed=true;dragging=false;doublePress=doubled;
            downX=click.x();downY=click.y();pressedAt=System.currentTimeMillis();
            return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy) {
        if(pressed) {
            boolean moved=Math.hypot(click.x()-downX,click.y()-downY)>3;
            if(moved && (doublePress || System.currentTimeMillis()-pressedAt>=180)) dragging=true;
            if(dragging) {
                cfg.x=Math.clamp(cfg.x+dx/Math.max(1,width-pw()),0d,1d);
                cfg.y=Math.clamp(cfg.y+dy/Math.max(1,height-ph()),0d,1d);
            }
            return true;
        }
        return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseReleased(Click click) {
        if(pressed) {
            pressed=false;
            if(!dragging) editorOpen=!editorOpen;
            dragging=false;cfg.save();
            return true;
        }
        return super.mouseReleased(click);
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        HarvestPulseHud.preview(ctx,cfg,px(),py());
        int x=px(),y=py(),w=pw(),h=ph();
        ctx.fill(x,y,x+w,y+1,0xDFFFFFFF);ctx.fill(x,y+h-1,x+w,y+h,0xDFFFFFFF);
        ctx.fill(x,y,x+1,y+h,0xDFFFFFFF);ctx.fill(x+w-1,y,x+w,y+h,0xDFFFFFFF);
        if(editorOpen) {
            int sw=Math.min(215,Math.max(120,width-16));
            int left=Math.clamp(x,5,Math.max(5,width-sw-5));
            int top=y+h+6;
            if(top+120>height-28)top=y-126;
            top=Math.clamp(top,6,Math.max(6,height-156));
            opacity.setPosition(left,top);
            background.setPosition(left,top+23);
            scale.setPosition(left,top+46);
            sound.setPosition(left,top+69);
            toast.setPosition(left,top+92);
        }
        visibility(editorOpen);
        super.render(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("Click bars: edit opacity / size. Hold + drag: move. Double-click + drag also works."),width/2,8,0xFFFFFFFF);
    }
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

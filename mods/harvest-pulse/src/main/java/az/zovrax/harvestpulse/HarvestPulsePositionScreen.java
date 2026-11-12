package az.zovrax.harvestpulse;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Single click edits the HUD directly; press-and-drag repositions it. */
public final class HarvestPulsePositionScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private HarvestPulseSlider scale,opacity,background;
    private ButtonWidget bellSound,toast,animation;
    private boolean dragging,popup,moved;
    private double dragDx,dragDy;

    public HarvestPulsePositionScreen(Screen parent) {
        super(Text.literal("HarvestPulse - HUD editor"));this.parent=parent;
    }
    @Override protected void init() {
        int w=Math.min(218,Math.max(130,width-18));
        scale=addDrawableChild(new HarvestPulseSlider(6,6,w,"Size",50,200,5,()->cfg.scale,v->cfg.scale=v));
        opacity=addDrawableChild(new HarvestPulseSlider(6,29,w,"Opacity",0,100,5,()->cfg.opacity,v->cfg.opacity=v));
        background=addDrawableChild(new HarvestPulseSlider(6,52,w,"Background",0,100,5,
            ()->cfg.backgroundOpacity,v->cfg.backgroundOpacity=v));
        bellSound=addDrawableChild(ButtonWidget.builder(Text.literal("Sound: "+on(cfg.soundAlerts)),b->{
            cfg.soundAlerts=!cfg.soundAlerts;cfg.save();b.setMessage(Text.literal("Sound: "+on(cfg.soundAlerts)));
        }).dimensions(6,75,w,20).build());
        toast=addDrawableChild(ButtonWidget.builder(Text.literal("Toast: "+on(cfg.toastAlerts)),b->{
            cfg.toastAlerts=!cfg.toastAlerts;cfg.save();b.setMessage(Text.literal("Toast: "+on(cfg.toastAlerts)));
        }).dimensions(6,98,w,20).build());
        animation=addDrawableChild(ButtonWidget.builder(Text.literal("Animation: "+on(cfg.animateAlerts)),b->{
            cfg.animateAlerts=!cfg.animateAlerts;cfg.save();b.setMessage(Text.literal("Animation: "+on(cfg.animateAlerts)));
        }).dimensions(6,121,w,20).build());
        popupVisibility(false);
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close())
            .dimensions(width/2-55,height-27,110,20).build());
    }
    private static String on(boolean b) {return b?"ON":"OFF";}
    private void popupVisibility(boolean value) {
        scale.visible=value;opacity.visible=value;background.visible=value;
        bellSound.visible=value;toast.visible=value;animation.visible=value;
    }
    private int px(){return HarvestPulseHud.x(cfg,width);}
    private int py(){return HarvestPulseHud.y(cfg,client,height,true);}
    private int pw(){return HarvestPulseHud.width(cfg);}
    private int ph(){return HarvestPulseHud.height(cfg,client,true);}
    private boolean hit(double mx,double my) {
        return mx>=px()&&mx<=px()+pw()&&my>=py()&&my<=py()+ph();
    }
    @Override public boolean mouseClicked(Click click,boolean doubled) {
        // Controls get first chance, even when they happen to overlap the HUD.
        if(super.mouseClicked(click,doubled))return true;
        if(click.buttonInfo().button()==GLFW.GLFW_MOUSE_BUTTON_LEFT&&hit(click.x(),click.y())) {
            dragging=true;moved=false;dragDx=0;dragDy=0;return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy) {
        if(dragging) {
            dragDx+=dx;dragDy+=dy;
            if(Math.abs(dragDx)>3||Math.abs(dragDy)>3)moved=true;
            if(moved) {
                cfg.x=Math.clamp(cfg.x+dx/Math.max(1,width-pw()),0d,1d);
                cfg.y=Math.clamp(cfg.y+dy/Math.max(1,height-ph()),0d,1d);
            }
            return true;
        }
        return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseReleased(Click click) {
        if(dragging) {
            dragging=false;
            if(!moved)popup=!popup;
            cfg.save();return true;
        }
        return super.mouseReleased(click);
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta) {
        renderBackground(ctx,mx,my,delta);
        HarvestPulseHud.preview(ctx,cfg,px(),py());
        int x=px(),y=py(),w=pw(),h=ph();
        ctx.fill(x,y,x+w,y+1,0xD7AAFFE0);
        ctx.fill(x,y+h-1,x+w,y+h,0xD7AAFFE0);
        ctx.fill(x,y,x+1,y+h,0xD7AAFFE0);
        ctx.fill(x+w-1,y,x+w,y+h,0xD7AAFFE0);
        if(popup) {
            int wid=Math.min(218,Math.max(130,width-18));
            int left=Math.clamp(px(),6,Math.max(6,width-wid-6));
            int top=py()+ph()+7;
            if(top+145>height-30)top=py()-153;
            top=Math.clamp(top,6,Math.max(6,height-176));
            scale.setPosition(left,top);
            opacity.setPosition(left,top+23);
            background.setPosition(left,top+46);
            bellSound.setPosition(left,top+69);
            toast.setPosition(left,top+92);
            animation.setPosition(left,top+115);
        }
        popupVisibility(popup);
        super.render(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Click HUD to edit  •  Hold and drag to move"),width/2,8,0xFFFFFFFF);
    }
    @Override public void close() {cfg.save();if(client!=null)client.setScreen(parent);}
}

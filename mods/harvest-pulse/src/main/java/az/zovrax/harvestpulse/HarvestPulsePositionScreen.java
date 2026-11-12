package az.zovrax.harvestpulse;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Draggable transparent HUD preview. Other mods' HUD positions are untouched. */
public final class HarvestPulsePositionScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private boolean dragging;
    public HarvestPulsePositionScreen(Screen parent){super(Text.literal("HarvestPulse - HUD position"));this.parent=parent;}
    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close())
            .dimensions(width/2-55,height-27,110,20).build());
    }
    private int px(){return HarvestPulseHud.x(cfg,width);}
    private int py(){return HarvestPulseHud.y(cfg,client,height,true);}
    private int pw(){return HarvestPulseHud.width(cfg);}
    private int ph(){return HarvestPulseHud.height(cfg,client,true);}
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(super.mouseClicked(click,doubled))return true;
        if(click.buttonInfo().button()==GLFW.GLFW_MOUSE_BUTTON_LEFT&&
            click.x()>=px()&&click.x()<=px()+pw()&&click.y()>=py()&&click.y()<=py()+ph()){
            dragging=true;return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){
        if(dragging){
            cfg.x=Math.clamp(cfg.x+dx/Math.max(1,width-pw()),0d,1d);
            cfg.y=Math.clamp(cfg.y+dy/Math.max(1,height-ph()),0d,1d);
            return true;
        }
        return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseReleased(Click click){
        if(dragging){dragging=false;cfg.save();return true;}
        return super.mouseReleased(click);
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){
        renderBackground(ctx,mouseX,mouseY,delta);
        HarvestPulseHud.preview(ctx,cfg,px(),py());
        int x=px(),y=py(),w=pw(),h=ph();
        ctx.fill(x,y,x+w,y+1,0xDFFFFFFF);ctx.fill(x,y+h-1,x+w,y+h,0xDFFFFFFF);
        ctx.fill(x,y,x+1,y+h,0xDFFFFFFF);ctx.fill(x+w-1,y,x+w,y+h,0xDFFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("Drag the field bars to move them"),width/2,8,0xFFFFFFFF);
    }
    @Override public void close(){cfg.save();if(client!=null)client.setScreen(parent);}
}

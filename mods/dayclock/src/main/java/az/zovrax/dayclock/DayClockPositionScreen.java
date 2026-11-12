package az.zovrax.dayclock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** The preview follows the cursor only while the preview itself is dragged. */
public final class DayClockPositionScreen extends Screen {
    private final Screen parent;
    private final DayClockConfig config = DayClockClient.config();
    private boolean dragging;

    public DayClockPositionScreen(Screen parent) {
        super(Text.literal("Move DayClock HUD"));
        this.parent = parent;
    }

    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width/2-50,height-28,100,20).build());
    }

    private int px() { return DayClockHud.x(config,MinecraftClient.getInstance(),width); }
    private int py() { return DayClockHud.y(config,MinecraftClient.getInstance(),height); }
    private int pw() { return DayClockHud.width(config,MinecraftClient.getInstance()); }
    private int ph() { return DayClockHud.height(config,MinecraftClient.getInstance()); }

    @Override public boolean mouseClicked(Click click,boolean doubled) {
        // Buttons must get first chance, otherwise the Done button can be
        // accidentally swallowed by the drag handler when HUD is moved there.
        if (super.mouseClicked(click,doubled)) return true;
        if (click.buttonInfo().button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
            && click.x() >= px() && click.x() < px()+pw()
            && click.y() >= py() && click.y() < py()+ph()) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override public boolean mouseDragged(Click click,double dx,double dy) {
        if (!dragging) return super.mouseDragged(click,dx,dy);
        config.x = Math.clamp(config.x+dx/Math.max(1d,width-pw()),0d,1d);
        config.y = Math.clamp(config.y+dy/Math.max(1d,height-ph()),0d,1d);
        return true;
    }

    @Override public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging=false;
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        DayClockHud.preview(ctx,config,px(),py());
        int x=px(),y=py(),w=pw(),h=ph();
        // Outline remains visible even when background/overall opacity are 0.
        ctx.fill(x,y,x+w,y+1,0xDDFFFFFF);
        ctx.fill(x,y+h-1,x+w,y+h,0xDDFFFFFF);
        ctx.fill(x,y,x+1,y+h,0xDDFFFFFF);
        ctx.fill(x+w-1,y,x+w,y+h,0xDDFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("Drag widget to move"),width/2,12,0xFFFFFFFF);
    }

    @Override public void close() {
        dragging=false;
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

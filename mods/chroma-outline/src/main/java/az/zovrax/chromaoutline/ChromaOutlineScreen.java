package az.zovrax.chromaoutline;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Three native settings pages; no Mod Menu or third-party UI library required. */
public final class ChromaOutlineScreen extends Screen {
    private final Screen parent;
    private final ChromaOutlineConfig config = ChromaOutlineClient.config();
    private int page = 0;

    ChromaOutlineScreen(Screen parent) {
        super(Text.literal("Chroma Outline settings"));
        this.parent = parent;
    }

    @Override protected void init() {
        int w = Math.min(310, Math.max(150, width - 28));
        int x = (width - w) / 2;
        int gap = 23;
        int top = Math.max(24, (height - 10 * gap) / 2);
        if (page == 0) {
            toggle(x,top,w,"Custom outline",()->config.enabled,v->config.enabled=v);
            addDrawableChild(ButtonWidget.builder(Text.literal("Color: " + config.mode),
                b -> { config.mode = "RAINBOW".equals(config.mode) ? "SOLID" : "RAINBOW";
                       config.save(); clearAndInit(); }).dimensions(x,top+gap,w,20).build());
            slider(x,top+2*gap,w,"Opacity",0,100,5,()->config.opacity,v->config.opacity=v,"%");
            slider(x,top+3*gap,w,"Line weight (approx.)",1,5,1,()->config.outlineWidth,v->config.outlineWidth=v,"");
            slider(x,top+4*gap,w,"Shape expansion",0,20,1,()->config.expansion,v->config.expansion=v," mm");
            toggle(x,top+5*gap,w,"Full block cube",()->config.fullCube,v->config.fullCube=v);
            toggle(x,top+6*gap,w,"Pulse effect",()->config.pulseEnabled,v->config.pulseEnabled=v);
            addDrawableChild(ButtonWidget.builder(Text.literal("Color controls >"),
                b -> goTo(1)).dimensions(x,top+7*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Pulse and presets >"),
                b -> goTo(2)).dimensions(x,top+8*gap,w,20).build());
        } else if (page == 1) {
            slider(x,top,w,"Fixed red",0,255,1,()->config.red,v->config.red=v,"");
            slider(x,top+gap,w,"Fixed green",0,255,1,()->config.green,v->config.green=v,"");
            slider(x,top+2*gap,w,"Fixed blue",0,255,1,()->config.blue,v->config.blue=v,"");
            slider(x,top+3*gap,w,"Rainbow speed",1,100,1,()->config.rainbowSpeed,v->config.rainbowSpeed=v,"");
            slider(x,top+4*gap,w,"Rainbow saturation",0,100,5,()->config.rainbowSaturation,v->config.rainbowSaturation=v,"%");
            slider(x,top+5*gap,w,"Rainbow brightness",1,100,1,()->config.rainbowBrightness,v->config.rainbowBrightness=v,"%");
            addDrawableChild(ButtonWidget.builder(Text.literal("Solid-color preset"), b -> {
                config.mode = "SOLID"; config.save(); clearAndInit();
            }).dimensions(x,top+7*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("< Back"), b -> goTo(0))
                .dimensions(x,top+8*gap,w,20).build());
        } else {
            slider(x,top,w,"Pulse speed",1,100,1,()->config.pulseSpeed,v->config.pulseSpeed=v,"");
            slider(x,top+gap,w,"Pulse depth",0,90,5,()->config.pulseDepth,v->config.pulseDepth=v,"%");
            addDrawableChild(ButtonWidget.builder(Text.literal("Preset: bright rainbow"), b -> {
                config.mode="RAINBOW"; config.rainbowSaturation=100; config.rainbowBrightness=100;
                config.rainbowSpeed=20; config.opacity=100; config.pulseEnabled=false;
                config.save(); clearAndInit();
            }).dimensions(x,top+3*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Preset: cyan"), b -> {
                config.mode="SOLID"; config.red=30; config.green=220; config.blue=255;
                config.opacity=100; config.pulseEnabled=false; config.save(); clearAndInit();
            }).dimensions(x,top+4*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Preset: glowing magenta"), b -> {
                config.mode="SOLID"; config.red=255; config.green=40; config.blue=220;
                config.opacity=100; config.pulseEnabled=true; config.pulseSpeed=15;
                config.pulseDepth=35; config.save(); clearAndInit();
            }).dimensions(x,top+5*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Reset all defaults"), b -> {
                copyDefaults(); config.save(); clearAndInit();
            }).dimensions(x,top+6*gap,w,20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("< Back"), b -> goTo(0))
                .dimensions(x,top+8*gap,w,20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(x,top+9*gap,w,20).build());
    }

    private void copyDefaults() {
        ChromaOutlineConfig d = new ChromaOutlineConfig();
        config.enabled=d.enabled; config.mode=d.mode; config.red=d.red;
        config.green=d.green; config.blue=d.blue; config.opacity=d.opacity;
        config.rainbowSpeed=d.rainbowSpeed; config.rainbowSaturation=d.rainbowSaturation;
        config.rainbowBrightness=d.rainbowBrightness; config.pulseEnabled=d.pulseEnabled;
        config.pulseSpeed=d.pulseSpeed; config.pulseDepth=d.pulseDepth;
        config.outlineWidth=d.outlineWidth; config.expansion=d.expansion;
        config.fullCube=d.fullCube;
    }

    private void slider(int x,int y,int w,String label,int min,int max,int step,
                        java.util.function.IntSupplier get, java.util.function.IntConsumer set,String unit) {
        addDrawableChild(new OutlineSlider(x,y,w,label,min,max,step,get,set,unit));
    }

    private void toggle(int x,int y,int w,String label,BooleanSupplier get,Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (get.getAsBoolean()?"ON":"OFF")), b -> {
            set.accept(!get.getAsBoolean());
            config.save();
            b.setMessage(Text.literal(label + ": " + (get.getAsBoolean()?"ON":"OFF")));
        }).dimensions(x,y,w,20).build());
    }

    private void goTo(int next) { page = next; clearAndInit(); }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}

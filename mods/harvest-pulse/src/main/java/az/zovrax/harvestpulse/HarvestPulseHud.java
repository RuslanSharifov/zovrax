package az.zovrax.harvestpulse;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Lightweight per-field crop progress bars. No textures, shaders or Mixins required. */
public final class HarvestPulseHud {
    private static final int WIDTH = 194;
    private static final int ROW_H = 33;
    private static final int PAD = 6;
    private HarvestPulseHud() { }

    public static List<HarvestPulseConfig.Field> visible(MinecraftClient mc, boolean preview) {
        List<HarvestPulseConfig.Field> out = new ArrayList<>();
        for (HarvestPulseConfig.Field f : HarvestPulseClient.config().fields) {
            if (f.showMode == 2) continue;
            if (!preview && !FieldTracker.isSameWorld(f, mc)) continue;
            // Ready-only never displays stale progress from unloaded chunks.
            if (f.showMode == 1 && !(f.sampled && f.loaded && FieldMath.ready(f.mature, f.total))) continue;
            out.add(f);
        }
        return out;
    }

    public static int width(HarvestPulseConfig cfg) { return Math.round(WIDTH * cfg.scale / 100f); }
    public static int height(HarvestPulseConfig cfg, MinecraftClient mc, boolean preview) {
        return Math.round((PAD*2 + Math.max(preview ? 1 : 0, visible(mc,preview).size())*ROW_H) * cfg.scale / 100f);
    }
    public static int x(HarvestPulseConfig cfg, int screenW) {
        return (int)Math.round(cfg.x * Math.max(0, screenW-width(cfg)));
    }
    public static int y(HarvestPulseConfig cfg, MinecraftClient mc, int screenH, boolean preview) {
        return (int)Math.round(cfg.y * Math.max(0, screenH-height(cfg,mc,preview)));
    }

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        HarvestPulseConfig cfg = HarvestPulseClient.config();
        if (!cfg.enabled || mc.player == null || mc.world == null || mc.currentScreen != null || mc.options.hudHidden) return;
        if (visible(mc,false).isEmpty()) return;
        draw(ctx,cfg,x(cfg,ctx.getScaledWindowWidth()),y(cfg,mc,ctx.getScaledWindowHeight(),false),false);
    }

    public static void preview(DrawContext ctx,HarvestPulseConfig cfg,int px,int py) {
        draw(ctx,cfg,px,py,true);
    }

    private static void draw(DrawContext ctx, HarvestPulseConfig cfg, int px, int py, boolean preview) {
        if (cfg.opacity <= 0) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        List<HarvestPulseConfig.Field> farms = visible(mc, preview);
        if (preview && farms.isEmpty()) {
            HarvestPulseConfig.Field example = new HarvestPulseConfig.Field();
            example.name = "Wheat field";
            example.total = 64; example.mature = 32; example.loaded = true; example.sampled = true;
            farms.add(example);
        }
        if (farms.isEmpty()) return;
        int textA = Math.clamp(cfg.opacity * 255 / 100,0,255);
        int bgA = Math.clamp(textA * cfg.backgroundOpacity / 100,0,255);
        int h = PAD*2+farms.size()*ROW_H;
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(px,py);
            ctx.getMatrices().scale(cfg.scale/100f,cfg.scale/100f);
            if (bgA > 0) ctx.fill(0,0,WIDTH,h,(bgA << 24)|0x11251C);
            int i=0;
            for (HarvestPulseConfig.Field f : farms) {
                int top = PAD+i*ROW_H;
                int mature = f.mature;
                int total = f.total;
                boolean fresh = f.loaded && f.sampled;
                boolean ready = fresh && FieldMath.ready(mature,total);
                int ratio = fresh ? FieldMath.percent(mature,total) : 0;
                String name = f.name == null ? "Field" : f.name;
                if (name.length() > 15) name=name.substring(0,14)+"…";
                int labelColor = (textA << 24) | 0xF1FFF4;
                ctx.drawTextWithShadow(mc.textRenderer,name,PAD,top+1,labelColor);
                String value = fresh ? switch (f.valueMode) {
                    case 1 -> ratio+"%";
                    case 2 -> mature+"/"+total;
                    default -> mature+"/"+total+" · "+ratio+"%";
                } : "awaiting server";
                if (ready) value += "  !";
                int vx=WIDTH-PAD-mc.textRenderer.getWidth(value) - (ready ? 13 : 0);
                ctx.drawTextWithShadow(mc.textRenderer,value,Math.max(70,vx),top+1,labelColor);
                if (ready) drawBell(ctx,WIDTH-PAD-10,top, textA);
                int barX=PAD,barY=top+17,barW=WIDTH-PAD*2;
                ctx.fill(barX,barY,barX+barW,barY+7,(Math.max(28,textA/2)<<24)|0x24382D);
                if (fresh && ratio>0) {
                    int fillW=Math.clamp(Math.round(barW*ratio/100f),0,barW);
                    ctx.fill(barX,barY,barX+fillW,barY+7,(textA<<24)|(ready?0xE5BF51:0x76D68A));
                }
                i++;
            }
        } finally { ctx.getMatrices().popMatrix(); }
    }

    private static void drawBell(DrawContext ctx,int x,int y,int alpha) {
        int c=(alpha<<24)|0xFFD365;
        ctx.fill(x+4,y+0,x+7,y+2,c); // bell crown
        ctx.fill(x+2,y+2,x+9,y+7,c);
        ctx.fill(x+1,y+7,x+10,y+9,c);
        ctx.fill(x+4,y+10,x+7,y+11,c);
    }
}

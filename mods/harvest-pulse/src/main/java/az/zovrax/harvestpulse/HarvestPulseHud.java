package az.zovrax.harvestpulse;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;

/** Up to eight independent bars with ready-only display and ease-out alert entry. */
public final class HarvestPulseHud {
    private static final int WIDTH=205;
    private static final int ROW_H=36;
    private static final int PAD=6;
    private HarvestPulseHud() { }

    public static List<HarvestPulseConfig.Field> visible(MinecraftClient mc,boolean preview) {
        List<HarvestPulseConfig.Field> out=new ArrayList<>();
        for(HarvestPulseConfig.Field f:HarvestPulseClient.config().fields) {
            if(f.showMode==2) continue;
            if(!preview&&!FieldTracker.isSameWorld(f,mc))continue;
            if(f.showMode==1 && !(f.sampled&&f.loaded&&FieldMath.ready(f.mature,f.total))) continue;
            out.add(f);
        }
        return out;
    }
    public static int width(HarvestPulseConfig c) { return Math.round(WIDTH*c.scale/100f); }
    public static int height(HarvestPulseConfig c,MinecraftClient mc,boolean preview) {
        return Math.round((Math.max(preview?1:0,visible(mc,preview).size())*ROW_H)*c.scale/100f);
    }
    public static int x(HarvestPulseConfig c,int w) {
        return (int)Math.round(c.x*Math.max(0,w-width(c)));
    }
    public static int y(HarvestPulseConfig c,MinecraftClient mc,int h,boolean preview) {
        return (int)Math.round(c.y*Math.max(0,h-height(c,mc,preview)));
    }
    public static void render(DrawContext ctx,RenderTickCounter ignored) {
        MinecraftClient mc=MinecraftClient.getInstance();
        HarvestPulseConfig cfg=HarvestPulseClient.config();
        if(mc.player==null||mc.world==null||mc.currentScreen!=null||mc.options.hudHidden) return;
        if(HarvestPulseClient.isSelecting()) selectionInfo(ctx,mc);
        if(!cfg.enabled||visible(mc,false).isEmpty()) return;
        draw(ctx,cfg,x(cfg,ctx.getScaledWindowWidth()),y(cfg,mc,ctx.getScaledWindowHeight(),false),false);
    }
    public static void preview(DrawContext ctx,HarvestPulseConfig cfg,int px,int py) {
        draw(ctx,cfg,px,py,true);
    }
    private static void selectionInfo(DrawContext ctx,MinecraftClient mc) {
        BlockPos a=HarvestPulseClient.firstCorner();
        BlockPos b=mc.crosshairTarget instanceof BlockHitResult hit?
            HarvestPulseClient.cropLevelPos(mc.world,hit.getBlockPos()):null;
        String message;
        int color;
        if(a==null) {
            message="HarvestPulse: right-click FIRST corner  |  [H] Cancel  |  Blue = saved";
            color=0xFF84EDB1;
        } else if(b==null) {
            message="Move crosshair to second corner  |  [H] Cancel";
            color=0xFFE9D688;
        } else {
            boolean valid=HarvestPulseClient.validPreview(a,b,FieldTracker.worldKey(mc),FieldTracker.dimension(mc));
            int w=(int)(Math.abs((long)a.getX()-b.getX())+1);
            int d=(int)(Math.abs((long)a.getZ()-b.getZ())+1);
            message=(valid?"GREEN: Select ":"RED: Invalid/overlap ")+w+"x"+d+
                "  |  Limit "+HarvestPulseClient.config().selectionLimit+"x"+
                HarvestPulseClient.config().selectionLimit+"  |  [H] Cancel";
            color=valid?0xFF82F4A0:0xFFFF8E83;
        }
        int x=Math.max(3,(ctx.getScaledWindowWidth()-mc.textRenderer.getWidth(message))/2);
        int y=ctx.getScaledWindowHeight()-51;
        ctx.fill(x-6,y-4,x+mc.textRenderer.getWidth(message)+6,y+13,0xBC13241B);
        ctx.drawTextWithShadow(mc.textRenderer,Text.literal(message),x,y,color);
    }
    private static double ease(double t) {
        t=Math.clamp(t,0d,1d);
        return 1-Math.pow(1-t,3);
    }
    private static void draw(DrawContext ctx,HarvestPulseConfig cfg,int px,int py,boolean preview) {
        if(cfg.opacity<=0)return;
        MinecraftClient mc=MinecraftClient.getInstance();
        List<HarvestPulseConfig.Field> farms=visible(mc,preview);
        if(preview&&farms.isEmpty()) {
            HarvestPulseConfig.Field sample=new HarvestPulseConfig.Field();
            sample.name="Wheat • example";sample.total=64;sample.mature=32;
            sample.loaded=true;sample.sampled=true;
            farms.add(sample);
        }
        if(farms.isEmpty())return;
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(px,py);
            ctx.getMatrices().scale(cfg.scale/100f,cfg.scale/100f);
            long now=System.currentTimeMillis();
            for(int i=0;i<farms.size();i++) {
                HarvestPulseConfig.Field f=farms.get(i);
                boolean fresh=f.loaded&&f.sampled;
                boolean ready=fresh&&FieldMath.ready(f.mature,f.total);
                double phase=(!preview&&cfg.animateAlerts&&ready&&f.readySinceMillis>0)
                    ? ease((now-f.readySinceMillis)/620.0) : 1d;
                int textA=Math.clamp((int)Math.round(cfg.opacity*255d/100d*phase),0,255);
                int bgA=Math.clamp(textA*cfg.backgroundOpacity/100,0,255);
                int slide=(!preview&&cfg.animateAlerts)?(int)Math.round((1-phase)*-42):0;
                int top=i*ROW_H;
                ctx.getMatrices().pushMatrix();
                try {
                    ctx.getMatrices().translate(slide,top);
                    if(bgA>0) ctx.fill(0,0,WIDTH,ROW_H-3,(bgA<<24)|0x11261D);
                    String name=f.name==null?"Field":f.name;
                    if(name.length()>17)name=name.substring(0,16)+"…";
                    int color=(textA<<24)|0xF2FFF3;
                    ctx.drawTextWithShadow(mc.textRenderer,name,PAD,5,color);
                    int percent=fresh?FieldMath.percent(f.mature,f.total):0;
                    String value=fresh?switch(f.valueMode) {
                        case 1 -> percent+"%";
                        case 2 -> f.mature+"/"+f.total;
                        default -> f.mature+"/"+f.total+"  "+percent+"%";
                    }:"scanning...";
                    int bellSpace=ready?17:0;
                    int valueX=Math.max(80,WIDTH-PAD-bellSpace-mc.textRenderer.getWidth(value));
                    ctx.drawTextWithShadow(mc.textRenderer,value,valueX,5,color);
                    if(ready)drawBell(ctx,WIDTH-18,4,textA);
                    int barY=21,barX=PAD,barW=WIDTH-PAD*2;
                    ctx.fill(barX,barY,barX+barW,barY+8,(Math.max(20,textA/2)<<24)|0x233C2C);
                    if(fresh&&percent>0) {
                        int fill=Math.clamp(Math.round(barW*percent/100f),0,barW);
                        ctx.fill(barX,barY,barX+fill,barY+8,(textA<<24)|(ready?0xF6C96B:0x64D98B));
                    }
                } finally {ctx.getMatrices().popMatrix();}
            }
        } finally {ctx.getMatrices().popMatrix();}
    }
    private static void drawBell(DrawContext ctx,int x,int y,int alpha) {
        int c=(alpha<<24)|0xFFD66D;
        ctx.fill(x+4,y,x+7,y+2,c);
        ctx.fill(x+2,y+2,x+9,y+7,c);
        ctx.fill(x+1,y+7,x+10,y+9,c);
        ctx.fill(x+4,y+10,x+7,y+12,c);
    }
}

package az.zovrax.smartreturn;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.List;

/**
 * Smoothly rotating vector compass arrow. This is rendered from geometry,
 * not locale-dependent Unicode glyphs; size, opacity, and screen position are
 * saved independently. Render works in first and third person.
 */
public final class SmartReturnHud {
    private static float visibleAngle=Float.NaN;
    private static long previousFrame;
    private SmartReturnHud(){}

    public static void render(DrawContext ctx,RenderTickCounter counter){
        MinecraftClient mc=MinecraftClient.getInstance();
        SmartReturnConfig cfg=SmartReturnClient.config();
        RouteRecorder routes=SmartReturnClient.routes();
        if(!cfg.enabled||!cfg.compass()||!routes.guiding()||
            mc.player==null||mc.options.hudHidden||mc.currentScreen!=null)return;
        List<Vec3d> path=routes.guidance();
        if(path.isEmpty())return;
        Vec3d player=new Vec3d(mc.player.getX(),mc.player.getY(),mc.player.getZ());
        Vec3d waypoint=path.get(path.size()-1);
        for(Vec3d p:path) {
            if(p.distanceTo(player)>2.0){waypoint=p;break;}
        }
        double dx=waypoint.x-player.x,dz=waypoint.z-player.z;
        float goal=MathHelper.wrapDegrees((float)(mc.player.getYaw()-
            Math.toDegrees(Math.atan2(-dx,dz))));
        long now=System.nanoTime();
        double dt=previousFrame==0?.016:Math.min(.1,(now-previousFrame)/1e9);
        previousFrame=now;
        if(!Float.isFinite(visibleAngle))visibleAngle=goal;
        visibleAngle=MathHelper.wrapDegrees(visibleAngle+
            MathHelper.wrapDegrees(goal-visibleAngle)*(float)(1-Math.exp(-dt*13)));
        String direction="";
        if(waypoint.y-player.y>1.15)direction=Text.translatable("smartreturn.hud.up").getString();
        else if(waypoint.y-player.y< -1.15)
            direction=Text.translatable("smartreturn.hud.down").getString();
        String metres=cfg.showDistance ?
            "  "+Text.translatable("smartreturn.hud.meters",Math.round(routes.distance())).getString():"";
        String label=Text.translatable("smartreturn.hud.return").getString()+
            (direction.isEmpty()?"":" · "+direction)+metres;
        drawCompass(ctx,mc,cfg,visibleAngle,label);
        if(cfg.throughWalls && cfg.arrows())
            SmartReturnWorldArrows.drawOverlay(ctx,mc,cfg,path);
    }

    /** Screen UI also uses this same preview, with no game world required. */
    public static void drawCompass(DrawContext ctx,MinecraftClient mc,
                                    SmartReturnConfig cfg,float angle,String label){
        float size=cfg.compassSize/100f;
        int w=Math.round(154*size),h=Math.round(52*size);
        int x=MathHelper.clamp(Math.round(cfg.compassX*ctx.getScaledWindowWidth()),
            w/2+3,Math.max(w/2+3,ctx.getScaledWindowWidth()-w/2-3));
        int y=MathHelper.clamp(Math.round(cfg.compassY*ctx.getScaledWindowHeight()),
            h/2+3,Math.max(h/2+3,ctx.getScaledWindowHeight()-h/2-3));
        int alpha=Math.round(cfg.compassOpacity*255/100f);
        int bg=(Math.round(alpha*.72f)<<24)|0x102a30;
        int accent=(alpha<<24)|0x64f3d3;
        int white=(alpha<<24)|0xe8fffa;
        ctx.fill(x-w/2,y-h/2,x+w/2,y+h/2,bg);
        ctx.fill(x-w/2,y-h/2,x-w/2+2,y+h/2,accent);
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate(x,y-8*size);
            ctx.getMatrices().scale(size,size);
            ctx.getMatrices().rotate((float)Math.toRadians(angle));
            // Vector arrowhead with smooth rotation. No text character is used
            // for direction; shape renders identically in all 5 languages.
            for(int yy=-18;yy<3;yy++){
                int half=Math.max(1,(yy+19)/2);
                ctx.fill(-half,yy,half+1,yy+1,accent);
            }
            ctx.fill(-3,-2,4,17,white);
            ctx.fill(-5,14,6,18,accent);
        }finally{ctx.getMatrices().popMatrix();}
        int max=Math.round(137*size);
        String shown=mc.textRenderer.trimToWidth(label,max);
        ctx.drawCenteredTextWithShadow(mc.textRenderer,shown,x,
            y+Math.round(13*size),white);
    }
}

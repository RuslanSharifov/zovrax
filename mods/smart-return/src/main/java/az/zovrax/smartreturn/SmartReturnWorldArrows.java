package az.zovrax.smartreturn;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import java.util.List;

/**
 * Ten lightweight world-space path visualisations. All positions are drawn
 * relative to the actual rendering camera rather than the player's first-person
 * eye, so third-person/front-facing views align with the world correctly.
 */
public final class SmartReturnWorldArrows {
    private static final BufferAllocator ALLOCATOR=new BufferAllocator(1<<19);
    private static final VertexConsumerProvider.Immediate BUFFER=
        VertexConsumerProvider.immediate(ALLOCATOR);
    private static boolean renderDisabled;
    private SmartReturnWorldArrows(){}

    public static void render(WorldRenderContext ctx){
        MinecraftClient mc=MinecraftClient.getInstance();
        SmartReturnConfig config=SmartReturnClient.config();
        RouteRecorder recorder=SmartReturnClient.routes();
        if(renderDisabled||mc.player==null||mc.world==null||mc.options.hudHidden||
            mc.currentScreen!=null||!config.enabled||!config.arrows()||
            !recorder.guiding()||config.throughWalls)return;
        List<Vec3d> way=recorder.guidance();
        if(way.size()<2)return;
        MatrixStack matrix=ctx.matrices();
        if(matrix==null)return;
        Vec3d camera=mc.gameRenderer.getCamera().getPos();
        matrix.push();
        matrix.translate(-camera.x,-camera.y,-camera.z);
        int stamps=0;
        double interval=2.5-config.effectDensity*.015;
        double clock=(System.nanoTime()%10_000_000_000L)/1e9;
        try{
            VertexConsumer consumer=BUFFER.getBuffer(RenderLayer.getLines());
            for(int i=0;i<way.size()-1 && stamps<130;i++){
                Vec3d a=way.get(i),b=way.get(i+1);
                Vec3d delta=b.subtract(a);
                double distance=delta.length();
                if(distance<=.05||distance>8)continue;
                if(a.squaredDistanceTo(camera)>46*46 && b.squaredDistanceTo(camera)>46*46)continue;
                if(!mc.world.isChunkLoaded((int)Math.floor(a.x)>>4,(int)Math.floor(a.z)>>4))continue;
                double yaw=Math.atan2(delta.x,delta.z);
                double step=Math.max(.65,interval);
                for(double moved=Math.min(step*.5,distance);moved<distance && stamps<130;moved+=step){
                    Vec3d p=a.lerp(b,moved/distance);
                    if(p.squaredDistanceTo(camera)>46*46)continue;
                    // Bounding line/footprint follows each recorded step and slope.
                    // Soft animation uses only changing geometry/color, no particles
                    // spawned into the game or persistent entity allocations.
                    float pulse=(float)(.77+.23*Math.sin(clock*4+stamps*.33));
                    drawStyle(matrix,consumer,p,yaw,config.effectStyle,stamps,clock,pulse);
                    stamps++;
                }
            }
        }catch(IllegalStateException e){
            renderDisabled=true;
            System.err.println("[SmartReturn] 3D path rendering disabled: "+e);
        }finally{
            matrix.pop();
            try{BUFFER.draw();}
            catch(IllegalStateException e){
                renderDisabled=true;
                System.err.println("[SmartReturn] Failed to flush 3D path: "+e);
            }
        }
    }

    private static void stamp(MatrixStack m,VertexConsumer lines,Vec3d p,
                              double yaw,double offsetX,double offsetZ,
                              double width,double length,double height,
                              float r,float g,float b,float alpha){
        m.push();
        try{
            m.translate(p.x,p.y,p.z);
            m.multiply(RotationAxis.POSITIVE_Y.rotation((float)yaw));
            VertexRendering.drawBox(m.peek(),lines,
                offsetX-width*.5,offsetZ-height*.5,-length*.5,
                offsetX+width*.5,offsetZ+height*.5,length*.5,
                r,g,b,alpha);
        }finally{m.pop();}
    }
    // offsetZ is vertical world height (stored as local Y); positive is up.
    private static void drawStyle(MatrixStack m,VertexConsumer out,Vec3d pos,
                                  double yaw,int style,int n,double clock,float pulse){
        Vec3d p=pos.add(0,.055,0);
        float teal=.85f*pulse,cream=.94f*pulse,blue=.98f*pulse;
        switch(style){
          case 0 -> { // Left/right real footprints, alternating each stride.
            double side=(n%2==0?-.18:.18);
            stamp(m,out,p,yaw,side,.06,.17,.34,.035,teal,cream,.75f,.9f);
            stamp(m,out,p,yaw,side,.06,.19,.08,.035,teal,cream,.75f,.9f);
          }
          case 1 -> // Short separated dashes
            stamp(m,out,p,yaw,0,.05,.11,.55,.035,teal,cream,.72f,.84f);
          case 2 -> // Solid light-tube pieces
            stamp(m,out,p,yaw,0,.06,.12,1.24,.05,.4f,.92f,blue,.84f);
          case 3 -> // Bright spots
            stamp(m,out,p,yaw,0,.08,.16,.16,.10,.9f,.9f,.56f,.9f);
          case 4 -> { // Ground chevrons pointing towards the exit
            stamp(m,out,p,yaw+.65,-.14,.07,.09,.43,.05,teal,cream,.9f,.88f);
            stamp(m,out,p,yaw-.65,.14,.07,.09,.43,.05,teal,cream,.9f,.88f);
          }
          case 5 -> { // Animated short signal posts
            double rise=.36+.14*Math.sin(clock*3+n*.45);
            stamp(m,out,p,yaw,0,rise*.5,.12,.12,rise,.3f,cream,blue,.8f);
            stamp(m,out,p,yaw,0,rise,.31,.31,.06,.3f,cream,blue,.88f);
          }
          case 6 -> { // Rhombus/diamond ground runes
            stamp(m,out,p,yaw+Math.PI/4,0,.12,.35,.35,.045,.9f,.7f,.98f,.86f);
            stamp(m,out,p,yaw+Math.PI/4,0,.12,.16,.16,.08,.45f,.97f,blue,.78f);
          }
          case 7 -> { // Glitter crosses, softly animated
            double rise=.10+.07*Math.sin(clock*5+n*.65);
            stamp(m,out,p,yaw,0,rise,.48,.08,.03,.9f,cream,.48f,.88f);
            stamp(m,out,p,yaw+Math.PI/2,0,rise,.48,.08,.03,.9f,cream,.48f,.88f);
          }
          case 8 -> { // Two parallel rail/ribbon guides
            stamp(m,out,p,yaw,-.24,.06,.065,1.22,.04,.3f,.8f,.96f,.75f);
            stamp(m,out,p,yaw,.24,.06,.065,1.22,.04,.3f,.8f,.96f,.75f);
          }
          case 9 -> { // Floating waypoint cubes
            double rise=.68+.18*Math.sin(clock*2.7+n*.45);
            stamp(m,out,p,yaw+clock*.5,0,rise,.37,.37,.37,teal,.92f,1f,.86f);
            stamp(m,out,p,yaw,0,rise*.4,.07,.07,rise*.55,teal,.72f,1f,.66f);
          }
          default -> stamp(m,out,p,yaw,0,.09,.14,.22,.03,teal,cream,blue,.85f);
        }
    }

    /**
     * True see-through-walls setting: screen-project actual world path positions
     * onto an unobstructed HUD layer, instead of relying on a depth-tested
     * world rendering pipeline. Screen projection uses the active camera, so
     * perspective (1st/3rd/front) stays consistent.
     */
    public static void drawOverlay(DrawContext ctx,MinecraftClient mc,
                                   SmartReturnConfig config,List<Vec3d> path){
        if(mc.world==null||mc.player==null||path.isEmpty())return;
        Camera camera=mc.gameRenderer.getCamera();
        Vec3d eye=camera.getPos();
        double yaw=Math.toRadians(camera.getYaw()),pitch=Math.toRadians(camera.getPitch());
        double sy=Math.sin(yaw),cy=Math.cos(yaw),sp=Math.sin(pitch),cp=Math.cos(pitch);
        Vec3d forward=new Vec3d(-sy*cp,-sp,cy*cp);
        Vec3d right=new Vec3d(cy,0,sy);
        Vec3d up=new Vec3d(-sy*sp,cp,cy*sp);
        double fov=mc.options.getFov().getValue();
        double factor=ctx.getScaledWindowHeight()/(2*Math.tan(Math.toRadians(fov)/2));
        int w=ctx.getScaledWindowWidth(),h=ctx.getScaledWindowHeight();
        int shown=0;
        int tint=0xC85AF2CC;
        // Limit to nearby and disjoint markers to minimise obscuration.
        for(int i=0;i<path.size()&&shown<38;i+=Math.max(1,110-config.effectDensity)/40+1){
            Vec3d delta=path.get(i).add(0,.55,0).subtract(eye);
            if(delta.lengthSquared()>52*52)break;
            double distance=delta.dotProduct(forward);
            if(distance<.18)continue;
            int x=(int)Math.round(w*.5+factor*delta.dotProduct(right)/distance);
            int y=(int)Math.round(h*.5-factor*delta.dotProduct(up)/distance);
            if(x<10||x>w-10||y<10||y>h-10)continue;
            int size=(int)Math.max(2,Math.min(8,13/distance+2));
            // A ring and central dot: crisp in either perspective and reliably
            // visible through walls, unlike depth-tested normal 3D markers.
            ctx.fill(x-size,y-size,x+size+1,y-size+1,tint);
            ctx.fill(x-size,y+size,x+size+1,y+size+1,tint);
            ctx.fill(x-size,y-size,x-size+1,y+size,tint);
            ctx.fill(x+size,y-size,x+size+1,y+size,tint);
            ctx.fill(x-1,y-1,x+2,y+2,0xEFFFF9B0);
            shown++;
        }
    }
}

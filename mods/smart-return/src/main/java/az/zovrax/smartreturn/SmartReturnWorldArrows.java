package az.zovrax.smartreturn;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import java.util.List;

/**
 * World-space arrow chevrons made of short, outlined 3D markers.
 * Uses dedicated buffers, NOT the world renderer's borrowed consumers.
 */
public final class SmartReturnWorldArrows {
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(1 << 18);
    private static final VertexConsumerProvider.Immediate LINES =
            VertexConsumerProvider.immediate(ALLOCATOR);
    private static boolean unavailable;
    private SmartReturnWorldArrows() { }

    public static void render(WorldRenderContext ctx) {
        if (unavailable) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        SmartReturnConfig config = SmartReturnClient.config();
        RouteRecorder routes = SmartReturnClient.routes();
        if (!config.enabled || !config.arrows() || !routes.guiding() ||
                mc.player == null || mc.world == null || mc.options.hudHidden ||
                mc.currentScreen != null) return;
        List<Vec3d> path = routes.guidance();
        if (path.size() < 2) return;
        MatrixStack matrices = ctx.matrices();
        if (matrices == null) return;
        Vec3d camera = mc.player.getCameraPosVec(1f);
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        try {
            VertexConsumer lines = LINES.getBuffer(RenderLayer.getLines());
            int drawn = 0;
            for (int i=0;i<path.size()-1 && drawn < 36;i++) {
                Vec3d from=path.get(i), to=path.get(i+1);
                if (from.squaredDistanceTo(mc.player.getPos()) > 50*50) break;
                if (from.distanceTo(to) > 10.0) continue;
                if (!mc.world.isChunkLoaded((int)Math.floor(from.x)>>4,(int)Math.floor(from.z)>>4)) continue;
                double dx = to.x-from.x, dz=to.z-from.z;
                double norm = Math.sqrt(dx*dx+dz*dz);
                if (norm < .1) {
                    mark(matrices, lines, to.add(0,.15,0), .22);
                } else {
                    double ux=dx/norm, uz=dz/norm;
                    Vec3d tip = from.lerp(to, 0.8).add(0,.12,0);
                    // A small chevron in the walking direction; all markers
                    // are world-positioned and therefore slope with the cave.
                    mark(matrices,lines,from.lerp(to,.35).add(0,.12,0),.12);
                    mark(matrices,lines,from.lerp(to,.56).add(0,.12,0),.12);
                    mark(matrices,lines,tip,.19);
                    mark(matrices,lines,tip.add(-ux*.34-uz*.27,0,-uz*.34+ux*.27),.12);
                    mark(matrices,lines,tip.add(-ux*.34+uz*.27,0,-uz*.34-ux*.27),.12);
                }
                drawn++;
            }
        } catch (IllegalStateException e) {
            unavailable = true;
            System.err.println("[SmartReturn] 3D arrows disabled for this session: " + e);
        } finally {
            matrices.pop();
            try { LINES.draw(); }
            catch (IllegalStateException e) {
                unavailable = true;
                System.err.println("[SmartReturn] Cannot flush 3D markers: " + e);
            }
        }
    }
    private static void mark(MatrixStack matrices, VertexConsumer lines, Vec3d p, double radius) {
        double y=p.y+.03;
        VertexRendering.drawBox(matrices.peek(),lines,
            p.x-radius,y,p.z-radius,p.x+radius,y+.07,p.z+radius,
            .27f,.96f,.75f,.88f);
    }
}

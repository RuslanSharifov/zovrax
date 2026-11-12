package az.zovrax.harvestpulse;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

/**
 * Translucent crop-area preview. Uses world-space near-camera tiles instead of
 * a massive 128x128 mesh, and follows uneven terrain via heightmap samples.
 * Azure indicates previously registered fields, green is a valid proposal,
 * red is oversized/overlapping/invalid. Only draws during field selection.
 */
public final class FieldSelectionRenderer {
    private static final int MAX_TILES = 720;
    private static final int RADIUS = 22;
    private FieldSelectionRenderer() { }

    public static void render(WorldRenderContext context) {
        MinecraftClient mc=MinecraftClient.getInstance();
        if(!HarvestPulseClient.isSelecting() || mc.player==null || mc.world==null || mc.options.hudHidden) return;
        VertexConsumerProvider provider=context.consumers();
        MatrixStack stack=context.matrices();
        if(provider==null || stack==null) return;
        Vec3d camera=mc.gameRenderer.getCamera().getPos();
        VertexConsumer fill=provider.getBuffer(RenderLayer.getDebugFilledBox());
        VertexConsumer outline=provider.getBuffer(RenderLayer.getLines());
        stack.push();
        try {
            stack.translate(-camera.x,-camera.y,-camera.z);
            int budget=MAX_TILES;
            String worldKey=FieldTracker.worldKey(mc);
            String dimension=FieldTracker.dimension(mc);
            for(HarvestPulseConfig.Field field:HarvestPulseClient.config().fields) {
                if(budget<=0) break;
                if(!worldKey.equals(field.worldKey)||!dimension.equals(field.dimension)) continue;
                budget=area(context,stack,fill,outline,mc,
                    Math.min(field.x1,field.x2),Math.min(field.y1,field.y2),Math.min(field.z1,field.z2),
                    Math.max(field.x1,field.x2),Math.max(field.y1,field.y2),Math.max(field.z1,field.z2),
                    0.22f,0.74f,1f,budget);
            }
            BlockPos first=HarvestPulseClient.firstCorner();
            BlockPos hover=null;
            if(mc.crosshairTarget instanceof BlockHitResult hit)
                hover=HarvestPulseClient.cropLevelPos(mc.world,hit.getBlockPos());
            if(first!=null) {
                BlockPos end=hover==null?first:hover;
                boolean allowed=HarvestPulseClient.validPreview(first,end,worldKey,dimension);
                area(context,stack,fill,outline,mc,
                    Math.min(first.getX(),end.getX()),Math.min(first.getY(),end.getY()),Math.min(first.getZ(),end.getZ()),
                    Math.max(first.getX(),end.getX()),Math.max(first.getY(),end.getY()),Math.max(first.getZ(),end.getZ()),
                    allowed?0.20f:1f, allowed?1f:0.21f, allowed?0.38f:0.22f,budget);
            } else if(hover!=null) {
                area(context,stack,fill,outline,mc,hover.getX(),hover.getY(),hover.getZ(),
                    hover.getX(),hover.getY(),hover.getZ(),0.22f,1f,0.38f,budget);
            }
        } finally { stack.pop(); }
    }

    private static int area(WorldRenderContext context,MatrixStack stack,
        VertexConsumer fill,VertexConsumer line,MinecraftClient mc,
        int minX,int minY,int minZ,int maxX,int maxY,int maxZ,
        float r,float g,float b,int remaining) {
        if(remaining<=0) return 0;
        int originX=mc.player.getBlockX(),originZ=mc.player.getBlockZ();
        int left=Math.max(minX,originX-RADIUS),right=Math.min(maxX,originX+RADIUS);
        int north=Math.max(minZ,originZ-RADIUS),south=Math.min(maxZ,originZ+RADIUS);
        if(left>right||north>south) return remaining;
        for(int x=left;x<=right && remaining>0;x++) for(int z=north;z<=south && remaining>0;z++) {
            int y=mc.world.getTopY(Heightmap.Type.WORLD_SURFACE,x,z);
            // Keep overlay over a selected farm's vertical area and avoid
            // drawing through the sky above unrelated tall constructions.
            y=Math.clamp(y,minY-1,maxY+2);
            double top=y+0.045;
            VertexRendering.drawFilledBox(stack,fill,x+0.06,top,z+0.06,
                x+0.94,top+0.018,z+0.94,r,g,b,0.19f);
            // Outline makes the chosen cells readable through crops and hills.
            if((x==minX||x==maxX||z==minZ||z==maxZ) ||
                ((x-left)%4==0 && (z-north)%4==0))
                VertexRendering.drawBox(stack.peek(),line,x+0.035,top,z+0.035,
                    x+0.965,top+0.12,z+0.965,r,g,b,0.78f);
            remaining--;
        }
        return remaining;
    }
}

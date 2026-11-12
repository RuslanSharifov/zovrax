package az.zovrax.harvestpulse;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

/**
 * Tinted transparent surfaces, clipped around the camera for frame-rate safety.
 * Green: valid selection. Red: exceeding limit/overlap. Cyan: saved field.
 * Each local tile follows the actual top surface so sloping farms remain visible.
 */
public final class PulseRegionRender {
    private static final int VIEW_RADIUS = 18;
    private static final int MAX_PREVIEW_TILES = 512;
    private static final int MAX_EXISTING_TILES = 160;
    private PulseRegionRender() { }

    public static void render(WorldRenderContext context) {
        MinecraftClient mc=MinecraftClient.getInstance();
        if (!HarvestPulseClient.selecting() || mc.player==null || mc.world==null) return;
        if (mc.currentScreen!=null || mc.options.hudHidden) return;
        MatrixStack matrices=context.matrices();
        VertexConsumerProvider providers=context.consumers();
        if (matrices==null || providers==null) return;
        VertexConsumer fills=providers.getBuffer(RenderLayer.getDebugFilledBox());
        VertexConsumer strokes=providers.getBuffer(RenderLayer.getLines());
        Vec3d camera=mc.player.getCameraPosVec(1.0f);
        matrices.push();
        matrices.translate(-camera.x,-camera.y,-camera.z);
        try {
            if(HarvestPulseClient.config().showExistingRegions) {
                int remaining=MAX_EXISTING_TILES;
                String dimension=FieldTracker.dimension(mc), world=FieldTracker.worldKey(mc);
                for (HarvestPulseConfig.Field field:HarvestPulseClient.config().fields) {
                    if(remaining<=0)break;
                    if(!field.worldKey.equals(world) || !field.dimension.equals(dimension))continue;
                    remaining-=paint(mc,matrices,fills,strokes,
                        Math.min(field.x1,field.x2),Math.max(field.x1,field.x2),
                        Math.min(field.z1,field.z2),Math.max(field.z1,field.z2),
                        0.20f,0.65f,1.00f,remaining);
                }
            }
            BlockPos first=HarvestPulseClient.firstCorner();
            if (first == null) {
                BlockPos hovered=HarvestPulseClient.hoverCorner(mc);
                if(hovered!=null)paint(mc,matrices,fills,strokes,
                    hovered.getX(),hovered.getX(),hovered.getZ(),hovered.getZ(),
                    1.0f,0.82f,0.31f,1);
                return;
            }
            BlockPos second=HarvestPulseClient.hoverCorner(mc);
            if(second==null)second=first;
            boolean valid=HarvestPulseClient.validSecond(mc,second);
            // Invalid dimensions cannot allocate large vertex buffers; render the
            // actual bounds only within the player's local view radius.
            paint(mc,matrices,fills,strokes,Math.min(first.getX(),second.getX()),
                Math.max(first.getX(),second.getX()),Math.min(first.getZ(),second.getZ()),
                Math.max(first.getZ(),second.getZ()),valid?0.23f:1.0f,
                valid?0.95f:0.23f,valid?0.48f:0.31f,MAX_PREVIEW_TILES);
        } finally { matrices.pop(); }
    }

    private static int paint(MinecraftClient mc,MatrixStack matrices,VertexConsumer fills,
                             VertexConsumer strokes,int minX,int maxX,int minZ,int maxZ,
                             float r,float g,float b,int budget) {
        int px=mc.player.getBlockX(),pz=mc.player.getBlockZ();
        int bx1=Math.max(minX,px-VIEW_RADIUS),bx2=Math.min(maxX,px+VIEW_RADIUS);
        int bz1=Math.max(minZ,pz-VIEW_RADIUS),bz2=Math.min(maxZ,pz+VIEW_RADIUS);
        int count=0;
        for(int x=bx1;x<=bx2 && count<budget;x++) {
            for(int z=bz1;z<=bz2 && count<budget;z++) {
                // No render-driven chunk loading. Only draw visible local terrain.
                if (!mc.world.isChunkLoaded(x>>4,z>>4))continue;
                int top=mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z);
                double y=top+0.025;
                VertexRendering.drawFilledBox(matrices,fills,
                    x+0.03,y,z+0.03,x+0.97,y+0.035,z+0.97,r,g,b,0.23f);
                if(x==minX || x==maxX || z==minZ || z==maxZ) {
                    VertexRendering.drawBox(matrices.peek(),strokes,
                        x+0.015,y-0.015,z+0.015,x+0.985,y+0.07,z+0.985,r,g,b,0.88f);
                }
                count++;
            }
        }
        return count;
    }
}

package az.zovrax.chromaoutline.mixin;

import az.zovrax.chromaoutline.ChromaOutlineClient;
import az.zovrax.chromaoutline.ChromaOutlineConfig;
import az.zovrax.chromaoutline.OutlinePalette;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the vanilla selected-block outline without touching unrelated world rendering. */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void zovrax$drawChromaOutline(MatrixStack matrices, VertexConsumer consumer,
            double cameraX, double cameraY, double cameraZ, OutlineRenderState state, int vanillaColor,
            CallbackInfo ci) {
        ChromaOutlineConfig config = ChromaOutlineClient.config();
        if (!config.enabled) return; // leave vanilla untouched
        ci.cancel();
        if (config.opacity == 0 || state == null) return;

        VoxelShape shape = config.fullCube ? VoxelShapes.fullCube() : state.shape();
        if (shape == null || shape.isEmpty()) return;

        double x = state.pos().getX() - cameraX;
        double y = state.pos().getY() - cameraY;
        double z = state.pos().getZ() - cameraZ;
        int color = OutlinePalette.resolve(config, System.nanoTime());
        double expansion = config.expansion * 0.001;

        if (expansion > 0) {
            // Expanded bounding boxes are intentional: avoid unsafe direct shape manipulation.
            var box = shape.getBoundingBox().expand(expansion);
            shape = VoxelShapes.cuboid(box);
        }
        VertexRendering.drawOutline(matrices, consumer, shape, x, y, z, color);
        // 1.21.10 does not expose a pixel-width argument. Parallel sub-milliblock
        // outline passes produce a visible weight control while preserving the line buffer.
        for (int i = 1; i < config.outlineWidth; i++) {
            double d = i * 0.0008;
            VertexRendering.drawOutline(matrices, consumer, shape, x + d, y, z, color);
            VertexRendering.drawOutline(matrices, consumer, shape, x - d, y, z, color);
            VertexRendering.drawOutline(matrices, consumer, shape, x, y + d, z, color);
            VertexRendering.drawOutline(matrices, consumer, shape, x, y - d, z, color);
            VertexRendering.drawOutline(matrices, consumer, shape, x, y, z + d, color);
            VertexRendering.drawOutline(matrices, consumer, shape, x, y, z - d, color);
        }
    }
}

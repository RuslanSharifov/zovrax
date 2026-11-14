package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

/**
 * Chunk compilers can read block states from cached snapshots without going
 * through RenderSectionRegion#getBlockState. Prevent the actual static door
 * quads from being submitted even on that path.
 *
 * Only affects vanilla chunk compilation, never items, entities, server state
 * or collision detection. The animated replacement still waits for mesh upload.
 */
@Mixin(ModelBlockRenderer.class)
public abstract class BlockModelRendererMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void zovrax$skipStaticDoor(BlockAndTintGetter view, List<BlockModelPart> parts,
            BlockState state, BlockPos pos, PoseStack matrices, VertexConsumer consumer,
            boolean cull, int overlay, CallbackInfo ci) {
        if (!(view instanceof RenderSectionRegion)) return;
        if (!(state.getBlock() instanceof DoorBlock)
                && !(state.getBlock() instanceof TrapDoorBlock)) return;
        if (MotionTracker.hidden(pos)) {
            MotionTracker.modelSuppressed(pos);
            ci.cancel();
        }
    }
}

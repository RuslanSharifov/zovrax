package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Secondary guard for renderers that bypass the chunk-region blockstate lookup. */
@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void zovrax$skipStatic(BlockQuadOutput output, float x, float y, float z,
                                    BlockAndTintGetter view, BlockPos pos, BlockState state,
                                    BlockStateModel model, long seed, CallbackInfo ci) {
        if (MotionTracker.hidden(pos)) ci.cancel();
    }
}

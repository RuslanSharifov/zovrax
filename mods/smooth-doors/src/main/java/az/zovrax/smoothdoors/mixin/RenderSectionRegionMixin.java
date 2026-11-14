package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hide the real block model only in the compiled rendering mesh, never in world/collision. */
@Mixin(RenderSectionRegion.class)
public abstract class RenderSectionRegionMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void zovrax$hideDoorInSection(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        boolean hidden = MotionTracker.hidden(pos);
        MotionTracker.regionRead(pos, hidden);
        if (hidden) cir.setReturnValue(Blocks.AIR.defaultBlockState());
    }
}

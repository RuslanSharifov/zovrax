package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A chunk's old, visible mesh stays on the GPU until replacement upload.
 * Only activate the moving model when that replacement is actually installed.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class SectionMeshMixin {
    @Shadow
    public abstract BlockPos getRenderOrigin();

    @Inject(method = "setSectionMesh", at = @At("TAIL"))
    private void zovrax$meshInstalled(SectionMesh mesh, CallbackInfo ci) {
        MotionTracker.meshUploaded(getRenderOrigin());
    }
}

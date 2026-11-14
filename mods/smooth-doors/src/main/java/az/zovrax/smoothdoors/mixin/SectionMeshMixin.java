package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observe vanilla section mesh uploads. Some external renderers replace the
 * method entirely; this optional hook must not crash the game in that case.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class SectionMeshMixin {
    @Inject(method = "setSectionMesh", at = @At("TAIL"), require = 0)
    private void zovrax$meshInstalled(SectionMesh mesh, CallbackInfo ci) {
        SectionRenderDispatcher.RenderSection self =
                (SectionRenderDispatcher.RenderSection) (Object) this;
        MotionTracker.meshUploaded(self.getRenderOrigin());
    }
}

package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Submits transformed vanilla baked quads during the 1.21.10 render extraction pass. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void zovrax$submitMotion(LevelRenderState worldState, SubmitNodeCollector collector,
                                     boolean outline, CallbackInfo ci) {
        MotionRenderer.submit(new PoseStack(), collector, worldState.cameraRenderState.pos);
    }
}

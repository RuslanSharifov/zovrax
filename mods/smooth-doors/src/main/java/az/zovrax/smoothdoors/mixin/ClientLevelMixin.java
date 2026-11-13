package az.zovrax.smoothdoors.mixin;

import az.zovrax.smoothdoors.MotionTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayDeque;
import java.util.Deque;

/** Observes both local and server-confirmed state changes, without intercepting gameplay. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Unique
    private final Deque<Observed> zovrax$changes = new ArrayDeque<>();

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void zovrax$before(BlockPos pos, BlockState state, int flags, int recursion,
                               CallbackInfoReturnable<Boolean> cir) {
        ClientLevel level = (ClientLevel) (Object) this;
        zovrax$changes.push(new Observed(pos.immutable(), level.getBlockState(pos)));
    }

    @Inject(method = "setBlock", at = @At("RETURN"))
    private void zovrax$after(BlockPos pos, BlockState state, int flags, int recursion,
                              CallbackInfoReturnable<Boolean> cir) {
        Observed before = zovrax$changes.pop();
        if (cir.getReturnValueZ()) {
            ClientLevel level = (ClientLevel) (Object) this;
            MotionTracker.changed(level, before.pos(), before.state(), level.getBlockState(before.pos()));
        }
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("HEAD"), require = 0)
    private void zovrax$verifiedBefore(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        zovrax$changes.push(new Observed(pos.immutable(), level.getBlockState(pos)));
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("RETURN"), require = 0)
    private void zovrax$verifiedAfter(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        Observed before = zovrax$changes.pop();
        ClientLevel level = (ClientLevel) (Object) this;
        MotionTracker.changed(level, before.pos(), before.state(), level.getBlockState(before.pos()));
    }

    @Unique
    private record Observed(BlockPos pos, BlockState state) {}
}

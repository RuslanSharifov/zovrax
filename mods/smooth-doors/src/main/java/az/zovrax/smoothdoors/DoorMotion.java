package az.zovrax.smoothdoors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Immutable transition endpoints; revealed is set after chunk rebuilding is requested. */
public final class DoorMotion {
    public final BlockPos basePos;
    public final BlockState closedModelState;
    public final boolean door;
    public final float from;
    public final float target;
    public final long startedNs;
    public final long durationNs;
    public boolean revealed;

    public DoorMotion(BlockPos basePos, BlockState closedModelState, boolean door,
                      float from, float target, long startedNs, long durationNs) {
        this.basePos = basePos.immutable();
        this.closedModelState = closedModelState;
        this.door = door;
        this.from = from;
        this.target = target;
        this.startedNs = startedNs;
        this.durationNs = durationNs;
    }

    public float openness(long nowNs) {
        return AnimationCurve.position(from, target, nowNs - startedNs, durationNs);
    }

    public long endNs() {
        return startedNs + durationNs;
    }
}

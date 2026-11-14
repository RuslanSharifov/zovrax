package az.zovrax.smoothdoors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One logical door/trapdoor transition. The section's GPU mesh swap is the
 * boundary between the static original and the animated replacement.
 */
public final class DoorMotion {
    public enum Phase {
        WAIT_HIDE,       // Original chunk mesh still on the GPU: do not draw a duplicate.
        ANIMATING,       // Hidden chunk mesh uploaded: draw only the moving model.
        WAIT_REVEAL      // At destination; keep drawing until vanilla mesh is uploaded.
    }

    public final BlockPos basePos;
    public final BlockState closedModelState;
    public final boolean door;
    public final float from;
    public final float target;
    public final long durationNs;
    public final long createdNs;

    public Phase phase;
    public long startedNs;
    public long phaseStartedNs;
    public int hideObserved;
    public int hideUploaded;
    public int revealObserved;
    public int revealUploaded;

    public DoorMotion(BlockPos basePos, BlockState closedModelState, boolean door,
                      float from, float target, long now, long durationNs, boolean alreadyHidden) {
        this.basePos = basePos.immutable();
        this.closedModelState = closedModelState;
        this.door = door;
        this.from = from;
        this.target = target;
        this.createdNs = now;
        this.durationNs = durationNs;
        this.phase = alreadyHidden ? Phase.ANIMATING : Phase.WAIT_HIDE;
        this.phaseStartedNs = now;
        this.startedNs = alreadyHidden ? now : 0L;
    }

    public int requiredMask() {
        return door && ((basePos.getY() & 15) == 15) ? 3 : 1;
    }

    /** 1=base section, 2=upper section when it crosses the 16-block boundary. */
    public int maskForSection(int sx, int sy, int sz) {
        if ((basePos.getX() >> 4) != sx || (basePos.getZ() >> 4) != sz) return 0;
        if ((basePos.getY() >> 4) == sy) return 1;
        return door && ((basePos.getY() & 15) == 15) && ((basePos.getY() + 1) >> 4) == sy ? 2 : 0;
    }

    /** Which section contains this door's own block position (not a neighbor). */
    public int maskForPosition(BlockPos pos) {
        if (basePos.equals(pos)) return 1;
        if (door && basePos.above().equals(pos))
            return ((basePos.getY() & 15) == 15) ? 2 : 1;
        return 0;
    }

    public float openness(long now) {
        if (phase == Phase.WAIT_HIDE) return from;
        if (phase == Phase.WAIT_REVEAL) return target;
        return AnimationCurve.position(from, target, now - startedNs, durationNs);
    }

    public void activate(long now) {
        phase = Phase.ANIMATING;
        phaseStartedNs = now;
        startedNs = now;
    }

    public void beginReveal(long now) {
        phase = Phase.WAIT_REVEAL;
        phaseStartedNs = now;
    }
}

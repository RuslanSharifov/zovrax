package az.zovrax.smoothdoors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controls handoff from the real compiled chunk geometry to a moving model.
 *
 * Never draw the moving model until the chunk mesh that omits the static door
 * has been uploaded. Keep the moving model at its final pose until the normal
 * chunk mesh has been uploaded again. This removes both overlapping doors and
 * holes during asynchronous rebuilds.
 *
 * RenderSectionRegion is accessed by chunk-builder worker threads; all state
 * mutations except HIDDEN.contains use the same lock.
 */
public final class MotionTracker {
    private static final Map<Long, DoorMotion> ACTIVE = new HashMap<>();
    private static final Set<Long> HIDDEN = ConcurrentHashMap.newKeySet();
    private static final Set<Long> TRACKED = ConcurrentHashMap.newKeySet();
    private static final long HIDE_TIMEOUT_NS = 5_000_000_000L;
    private static final long REVEAL_TIMEOUT_NS = 8_000_000_000L;
    private static ClientLevel world;

    private MotionTracker() {}

    private static void setWorld(ClientLevel level) {
        if (world == level) return;
        ACTIVE.clear();
        HIDDEN.clear();
        TRACKED.clear();
        world = level;
    }

    public static boolean hidden(BlockPos pos) {
        return HIDDEN.contains(pos.asLong());
    }

    /** Lock-free hot-path filter: almost all chunk blocks are not animating. */
    public static boolean tracked(BlockPos pos) {
        return TRACKED.contains(pos.asLong());
    }

    public static synchronized List<DoorMotion> snapshot() {
        return new ArrayList<>(ACTIVE.values());
    }

    public static synchronized void changed(ClientLevel level, BlockPos pos,
                                             BlockState previous, BlockState next) {
        setWorld(level);
        if (previous == next) return;
        boolean door = previous.getBlock() instanceof DoorBlock;
        boolean trapdoor = previous.getBlock() instanceof TrapDoorBlock;
        if (previous.getBlock() != next.getBlock() || (!door && !trapdoor)) {
            cancelContaining(pos);
            return;
        }
        if (!previous.hasProperty(BlockStateProperties.OPEN)
                || !next.hasProperty(BlockStateProperties.OPEN)) return;
        boolean wasOpen = previous.getValue(BlockStateProperties.OPEN);
        boolean isOpen = next.getValue(BlockStateProperties.OPEN);
        if (wasOpen == isOpen) return;

        BlockPos anchor = door && next.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? pos.below() : pos;
        long key = anchor.asLong();
        MotionConfig config = SmoothDoorsClient.config();
        if (!config.enabled || (door && !config.doors) || (trapdoor && !config.trapdoors)) {
            cancel(key);
            return;
        }

        long now = System.nanoTime();
        DoorMotion prior = ACTIVE.get(key);
        float target = isOpen ? 1f : 0f;

        // Both halves of a tall door can produce events for the same interaction.
        if (prior != null && prior.target == target && prior.door == door) return;

        float from = prior == null ? (wasOpen ? 1f : 0f) : prior.openness(now);
        boolean alreadyHidden = prior != null && prior.phase != DoorMotion.Phase.WAIT_HIDE;
        int millis = isOpen ? config.openingMs : config.closingMs;
        DoorMotion motion = new DoorMotion(anchor,
                next.setValue(BlockStateProperties.OPEN, false), door,
                from, target, now, AnimationCurve.durationNs(millis, from, target),
                alreadyHidden);
        ACTIVE.put(key, motion);

        for (BlockPos affected : positions(motion)) {
            HIDDEN.add(affected.asLong());
            TRACKED.add(affected.asLong());
            dirty(affected);
        }
    }

    /**
     * The chunk compiler has read this particular position. A callback from
     * the GPU mesh upload will confirm when that version becomes visible.
     */
    public static synchronized void regionRead(BlockPos pos, boolean isHidden) {
        DoorMotion motion = ACTIVE.get(pos.asLong());
        if (motion == null) motion = ACTIVE.get(pos.below().asLong());
        if (motion == null) return;
        int mask = motion.maskForPosition(pos);
        if (mask == 0) return;
        if (isHidden && motion.phase == DoorMotion.Phase.WAIT_HIDE) {
            motion.hideObserved |= mask;
        } else if (!isHidden && motion.phase == DoorMotion.Phase.WAIT_REVEAL) {
            motion.revealObserved |= mask;
        }
    }

    /**
     * Invoked after SectionRenderDispatcher installs an uploaded section mesh.
     * This is later than its compile and later than its region blockstate reads.
     */
    public static synchronized void meshUploaded(BlockPos sectionOrigin) {
        int sx = sectionOrigin.getX() >> 4;
        int sy = sectionOrigin.getY() >> 4;
        int sz = sectionOrigin.getZ() >> 4;
        long now = System.nanoTime();
        Iterator<DoorMotion> cursor = ACTIVE.values().iterator();
        while (cursor.hasNext()) {
            DoorMotion motion = cursor.next();
            int relevantSection = motion.maskForSection(sx, sy, sz);
            if (relevantSection == 0) continue;
            if (motion.phase == DoorMotion.Phase.WAIT_HIDE
                    && (motion.hideObserved & relevantSection) == relevantSection) {
                motion.hideUploaded |= relevantSection;
                if ((motion.hideUploaded & motion.requiredMask()) == motion.requiredMask()) {
                    motion.activate(now);
                }
            } else if (motion.phase == DoorMotion.Phase.WAIT_REVEAL
                    && (motion.revealObserved & relevantSection) == relevantSection) {
                motion.revealUploaded |= relevantSection;
                if ((motion.revealUploaded & motion.requiredMask()) == motion.requiredMask()) {
                    untrack(motion);
                    cursor.remove();
                }
            }
        }
    }

    public static synchronized void tick(ClientLevel level) {
        setWorld(level);
        if (level == null || ACTIVE.isEmpty()) return;
        MotionConfig config = SmoothDoorsClient.config();
        long now = System.nanoTime();
        Iterator<DoorMotion> cursor = ACTIVE.values().iterator();
        while (cursor.hasNext()) {
            DoorMotion motion = cursor.next();
            BlockState current = level.getBlockState(motion.basePos);
            boolean disabled = !config.enabled || (motion.door && !config.doors)
                    || (!motion.door && !config.trapdoors);
            if (disabled || current.getBlock() != motion.closedModelState.getBlock()) {
                unhideAndDirty(motion);
                untrack(motion);
                cursor.remove();
                continue;
            }
            if (motion.phase == DoorMotion.Phase.WAIT_HIDE
                    && now - motion.phaseStartedNs > HIDE_TIMEOUT_NS) {
                // A missing upload callback must never leave an invisible door.
                unhideAndDirty(motion);
                cursor.remove();
            } else if (motion.phase == DoorMotion.Phase.ANIMATING
                    && now - motion.startedNs >= motion.durationNs) {
                motion.beginReveal(now);
                unhideAndDirty(motion);
            } else if (motion.phase == DoorMotion.Phase.WAIT_REVEAL
                    && now - motion.phaseStartedNs > REVEAL_TIMEOUT_NS) {
                // Recovery if renderer is replaced or this chunk is unloaded.
                untrack(motion);
                cursor.remove();
            }
        }
    }

    private static List<BlockPos> positions(DoorMotion motion) {
        return motion.door ? List.of(motion.basePos, motion.basePos.above()) : List.of(motion.basePos);
    }

    private static void untrack(DoorMotion motion) {
        for (BlockPos pos : positions(motion)) TRACKED.remove(pos.asLong());
    }

    private static void unhideAndDirty(DoorMotion motion) {
        for (BlockPos pos : positions(motion)) {
            HIDDEN.remove(pos.asLong());
            dirty(pos);
        }
    }

    private static void cancel(long key) {
        DoorMotion motion = ACTIVE.remove(key);
        if (motion != null) {
            unhideAndDirty(motion);
            untrack(motion);
        }
    }

    private static void cancelContaining(BlockPos pos) {
        for (DoorMotion motion : new ArrayList<>(ACTIVE.values())) {
            if (motion.maskForPosition(pos) != 0) cancel(motion.basePos.asLong());
        }
    }

    private static void dirty(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.levelRenderer != null) {
            client.levelRenderer.setSectionDirty(pos.getX() >> 4,
                    pos.getY() >> 4, pos.getZ() >> 4);
        }
    }
}

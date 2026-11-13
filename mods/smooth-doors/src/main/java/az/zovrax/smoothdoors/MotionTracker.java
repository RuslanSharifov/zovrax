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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tracks visible transitions only; no world scan and no server-side mutations. */
public final class MotionTracker {
    private static final Map<Long, DoorMotion> ACTIVE = new HashMap<>();
    private static final Set<Long> HIDDEN = new HashSet<>();
    private static final long REBUILD_GRACE_NS = 170_000_000L;
    private static ClientLevel world;

    private MotionTracker() {}

    private static void setWorld(ClientLevel level) {
        if (world == level) return;
        ACTIVE.clear();
        HIDDEN.clear();
        world = level;
    }

    public static boolean hidden(BlockPos pos) {
        return HIDDEN.contains(pos.asLong());
    }

    public static List<DoorMotion> snapshot() {
        return new ArrayList<>(ACTIVE.values());
    }

    public static void changed(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        setWorld(level);
        if (oldState == newState) return;
        boolean door = oldState.getBlock() instanceof DoorBlock;
        boolean trapdoor = oldState.getBlock() instanceof TrapDoorBlock;
        if (oldState.getBlock() != newState.getBlock() || (!door && !trapdoor)) {
            cancelContaining(pos);
            return;
        }
        if (!oldState.hasProperty(BlockStateProperties.OPEN)
                || !newState.hasProperty(BlockStateProperties.OPEN)) return;
        boolean oldOpen = oldState.getValue(BlockStateProperties.OPEN);
        boolean newOpen = newState.getValue(BlockStateProperties.OPEN);
        if (oldOpen == newOpen) return;

        BlockPos anchor = door && newState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? pos.below() : pos;
        long key = anchor.asLong();
        MotionConfig config = SmoothDoorsClient.config();
        if (!config.enabled || (door && !config.doors) || (trapdoor && !config.trapdoors)) {
            cancel(key);
            return;
        }

        long now = System.nanoTime();
        DoorMotion previous = ACTIVE.get(key);
        float from = previous == null ? (oldOpen ? 1f : 0f) : previous.openness(now);
        float target = newOpen ? 1f : 0f;
        int durationMs = newOpen ? config.openingMs : config.closingMs;
        DoorMotion next = new DoorMotion(anchor, newState.setValue(BlockStateProperties.OPEN, false),
                door, from, target, now, AnimationCurve.durationNs(durationMs, from, target));
        ACTIVE.put(key, next);
        hide(next);
    }

    public static void tick(ClientLevel level) {
        setWorld(level);
        if (level == null || ACTIVE.isEmpty()) return;
        MotionConfig config = SmoothDoorsClient.config();
        if (!config.enabled) {
            for (DoorMotion motion : snapshot()) reveal(motion);
            ACTIVE.clear();
            return;
        }

        long now = System.nanoTime();
        Iterator<Map.Entry<Long, DoorMotion>> cursor = ACTIVE.entrySet().iterator();
        while (cursor.hasNext()) {
            DoorMotion motion = cursor.next().getValue();
            BlockState state = level.getBlockState(motion.basePos);
            if (state.getBlock() != motion.closedModelState.getBlock()
                    || (motion.door && !config.doors)
                    || (!motion.door && !config.trapdoors)) {
                reveal(motion);
                cursor.remove();
                continue;
            }
            if (now >= motion.endNs() && !motion.revealed) {
                reveal(motion);
                motion.revealed = true;
            }
            if (now >= motion.endNs() + REBUILD_GRACE_NS) cursor.remove();
        }
    }

    private static List<BlockPos> positions(DoorMotion motion) {
        return motion.door ? List.of(motion.basePos, motion.basePos.above()) : List.of(motion.basePos);
    }

    private static void hide(DoorMotion motion) {
        for (BlockPos at : positions(motion)) {
            HIDDEN.add(at.asLong());
            dirty(at);
        }
    }

    private static void reveal(DoorMotion motion) {
        for (BlockPos at : positions(motion)) {
            HIDDEN.remove(at.asLong());
            dirty(at);
        }
    }

    private static void cancel(long key) {
        DoorMotion motion = ACTIVE.remove(key);
        if (motion != null) reveal(motion);
    }

    private static void cancelContaining(BlockPos pos) {
        for (DoorMotion motion : snapshot()) {
            if (motion.basePos.equals(pos) || (motion.door && motion.basePos.above().equals(pos))) {
                cancel(motion.basePos.asLong());
            }
        }
    }

    private static void dirty(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.levelExtractor != null) {
            client.levelExtractor.blockChanged(pos, 3);
        }
    }
}

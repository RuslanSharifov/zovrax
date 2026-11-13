package az.zovrax.smoothdoors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Render the *closed* vanilla baked model rotated around its physical hinge.
 * Draw using Fabric's 1.21.10 world render event in the extraction pass.
 */
public final class MotionRenderer {
    private static final float NEAR = 1.5f / 16f;
    private static final float FAR = 1f - NEAR;
    private MotionRenderer() {}

    public static void draw(WorldRenderContext context) {
        Minecraft game = Minecraft.getInstance();
        ClientLevel level = game.level;
        if (level == null || !SmoothDoorsClient.config().enabled) return;
        Vec3 camera = game.gameRenderer.getMainCamera().getPosition();
        PoseStack pose = new PoseStack();
        long now = System.nanoTime();
        for (DoorMotion motion : MotionTracker.snapshot()) {
            if (motion.revealed) continue;
            float angle = motion.openness(now) * ((float) Math.PI / 2f);
            renderOne(context, pose, level, camera, motion, motion.basePos, false, angle);
            if (motion.door) {
                renderOne(context, pose, level, camera, motion, motion.basePos.above(), true, angle);
            }
        }
    }

    private static void renderOne(WorldRenderContext context, PoseStack pose,
                                  ClientLevel level, Vec3 camera, DoorMotion motion,
                                  BlockPos pos, boolean upper, float angle) {
        BlockState state = motion.closedModelState;
        if (level.getBlockState(pos).getBlock() != state.getBlock()) return;
        if (motion.door) {
            state = state.setValue(DoorBlock.HALF, upper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
        }
        Matrix4f transform = motion.door ? rotateDoor(state, angle) : rotateTrapdoor(state, angle);
        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        pose.mulPose(transform);
        int light = 15728880;
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, pose, context.consumers(), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private static Matrix4f rotateDoor(BlockState state, float angle) {
        Direction facing = state.getValue(DoorBlock.FACING);
        boolean left = state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
        float x = switch (facing) {
            case EAST -> NEAR;
            case WEST -> FAR;
            case NORTH -> left ? NEAR : FAR;
            case SOUTH -> left ? FAR : NEAR;
            default -> 0f;
        };
        float z = switch (facing) {
            case NORTH -> FAR;
            case SOUTH -> NEAR;
            case EAST -> left ? NEAR : FAR;
            case WEST -> left ? FAR : NEAR;
            default -> 0f;
        };
        return rotateY(x, z, left ? angle : -angle);
    }

    private static Matrix4f rotateTrapdoor(BlockState state, float angle) {
        Direction facing = state.getValue(TrapDoorBlock.FACING);
        boolean top = state.getValue(TrapDoorBlock.HALF) == Half.TOP;
        float y = top ? FAR : NEAR;
        return switch (facing) {
            case NORTH -> rotateX(y, FAR, top ? -angle : angle);
            case SOUTH -> rotateX(y, NEAR, top ? angle : -angle);
            case WEST -> rotateZ(FAR, y, top ? angle : -angle);
            case EAST -> rotateZ(NEAR, y, top ? -angle : angle);
            default -> new Matrix4f();
        };
    }

    private static Matrix4f rotateY(float x, float z, float angle) {
        return new Matrix4f().translate(x, 0, z).rotateY(angle).translate(-x, 0, -z);
    }
    private static Matrix4f rotateX(float y, float z, float angle) {
        return new Matrix4f().translate(0, y, z).rotateX(angle).translate(0, -y, -z);
    }
    private static Matrix4f rotateZ(float x, float y, float angle) {
        return new Matrix4f().translate(x, y, 0).rotateZ(angle).translate(-x, -y, 0);
    }
}

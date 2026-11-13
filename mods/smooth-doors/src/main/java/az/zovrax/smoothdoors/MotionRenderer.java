package az.zovrax.smoothdoors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the original baked closed-state model around its real hinge.
 * Only transitions are submitted, avoiding per-frame updates to every door.
 */
public final class MotionRenderer {
    private static final float PIVOT_NEAR = 1.5f / 16f;
    private static final float PIVOT_FAR = 1f - PIVOT_NEAR;

    private MotionRenderer() {}

    public static void submit(PoseStack poses, SubmitNodeCollector collector, Vec3 cameraPosition) {
        Minecraft game = Minecraft.getInstance();
        ClientLevel world = game.level;
        if (world == null) return;
        BlockStateModelSet models = game.getModelManager().getBlockStateModelSet();
        long now = System.nanoTime();
        for (DoorMotion motion : MotionTracker.snapshot()) {
            float angle = motion.openness(now) * ((float) Math.PI / 2f);
            renderAt(world, models, poses, collector, cameraPosition, motion,
                    motion.basePos, false, angle);
            if (motion.door) {
                renderAt(world, models, poses, collector, cameraPosition, motion,
                        motion.basePos.above(), true, angle);
            }
        }
    }

    private static void renderAt(ClientLevel world, BlockStateModelSet models,
                                 PoseStack poses, SubmitNodeCollector collector, Vec3 camera,
                                 DoorMotion motion, BlockPos pos, boolean upper, float angle) {
        if (world.getBlockState(pos).getBlock() != motion.closedModelState.getBlock()) return;
        BlockState closed = motion.closedModelState;
        if (motion.door) {
            closed = closed.setValue(DoorBlock.HALF,
                    upper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
        }
        BlockStateModel model = models.get(closed);
        BlockModelRenderState renderState = new BlockModelRenderState();
        List<BlockStateModelPart> parts = renderState.setupModel(
                new Matrix4f(), model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
        model.collectParts(renderState.scratchRandomSource(closed.getSeed(pos)), parts);

        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) quads.addAll(part.getQuads(direction));
            quads.addAll(part.getQuads(null));
        }
        if (quads.isEmpty()) return;

        Matrix4f rotation = motion.door ? doorRotation(closed, angle) : trapdoorRotation(closed, angle);
        RenderType layer = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT)
                ? RenderTypes.translucentMovingBlock() : RenderTypes.cutoutMovingBlock();
        int light = LightCoordsUtil.getLightCoords(world, pos);
        poses.pushPose();
        poses.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        poses.mulPose(rotation);
        collector.submitCustomGeometry(poses, layer,
                (pose, consumer) -> drawQuads(consumer, pose, quads, light));
        poses.popPose();
    }

    private static void drawQuads(VertexConsumer consumer, PoseStack.Pose pose,
                                  List<BakedQuad> quads, int light) {
        QuadInstance info = new QuadInstance();
        info.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        info.setLightCoords(light);
        Vector3f normal = new Vector3f();
        for (BakedQuad quad : quads) {
            pose.transformNormal(quad.direction().getUnitVec3f(), normal).normalize();
            float shade = 0.65f + 0.35f * Math.abs(normal.y());
            info.setColor(ARGB.colorFromFloat(1f, shade, shade, shade));
            consumer.putBakedQuad(pose, quad, info);
        }
    }

    private static Matrix4f doorRotation(BlockState state, float angle) {
        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        float turn = angle * (hinge == DoorHingeSide.LEFT ? 1f : -1f);
        float x = switch (facing) {
            case EAST -> PIVOT_NEAR;
            case WEST -> PIVOT_FAR;
            case NORTH -> hinge == DoorHingeSide.LEFT ? PIVOT_NEAR : PIVOT_FAR;
            case SOUTH -> hinge == DoorHingeSide.LEFT ? PIVOT_FAR : PIVOT_NEAR;
            default -> 0f;
        };
        float z = switch (facing) {
            case NORTH -> PIVOT_FAR;
            case SOUTH -> PIVOT_NEAR;
            case EAST -> hinge == DoorHingeSide.LEFT ? PIVOT_NEAR : PIVOT_FAR;
            case WEST -> hinge == DoorHingeSide.LEFT ? PIVOT_FAR : PIVOT_NEAR;
            default -> 0f;
        };
        return rotateY(x, z, turn);
    }

    private static Matrix4f trapdoorRotation(BlockState state, float angle) {
        Direction facing = state.getValue(TrapDoorBlock.FACING);
        boolean top = state.getValue(TrapDoorBlock.HALF) == Half.TOP;
        float y = top ? PIVOT_FAR : PIVOT_NEAR;
        return switch (facing) {
            case NORTH -> rotateX(y, PIVOT_FAR, top ? -angle : angle);
            case SOUTH -> rotateX(y, PIVOT_NEAR, top ? angle : -angle);
            case WEST -> rotateZ(PIVOT_FAR, y, top ? angle : -angle);
            case EAST -> rotateZ(PIVOT_NEAR, y, top ? -angle : angle);
            default -> new Matrix4f();
        };
    }

    private static Matrix4f rotateY(float x, float z, float angle) {
        return new Matrix4f().translate(x, 0f, z).rotateY(angle).translate(-x, 0f, -z);
    }
    private static Matrix4f rotateX(float y, float z, float angle) {
        return new Matrix4f().translate(0f, y, z).rotateX(angle).translate(0f, -y, -z);
    }
    private static Matrix4f rotateZ(float x, float y, float angle) {
        return new Matrix4f().translate(x, y, 0f).rotateZ(angle).translate(-x, -y, 0f);
    }
}

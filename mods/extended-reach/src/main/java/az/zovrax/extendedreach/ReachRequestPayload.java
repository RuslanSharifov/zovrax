package az.zovrax.extendedreach;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Small C2S request; range values must always be validated server-side. */
public record ReachRequestPayload(int blocks) implements CustomPayload {
    public static final Id<ReachRequestPayload> ID =
        new Id<>(Identifier.of(ExtendedReachMod.ID, "set_block_reach"));
    public static final PacketCodec<RegistryByteBuf, ReachRequestPayload> CODEC =
        PacketCodec.tuple(PacketCodecs.VAR_INT, ReachRequestPayload::blocks, ReachRequestPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

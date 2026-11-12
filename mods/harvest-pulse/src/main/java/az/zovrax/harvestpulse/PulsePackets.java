package az.zovrax.harvestpulse;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Bounded JSON envelopes. Never trust a client-supplied crop age or counter. */
public final class PulsePackets {
    private PulsePackets() { }

    public record Action(String json) implements CustomPayload {
        public static final Id<Action> ID = new Id<>(Identifier.of("zovrax_harvestpulse", "action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC =
            PacketCodec.tuple(PacketCodecs.string(2048), Action::json, Action::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Snapshot(String json) implements CustomPayload {
        public static final Id<Snapshot> ID = new Id<>(Identifier.of("zovrax_harvestpulse", "snapshot"));
        public static final PacketCodec<RegistryByteBuf, Snapshot> CODEC =
            PacketCodec.tuple(PacketCodecs.string(8192), Snapshot::json, Snapshot::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
}

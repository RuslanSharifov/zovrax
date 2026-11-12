package az.zovrax.extendedreach;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

/** Authoritative block reach changes. Vanilla synchronizes this player attribute. */
public final class ExtendedReachMod implements ModInitializer {
    public static final String ID = "zovrax_extended_reach";
    // Only online players are tracked; the client resends its persisted choice on reconnect.
    private static final Map<UUID, Integer> REACH_BY_PLAYER = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(ReachRequestPayload.ID, ReachRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ReachRequestPayload.ID, (payload, context) -> {
            final ServerPlayerEntity player = context.player();
            int allowed = ReachLimits.clamp(payload.blocks());
            REACH_BY_PLAYER.put(player.getUuid(), allowed);
            applyReach(player, allowed);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Integer reach = REACH_BY_PLAYER.get(newPlayer.getUuid());
            if (reach != null) {
                applyReach(newPlayer, reach);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            REACH_BY_PLAYER.remove(handler.player.getUuid()));
    }

    private static void applyReach(ServerPlayerEntity player, int blocks) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (instance != null) {
            // Authoritative server attribute: controls both breaking and placing validation.
            instance.setBaseValue(ReachLimits.clamp(blocks));
        }
    }
}

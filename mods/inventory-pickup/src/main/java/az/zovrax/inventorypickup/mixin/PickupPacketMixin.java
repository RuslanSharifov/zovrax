package az.zovrax.inventorypickup.mixin;

import az.zovrax.inventorypickup.PickupTracker;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Read-only hook into vanilla's actual item/orb collection animation packet. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class PickupPacketMixin {
    @Inject(method = "onItemPickupAnimation", at = @At("HEAD"))
    private void zovrax$onPickup(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        PickupTracker.onPickupAnimation(packet);
    }
}

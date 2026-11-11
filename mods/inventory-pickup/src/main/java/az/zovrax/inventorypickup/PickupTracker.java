package az.zovrax.inventorypickup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;

/**
 * Ground-pickup events only. Never infers pickups from inventory slot changes,
 * item component changes, crafting or container transfers.
 */
public final class PickupTracker {
    private PickupTracker() { }

    public static void onPickupAnimation(ItemPickupAnimationS2CPacket packet) {
        MinecraftClient client = MinecraftClient.getInstance();
        // Packet callbacks can initially run on Netty; vanilla redispatches to main.
        if (!client.isOnThread() || client.world == null || client.player == null) return;
        if (packet.getCollectorEntityId() != client.player.getId()) return;

        Entity entity = client.world.getEntityById(packet.getEntityId());
        if (entity instanceof ItemEntity dropped) {
            ItemStack stack = dropped.getStack();
            int amount = packet.getStackAmount();
            if (amount > 0 && !stack.isEmpty()) {
                InventoryPickupClient.recordGroundItem(stack.copyWithCount(1), amount);
            }
        } else if (entity instanceof ExperienceOrbEntity orb) {
            int amount = orb.getValue();
            if (amount > 0) InventoryPickupClient.recordGroundExperience(amount);
        }
    }
}

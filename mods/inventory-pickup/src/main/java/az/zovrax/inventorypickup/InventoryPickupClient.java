package az.zovrax.inventorypickup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class InventoryPickupClient implements ClientModInitializer {
    public static final String MOD_ID = "zovrax_inventory_pickup";

    private static final PickupConfig CONFIG = PickupConfig.load();
    private static final List<PickupEvent> PENDING = new ArrayList<>();
    private static Object trackedWorld;
    private record PickupEvent(ItemStack stack, int amount, boolean experience) { }

    public static void recordGroundItem(ItemStack stack, int amount) {
        if (amount > 0) PENDING.add(new PickupEvent(stack.copyWithCount(1), amount, false));
    }

    public static void recordGroundExperience(int amount) {
        if (amount > 0) PENDING.add(new PickupEvent(ItemStack.EMPTY, amount, true));
    }

    private static int inventoryTotal(MinecraftClient client, ItemStack item) {
        int total = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack slot = client.player.getInventory().getStack(i);
            if (ItemStack.areItemsAndComponentsEqual(slot, item)) total += slot.getCount();
        }
        return total;
    }
    private static final PickupHudRenderer HUD = new PickupHudRenderer();
    private static KeyBinding settingsKey;

    public static PickupConfig config() {
        return CONFIG;
    }

    public static void preview() {
        HUD.enqueue(new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND), 4, 9, CONFIG);
    }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zovrax_inventory_pickup.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "pickup_notifications"),
                HUD::render
        );
    }

    private void onClientTick(MinecraftClient client) {
        while (settingsKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new PickupSettingsScreen(null));
            }
        }
        if (client.world != trackedWorld || client.player == null) {
            trackedWorld = client.world;
            PENDING.clear();
        }
        // Only verified ground pickups enter this queue; resolve totals after
        // vanilla sync has updated inventory, never by diffing item components.
        for (PickupEvent event : PENDING) {
            if (!CONFIG.enabled || client.player == null) continue;
            if (event.experience()) HUD.enqueueExperience(event.amount(), CONFIG);
            else HUD.enqueue(event.stack(), event.amount(), inventoryTotal(client, event.stack()), CONFIG);
        }
        PENDING.clear();
        HUD.prune(CONFIG);
    }
}

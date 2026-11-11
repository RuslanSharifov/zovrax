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

public final class InventoryPickupClient implements ClientModInitializer {
    public static final String MOD_ID = "zovrax_inventory_pickup";

    private static final PickupConfig CONFIG = PickupConfig.load();
    private static final PickupTracker TRACKER = new PickupTracker();
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
        // Track silently when disabled so re-enabling does not replay old pickups.
        TRACKER.tick(client, (item, inventoryTotal) -> {
            if (CONFIG.enabled) HUD.enqueue(item.stack(), item.amount(), inventoryTotal, CONFIG);
        });
        HUD.prune(CONFIG);
    }
}

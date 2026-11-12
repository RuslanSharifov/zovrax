package az.zovrax.extendedreach;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Native settings shortcut, persisted client preference and server sync. */
public final class ExtendedReachClient implements ClientModInitializer {
    private static final ReachConfig CONFIG = ReachConfig.load();
    private KeyBinding settingsKey;

    public static ReachConfig config() {
        return CONFIG;
    }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(
            Identifier.of(ExtendedReachMod.ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_extended_reach.settings", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O, category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendReach());
    }

    private void onTick(MinecraftClient client) {
        while (settingsKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ReachSettingsScreen(null));
            }
        }
    }

    public static void setBlocks(int blocks) {
        int clamped = ReachLimits.clamp(blocks);
        if (CONFIG.blocks != clamped) {
            CONFIG.blocks = clamped;
            CONFIG.save();
            sendReach();
        }
    }

    private static void sendReach() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null && ClientPlayNetworking.canSend(ReachRequestPayload.ID)) {
            ClientPlayNetworking.send(new ReachRequestPayload(CONFIG.blocks));
        }
    }
}

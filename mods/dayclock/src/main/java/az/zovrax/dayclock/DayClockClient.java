package az.zovrax.dayclock;

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

/** Registers a minimal client-only HUD and an independent settings hotkey. */
public final class DayClockClient implements ClientModInitializer {
    public static final String ID = "zovrax_dayclock";
    private static final DayClockConfig CONFIG = DayClockConfig.load();
    private KeyBinding settingsKey;

    public static DayClockConfig config() { return CONFIG; }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_dayclock.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::endTick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of(ID, "overlay"), DayClockHud::render);
    }

    private void endTick(MinecraftClient client) {
        while (settingsKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new DayClockSettingsScreen(null));
            }
        }
    }
}

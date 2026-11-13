package az.zovrax.smoothdoors;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/** All animation is rendered on clients. Server behavior and collision stay vanilla. */
public final class SmoothDoorsClient implements ClientModInitializer {
    public static final String MOD_ID = "zovrax_smooth_doors";
    private static final MotionConfig CONFIG = MotionConfig.load();
    private KeyMapping settingsKey;

    public static MotionConfig config() {
        return CONFIG;
    }

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.create(ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.zovrax_smooth_doors.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MotionTracker.tick(client.level);
            while (settingsKey.consumeClick()) {
                if (client.level != null && client.screen == null) {
                    client.setScreen(new SmoothDoorsSettingsScreen(null));
                }
            }
        });
    }
}

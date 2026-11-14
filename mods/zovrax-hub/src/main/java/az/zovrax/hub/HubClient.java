package az.zovrax.hub;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Independent client-only launcher; never intercepts other mods' gameplay keybinds. */
public final class HubClient implements ClientModInitializer {
    public static final String ID = "zovrax_hub";
    private KeyBinding hubKey;

    @Override public void onInitializeClient() {
        HubCatalog.loadCache();
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));
        hubKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_hub.open", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        while (hubKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new HubScreen(null));
        }
    }
}

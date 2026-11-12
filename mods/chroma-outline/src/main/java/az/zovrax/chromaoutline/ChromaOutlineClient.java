package az.zovrax.chromaoutline;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ChromaOutlineClient implements ClientModInitializer {
    public static final String MOD_ID = "zovrax_chroma_outline";
    private static final ChromaOutlineConfig CONFIG = ChromaOutlineConfig.load();
    private KeyBinding settingsKey;

    public static ChromaOutlineConfig config() { return CONFIG; }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_chroma_outline.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        while (settingsKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new ChromaOutlineScreen(null));
            }
        }
    }
}

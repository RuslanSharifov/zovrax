package az.zovrax.smartreturn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Client-only route recording. No Mixin, world editing, or server requirement. */
public final class SmartReturnClient implements ClientModInitializer {
    public static final String ID = "zovrax_smart_return";
    private static final SmartReturnConfig CONFIG = SmartReturnConfig.load();
    private static final RouteRecorder ROUTES = new RouteRecorder();
    private static KeyBinding guideKey;
    private static KeyBinding settingsKey;

    public static SmartReturnConfig config() { return CONFIG; }
    public static RouteRecorder routes() { return ROUTES; }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID,"main"));
        guideKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_smart_return.return", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_smart_return.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of(ID, "return_compass"), SmartReturnHud::render);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(SmartReturnWorldArrows::render);
    }

    private void tick(MinecraftClient client) {
        if (client.player != null && client.currentScreen == null) {
            while (guideKey.wasPressed()) ROUTES.toggle(client);
            while (settingsKey.wasPressed())
                client.setScreen(new SmartReturnSettings(null));
        }
        ROUTES.tick(client, CONFIG);
    }
}

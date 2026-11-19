package az.zovrax.biometracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class BiomeTrackerClient implements ClientModInitializer {
    public static final String MOD_ID = "zovrax_biome_tracker";
    private static final BiomeTrackerConfig CONFIG = BiomeTrackerConfig.load();
    private static final BiomeStats STATS = BiomeStats.load();
    private KeyBinding settingsKey;
    private String previousBiome = "";

    public static BiomeTrackerConfig config() { return CONFIG; }
    public static BiomeStats stats() { return STATS; }

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_biome_tracker.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::endTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> STATS.endSession());
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of(MOD_ID, "biome_hud"), BiomeTrackerHud::render);
    }

    private void endTick(MinecraftClient client) {
        while (settingsKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new BiomeTrackerSettingsScreen(null));
            }
        }
        if (client.player == null || client.world == null) {
            if (!previousBiome.isEmpty()) STATS.endSession();
            previousBiome = "";
            return;
        }

        BlockPos pos = client.player.getBlockPos();
        String biomeId = client.world.getBiome(pos).getKey()
            .map(key -> key.getValue().toString()).orElse("minecraft:unknown");
        String worldBiomeKey = client.world.getRegistryKey().getValue() + "|" + biomeId;
        STATS.tick(biomeId, client.world.getRegistryKey().getValue().toString());

        if (!previousBiome.isEmpty() && !previousBiome.equals(worldBiomeKey) && CONFIG.notifications) {
            client.inGameHud.setTitle(Text.translatable("text.zovrax_biome_tracker.discovered"));
            client.inGameHud.setSubtitle(biomeText(biomeId));
            client.inGameHud.setTitleTicks(10, 55, 20);
            if (CONFIG.notificationSound) client.player.playSound(SoundEvents.UI_TOAST_IN, 0.85f, 1.0f);
        }
        previousBiome = worldBiomeKey;

        if (CONFIG.showBoundaries && client.world.getTime() % 8 == 0) {
            BiomeBoundaryRenderer.showNearby(client, pos, biomeId);
        }
    }

    static Text biomeText(String biomeId) {
        Identifier id = Identifier.tryParse(biomeId);
        if (id == null) return Text.literal(biomeId);
        return Text.translatable("biome." + id.getNamespace() + "." + id.getPath());
    }
}

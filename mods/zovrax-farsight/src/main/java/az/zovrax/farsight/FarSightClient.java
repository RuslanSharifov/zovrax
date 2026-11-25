package az.zovrax.farsight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Client-only adaptive controller. It never changes simulation distance or world data. */
public final class FarSightClient implements ClientModInitializer {
    private static final FarSightConfig CONFIG = FarSightConfig.load();
    private KeyBinding toggleKey;
    private int ticksUntilSample;
    private int baselineDistance = -1;
    private int lastAppliedDistance = -1;
    private boolean wasInWorld;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(FarSightConfig.MOD_ID, "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.zovrax_farsight.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            CONFIG.adaptiveEnabled = !CONFIG.adaptiveEnabled;
            if (CONFIG.adaptiveEnabled && client.world != null) {
                baselineDistance = client.options.getViewDistance().getValue();
                lastAppliedDistance = baselineDistance;
            } else if (!CONFIG.adaptiveEnabled) {
                restoreUserDistance(client);
            }
            CONFIG.save();
            if (client.player != null) client.player.sendMessage(Text.literal(
                "FarSight adaptive mode " + (CONFIG.adaptiveEnabled ? "enabled" : "disabled")), true);
        }

        if (client.world == null || client.player == null) {
            if (wasInWorld && CONFIG.adaptiveEnabled) restoreUserDistance(client);
            wasInWorld = false;
            ticksUntilSample = 0;
            return;
        }
        if (!wasInWorld) {
            wasInWorld = true;
            if (CONFIG.adaptiveEnabled) {
                baselineDistance = client.options.getViewDistance().getValue();
                lastAppliedDistance = baselineDistance;
            }
        }
        if (!CONFIG.adaptiveEnabled) return;
        if (baselineDistance < 0) {
            baselineDistance = client.options.getViewDistance().getValue();
            lastAppliedDistance = baselineDistance;
        }

        int current = client.options.getViewDistance().getValue();
        // Manual slider edits become the new player-selected adaptive ceiling.
        if (lastAppliedDistance >= 0 && current != lastAppliedDistance) {
            baselineDistance = current;
            lastAppliedDistance = current;
        }
        if (++ticksUntilSample < CONFIG.sampleIntervalTicks) return;
        ticksUntilSample = 0;
        adapt(client);
    }

    private void adapt(MinecraftClient client) {
        Runtime runtime = Runtime.getRuntime();
        long maxHeap = runtime.maxMemory();
        long usedHeap = runtime.totalMemory() - runtime.freeMemory();
        int heapPercent = maxHeap <= 0 ? 0 : (int) Math.min(100, usedHeap * 100L / maxHeap);
        int fps = client.getCurrentFps();
        int current = client.options.getViewDistance().getValue();
        int ceiling = clamp(baselineDistance, CONFIG.minRenderDistance, CONFIG.maxRenderDistance);
        boolean memoryPressure = heapPercent >= CONFIG.heapPressurePercent;
        boolean framePressure = fps > 0 && fps < CONFIG.targetFps - CONFIG.fpsDropTolerance;
        int next = current;

        if (memoryPressure || framePressure) {
            next = Math.max(CONFIG.minRenderDistance, current - 1);
        } else if (fps >= CONFIG.targetFps + CONFIG.fpsRecoveryHeadroom
                && heapPercent <= CONFIG.heapPressurePercent - CONFIG.heapRecoveryHeadroom) {
            next = Math.min(ceiling, current + 1);
        }
        if (next != current) {
            client.options.getViewDistance().setValue(next);
            lastAppliedDistance = next;
        }
    }

    private void restoreUserDistance(MinecraftClient client) {
        if (baselineDistance >= 0) client.options.getViewDistance().setValue(baselineDistance);
        baselineDistance = -1;
        lastAppliedDistance = -1;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

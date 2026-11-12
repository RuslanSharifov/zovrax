package az.zovrax.smartreturn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Observes local exploration without changing Minecraft world state. */
public final class RouteRecorder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private Object worldObject;
    private String worldId;
    private Path saveFile;
    private RouteGraph graph;
    private boolean guiding;
    private boolean paused;
    private boolean reminderSent;
    private boolean newTripReady;
    private int ticks;
    private int enclosedTicks;
    private int outdoorTicks;
    private int savedAt;
    private int recordedAt;
    private Vec3d lastOutdoor;
    private int lastOutdoorAt;
    private final ArrayDeque<Vec3d> prelude = new ArrayDeque<>();
    private List<Vec3d> guidance = List.of();

    public RouteGraph graph() { return graph; }
    public boolean guiding() { return guiding; }
    public List<Vec3d> guidance() { return guidance; }

    private static void message(MinecraftClient mc, String text) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("SmartReturn: " + text), true);
    }

    private String locationId(MinecraftClient mc) {
        String game = mc.getCurrentServerEntry() != null
                ? "server:" + mc.getCurrentServerEntry().address
                : mc.getServer() != null
                    ? "singleplayer:" + mc.getServer().getSaveProperties().getLevelName()
                    : "unknown-local-world";
        return game + "/" + mc.world.getRegistryKey().getValue();
    }

    private Path fileFor(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder name = new StringBuilder();
        for (byte valueByte : hash) name.append(String.format("%02x", valueByte & 0xff));
        return FabricLoader.getInstance().getConfigDir()
                .resolve("zovrax-smart-return").resolve("routes").resolve(name + ".json");
    }

    private void changeWorld(MinecraftClient mc) {
        flush();
        worldObject = mc.world;
        worldId = null;
        graph = null;
        guidance = List.of();
        guiding = false;
        paused = true;
        reminderSent = false;
        newTripReady = false;
        ticks = enclosedTicks = outdoorTicks = savedAt = recordedAt = 0;
        lastOutdoor = null;
        lastOutdoorAt = 0;
        prelude.clear();
        saveFile = null;
        if (mc.world == null) return;
        try {
            worldId = locationId(mc);
            saveFile = fileFor(worldId);
            if (Files.isRegularFile(saveFile)) {
                JsonObject record = JsonParser.parseString(Files.readString(saveFile)).getAsJsonObject();
                if (worldId.equals(record.get("world").getAsString()))
                    graph = RouteGraph.fromJson(record.getAsJsonObject("route"));
            }
        } catch (Exception e) {
            System.err.println("[SmartReturn] Saved route unavailable: " + e);
            graph = null;
        }
    }

    public void tick(MinecraftClient mc, SmartReturnConfig config) {
        if (worldObject != mc.world) changeWorld(mc);
        if (mc.player == null || mc.world == null || !config.enabled) return;
        ticks++;
        if (ticks % 10 != 0) return; // ~2 samples/s, minimal client work
        Vec3d pos = mc.player.getPos();
        BlockPos feet = mc.player.getBlockPos();
        int roofDepth = mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                feet.getX(), feet.getZ()) - feet.getY();
        boolean hasSky = mc.world.isSkyVisible(feet.up());
        boolean cave = !hasSky && roofDepth >= 8;
        if (hasSky) {
            lastOutdoor = pos;
            lastOutdoorAt = ticks;
        }
        if (cave) {
            enclosedTicks += 10;
            outdoorTicks = 0;
            if (graph == null || paused) {
                if (prelude.isEmpty() || prelude.getLast().distanceTo(pos) > 1.2) prelude.addLast(pos);
                while (prelude.size() > 100) prelude.removeFirst();
            }
            if (enclosedTicks >= 120 && (graph == null || paused)) {
                if (graph == null || newTripReady
                        || graph.nearest(pos, 5.0) < 0) {
                    Vec3d entrance = lastOutdoor != null && ticks - lastOutdoorAt < 1200
                            && lastOutdoor.distanceTo(pos) < 64
                            ? lastOutdoor : prelude.getFirst();
                    graph = new RouteGraph(entrance);
                    recordedAt = ticks - enclosedTicks;
                    reminderSent = false;
                    newTripReady = false;
                    for (Vec3d waypoint : prelude) graph.sample(waypoint);
                } else {
                    // Resuming the recorded path after returning to the same cave.
                    for (Vec3d waypoint : prelude) graph.sample(waypoint);
                }
                prelude.clear();
                paused = false;
                flush();
            }
        } else {
            outdoorTicks += 10;
            enclosedTicks = 0;
            prelude.clear();
            if (graph != null && outdoorTicks >= 120) {
                paused = true;
                if (graph.entrance().distanceTo(pos) < 12 && guiding) {
                    guiding = false;
                    guidance = List.of();
                    message(mc, "You are back near the cave entrance.");
                }
                if (graph.entrance().distanceTo(pos) > 60) newTripReady = true;
            }
        }
        if (graph != null && !paused) {
            graph.sample(pos);
            if (config.reminders && !reminderSent
                    && ticks - recordedAt >= config.reminderMinutes * 1200) {
                reminderSent = true;
                message(mc, "Cave path saved quietly. Press R if you want directions back.");
            }
        }
        if (guiding) {
            guidance = graph == null ? List.of() : graph.homeward(pos);
            if (guidance.isEmpty()) {
                guiding = false;
                message(mc, "Cannot find a connected walked route from here.");
            } else if (pos.distanceTo(graph.entrance()) < 3.0) {
                guiding = false;
                guidance = List.of();
                message(mc, "Cave entrance reached.");
            }
        }
        if (graph != null && ticks - savedAt >= 600) flush();
    }

    public void toggle(MinecraftClient mc) {
        if (mc.player == null) return;
        if (guiding) {
            guiding = false;
            guidance = List.of();
            message(mc, "Return guidance off. Recording remains active.");
            return;
        }
        if (graph == null) {
            message(mc, "No cave path recorded yet.");
            return;
        }
        guidance = graph.homeward(mc.player.getPos());
        if (guidance.isEmpty()) {
            message(mc, "Move back onto your recorded path first.");
            return;
        }
        guiding = true;
        message(mc, "Return guidance on. Press R again to hide it.");
    }

    public void forget(MinecraftClient mc) {
        graph = null;
        guidance = List.of();
        guiding = false;
        paused = true;
        newTripReady = false;
        reminderSent = false;
        enclosedTicks = 0;
        prelude.clear();
        if (saveFile != null) {
            try { Files.deleteIfExists(saveFile); }
            catch (Exception e) { System.err.println("[SmartReturn] Failed to delete route: " + e); }
        }
        message(mc, "Saved cave path cleared.");
    }

    public void flush() {
        if (saveFile == null || graph == null || worldId == null) return;
        try {
            Files.createDirectories(saveFile.getParent());
            JsonObject record = new JsonObject();
            record.addProperty("world", worldId);
            record.add("route", graph.toJson());
            Path temp = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(record), StandardCharsets.UTF_8);
            Files.move(temp, saveFile, StandardCopyOption.REPLACE_EXISTING);
            savedAt = ticks;
        } catch (Exception e) {
            System.err.println("[SmartReturn] Could not save cave route: " + e);
        }
    }
}

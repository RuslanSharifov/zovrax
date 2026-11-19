package az.zovrax.biometracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BiomeStats {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-biome-tracker-stats.json");
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Long>>() {}.getType();
    private final Map<String, Long> ticks = new LinkedHashMap<>();
    private String currentKey = "";
    private long currentSessionTicks;
    private long ticksSinceSave;

    public static BiomeStats load() {
        BiomeStats stats = new BiomeStats();
        try {
            if (Files.isRegularFile(FILE)) {
                Map<String, Long> stored = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), MAP_TYPE);
                if (stored != null) stored.forEach((key, value) -> {
                    if (key != null && value != null && value >= 0) stats.ticks.put(key, value);
                });
            }
        } catch (Exception e) {
            System.err.println("[Biome Tracker] Could not load statistics: " + e.getMessage());
        }
        return stats;
    }

    public void tick(String biome, String dimension) {
        String key = dimension + "|" + biome;
        if (!key.equals(currentKey)) {
            currentKey = key;
            currentSessionTicks = 0;
        }
        ticks.merge(key, 1L, Long::sum);
        currentSessionTicks++;
        if (++ticksSinceSave >= 100) save();
    }

    public long currentSessionTicks() { return currentSessionTicks; }
    public long ticksFor(String key) { return ticks.getOrDefault(key, 0L); }
    public Map<String, Long> snapshot() { return new LinkedHashMap<>(ticks); }

    public static String formatTicks(long ticks) {
        long seconds = ticks / 20;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours > 0 ? String.format("%dh %02dm", hours, minutes)
            : String.format("%dm %02ds", minutes, seconds % 60);
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(ticks, MAP_TYPE) + "\n", StandardCharsets.UTF_8);
            ticksSinceSave = 0;
        } catch (Exception e) {
            System.err.println("[Biome Tracker] Could not save statistics: " + e.getMessage());
        }
    }
}

package az.zovrax.harvestpulse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** All user fields and visibility preferences are local, independent of other Zovrax mods. */
public final class HarvestPulseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-harvest-pulse.json");

    public boolean enabled = true;
    public int scale = 100;
    /** Chosen by the player; independent of server hard limit. */
    public int selectionLimit = 16;
    public boolean soundAlerts = true;
    public boolean toastAlerts = true;
    public boolean animateAlerts = true;
    public int opacity = 100;
    public int backgroundOpacity = 45;
    public double x = 0.025;
    public double y = 0.34;
    public List<Field> fields = new ArrayList<>();

    public static final class Field {
        public String id = UUID.randomUUID().toString();
        public String name = "New field";
        public String worldKey = "";
        public String dimension = "";
        public int x1, y1, z1, x2, y2, z2;
        /** 0 always, 1 only at 100%, 2 hidden. */
        public int showMode = 0;
        /** 0 both, 1 percentage, 2 mature / total. */
        public int valueMode = 0;

        // Runtime state is not persisted; do not confuse stale chunk data with a live measurement.
        public transient int mature;
        public transient int total;
        public transient boolean loaded;
        public transient boolean sampled;
        public transient long lastSampleMillis;
        public transient long readySinceMillis;
        public transient boolean notifiedReady;
    }

    public static HarvestPulseConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                HarvestPulseConfig cfg = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), HarvestPulseConfig.class);
                if (cfg != null) { cfg.sanitize(); return cfg; }
            }
        } catch (Exception e) {
            System.err.println("[HarvestPulse] Could not read config: " + e.getMessage());
        }
        return new HarvestPulseConfig();
    }

    public Field byId(String id) {
        if (id == null) return null;
        for (Field f : fields) if (id.equals(f.id)) return f;
        return null;
    }

    public void sanitize() {
        scale = Math.clamp(scale, 50, 200);
        if (!FieldMath.validLimit(selectionLimit)) selectionLimit = 16;
        opacity = Math.clamp(opacity, 0, 100);
        backgroundOpacity = Math.clamp(backgroundOpacity, 0, 100);
        if (!Double.isFinite(x)) x = 0.025;
        if (!Double.isFinite(y)) y = 0.34;
        x = Math.clamp(x, 0d, 1d);
        y = Math.clamp(y, 0d, 1d);
        if (fields == null) fields = new ArrayList<>();
        fields.removeIf(f -> f == null || !FieldMath.validArea(f.x1, f.y1, f.z1, f.x2, f.y2, f.z2));
        if (fields.size() > 64) fields = new ArrayList<>(fields.subList(0, 64));
        for (Field f : fields) {
            if (f.id == null || f.id.isBlank()) f.id = UUID.randomUUID().toString();
            if (f.name == null || f.name.isBlank()) f.name = "New field";
            f.name = f.name.trim().substring(0, Math.min(32, f.name.trim().length()));
            if (f.worldKey == null) f.worldKey = "";
            if (f.dimension == null) f.dimension = "";
            f.showMode = Math.clamp(f.showMode, 0, 2);
            f.valueMode = Math.clamp(f.valueMode, 0, 2);
        }
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[HarvestPulse] Could not save config: " + e.getMessage());
        }
    }
}

package az.zovrax.extendedreach;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Client's independent, persistent setting. The server still validates packets. */
public final class ReachConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("zovrax-extended-reach.json");

    public int blocks = ReachLimits.DEFAULT;

    public static ReachConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                ReachConfig cfg = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), ReachConfig.class);
                if (cfg != null) {
                    cfg.blocks = ReachLimits.clamp(cfg.blocks);
                    return cfg;
                }
            }
        } catch (Exception e) {
            System.err.println("[Extended Reach] Could not read config: " + e.getMessage());
        }
        return new ReachConfig();
    }

    public void save() {
        blocks = ReachLimits.clamp(blocks);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[Extended Reach] Could not write config: " + e.getMessage());
        }
    }
}

package az.zovrax.smoothdoors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Independent JSON config: no Mod Menu dependency and no shared settings. */
public final class MotionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("zovrax_smooth_doors.json");

    public boolean enabled = true;
    public boolean doors = true;
    public boolean trapdoors = true;
    public int openingMs = 350;
    public int closingMs = 350;

    public static MotionConfig load() {
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                MotionConfig parsed = GSON.fromJson(reader, MotionConfig.class);
                if (parsed != null) {
                    parsed.clamp();
                    return parsed;
                }
            } catch (Exception ignored) {
                // Malformed settings cannot prevent Minecraft startup.
            }
        }
        return new MotionConfig();
    }

    public void clamp() {
        openingMs = Math.clamp(openingMs, 100, 2000);
        closingMs = Math.clamp(closingMs, 100, 2000);
    }

    public void save() {
        clamp();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception ignored) {
            // A read-only directory must never crash the client.
        }
    }
}

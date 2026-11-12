package az.zovrax.smartreturn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Own settings file; no dependencies on other Zovrax mods. */
public final class SmartReturnConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-smart-return.json");
    public boolean enabled = true;
    public boolean reminders = true;
    public int reminderMinutes = 3;
    public int displayMode = 0; // 0=3D arrows, 1=mini compass, 2=both
    public boolean showDistance = true;

    public boolean arrows() { return displayMode == 0 || displayMode == 2; }
    public boolean compass() { return displayMode == 1 || displayMode == 2; }

    public static SmartReturnConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                SmartReturnConfig loaded = GSON.fromJson(Files.readString(FILE), SmartReturnConfig.class);
                if (loaded != null) {
                    loaded.validate();
                    return loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("[SmartReturn] Cannot read settings: " + e);
        }
        return new SmartReturnConfig();
    }

    private void validate() {
        displayMode = Math.max(0, Math.min(2, displayMode));
        reminderMinutes = Math.max(1, Math.min(20, reminderMinutes));
    }

    public void save() {
        validate();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[SmartReturn] Cannot save settings: " + e);
        }
    }
}

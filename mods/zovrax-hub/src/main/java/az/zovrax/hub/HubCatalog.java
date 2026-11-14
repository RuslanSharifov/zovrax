package az.zovrax.hub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * Read-only static JSON "API" hosted by ZOVRAX GitHub Pages.
 * All network and filesystem operations run off the render thread.
 * No remote Java code or user-controlled server URLs are ever executed.
 */
public final class HubCatalog {
    public static final String VERSION = "1.21.10";
    public static final String BASE = "https://ruslansharifov.github.io/zovrax/";
    private static final int MAX_BYTES = 4_000_000;
    private static final Path CACHE = FabricLoader.getInstance().getConfigDir()
        .resolve("zovrax-hub").resolve("catalog.json");
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ZOVRAX-Hub-Catalog");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<String, List<Download>> DETAILS = new HashMap<>();
    private static final Set<String> DETAIL_DONE = new HashSet<>();
    private static final Set<String> DETAIL_PENDING = new HashSet<>();
    private static volatile List<Item> items = List.of();
    private static volatile String status = "Offline cache";
    private static volatile boolean updating;

    public record Item(String slug, String name, String summary, String category,
                       String updatedAt, String latestVersion, List<String> versions) {
        public boolean listedForCurrentVersion() { return versions.contains(VERSION); }
    }

    public record Download(String version, String label, String url) {}

    private HubCatalog() {}

    public static List<Item> items() { return items; }
    public static String status() { return status; }
    public static boolean updating() { return updating; }
    public static boolean detailReady(String slug) { return DETAIL_DONE.contains(slug); }

    public static Download compatibleDownload(String slug) {
        for (Download download : DETAILS.getOrDefault(slug, List.of())) {
            return download;
        }
        return null;
    }

    public static void loadCache() {
        try {
            if (Files.isRegularFile(CACHE)) {
                items = readCatalog(Files.readString(CACHE, StandardCharsets.UTF_8));
                status = "Saved catalog (offline available)";
            }
        } catch (Exception ex) {
            status = "No valid saved catalog";
        }
    }

    public static void refresh(Runnable onChange) {
        if (updating) return;
        updating = true;
        status = "Updating catalog...";
        IO.execute(() -> {
            List<Item> updated = null;
            String state;
            try {
                String content = fetch(BASE + "data/catalog.json");
                updated = readCatalog(content);
                persist(CACHE, content);
                state = "Catalog updated from ZOVRAX";
            } catch (Exception ex) {
                state = items.isEmpty() ? "Offline: catalog unavailable" :
                    "Offline: showing saved catalog";
            }
            List<Item> result = updated;
            String message = state;
            MinecraftClient.getInstance().execute(() -> {
                if (result != null) items = result;
                status = message;
                updating = false;
                onChange.run();
            });
        });
    }

    public static void requestDetail(Item item, Runnable onChange) {
        String slug = item.slug();
        if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                || DETAIL_DONE.contains(slug) || !DETAIL_PENDING.add(slug)) return;
        IO.execute(() -> {
            Path cached = CACHE.getParent().resolve("details").resolve(slug + ".json");
            List<Download> result = null;
            try {
                String raw = fetch(BASE + "data/mods/" + slug + ".json");
                result = readDetails(raw, slug);
                persist(cached, raw);
            } catch (Exception ex) {
                try {
                    if (Files.isRegularFile(cached))
                        result = readDetails(Files.readString(cached, StandardCharsets.UTF_8), slug);
                } catch (Exception ignored) {
                    // Lack of network/cache must not break the Hub.
                }
            }
            List<Download> downloads = result;
            MinecraftClient.getInstance().execute(() -> {
                DETAIL_PENDING.remove(slug);
                DETAIL_DONE.add(slug);
                DETAILS.put(slug, downloads == null ? List.of() : downloads);
                onChange.run();
            });
        });
    }

    public static String modPage(String slug) {
        if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) return BASE;
        return BASE + "#/mods/" + slug;
    }

    private static String fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(9))
            .GET().header("Accept", "application/json").build();
        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200 || response.body().length > MAX_BYTES)
            throw new IllegalStateException("Catalog HTTP " + response.statusCode());
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    private static void persist(Path file, String data) {
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, data, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (java.io.IOException unsupportedAtomic) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
            // Read-only folders are okay; live catalog still works.
        }
    }

    private static List<Item> readCatalog(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray mods = root.getAsJsonArray("mods");
        if (mods == null) throw new IllegalArgumentException("Missing catalog.mods");
        List<Item> found = new ArrayList<>();
        for (JsonElement element : mods) {
            if (!element.isJsonObject()) continue;
            JsonObject m = element.getAsJsonObject();
            String slug = string(m, "slug");
            if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) continue;
            JsonObject summary = m.has("summary") && m.get("summary").isJsonObject()
                ? m.getAsJsonObject("summary") : new JsonObject();
            found.add(new Item(slug, string(m, "name"), string(summary, "en"),
                string(m, "category"), string(m, "updatedAt"), string(m, "latestVersion"),
                List.copyOf(strings(m.get("minecraftVersions")))));
        }
        return List.copyOf(found);
    }

    private static List<Download> readDetails(String json, String slug) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!slug.equals(string(root, "slug"))) throw new IllegalArgumentException("Slug mismatch");
        List<Download> matches = new ArrayList<>();
        for (JsonElement re : array(root, "releases")) {
            if (!re.isJsonObject()) continue;
            JsonObject release = re.getAsJsonObject();
            String version = string(release, "version");
            for (JsonElement de : array(release, "downloads")) {
                if (!de.isJsonObject()) continue;
                JsonObject download = de.getAsJsonObject();
                List<String> versions = strings(download.has("minecraftVersions")
                    ? download.get("minecraftVersions") : release.get("minecraftVersions"));
                List<String> loaders = strings(download.has("loaders")
                    ? download.get("loaders") : release.get("loaders"));
                if (!versions.contains(VERSION) || !loaders.contains("Fabric")) continue;
                String relative = string(download, "url");
                String prefix = "data/mods/" + slug + "/releases/";
                if (!relative.startsWith(prefix) || !relative.endsWith(".jar")
                    || relative.contains("..") || relative.contains("\\")
                    || !relative.matches("[a-zA-Z0-9_./+-]+")) continue;
                matches.add(new Download(version, string(download, "label"), BASE + relative));
            }
        }
        matches.sort(Comparator.comparing(Download::version, Comparator.reverseOrder()));
        return List.copyOf(matches);
    }

    private static JsonArray array(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonArray()
            ? object.getAsJsonArray(key) : new JsonArray();
    }

    private static List<String> strings(JsonElement element) {
        List<String> values = new ArrayList<>();
        if (element != null && element.isJsonArray()) {
            for (JsonElement part : element.getAsJsonArray()) {
                if (part.isJsonPrimitive() && part.getAsJsonPrimitive().isString())
                    values.add(part.getAsString());
            }
        }
        return values;
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive()
            && value.getAsJsonPrimitive().isString() ? value.getAsString() : "";
    }
}

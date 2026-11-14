package az.zovrax.hub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Minecraft-native, translucent ZOVRAX window.
 * Installed tab: official mod list + native settings screens (their own save code).
 * Explore tab: searchable online catalog, filters, date sorting, verified downloads.
 */
public final class HubScreen extends Screen {
    private static final int BG = 0xD6162334;
    private static final int PANEL = 0xB7111A2A;
    private static final int TEXT = 0xFFE9F8FA;
    private static final int SUB = 0xFFA8C1D0;
    private static final int CYAN = 0xFF90F0E4;

    private final Screen parent;
    private boolean explore;
    private String query = "";
    private String category = "All";
    private boolean newest = true;
    private String selectedId;
    private int page;
    private TextFieldWidget searchField;
    private int x, y, w, h, sidebar, rightX, rightW;
    private List<HubCatalog.Item> matched = List.of();
    private List<HubRegistry.Known> installed = List.of();

    public HubScreen(Screen parent) {
        super(Text.literal("ZOVRAX Hub"));
        this.parent = parent;
    }

    @Override protected void init() {
        x = 14;
        y = 12;
        w = Math.max(220, width - 28);
        h = Math.max(145, height - 24);
        sidebar = Math.max(112, Math.min(212, (w * 37) / 100));
        rightX = x + sidebar + 8;
        rightW = Math.max(95, w - sidebar - 18);
        installed = HubRegistry.installed();
        matched = filteredCatalog();

        addDrawableChild(ButtonWidget.builder(Text.literal("My Mods"), b -> switchTab(false))
            .dimensions(x + 8, y + 24, (sidebar - 20) / 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Explore"), b -> switchTab(true))
            .dimensions(x + 12 + (sidebar - 20) / 2, y + 24,
                (sidebar - 20) / 2, 20).build());

        int listTop;
        if (explore) {
            searchField = new TextFieldWidget(textRenderer,
                x + 8, y + 50, sidebar - 16, 19, Text.literal("Search ZOVRAX mods"));
            searchField.setMaxLength(80);
            searchField.setText(query);
            searchField.setChangedListener(this::searchChanged);
            searchField.setPlaceholder(Text.literal("Search mods..."));
            addDrawableChild(searchField);
            addDrawableChild(ButtonWidget.builder(Text.literal(shortText("Category: " + category, sidebar - 24)),
                b -> cycleCategory()).dimensions(x + 8, y + 73, sidebar - 16, 19).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(newest ? "Sort: Recently updated" : "Sort: Name A-Z"),
                b -> { newest = !newest; page = 0; clearAndInit(); })
                .dimensions(x + 8, y + 95, sidebar - 16, 19).build());
            listTop = y + 121;
            if (selectedId == null && !matched.isEmpty()) selectedId = matched.getFirst().slug();
        } else {
            searchField = null;
            listTop = y + 53;
            if (selectedId == null && !installed.isEmpty()) selectedId = installed.getFirst().id();
        }

        int rows = Math.max(1, (y + h - 43 - listTop) / 23);
        int total = explore ? matched.size() : installed.size();
        int maxPage = Math.max(0, (total - 1) / rows);
        page = Math.min(Math.max(0, page), maxPage);
        for (int row = 0; row < rows; row++) {
            int index = page * rows + row;
            if (index >= total) break;
            String id = explore ? matched.get(index).slug() : installed.get(index).id();
            String name = explore ? matched.get(index).name() : installed.get(index).name();
            String label = (id.equals(selectedId) ? "• " : "  ") + name;
            addDrawableChild(ButtonWidget.builder(Text.literal(shortText(label, sidebar - 24)),
                b -> choose(id)).dimensions(x + 8, listTop + row * 23, sidebar - 16, 20).build());
        }

        int bottom = y + h - 30;
        ButtonWidget back = addDrawableChild(ButtonWidget.builder(Text.literal("<"),
            b -> { page--; clearAndInit(); })
            .dimensions(x + 8, bottom, 25, 19).build());
        back.active = page > 0;
        ButtonWidget forward = addDrawableChild(ButtonWidget.builder(Text.literal(">"),
            b -> { page++; clearAndInit(); })
            .dimensions(x + sidebar - 33, bottom, 25, 19).build());
        forward.active = page < maxPage;

        if (explore) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"),
                b -> HubCatalog.refresh(this::rebuild)).dimensions(rightX + 6,
                    y + h - 33, Math.min(80, rightW - 12), 21).build());
            HubCatalog.Item item = selectedCatalog();
            if (item != null) {
                HubCatalog.requestDetail(item, this::rebuild);
                HubCatalog.Download download = HubCatalog.compatibleDownload(item.slug());
                int buttonY = y + h - 61;
                ButtonWidget downloadButton = addDrawableChild(ButtonWidget.builder(
                    Text.literal(download != null ? "Download for " + HubCatalog.VERSION :
                        HubCatalog.detailReady(item.slug()) ? "No compatible download" :
                            "Checking versions..."),
                    b -> {
                        HubCatalog.Download selected = HubCatalog.compatibleDownload(item.slug());
                        if (selected != null) client.setScreen(
                            new HubLinkScreen(this, selected.url(), "Download " + item.name()));
                    }).dimensions(rightX + 6, buttonY, Math.max(90, rightW - 12), 21).build());
                downloadButton.active = download != null;
                addDrawableChild(ButtonWidget.builder(Text.literal("View on ZOVRAX website"), b ->
                    client.setScreen(new HubLinkScreen(this, HubCatalog.modPage(item.slug()),
                        "Open mod page"))).dimensions(rightX + 6, buttonY - 25,
                        Math.max(90, rightW - 12), 20).build());
            }
        } else {
            HubRegistry.Known mod = selectedInstalled();
            if (mod != null) {
                ButtonWidget settings = addDrawableChild(ButtonWidget.builder(Text.literal(
                    mod.hasSettings() ? "Open mod settings" : "No settings (resource-only)"),
                    b -> openSettings(mod)).dimensions(rightX + 6, y + h - 61,
                    Math.max(90, rightW - 12), 21).build());
                settings.active = mod.hasSettings();
            }
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(rightX + Math.max(2, rightW - 73), y + h - 33, 66, 21).build());
    }

    private void searchChanged(String text) {
        if (text.equals(query)) return;
        query = text;
        page = 0;
        // Recreate filtered result buttons; retain keyboard focus for continuous typing.
        clearAndInit();
        if (searchField != null) {
            setFocused(searchField);
            searchField.setFocused(true);
            searchField.setCursorToEnd(false);
        }
    }

    private void cycleCategory() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (HubCatalog.Item item : HubCatalog.items()) {
            if (!item.category().isBlank() && !categories.contains(item.category()))
                categories.add(item.category());
        }
        categories.subList(1, categories.size()).sort(String::compareToIgnoreCase);
        int index = categories.indexOf(category);
        category = categories.get((index + 1) % categories.size());
        page = 0;
        selectedId = null;
        clearAndInit();
    }

    private List<HubCatalog.Item> filteredCatalog() {
        String needle = query.toLowerCase(Locale.ROOT).strip();
        List<HubCatalog.Item> matches = new ArrayList<>();
        for (HubCatalog.Item item : HubCatalog.items()) {
            if (!category.equals("All") && !category.equals(item.category())) continue;
            String haystack = (item.name() + " " + item.summary() + " " + item.slug())
                .toLowerCase(Locale.ROOT);
            if (!haystack.contains(needle)) continue;
            matches.add(item);
        }
        matches.sort(newest
            ? Comparator.comparing(HubCatalog.Item::updatedAt).reversed()
                .thenComparing(HubCatalog.Item::name)
            : Comparator.comparing(HubCatalog.Item::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(matches);
    }

    private void switchTab(boolean next) {
        if (explore == next) return;
        explore = next;
        page = 0;
        selectedId = null;
        clearAndInit();
        if (explore) {
            HubCatalog.refresh(this::rebuild);
        }
    }

    private void choose(String id) {
        selectedId = id;
        clearAndInit();
    }

    private void rebuild() {
        if (client != null && client.currentScreen == this) {
            clearAndInit();
        }
    }

    private HubRegistry.Known selectedInstalled() {
        for (HubRegistry.Known mod : installed)
            if (mod.id().equals(selectedId)) return mod;
        return installed.isEmpty() ? null : installed.getFirst();
    }

    private HubCatalog.Item selectedCatalog() {
        for (HubCatalog.Item item : matched)
            if (item.slug().equals(selectedId)) return item;
        return matched.isEmpty() ? null : matched.getFirst();
    }

    private void openSettings(HubRegistry.Known mod) {
        try {
            Screen screen = mod.settings(this);
            if (screen != null && client != null) client.setScreen(screen);
        } catch (ReflectiveOperationException | LinkageError ex) {
            if (client != null) client.setScreen(new HubMessageScreen(this,
                "Cannot open " + mod.name() + " settings",
                "The installed version may not expose its settings screen."));
        }
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x66101520);
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.fill(x + 3, y + 3, x + w - 3, y + 4, 0x668CF8EC);
        ctx.fill(x + 2, y + 49, x + sidebar + 3, y + h - 4, PANEL);
        ctx.fill(rightX, y + 24, x + w - 5, y + h - 5, PANEL);
        ctx.drawTextWithShadow(textRenderer, "ZOVRAX HUB", x + 8, y + 9, CYAN);
        ctx.drawTextWithShadow(textRenderer, explore ? "DISCOVER" : "INSTALLED",
            rightX + 9, y + 9, TEXT);
        if (explore) renderExplore(ctx);
        else renderInstalled(ctx);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderInstalled(DrawContext ctx) {
        HubRegistry.Known mod = selectedInstalled();
        if (mod == null) {
            lines(ctx, "No official ZOVRAX mods installed.", rightX + 12,
                y + 78, rightW - 25, SUB, 4);
            return;
        }
        tile(ctx, mod.name());
        ctx.drawTextWithShadow(textRenderer, shortText(mod.name(), rightW - 82),
            rightX + 71, y + 63, TEXT);
        ctx.drawTextWithShadow(textRenderer, "Version: " + mod.version(),
            rightX + 71, y + 81, SUB);
        lines(ctx, mod.description(), rightX + 12, y + 125, rightW - 25, TEXT, 5);
        lines(ctx, "Settings stay in the original mod. Opening the native editor returns to Hub.",
            rightX + 12, y + 192, rightW - 25, SUB, 5);
    }

    private void renderExplore(DrawContext ctx) {
        HubCatalog.Item item = selectedCatalog();
        ctx.drawTextWithShadow(textRenderer,
            shortText(HubCatalog.status(), sidebar - 16), x + 8, y + h - 43, SUB);
        if (item == null) {
            lines(ctx, HubCatalog.items().isEmpty() ?
                "Catalog unavailable. Connect to the internet and click Refresh." :
                "No mods match this search or category.", rightX + 12,
                y + 75, rightW - 25, SUB, 6);
            return;
        }
        tile(ctx, item.name());
        ctx.drawTextWithShadow(textRenderer, shortText(item.name(), rightW - 81),
            rightX + 71, y + 63, TEXT);
        ctx.drawTextWithShadow(textRenderer, "Category: " + item.category(),
            rightX + 71, y + 81, SUB);
        lines(ctx, item.summary(), rightX + 12, y + 124, rightW - 25, TEXT, 5);
        int yy = y + 195;
        ctx.drawTextWithShadow(textRenderer, "Updated: " + item.updatedAt(),
            rightX + 12, yy, SUB);
        ctx.drawTextWithShadow(textRenderer, "Latest: " +
            (item.latestVersion().isBlank() ? "Not released" : item.latestVersion()),
            rightX + 12, yy + 15, SUB);
        boolean listed = item.listedForCurrentVersion();
        ctx.drawTextWithShadow(textRenderer,
            "Minecraft " + HubCatalog.VERSION + ": " +
                (listed ? "listed" : "not listed"),
            rightX + 12, yy + 30, listed ? CYAN : 0xFFFFB6A2);
        if (HubCatalog.detailReady(item.slug())) {
            HubCatalog.Download dl = HubCatalog.compatibleDownload(item.slug());
            ctx.drawTextWithShadow(textRenderer,
                dl == null ? "No confirmed Fabric download" : "Fabric download: " + dl.version(),
                rightX + 12, yy + 45, dl == null ? 0xFFFFB6A2 : CYAN);
        }
    }

    private void tile(DrawContext ctx, String name) {
        int bx = rightX + 12, by = y + 55;
        ctx.fill(bx, by, bx + 51, by + 51, 0xC4498197);
        ctx.fill(bx + 2, by + 2, bx + 49, by + 49, 0xC9213D56);
        String initials = name.isEmpty() ? "Z" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        ctx.drawCenteredTextWithShadow(textRenderer, initials, bx + 25, by + 22, CYAN);
    }

    private String shortText(String value, int pixels) {
        if (textRenderer.getWidth(value) <= pixels) return value;
        String result = value;
        while (!result.isEmpty() && textRenderer.getWidth(result + "...") > pixels)
            result = result.substring(0, result.length() - 1);
        return result + "...";
    }

    private void lines(DrawContext ctx, String text, int left, int top,
                       int availableWidth, int color, int maxLines) {
        String[] words = text.split("\\s+");
        String current = "";
        int row = 0;
        for (String word : words) {
            String combined = current.isEmpty() ? word : current + " " + word;
            if (textRenderer.getWidth(combined) <= availableWidth || current.isEmpty()) {
                current = combined;
            } else {
                ctx.drawTextWithShadow(textRenderer, shortText(current, availableWidth),
                    left, top + row * 13, color);
                row++;
                if (row >= maxLines) return;
                current = word;
            }
        }
        if (!current.isEmpty() && row < maxLines)
            ctx.drawTextWithShadow(textRenderer, shortText(current, availableWidth),
                left, top + row * 13, color);
    }

    @Override public void close() {
        if (client != null) client.setScreen(parent);
    }
}

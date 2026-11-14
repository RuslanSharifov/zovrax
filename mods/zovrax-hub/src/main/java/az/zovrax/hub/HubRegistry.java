package az.zovrax.hub;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screen.Screen;

/**
 * Curated identities: a random mod cannot opt in just by naming itself "ZOVRAX".
 * Legacy released JARs use their known IDs and author metadata; future JARs
 * may additionally declare custom.zovraxHub for an explicit API contract.
 * This is a curated UI list, not a cryptographic signature verification.
 */
public final class HubRegistry {
    private HubRegistry() {}

    public record Known(String id, String name, String slug, String screenClass, String description) {
        public boolean installed() {
            return FabricLoader.getInstance().getModContainer(id).filter(mod ->
                mod.getMetadata().getAuthors().stream().anyMatch(author -> {
                    String name = author.getName();
                    return "Ruslan Sharifov".equalsIgnoreCase(name)
                        || "ruslan".equalsIgnoreCase(name);
                })
            ).isPresent();
        }

        public String version() {
            return FabricLoader.getInstance().getModContainer(id)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("");
        }

        public boolean hasSettings() { return screenClass != null; }

        public Screen settings(Screen parent) throws ReflectiveOperationException {
            if (screenClass == null) return null;
            if (!installed()) return null;
            // The class name comes only from our built-in allowlist, never from remote JSON.
            Class<?> type = Class.forName(screenClass);
            if (!Screen.class.isAssignableFrom(type))
                throw new ReflectiveOperationException("Not a Minecraft Screen");
            Constructor<?> constructor = type.getConstructor(Screen.class);
            return (Screen) constructor.newInstance(parent);
        }
    }

    private static final List<Known> OFFICIAL = List.of(
        new Known("zovrax_armorline", "ArmorLine", "armorline",
            "az.zovrax.armorline.ArmorSettingsScreen", "Armor layout, durability, scale and position"),
        new Known("zovrax_chroma_outline", "Chroma Outline", "chroma-outline",
            "az.zovrax.chromaoutline.ChromaOutlineScreen", "Block outline colors and animation"),
        new Known("clear_connected_glass", "Clear Connected Glass", "clear-connected-glass",
            null, "Resource-only glass pack; no settings screen"),
        new Known("zovrax_dayclock", "DayClock HUD", "dayclock",
            "az.zovrax.dayclock.DayClockSettingsScreen", "Clock, day counter and HUD options"),
        new Known("zovrax_extended_reach", "Extended Reach", "extended-reach",
            "az.zovrax.extendedreach.ReachSettingsScreen", "Client request for server-side reach distance"),
        new Known("zovrax_harvestpulse", "HarvestPulse", "harvest-pulse",
            "az.zovrax.harvestpulse.HarvestPulseScreen", "Field selection and farming notifications"),
        new Known("zovrax_inventory_pickup", "Inventory Pickup", "inventory-pickup",
            "az.zovrax.inventorypickup.PickupSettingsScreen", "Pickup notification appearance and placement"),
        new Known("zovrax_smart_return", "SmartReturn", "smart-return",
            "az.zovrax.smartreturn.SmartReturnSettings", "Route guidance and HUD appearance"),
        new Known("zovrax_smooth_doors", "Smooth Doors", "smooth-doors",
            "az.zovrax.smoothdoors.SmoothDoorsSettingsScreen", "Door and trapdoor animation")
    );

    public static List<Known> installed() {
        List<Known> found = new ArrayList<>();
        for (Known known : OFFICIAL) if (known.installed()) found.add(known);
        return found;
    }
}

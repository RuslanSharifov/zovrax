package az.zovrax.extendedreach.compat;

import az.zovrax.extendedreach.ReachSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Loaded by the optional Mod Menu entrypoint, never by Fabric directly. */
public final class ExtendedReachModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReachSettingsScreen::new;
    }
}

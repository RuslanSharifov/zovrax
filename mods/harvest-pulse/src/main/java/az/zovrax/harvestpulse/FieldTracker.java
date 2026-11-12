package az.zovrax.harvestpulse;

import net.minecraft.client.MinecraftClient;

/** The client NEVER estimates growth using unloaded chunks; only trusted server snapshots. */
public final class FieldTracker {
    private static String scope = "";
    private FieldTracker() {}
    public static String worldKey(MinecraftClient mc) {
        if (mc.isIntegratedServerRunning() && mc.getServer()!=null)
            return "singleplayer:"+mc.getServer().getSaveProperties().getLevelName();
        if (mc.getCurrentServerEntry()!=null) return "server:"+mc.getCurrentServerEntry().address;
        return "unknown";
    }
    public static String dimension(MinecraftClient mc) {
        return mc.world==null?"":mc.world.getRegistryKey().getValue().toString();
    }
    public static boolean isSameWorld(HarvestPulseConfig.Field f,MinecraftClient mc) {
        return mc.world!=null&&f.worldKey.equals(worldKey(mc));
    }
    public static void tick(MinecraftClient mc) {
        String next = mc.world == null ? "" : worldKey(mc);
        if(!next.equals(scope)) {
            scope=next;
            for (HarvestPulseConfig.Field f:HarvestPulseClient.config().fields) {
                f.sampled=false;f.loaded=false;f.mature=0;f.total=0;f.lastSampleMillis=0;
            }
        }
        // Don't present old snapshots after disconnection or server pauses.
        long now=System.currentTimeMillis();
        for (HarvestPulseConfig.Field f:HarvestPulseClient.config().fields)
            if (f.sampled && now-f.lastSampleMillis>10_000) {f.sampled=false;f.loaded=false;}
    }
}

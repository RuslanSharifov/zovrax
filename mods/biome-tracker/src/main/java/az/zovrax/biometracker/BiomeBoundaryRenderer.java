package az.zovrax.biometracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public final class BiomeBoundaryRenderer {
    private static long lastScan = Long.MIN_VALUE;
    private BiomeBoundaryRenderer() {}

    /**
     * Marks sampled biome transitions with subtle particles. The scan is client-side,
     * bounded to a 24-block radius and throttled to avoid expensive per-frame work.
     */
    public static void showNearby(MinecraftClient client, BlockPos center, String centerBiome) {
        if (client.world == null || client.player == null) return;
        long gameTime = client.world.getTime();
        if (gameTime == lastScan) return;
        lastScan = gameTime;

        int radius = 24;
        int step = 2;
        int y = center.getY();
        for (int x = -radius; x <= radius; x += step) {
            for (int z = -radius; z <= radius; z += step) {
                int worldX = center.getX() + x;
                int worldZ = center.getZ() + z;
                BlockPos sample = new BlockPos(worldX, y, worldZ);
                String here = client.world.getBiome(sample).getKey()
                    .map(key -> key.getValue().toString()).orElse("");
                if (here.isEmpty()) continue;
                String east = client.world.getBiome(sample.east(step)).getKey()
                    .map(key -> key.getValue().toString()).orElse("");
                String south = client.world.getBiome(sample.south(step)).getKey()
                    .map(key -> key.getValue().toString()).orElse("");
                if ((!here.equals(east) || !here.equals(south)) &&
                    (here.equals(centerBiome) || east.equals(centerBiome) || south.equals(centerBiome))) {
                    client.world.addParticle(ParticleTypes.END_ROD, false, false,
                        worldX + 0.5, y + 0.15, worldZ + 0.5, 0, 0.002, 0);
                }
            }
        }
    }
}

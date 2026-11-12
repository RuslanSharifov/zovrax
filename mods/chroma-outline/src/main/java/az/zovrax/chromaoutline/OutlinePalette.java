package az.zovrax.chromaoutline;

/** Integrates standalone color math with client settings. */
public final class OutlinePalette {
    private OutlinePalette() {}

    public static int resolve(ChromaOutlineConfig config, long nowNanos) {
        double seconds = nowNanos / 1_000_000_000.0;
        int color = "RAINBOW".equals(config.mode)
            ? ColorMath.hsv(seconds * config.rainbowSpeed / 60.0,
                config.rainbowSaturation / 100.0, config.rainbowBrightness / 100.0, config.opacity)
            : ColorMath.argb(config.red, config.green, config.blue, config.opacity);
        if (config.pulseEnabled) {
            double wave = (1.0 + Math.sin(seconds * config.pulseSpeed * Math.PI / 10.0)) / 2.0;
            double brightness = 1.0 - config.pulseDepth / 100.0 * wave;
            color = ColorMath.scaleBrightness(color, brightness);
        }
        return color;
    }
}

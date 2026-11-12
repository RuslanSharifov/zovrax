package az.zovrax.chromaoutline;

/** Pure Java color math so calculations can be tested without a Minecraft runtime. */
public final class ColorMath {
    private ColorMath() {}

    public static int argb(int r, int g, int b, int opacity) {
        return (Math.clamp(Math.round(opacity * 255f / 100f), 0, 255) << 24)
            | (Math.clamp(r, 0, 255) << 16)
            | (Math.clamp(g, 0, 255) << 8)
            | Math.clamp(b, 0, 255);
    }

    public static int hsv(double hue, double saturation, double value, int opacity) {
        hue = ((hue % 1.0) + 1.0) % 1.0;
        saturation = Math.clamp(saturation, 0.0, 1.0);
        value = Math.clamp(value, 0.0, 1.0);
        double h = hue * 6.0;
        int sector = (int) h;
        double f = h - sector;
        double p = value * (1.0 - saturation);
        double q = value * (1.0 - f * saturation);
        double t = value * (1.0 - (1.0 - f) * saturation);
        double r, g, b;
        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }
        return argb((int)Math.round(r * 255), (int)Math.round(g * 255), (int)Math.round(b * 255), opacity);
    }

    public static int scaleBrightness(int argb, double amount) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        return (a << 24)
            | (Math.clamp((int)Math.round(r * amount), 0, 255) << 16)
            | (Math.clamp((int)Math.round(g * amount), 0, 255) << 8)
            | Math.clamp((int)Math.round(b * amount), 0, 255);
    }

    public static int resolve(ChromaOutlineConfig config, long nowNanos) {
        double seconds = nowNanos / 1_000_000_000.0;
        int color = "RAINBOW".equals(config.mode)
            ? hsv(seconds * config.rainbowSpeed / 60.0,
                config.rainbowSaturation / 100.0, config.rainbowBrightness / 100.0, config.opacity)
            : argb(config.red, config.green, config.blue, config.opacity);
        if (config.pulseEnabled) {
            double wave = (1.0 + Math.sin(seconds * config.pulseSpeed * Math.PI / 10.0)) / 2.0;
            double brightness = 1.0 - config.pulseDepth / 100.0 * wave;
            color = scaleBrightness(color, brightness);
        }
        return color;
    }
}

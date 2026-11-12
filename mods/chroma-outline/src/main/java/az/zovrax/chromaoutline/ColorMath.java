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

}

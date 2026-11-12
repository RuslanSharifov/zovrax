import az.zovrax.chromaoutline.ColorMath;

public final class ColorMathCheck {
    public static void main(String[] args) {
        expect(ColorMath.argb(255,0,0,100) == 0xFFFF0000, "opaque red");
        expect(ColorMath.argb(0,255,0,0) == 0x0000FF00, "transparent green");
        expect(ColorMath.hsv(0.0,1.0,1.0,100) == 0xFFFF0000, "rainbow start");
        expect(ColorMath.hsv(1.0/3.0,1.0,1.0,100) == 0xFF00FF00, "rainbow green");
        expect(ColorMath.hsv(2.0/3.0,1.0,1.0,100) == 0xFF0000FF, "rainbow blue");
        expect(ColorMath.scaleBrightness(0x80FF8040,0.5) == 0x80804020, "pulse dim");
        System.out.println("Color math checks passed");
    }
    private static void expect(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
}

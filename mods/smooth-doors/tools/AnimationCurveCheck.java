import az.zovrax.smoothdoors.AnimationCurve;

public class AnimationCurveCheck {
    private static void near(float found, float expected) {
        if (Math.abs(found - expected) > 0.00001f)
            throw new AssertionError(found + " != " + expected);
    }
    public static void main(String[] args) {
        near(AnimationCurve.smooth(-1), 0);
        near(AnimationCurve.smooth(0), 0);
        near(AnimationCurve.smooth(0.5f), 0.5f);
        near(AnimationCurve.smooth(1), 1);
        near(AnimationCurve.smooth(2), 1);
        near(AnimationCurve.position(0, 1, 175_000_000L, 350_000_000L), 0.5f);
        near(AnimationCurve.position(1, 0, 175_000_000L, 350_000_000L), 0.5f);
        near(AnimationCurve.position(1, 0, 900_000_000L, 350_000_000L), 0f);
        if (AnimationCurve.durationNs(350, 0.5f, 1f) != 175_000_000L)
            throw new AssertionError("reverse must preserve angular speed");
        System.out.println("Smooth Doors animation math: OK");
    }
}

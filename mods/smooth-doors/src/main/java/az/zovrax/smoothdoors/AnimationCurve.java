package az.zovrax.smoothdoors;

/** Frame-independent interpolation: smooth transitions and correct reversal duration. */
public final class AnimationCurve {
    private AnimationCurve() {}

    public static float clamp01(float f) {
        return Math.min(1f, Math.max(0f, f));
    }

    public static float smooth(float progress) {
        float t = clamp01(progress);
        return t * t * (3f - 2f * t);
    }

    public static float position(float from, float to, long elapsedNs, long durationNs) {
        if (durationNs <= 0) return to;
        float t = clamp01((float) ((double) elapsedNs / durationNs));
        return from + (to - from) * smooth(t);
    }

    public static long durationNs(int fullDurationMs, float from, float to) {
        return Math.max(1L, Math.round(fullDurationMs * 1_000_000d * Math.abs(to - from)));
    }
}

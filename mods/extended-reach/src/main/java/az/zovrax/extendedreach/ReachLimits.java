package az.zovrax.extendedreach;

/** Server and client use the exact same supported slider bounds. */
public final class ReachLimits {
    public static final int MIN = 5;
    public static final int MAX = 16;
    public static final int DEFAULT = 8;
    private ReachLimits() {}

    public static int clamp(int requested) {
        return Math.clamp(requested, MIN, MAX);
    }
}

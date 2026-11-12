package az.zovrax.harvestpulse;

/** Dimension/crop-independent rules, also exercised by a standalone Java test. */
public final class FieldMath {
    public static final int MAX_EDGE = 16;
    public static final int MAX_VERTICAL_SPAN = 2;
    public static final int MAX_FIELDS = 8;
    private FieldMath() { }

    public static boolean validArea(int ax, int ay, int az, int bx, int by, int bz) {
        long width = Math.abs((long) ax - bx) + 1;
        long depth = Math.abs((long) az - bz) + 1;
        long height = Math.abs((long) ay - by);
        return width <= MAX_EDGE && depth <= MAX_EDGE && height <= MAX_VERTICAL_SPAN;
    }

    /** Probability of at least one vanilla random-tick selection for a single crop in elapsed ticks. */
    public static double randomTickChance(int speed, int elapsed) {
        if (speed <= 0 || elapsed <= 0) return 0.0;
        return 1.0 - Math.pow(1.0 - 1.0 / 4096.0, (double) Math.min(speed, 4096) * elapsed);
    }

    /** Empty fields are 0%, not 100% or ready. */
    public static int percent(int mature, int total) {
        if (total <= 0) return 0;
        return Math.clamp((int) Math.round((double) mature * 100d / total), 0, 100);
    }

    public static boolean ready(int mature, int total) {
        return total > 0 && mature >= total;
    }

    public static boolean ageAtMaximum(int age, int maxAge) {
        return maxAge > 0 && age >= maxAge;
    }
}

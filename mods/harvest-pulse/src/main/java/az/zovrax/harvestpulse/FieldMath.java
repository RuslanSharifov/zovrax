package az.zovrax.harvestpulse;

/** Dimension/crop-independent rules, also exercised by a standalone Java test. */
public final class FieldMath {
    public static final int MAX_EDGE = 128;
    public static final int MAX_VERTICAL_SPAN = 32;
    public static final int MAX_FIELDS = 8;
    private FieldMath() { }

    public static boolean validArea(int ax, int ay, int az, int bx, int by, int bz) {
        return validArea(ax, ay, az, bx, by, bz, MAX_EDGE);
    }

    public static boolean validArea(int ax, int ay, int az, int bx, int by, int bz, int limit) {
        long width = Math.abs((long) ax - bx) + 1;
        long depth = Math.abs((long) az - bz) + 1;
        long height = Math.abs((long) ay - by);
        return limit >= 16 && limit <= MAX_EDGE && width <= limit && depth <= limit
            && height <= MAX_VERTICAL_SPAN;
    }

    public static int nextLimit(int value) {
        return switch (value) { case 16 -> 32; case 32 -> 64; case 64 -> 128; default -> 16; };
    }

    /** Rectangular overlap in the horizontal plane, regardless of corner order. */
    public static boolean overlaps(int ax1,int az1,int ax2,int az2,
                                   int bx1,int bz1,int bx2,int bz2) {
        return Math.max(Math.min(ax1,ax2),Math.min(bx1,bx2)) <=
                   Math.min(Math.max(ax1,ax2),Math.max(bx1,bx2))
            && Math.max(Math.min(az1,az2),Math.min(bz1,bz2)) <=
                   Math.min(Math.max(az1,az2),Math.max(bz1,bz2));
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

package az.zovrax.harvestpulse;

/** Pure range/progress arithmetic shared by UI and server, tested without Minecraft. */
public final class FieldMath {
    public static final int MAX_EDGE = 128;
    public static final int MAX_VERTICAL_SPAN = 32;
    public static final int MAX_FIELDS = 8;
    public static final int[] SIZES = {16, 32, 64, 128};
    private FieldMath() { }

    public static boolean validLimit(int side) {
        for (int n : SIZES) if (n == side) return true;
        return false;
    }
    public static boolean validArea(int ax,int ay,int az,int bx,int by,int bz) {
        return validArea(ax,ay,az,bx,by,bz,MAX_EDGE);
    }
    public static boolean validArea(int ax,int ay,int az,int bx,int by,int bz,int side) {
        long w = Math.abs((long) ax-bx)+1;
        long d = Math.abs((long) az-bz)+1;
        long h = Math.abs((long) ay-by);
        return validLimit(side) && w <= side && d <= side && h <= MAX_VERTICAL_SPAN;
    }
    public static boolean overlaps(int ax1,int az1,int ax2,int az2,int bx1,int bz1,int bx2,int bz2) {
        return Math.max(Math.min(ax1,ax2),Math.min(bx1,bx2)) <= Math.min(Math.max(ax1,ax2),Math.max(bx1,bx2)) &&
            Math.max(Math.min(az1,az2),Math.min(bz1,bz2)) <= Math.min(Math.max(az1,az2),Math.max(bz1,bz2));
    }
    /** Probability of at least one vanilla random tick over an interval. */
    public static double randomTickChance(int speed,int elapsed) {
        if(speed <= 0 || elapsed <= 0) return 0;
        return 1.0-Math.pow(1.0-1.0/4096.0, (double)Math.min(speed,4096)*elapsed);
    }
    /** Percent reaches 100 only when every crop slot is mature (no rounding 99.8 to 100). */
    public static int percent(int mature,int total) {
        if(total <= 0) return 0;
        if(mature >= total) return 100;
        return Math.clamp((int)Math.floor(100.0*Math.max(0,mature)/total),0,99);
    }
    public static boolean ready(int mature,int total) { return total>0 && mature>=total; }
    public static boolean ageAtMaximum(int age,int maxAge) { return maxAge>0 && age>=maxAge; }
}

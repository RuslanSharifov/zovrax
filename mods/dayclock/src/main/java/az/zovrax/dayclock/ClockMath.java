package az.zovrax.dayclock;

/** Minecraft time: tick 0 is 06:00; 24,000 ticks make one in-game day. */
public final class ClockMath {
    private ClockMath() { }

    public static long completedDays(long timeOfDay) {
        return Math.floorDiv(timeOfDay, 24000L);
    }

    public static String clock(long timeOfDay) {
        long tick = Math.floorMod(timeOfDay, 24000L);
        long minutes = ((tick + 6000L) * 1440L / 24000L) % 1440L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60L, minutes % 60L);
    }

    /** Day count is world elapsed days, displayed one-based (first day = 1). */
    public static long displayedDay(long timeOfDay) {
        return completedDays(timeOfDay) + 1L;
    }
}

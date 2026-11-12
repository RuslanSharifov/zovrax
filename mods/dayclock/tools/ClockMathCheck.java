import az.zovrax.dayclock.ClockMath;
public final class ClockMathCheck {
    private static void equals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        equals("06:00", ClockMath.clock(0));
        equals("12:00", ClockMath.clock(6000));
        equals("18:32", ClockMath.clock(12542));
        equals("00:00", ClockMath.clock(18000));
        equals("05:00", ClockMath.clock(23000));
        equals("06:00", ClockMath.clock(24000));
        equals(1L, ClockMath.displayedDay(0));
        equals(1L, ClockMath.displayedDay(23999));
        equals(2L, ClockMath.displayedDay(24000));
        System.out.println("DayClock clock and day conversion tests passed");
    }
}

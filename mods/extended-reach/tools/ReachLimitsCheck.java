import az.zovrax.extendedreach.ReachLimits;

public final class ReachLimitsCheck {
    public static void main(String[] args) {
        check(-999, 5);
        check(0, 5);
        check(5, 5);
        check(8, 8);
        check(16, 16);
        check(17, 16);
        check(Integer.MAX_VALUE, 16);
        if (ReachLimits.DEFAULT < ReachLimits.MIN || ReachLimits.DEFAULT > ReachLimits.MAX)
            throw new AssertionError("Default out of bounds");
        System.out.println("Extended Reach range tests passed");
    }
    private static void check(int requested, int expected) {
        int actual = ReachLimits.clamp(requested);
        if (actual != expected)
            throw new AssertionError(requested + " => " + actual + ", expected " + expected);
    }
}

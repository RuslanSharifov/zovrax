import az.zovrax.harvestpulse.FieldMath;
public final class FieldMathCheck {
  private static void check(boolean condition, String msg) { if (!condition) throw new AssertionError(msg); }
  public static void main(String[] args) {
    check(FieldMath.validArea(0,64,0,7,64,7), "8x8 works");
    check(FieldMath.validArea(-20,70,-20,-13,70,-13), "negative coords work");
    check(!FieldMath.validArea(-20,70,-20,20,70,20), "large area rejected");
    check(!FieldMath.validArea(0,70,0,64,70,0), "oversize rejected");
    check(!FieldMath.validArea(0,70,0,1,77,1), "vertical span rejected");
    check(FieldMath.percent(32,64)==50, "32/64=50%");
    check(FieldMath.percent(34,64)==53, "rounds to nearest");
    check(FieldMath.percent(0,0)==0 && !FieldMath.ready(0,0), "empty not ready");
    check(FieldMath.ready(64,64) && !FieldMath.ready(63,64), "ready iff all crops mature");
    check(FieldMath.ageAtMaximum(7,7) && !FieldMath.ageAtMaximum(6,7), "age maturity");
    check(FieldMath.randomTickChance(0,20)==0d, "randomTickSpeed 0 disables growth");
    check(FieldMath.randomTickChance(3,20)>0.014 && FieldMath.randomTickChance(3,20)<0.015, "vanilla random tick probability");
    System.out.println("FieldMathCheck passed");
  }
}

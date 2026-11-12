import az.zovrax.harvestpulse.FieldMath;
public final class FieldMathCheck {
  private static void check(boolean condition, String msg) { if (!condition) throw new AssertionError(msg); }
  public static void main(String[] args) {
    check(FieldMath.validArea(0,64,0,7,64,7), "8x8 works");
    check(FieldMath.validArea(-20,70,-20,-13,70,-13), "negative coords work");
    check(!FieldMath.validArea(-20,70,-20,20,70,20,16), "16x16 selection rejects oversized region");
    check(FieldMath.validArea(-20,70,-20,20,70,20,64), "64x64 selection accepts 41x41 region");
    check(!FieldMath.validArea(0,70,0,128,70,0,128), "129-wide rejected");
    check(FieldMath.validArea(0,70,0,127,70,127,128), "128x128 permitted");
    check(FieldMath.validArea(0,70,0,1,77,1,16), "uneven land up to 32 height accepted");
    check(!FieldMath.validArea(0,70,0,1,103,1,16), "height >32 rejected");
    check(FieldMath.nextLimit(16)==32 && FieldMath.nextLimit(32)==64 && FieldMath.nextLimit(64)==128 && FieldMath.nextLimit(128)==16, "limit cycling");
    check(FieldMath.overlaps(0,0,7,7,5,5,12,12), "overlap detected");
    check(!FieldMath.overlaps(0,0,7,7,8,8,15,15), "adjacent regions allowed");
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

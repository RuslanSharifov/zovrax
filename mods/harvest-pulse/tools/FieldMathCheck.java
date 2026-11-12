import az.zovrax.harvestpulse.FieldMath;
public class FieldMathCheck {
    public static void main(String[] args) {
        if(FieldMath.percent(32,64)!=50||FieldMath.percent(0,64)!=0||FieldMath.percent(64,64)!=100) throw new AssertionError("percent");
        if(FieldMath.percent(1023,1024)!=99||FieldMath.ready(1023,1024)||!FieldMath.ready(1024,1024)) throw new AssertionError("rounding");
        if(FieldMath.ready(0,0)||FieldMath.percent(0,0)!=0) throw new AssertionError("empty");
        for(int n:FieldMath.SIZES) {
            if(!FieldMath.validArea(0,64,0,n-1,96,n-1,n)) throw new AssertionError("valid "+n);
            if(FieldMath.validArea(0,64,0,n,64,n,n)) throw new AssertionError("overflow "+n);
        }
        if(FieldMath.validArea(0,64,0,128,64,128)||FieldMath.validArea(0,64,0,15,64,15,18)) throw new AssertionError("bad dimension/limit");
        if(!FieldMath.overlaps(0,0,15,15,15,15,19,20)||FieldMath.overlaps(0,0,2,2,3,3,4,4)) throw new AssertionError("overlap");
        if(FieldMath.randomTickChance(0,1200)!=0||FieldMath.randomTickChance(3,1200)<=0) throw new AssertionError("tick");
        System.out.println("HarvestPulse 0.2.0 math checks passed");
    }
}

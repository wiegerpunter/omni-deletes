package omni;

public class Formulas {

    static double ramOmniKmin(int numAttrsToUse, int depth, int width, int B, int smallb) {
        //return (double) depth * width * numAttrsToUse * (B * (smallb + Math.log(B) + 3 * 32 + 1) + 32);
        return (double) depth * width * numAttrsToUse * (B * smallb + 32);

    }

    public static long ramSingleKmin(int B, int smallb) {
        //return (long) (B * (smallb + Math.log(B) + 3 * 32 + 1) + 32);
        return (long) B * smallb + 32;
    }

    static int smallb(int B, double delta, int depth) {
        return 31;////(Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/delta)));
    }

    static int ramOmniTwoLHS(int numAttrsToUse, int depth, int width, int numTwoLHSReps) {
        return depth * width * numAttrsToUse * (numTwoLHSReps * 31 * 32 * 32 + 32);
        //return depth * width * numAttrsToUse * (numTwoLHSReps * 31 * 32 + 32);
    }
}

package omni;

public class Formulas {

    static double ramOmniKmin(int numAttrsToUse, int depth, int width, int B, int smallb) {
        return (double) depth * width * numAttrsToUse * (B * (smallb + 3 * 32 + 1) + 32);
    }

    public static long ramSingleKmin(int B, int smallb) {
        return B * (smallb + 3 * 32 + 1) + 32;
    }

    static int smallb(int B, double delta) {
        return (int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/delta));
    }

    static int ramOmniTwoLHS(int numAttrsToUse, int depth, int width, int numTwoLHSReps) {
        return depth * width * numAttrsToUse * (numTwoLHSReps * 31 * 32 * 32 + 32);
    }
}

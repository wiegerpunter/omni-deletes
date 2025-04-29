package omni.parameterSetting;

import omni.Main;

public class DetermineB {

    private final long ram;
    private int depth;
    private int width;
    private int numAttrsToUse;
    private double delta;
    private int b;
    private int maxB = Integer.MAX_VALUE;
    private int signatureFactor = 1;
    private boolean compb = true;
    public DetermineB(long ram) {
        this.ram = ram;
    }

    public int determineB(int depth, int width, int p, double delta, int b, int signatureFactor) {
        this.depth = depth;
        this.width = width;
        this.numAttrsToUse = p;
        this.delta = delta;
        this.b = b;
        this.signatureFactor = signatureFactor;
        if (b > 0) {
            compb = false;
            maxB = (int) Math.pow(2, b);
        }
        return search(0, 2, 0);
    }

    int highB = Integer.MAX_VALUE;
    public int search(int prevB, int B, int iters) {
        if (B <= 1) {
            return B;
        }
        if (B > maxB) {
            // Here, we can return maxB, but if ram is not reached, we can still increase w.
            if (compRam(maxB) < ram) {
                return maxB;
            } else {
                B = maxB;
            }
        }
        if (iters > 100){
            if (compRam(prevB) < ram) {
//                System.out.println("Returned after " + iters + " iterations with ram = " + compRam(prevB) +
//                        " Could have used " + ram + " delta is " + (compRam(prevB) - ram));
//                System.out.println("B = " + prevB);
//                if (compb) {
//                    System.out.println("small b = " + Formulas.smallb(prevB, delta));
//                } else {
//                    System.out.println("small b = " + b);
//                }
//                System.out.println("ram = " + compRam(prevB)/(8*Math.pow(10, 6)) + " MB");
                return prevB;
            } else {
                throw new RuntimeException("Too many iterations");
            }
        }
        iters++;
        double usedM = compRam(B);

        if (ram*0.999999 <= usedM && usedM <= ram) {
            System.out.println("Returned after " + iters + " iterations with ram = " + compRam(B) +
                    " Could have used " + ram + " delta is " + (compRam(B) - ram));
            return B;
        }else if (usedM < ram*0.999999) {
            return search(B, B*2, iters);
        } else {
            return search(prevB, (int) Math.ceil((double) (B + prevB)/2), iters);
        }
//        } else if (usedM < ram*0.99) {
//            if (highB == Integer.MAX_VALUE)
//                return search(B, B*2, iters);
//            else
//                return search(B, Math.min(highB, B*2), iters);
//        } else {
//            highB = B;
//            return search(prevB, (int) Math.ceil((double) (B + prevB)/2), iters);
//        }
    }

    public double compRam(int B) {
        int smallb;
        if (compb) {
            smallb = Formulas.smallb(B, delta, depth);//int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/delta));
            // No max B check, since small b can still be increased.
        } else {
            smallb = b;
        }

        //return (double) depth * width*(B * (smallb) + 32) * numAttrsToUse;
        if (Main.rangeQueries) {
            return Main.dyadicRangeBits /Math.log(2) * Formulas.ramOmniKmin(numAttrsToUse, depth, width, B, smallb);
            // * depth * width*(B * (smallb + 3 * 32 + 1) + 32) * numAttrsToUse;
        } else {
            return Formulas.ramOmniKmin(numAttrsToUse, depth, width, B, smallb);//double) depth * width * numAttrsToUse *(B * (smallb + 3 * 32 + 1) + 32);
        }
    }


}

package omni.Experiments.parameterSetting;

public class DetermineBKmin {

    private final long ram;
    private int numAttrsToUse;
    private double delta;
    public DetermineBKmin(long ram) {
        this.ram = ram;
    }

    public int determineB(int p, double delta) {
        this.numAttrsToUse = p;
        this.delta = delta;
        return search(0, 2, 0);
    }
    int highB = Integer.MAX_VALUE;
    public int search(int prevB, int B, int iters) {
        if (B <= 1) {
            return B;
        }
        if (iters > 100){
            if (compRam(prevB) < ram) {
                return prevB;
            } else {
                throw new RuntimeException("Too many iterations");
            }
        }
        iters++;
        double usedM = compRam(B);

        if (ram*0.99 <= usedM && usedM <= ram) {
            return B;
        } else if (usedM < ram*0.99) {
            if (highB == Integer.MAX_VALUE)
                return search(B, B*2, iters);
            else
                return search(B, Math.min(highB, B*2), iters);
        } else {
            highB = B;
            return search(prevB, (int) Math.ceil((double) (B + prevB)/2), iters);
        }
    }

    public double compRam(int B) {
        int smallb = Formulas.smallb(B, delta, 1);//int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/delta));

        //return (double) depth * width*(B * (smallb) + 32) * numAttrsToUse;

        return Formulas.ramSingleKmin(B, smallb);//double) (B * (smallb + 3 * 32 + 1 + 64)) * numAttrsToUse + 32;
    }


}

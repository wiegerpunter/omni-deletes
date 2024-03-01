package omni.omniTwoLHS;
import omni.Main;

import java.util.Random;

public class CountMin {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    TWOLHS[][][] CMTwoLHS;
    Kmin[][] CMKmin;
    final int depth;
    final int width;
    final int maxSize;
    final int b;
    int numTwoLHSReps;

    int attr;
    final Random rn = new Random(Main.repetition);


    private int[] hash_a;
    private int[] hash_b;
    private int[] hash_c;
    final boolean useTwoLHS;
    final boolean twoLHSFast;
    final boolean useTwoKmin;

    public CountMin(int attr, int[] parameters,
                    boolean useTwoLHS, boolean useTwoKmin, boolean twoLHSFast){//int depth, int width, int numTwoLHSReps, int B, int b) {
        this.attr = attr;
        this.depth = parameters[0];
        this.width = parameters[1];
        this.useTwoLHS = useTwoLHS;
        this.useTwoKmin = useTwoKmin;
        this.twoLHSFast = twoLHSFast;
        if (useTwoLHS) {
            this.numTwoLHSReps = parameters[2];
            this.maxSize = -1;
            this.b = -1;
        } else {
            this.maxSize = parameters[2];
            this.b = parameters[3];
            this.numTwoLHSReps = -1;
        }
        initSketch();
    }


    public void initSketch() {

        int[][] primes = Hash.randomPrimes(depth, attr);
        hash_a = primes[0];
        hash_b = primes[1];
        hash_c = primes[2];

        if (useTwoLHS) {
            CMTwoLHS = new TWOLHS[depth][width][numTwoLHSReps];
            for (int j = 0; j < depth; j++) {
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        CMTwoLHS[j][i][k] = new TWOLHS(k);
                    }
                }
            }
        } else {
            CMKmin = new Kmin[depth][width];
            for (int j = 0; j < depth; j++) {
                for (int i = 0; i < width; i++) {
                        CMKmin[j][i] = new Kmin(maxSize, b, useTwoKmin);//Main.withDeletes);
                }
            }
        }
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hashes = new int[depth];
        rn.setSeed(attrValue + Main.repetition);
        for (int i = 0; i < depth; i++) hashes[i] = rn.nextInt(width);
        return hashes;
//        int[] hashes = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            hashes[i] = (int) ((((hash_a[i] * attrValue + hash_b[i]) % hash_c[i]) % width + width) % width);
//            attrValue = hashes[i];
//        }
//        return hashes;
    }

//    public void add(int id, long attrValue) {
//        // Test if all element in A and B are consistent
//        int[] hashes = hash(attrValue, Main.depth, Main.width);
//        for (int j = 0; j < Main.depth; j++) {
//            int w = hashes[j];
//            CM[j][w].add(id); // Hash in Sample based on id
//        }
//    }



    public void ingest(long attrValue, long hx, long[][] vals, int hashG, int sign) {
        // Test if all element in A and B are consistent
        int[] hashes = hash(attrValue, depth, width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            if (useTwoLHS) {
                if (twoLHSFast) {
                    CMTwoLHS[j][w][hashG].ingest(hx, sign); // Hash in Sample  as in section 5 of paper
                } else {
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        CMTwoLHS[j][w][k].ingest(vals[k], sign); // Hash in Sample  as in section 3 of paper
                        //CMTwoLHS[j][w][k].ingest(hx, sign); // Hash in Sample  as in section 3 of paper
                    }
                }

                //
            } else {
                CMKmin[j][w].ingest(hx, sign); // In Kminwise
            }
        }
    }

    public TWOLHS[][] queryTwoLHS(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);

        TWOLHS[][] result;
        result = new TWOLHS[depth][numTwoLHSReps];

        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = CMTwoLHS[j][w];
        }
        return result;
    }
    public Kmin[] queryKmin(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);

        Kmin[] result;
        result = new Kmin[depth];

        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = CMKmin[j][w];
        }
        return result;
    }
//    public Sample[][] query(long attrValue) {
//        int[] hashes = hash(attrValue, depth, width);
//
//        Sample[][] result;
//        result = new Sample[depth][numTwoLHSReps];
//
//        for (int j = 0; j < depth; j++) {
//            int w = hashes[j];
//            result[j] = CM[j][w];
//        }
//        return result;
//    }

    public long getMemoryUsage() {
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                if (useTwoLHS)
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        memoryUsage += CMTwoLHS[j][i][k].getMemoryUsage();
                    }
                else
                    memoryUsage += CMKmin[j][i].getMemoryUsage();
            }
        }
        return memoryUsage;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                if (useTwoLHS) {
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        CMTwoLHS[j][i][k].reset();
                    }
                } else {
                    CMKmin[j][i].reset();
                }
            }
        }
    }

    public int getFilledKSamples() {
        int filledKSamples = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                filledKSamples += CMKmin[j][i].sketch.size();
                }
            }
        return filledKSamples;
    }
}
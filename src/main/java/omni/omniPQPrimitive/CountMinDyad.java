package omni.omniPQPrimitive;
import omni.Main;

import omni.PriorityQueue.PriorityQueue;
import java.util.Random;

public class CountMinDyad {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    Kmin[][] CM;
    TWOLHS[][][] CMTwoLHS;
    int attr;
    long intervalSize;
    final Random rn = new Random();
    final int depth;
    final int width;
    final int maxSize;
    final int b;
    final int numTwoLHSReps;
    final int dyadicRangeBits;
    final boolean useTwoLHS;
    final int seed;
    final double BetaKmin;
    final boolean dynamicResizing;

    int n = 0;

    public CountMinDyad(int attr, long intervalSize, int dyadicRangeBits, int[] parameters,
                        boolean useTwoLHS, boolean dynamicResizing,
                        double BetaKmin, int seed) {
        this.attr = attr;
        this.intervalSize = intervalSize;
        this.dyadicRangeBits = dyadicRangeBits;
        this.depth = parameters[0];
        this.width = parameters[1];
        this.useTwoLHS = useTwoLHS;
        this.dynamicResizing = dynamicResizing;
        this.seed = seed;
        this.BetaKmin = BetaKmin;
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
        if (useTwoLHS)
            CMTwoLHS = new TWOLHS[depth][width][numTwoLHSReps];
        else
            CM = new Kmin[depth][width];

        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                if (useTwoLHS)
                    for (int k = 0; k < numTwoLHSReps; k++)
                        CMTwoLHS[j][i][k] = new TWOLHS(seed, k);
                else
                    CM[j][i] = new Kmin(maxSize, b, Main.withDeletes, BetaKmin, dynamicResizing, seed);

            }
        }
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hash = new int[depth];
        //int attrValueInt = (int) attrValue;
        rn.setSeed(attrValue + seed);
        for (int i = 0; i < depth; i++) {
            hash[i] = rn.nextInt(width);
        }
        return hash;
    }
    public long getRangeSignature(long start, long stop) {
        long sum = start;
        sum<<=dyadicRangeBits;
        sum |= stop;
        return sum;
    }

    public void add(long lower, long higher, int hx) {
        // Test if all element in A and B are consistent
        long sig = getRangeSignature(lower, higher);
        int[] hashes = hash(sig, depth,width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            CM[j][w].add(hx); // Hash in Sample based on id
        }
        n += 1;
    }

    public PriorityQueue[] rangeQuery(long lower, long higher, int[] seenN) {
        PriorityQueue[] set = new PriorityQueue[depth];
        long sig = getRangeSignature(lower, higher);
        int[] hashes = hash(sig, depth, width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            set[j] = PriorityQueue.getCopy(CM[j][w].sketch);
            seenN[j] += CM[j][w].n;
            // Hash in Sample based on id
        }
        return set;
    }
    public long getMemoryUsage() {
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                memoryUsage += CM[j][i].getMemoryUsage();
            }
        }
        return memoryUsage;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i].reset();
            }
        }
    }

    public void testUniformity() {
        System.out.println("Testing Uniformity");
        int[] counts = new int[width];
        for (int i = 0; i < width; i++) {
            counts[i] = 0;
        }
        for (int i = 0; i < 1000000; i++) {
            int[] hashes = hash(i, depth, width);
            for (int j = 0; j < depth; j++) {
                int w = hashes[j];
                counts[w]++;
            }
        }
        for (int i = 0; i < width; i++) {
            System.out.println(counts[i]);
        }
    }


/*
    public int[] testAdd(int id, int attr) {
        return hash(attr, Main.depth, Main.width);
    }

    public void testUniformity() {
        System.out.println("Testing Uniformity");
        int[] counts = new int[Main.width];
        for (int i = 0; i < Main.width; i++) {
            counts[i] = 0;
        }
        for (int i = 0; i < 100000; i++) {
            int[] hashes = hash(i, Main.depth, Main.width);
            for (int j = 0; j < Main.depth; j++) {
                int w = hashes[j];
                counts[w]++;
            }
        }
        for (int i = 0; i < Main.width; i++) {
            System.out.println(counts[i]);
        }
    }*/

    /*
        Input: 1. Attribute
        Output: 2. Arraylist of size depth with all relevant Distinct samples for single attribute
     */
}
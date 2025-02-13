package omni.omniPQPrimitive;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.ByteBuffer;
import java.util.Random;

public class AttributeSketch {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    TWOLHS[][][] CMTwoLHS;
    public Kmin[][] CMKminTreeSet;
    final int depth;
    final int width;
    final int orgMaxSize;
    final int b;
    int numTwoLHSReps;

    int attr;
    Random rn;
    final int seed;
    public static XXHash64 hashFunc = XXHashFactory.fastestInstance().hash64();

//    private int[] hash_a;
//    private int[] hash_b;
//    private int[] hash_c;
    final boolean useTwoLHS;
    final boolean twoLHSFast;
    final boolean useTwoKmin;
    final double BetaKmin;
    final boolean dynamicResizing;
    final boolean dynamicSampleSizes;
    int insertCount = 0;

    HashFunction[] xx;

    public AttributeSketch(int attr, int[] parameters,
                           boolean useTwoLHS, boolean useTwoKmin, double BetaKmin,
                           boolean twoLHSFast, boolean dynamicResizing, boolean dynamicSampleSizes, int seed){
        //int depth, int width, int numTwoLHSReps, int B, int b) {
        this.attr = attr;
        this.depth = parameters[0];
        this.width = parameters[1];
        this.useTwoLHS = useTwoLHS;
        this.useTwoKmin = useTwoKmin;
        this.twoLHSFast = twoLHSFast;
        this.seed = seed;
        this.BetaKmin = BetaKmin;
        this.dynamicResizing = dynamicResizing;
        this.dynamicSampleSizes = dynamicSampleSizes;
        rn = new Random(seed);
        if (useTwoLHS) {
            this.numTwoLHSReps = parameters[2];
            this.orgMaxSize = -1;
            this.b = -1;
        } else {
            this.orgMaxSize = parameters[2];
            this.b = parameters[3];
            this.numTwoLHSReps = -1;
        }
        initSketch();
    }


    public void initSketch() {

//        int[][] primes = Hash.randomPrimes(depth, attr);
//        hash_a = primes[0];
//        hash_b = primes[1];
//        hash_c = primes[2];
//        System.out.println("CM: hash_a = " + hash_a[0]);
//        System.out.println("CM: hash_b = " + hash_b[0]);
//        System.out.println("CM: hash_c = " + hash_c[0]);

        if (useTwoLHS) {
            CMTwoLHS = new TWOLHS[depth][width][numTwoLHSReps];
            for (int j = 0; j < depth; j++) {
                for (int i = 0; i < width; i++) {
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        CMTwoLHS[j][i][k] = new TWOLHS(seed, k);
                    }
                }
            }
        } else {
            CMKminTreeSet = new Kmin[depth][width];
            for (int j = 0; j < depth; j++) {
                for (int i = 0; i < width; i++) {
                    CMKminTreeSet[j][i] = new Kmin(orgMaxSize, b, useTwoKmin, BetaKmin, dynamicResizing, seed);//Main.withDeletes);
                }
            }
        }
        xx = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            xx[i] = Hashing.murmur3_32_fixed(seed + i);
        }
    }

    public byte[] longToBytes(long attrValue) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(attrValue);
        return buffer.array();
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hashes = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            if (i<4){
//                hashes[i] = (int) ((hash_long >> (16 * i)) & 0xffff) % width;
//            } else {
//                hashes[i] = (int) ((hash_long >> (16 * (i) - 1)) & 0xffff) % width;
//            }
//        }
//        return hashes;

        // Make new hash function based on Random rn
//        rn_cm_hash.setSeed(this.seed + 18 + hash_long);
        for (int i = 0; i < depth; i++) {
//            hashes[i] = ((xx.hashLong(hash_long)).asInt() % width + width) % width;// rn_cm_hash.nextInt(width); // TODO: check if it makes a difference
//            hashes[i] = ((xx.hashLong(hash_long)).asInt() % width);
            int hash = (xx[i].hashLong(attrValue).asInt() % width);
            if (hash < 0) {
                hash = hash + width;
            }
            hashes[i] = hash;
        }
        return hashes;

        //int[] hashes = new int[depth];
        //byte[] byte_key = longToBytes(attrValue);
        //byte[] byte_key = utils.StringToByte(sp);
        //long hash_key_long = hashFunc.hash(byte_key, 0, byte_key.length, this.seed);
        //return getHashArray(attrValue, depth, width);
        //return getHashArray(hash_key_long, depth, width);
//        rn.setSeed(attrValue + this.seed);
//        for (int i = 0; i < depth; i++) hashes[i] = rn.nextInt(width);
//        return hashes; // is hash function okay.
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



    public void ingest(long attrValue, int[] hx, long[][] vals, int hashG, int sign) {
        // Test if all element in A and B are consistent
        int[] hashes = hash(attrValue, depth, width);
        insertCount += sign;
        if (dynamicSampleSizes) {
            if (insertCount == 1000000) {
                updateSampleSizes();

            }
        }
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            if (useTwoLHS) {
                if (twoLHSFast) {
                    CMTwoLHS[j][w][hashG].ingest(hx[j], sign); // Hash in Sample  as in section 5 of paper
                } else {
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        CMTwoLHS[j][w][k].ingest(vals[k], sign); // Hash in Sample  as in section 3 of paper
                        //CMTwoLHS[j][w][k].ingest(hx, sign); // Hash in Sample  as in section 3 of paper
                    }
                }

                //
            } else {
                CMKminTreeSet[j][w].ingest(hx[j], sign); // In Kminwise
            }
        }
    }
    private void updateSampleSizes() {
        for (int j = 0; j < depth; j++) {
            int[] currentN = new int[width];
            int currentNMax = 0;

            // 1. Gather current sample sizes and counts
            for (int i = 0; i < width; i++) {
                currentN[i] = CMKminTreeSet[j][i].n;
                if (currentN[i] > currentNMax) {
                    currentNMax = currentN[i];
                }
            }

            // 2. Define minimum sample size (B_min = B0 / 2)
            int total = orgMaxSize * width;
            int B_min = (int) (0.9*orgMaxSize);  // Assume all initial sample sizes are equal to B0
            int excessBudget = total - (B_min * width);

            // 3. Calculate normalized weights for cells based on frequency
            double totalWeight = 0.0;
            double[] normalizedWeights = new double[width];
            for (int i = 0; i < width; i++) {
                normalizedWeights[i] = (double) currentN[i] / currentNMax;
                totalWeight += normalizedWeights[i];
            }
            int usedExcessBudget = 0;

            // 4. Redistribute excess budget proportionally based on weights
            int[] newSampleSizes = new int[width];
            for (int i = 0; i < width; i++) {
                int excessAllocation = (int) Math.floor((normalizedWeights[i] / totalWeight) * excessBudget);
                newSampleSizes[i] = B_min + excessAllocation;
                usedExcessBudget += B_min + excessAllocation;
            }

            if (usedExcessBudget > total) {
                System.out.println("Error: Used more excess budget than available");
            }

            // 5. Apply the updated sample sizes
            for (int i = 0; i < width; i++) {
                CMKminTreeSet[j][i].changeK(newSampleSizes[i]);
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
        Kmin[] result = new Kmin[depth];
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = new Kmin(CMKminTreeSet[j][w]);
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
                    memoryUsage += CMKminTreeSet[j][i].getMemoryUsage();
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
                    CMKminTreeSet[j][i].reset();
                }
            }
        }
    }

    public int getFilledKSamples() {
        int filledKSamples = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                filledKSamples += CMKminTreeSet[j][i].sketch.size;
                }
            }
        return filledKSamples;
    }

    Random rn_cm_hash = new Random();


    public int getCollisions() {
        int collissions = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                if (useTwoLHS) {
                    continue;
                } else {
                    collissions += CMKminTreeSet[j][i].collissions;
                }
            }
        }
        return collissions;
    }
}
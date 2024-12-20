package omni.omniReservoir;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.ByteBuffer;
import java.util.Random;

// make simple new datastructure of int n an IntHeapPriorityQueue

public class AttributeSketch {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    TWOLHS[][][] CMTwoLHS;
    public Kmin[][] CMKminTreeSet;
    public Kmin[] CMRow;
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
    final boolean dynamicSampleSizes;
    int insertCount = 0;

    public AttributeSketch(int attr, int[] parameters,
                           boolean useTwoLHS, boolean useTwoKmin, double BetaKmin,
                           boolean twoLHSFast, boolean dynamicSampleSizes, int seed){
        //int depth, int width, int numTwoLHSReps, int B, int b) {
        this.attr = attr;
        this.depth = parameters[0];
        this.width = parameters[1];
        this.useTwoLHS = useTwoLHS;
        this.useTwoKmin = useTwoKmin;
        this.twoLHSFast = twoLHSFast;
        this.seed = seed;
        this.BetaKmin = BetaKmin;
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
            //CMKminTreeSet = new Kmin[depth][width];
            CMRow = new Kmin[depth];
            for (int j = 0; j < depth; j++) {
                CMRow[j] = new Kmin(orgMaxSize, width, b, false, BetaKmin, seed);
//                maxSignaturesPerRow[j] = new int[]{0, (int) Math.pow(2, b) - 1};
//
//                for (int i = 0; i < width; i++) {
//                    CMKminTreeSet[j][i] = new Kmin(orgMaxSize * width, b, useTwoKmin, BetaKmin, seed);//Main.withDeletes);
//                }

            }
        }
    }

    public byte[] longToBytes(long attrValue) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(attrValue);
        return buffer.array();
    }

    int[] hash(long attrValue, int depth, int width) {
        //int[] hashes = new int[depth];
        //byte[] byte_key = longToBytes(attrValue);
        //byte[] byte_key = utils.StringToByte(sp);
        //long hash_key_long = hashFunc.hash(byte_key, 0, byte_key.length, this.seed);
        return getHashArray(attrValue, depth, width);
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
                CMRow[j].ingest(hx[j], w, sign); // In Kminwise
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
    public queryRes queryKmin(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);

        int[] n = new int[depth];
        int[] curSampleSizes = new int[depth];
        IntHeapPriorityQueue[] result;
        result = new IntHeapPriorityQueue[depth];

        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = CMRow[j].getSampleToQuery(w);
            n[j] = CMRow[j].numInserts[w];
            curSampleSizes[j] = result[j].size();//MRow[j].curSampleSize;
        }

        return new queryRes(result, n, curSampleSizes);
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
            if (useTwoLHS) {
                for (int i = 0; i < width; i++) {
                    //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        memoryUsage += CMTwoLHS[j][i][k].getMemoryUsage();
                    }
                }
            }
            memoryUsage += CMRow[j].getMemoryUsage();
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
                    CMRow[j].reset();
                }
            }
        }
    }

    public int getFilledKSamples() {
        int filledKSamples = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                filledKSamples += CMRow[j].sketch[i].size();
                }
            }
        return filledKSamples;
    }

    Random rn_cm_hash = new Random();


    HashFunction xx = Hashing.murmur3_32_fixed();
    public int[] getHashArray(final long hash_long, int depth, int width) {
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
            xx = Hashing.murmur3_32_fixed(i);
            //hashes[i] = ((xx.hashLong(hash_long)).asInt() % width + width) % width;// rn_cm_hash.nextInt(width); //tODO: check if this is okay
            int hash = (xx.hashLong(hash_long).asInt() % width);
            if (hash < 0) {
                hash = hash + width;
            }
            hashes[i] = hash;
        }
        return hashes;
    }

    public int getCollisions() {
        int collissions = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                if (useTwoLHS) {
                    continue;
                } else {
                    collissions += CMKminTreeSet[j][i].collisions;
                }
            }
        }
        return collissions;
    }

    public long getMemoryUsagePQ() {
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            if (useTwoLHS) {
                for (int i = 0; i < width; i++) {
                    //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                    for (int k = 0; k < numTwoLHSReps; k++) {
                        memoryUsage += CMTwoLHS[j][i][k].getMemoryUsage();
                    }
                }
            }
            memoryUsage += CMRow[j].getMemoryUsagePQ();
        }
        return memoryUsage;
    }
}
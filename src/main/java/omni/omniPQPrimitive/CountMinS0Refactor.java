package omni.omniPQPrimitive;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.PriorityQueue.PriorityQueue;

import java.util.*;
import java.util.stream.Collectors;

public class CountMinS0Refactor {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    HashSet<Integer>[][] CM;
    //Tree[][] CMPQ;
    final int depth;
    final int width;
    final int sampleSize;
    final int sampleBufferSize;
    final int seed;
    int attr;
    HashFunction[] xx;
    //final Random rn = new Random(Main.repetition);
    int[][] maintained_rids;

    Random rn;

    public CountMinS0Refactor(int attr, int depth, int width, int sampleSize, int sampleBufferSize, int seed) {
        this.attr = attr;
        this.depth = depth;
        this.width = width;
        this.sampleSize = sampleSize;
        this.sampleBufferSize = sampleBufferSize;
        this.seed = seed;
        rn = new Random(seed);
        initSketch();
    }

//    public CountMinS0(int attr, int depth, int width, int numTwoLHSReps, int b) {
//        this.attr = attr;
//        this.depth = depth;
//        this.width = width;
//        this.numTwoLHSReps = numTwoLHSReps;
//
//        this.b = b;
//        initSketch();
//    }


    public void initSketch() {
        maintained_rids = new int[sampleSize][depth]; // For each sample, row, we maintain the column index at that row.
//        int[][] primes = Hash.randomPrimes(depth, attr);
//        hash_a = primes[0];
//        hash_b = primes[1];
//        hash_c = primes[2];
//        System.out.println("CMS0: hash_a = " + hash_a[0]);
//        System.out.println("CMS0: hash_b = " + hash_b[0]);
//        System.out.println("CMS0: hash_c = " + hash_c[0]);
        CM = new HashSet[depth][width];
//        CMPQ = new PriorityQueue[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i] = new HashSet<>(sampleSize / width);
//                CMPQ[j][i] = new PriorityQueue(sampleSize + sampleBufferSize);
            }
        }
        xx = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            xx[i] = Hashing.murmur3_32_fixed(i);
        }
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hashes = new int[depth];
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


//        int[] hash = new int[depth];
//        rn.setSeed(attrValue + Main.repetition);
//        for (int i = 0; i < depth; i++) hash[i] = rn.nextInt(width);
//        return hash;
        //return getHashArray(attrValue, depth, width);
//        int[] hashes = new int[depth];
//        rn.setSeed(attrValue + this.seed);
//
//        for (int i = 0; i < depth; i++) {
//            hashes[i] = rn.nextInt(width);//(int) (((hash_a[i] * attrValue + hash_b[i]) % hash_c[i]) % width + width) % width;
//            //attrValue = hashes[i];
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
    public void ingest(long attrValue, int sampleToReplace, int sign) {
        // Test if all element in A and B are consistent
        int[] hashes = hash(attrValue, depth, width);
        int[] oldHashes = maintained_rids[sampleToReplace];
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            if (sign == 1) {
                int oldHash = oldHashes[j];
                if (oldHash != w) {
                    CM[j][oldHash].remove(sampleToReplace);
                    CM[j][w].add(sampleToReplace);
                    oldHashes[j] = w;
                }
            } else {
                CM[j][w].remove(sampleToReplace);
            }
        }
    }

    public HashSet<Integer>[] query(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);
        HashSet<Integer>[] result = new HashSet[depth];

//        PriorityQueue[] resultPQ = new PriorityQueue[depth];
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = getCopy(j, w);
        }

        return result;
    }

    public long getMemoryUsage() {
        long memoryUsage = 0;
        double sizeSigSample = Math.log(sampleSize) / Math.log(2);
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                memoryUsage += (long) (CM[j][i].size() * sizeSigSample);
            }
        }
        memoryUsage += (long) (maintained_rids.length * depth * Math.log(width)/Math.log(2));
        return memoryUsage;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i].clear();
            }
        }
        maintained_rids = new int[sampleSize][depth];
    }

//    public TreeSet<Integer> getCopy(int row, int column) {
//        // copy of treeset for querying
//        return new TreeSet<>(CM[row][column]);
//    }

    public HashSet<Integer> getCopy(int row, int column) {
//        return new HashSet<>(CM[row][column]);
        return CM[row][column];

    }

//    public void sort() {
//        // sort all arraylists in CM:
//        for (int j = 0; j < depth; j++) {
//            for (int i = 0; i < width; i++) {
//                Collections.sort(CM[j][i]);
//            }
//        }
//    }


    /*
        Input: 1. Attribute
        Output: 2. Arraylist of size depth with all relevant Distinct samples for single attribute
     */
}
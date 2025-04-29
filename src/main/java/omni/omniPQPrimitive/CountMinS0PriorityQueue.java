package omni.omniPQPrimitive;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.PriorityQueue.PriorityQueue;

import java.util.*;

public class CountMinS0PriorityQueue {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    PriorityQueue[][] CM;
    //Tree[][] CMPQ;
    final int depth;
    final int width;
    final int sampleSize;
    final int sampleBufferSize;
    final int seed;
    int attr;
    HashFunction[] xx;
    //final Random rn = new Random(Main.repetition);

    Random rn;

    public CountMinS0PriorityQueue(int attr, int depth, int width, int sampleSize, int sampleBufferSize, int seed) {
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

//        int[][] primes = Hash.randomPrimes(depth, attr);
//        hash_a = primes[0];
//        hash_b = primes[1];
//        hash_c = primes[2];
//        System.out.println("CMS0: hash_a = " + hash_a[0]);
//        System.out.println("CMS0: hash_b = " + hash_b[0]);
//        System.out.println("CMS0: hash_c = " + hash_c[0]);
        CM = new PriorityQueue[depth][width];
//        CMPQ = new PriorityQueue[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i] = new PriorityQueue(sampleSize / width);
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
    public void ingest(long attrValue, int id, int sign) {
        // Test if all element in A and B are consistent
        int[] hashes = hash(attrValue, depth, width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            if (sign == 1) {
                CM[j][w].add(id);
            } else {
                CM[j][w].remove(id);
            }
        }
    }

    public PriorityQueue[] query(long attrValue) {
        if (!toRemoveId.isEmpty()) { // otherwise, scaling is not correct
            removeList();
        }

        int[] hashes = hash(attrValue, depth, width);
//
//         result;
        PriorityQueue[] result = new PriorityQueue[depth];

//        PriorityQueue[] resultPQ = new PriorityQueue[depth];
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = PriorityQueue.getCopy(CM[j][w]);
        }

        return result;
    }

    public long getMemoryUsage() {
        if (!toRemoveId.isEmpty()) {
            removeList();
        }
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                memoryUsage += CM[j][i].size() * 32L;
            }
        }
        if (memoryUsage/ 32L > (long) depth * sampleSize) {
            System.out.println("Too many samples in CMS0 of attr " + attr + ": " + memoryUsage/ 32);
        }
        return memoryUsage;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i].clear();
            }
        }
    }

    ArrayList<Integer> toRemoveId = new ArrayList<>();

    public void removeId(int removeId) {
        toRemoveId.add(removeId);
        if (toRemoveId.size() > sampleBufferSize) {
            removeList();
        }
    }

    private void removeList() {
//        Collections.sort(toRemoveId);
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
//                for (Long aLong : toRemoveId) {
//                    CM[j][i].remove(aLong);
//                }
//                toRemoveIdSet.forEach(CM[j][i]::remove);
//                Integer[] treeArray = CM[j][i].toArray();
                for (Integer aLong : toRemoveId) {
//                    if (CM[j][i].isEmpty() || CM[j][i].first() < aLong) {
//                        break;
//                    }
                    if (!CM[j][i].isEmpty()) {
                        CM[j][i].remove(aLong);
                    }
                }
//                toRemoveId.forEach(CM[j][i]::remove);

                //toremove id is sorted, treesets in CM are sorted, so we can break after first removal

            }
        }
        toRemoveId.clear();
    }

    public PriorityQueue getCopy(int row, int column) {
        // copy of treeset for querying
        return new PriorityQueue(CM[row][column]);
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
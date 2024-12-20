package omni.omniPQ;

import java.util.Random;
import java.util.TreeSet;

public class CountMinS0 {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    TreeSet<Long>[][] CM;
    int depth;
    int width;
    int maxSize;
    int b;
    int numTwoLHSReps;
    final int seed;
    int attr;
    //final Random rn = new Random(Main.repetition);


    private int[] hash_a;
    private int[] hash_b;
    private int[] hash_c;
    Random rn;

    public CountMinS0(int attr, int[] parameters, int seed) {
        this.attr = attr;
        depth = parameters[0];
        width = parameters[1];
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
        CM = new TreeSet[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i] = new TreeSet<Long>();
            }
        }
    }

    int[] hash(long attrValue, int depth, int width) {
//        int[] hash = new int[depth];
//        rn.setSeed(attrValue + Main.repetition);
//        for (int i = 0; i < depth; i++) hash[i] = rn.nextInt(width);
//        return hash;
        int[] hashes = new int[depth];
        rn.setSeed(attrValue + this.seed);

        for (int i = 0; i < depth; i++) {
            hashes[i] = rn.nextInt(width);//(int) (((hash_a[i] * attrValue + hash_b[i]) % hash_c[i]) % width + width) % width;
            //attrValue = hashes[i];
        }
        return hashes;
    }

//    public void add(int id, long attrValue) {
//        // Test if all element in A and B are consistent
//        int[] hashes = hash(attrValue, Main.depth, Main.width);
//        for (int j = 0; j < Main.depth; j++) {
//            int w = hashes[j];
//            CM[j][w].add(id); // Hash in Sample based on id
//        }
//    }
    public void ingest(long attrValue, long id, int sign) {
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

    public TreeSet<Long>[] query(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);

        TreeSet<Long>[] result;
        result = new TreeSet[depth];

        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = CM[j][w];
        }
        return result;
    }

    public long getMemoryUsage() {
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                memoryUsage += CM[j][i].size() * 4L;
            }
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
package omni.omniRefactor;
import omni.Main;

import java.util.Random;

public class CountMin {

    //ArrayList<ArrayList<Sample>> CM = new ArrayList<>();

    Sample[][] CM;
    int depth;
    int width;
    int maxSize;
    int b;

    int attr;
    final Random rn = new Random();

    public CountMin(int attr) {
        this.attr = attr;
        depth = Main.depth;
        width = Main.width;
        maxSize = Main.maxSize;
        b = Main.b;
        initSketch();
    }

    public CountMin(int attr, int depth, int width, int maxSize, int b) {
        this.attr = attr;
        this.depth = depth;
        this.width = width;
        this.maxSize = maxSize;
        this.b = b;
        initSketch();
    }


    public void initSketch() {
        CM = new Sample[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                CM[j][i] = new Kmin(Main.deltaDS, maxSize, b);
            }
        }
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hash = new int[depth];
        rn.setSeed(attrValue + Main.repetition);
        for (int i = 0; i < depth; i++) hash[i] = rn.nextInt(width);
        return hash;
    }

//    public void add(int id, long attrValue) {
//        // Test if all element in A and B are consistent
//        int[] hashes = hash(attrValue, Main.depth, Main.width);
//        for (int j = 0; j < Main.depth; j++) {
//            int w = hashes[j];
//            CM[j][w].add(id); // Hash in Sample based on id
//        }
//    }
    public void add(long attrValue, long hx) {
        // Test if all element in A and B are consistent
        int[] hashes = hash(attrValue, depth, width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            CM[j][w].add(hx); // Hash in Sample based on id
        }
    }

    public Sample[] query(long attrValue) {
        int[] hashes = hash(attrValue, depth, width);

        Sample[] result;
        result = new Kmin[depth];

        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            result[j] = CM[j][w];
            //result.add(new DistinctSample(CM.get(j).get(w)));
        }
        return result;
    }

    public long getMemoryUsage() {
        long memoryUsage = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                if (Main.useDS) {
                    memoryUsage += CM[j][i].curSampleSize * 32L;
                } else {
                    memoryUsage += CM[j][i].getMemoryUsage();
                }
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


    /*
        Input: 1. Attribute
        Output: 2. Arraylist of size depth with all relevant Distinct samples for single attribute
     */
}
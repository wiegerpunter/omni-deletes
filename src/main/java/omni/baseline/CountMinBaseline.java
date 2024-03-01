package omni.baseline;

import omni.Query;
import omni.Record;

import java.util.ArrayList;
import java.util.Random;

public class CountMinBaseline {
    ArrayList<ArrayList<Integer>> CM = new ArrayList<>();
    ArrayList<Boolean> attrs;

    Random rn = new Random(1);

    public CountMinBaseline(ArrayList<Boolean> attrs) {
        this.attrs = attrs;
        initSketch();
    }

    public void initSketch() {
        for (int j = 0; j < SimpleSketch.depth; j++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int i = 0; i < SimpleSketch.width; i++) {
                row.add(0);
            }
            CM.add(row);
        }
    }

    int[] hash(long attrValue, int depth, int width) {
        int[] hash = new int[depth];
        rn.setSeed(attrValue);
        for (int i = 0; i < depth; i++) hash[i] = rn.nextInt(width);
        return hash;
    }

    public void add(Record record) {
        // Test if all element in A and B are consistent
        long value = 0;
        for (int i = 0; i < attrs.size(); i++) {
            if (attrs.get(i)) {
                value += ((Long)record.getRecord()[i]).hashCode();
            }
        }

        int[] hashes = hash(value, SimpleSketch.depth, SimpleSketch.width);
        for (int j = 0; j < SimpleSketch.depth; j++) {
            int w = hashes[j];
            CM.get(j).set(w, CM.get(j).get(w) + 1);
        }
    }

    public int query(Query query) {
        long value = 0;
        for (int i = 0; i < attrs.size(); i++) {
            if (attrs.get(i)) {
                value += ((Long) query.getRecord()[i]).hashCode();
            }
        }
        int min = Integer.MAX_VALUE;
        int[] hashes = hash(value, SimpleSketch.depth, SimpleSketch.width);
        for (int j = 0; j < SimpleSketch.depth; j++) {
            int w = hashes[j];
            min = Math.min(min, CM.get(j).get(w));
        }
        return min;
    }

    public int getMemoryUsage() {
        return SimpleSketch.depth * SimpleSketch.width * 4;
    }

}

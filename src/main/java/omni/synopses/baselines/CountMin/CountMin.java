package omni.synopses.baselines.CountMin;

import omni.Experiments.QueryInfo;
import omni.synopses.SynopsisRefactor;

import java.util.Arrays;
import java.util.Random;

public class CountMin  extends SynopsisRefactor {
    public int[][] CM;
    private final int depth;
    private final int width;
    Random rn;


    public CountMin(long ram, int numStoredAttributes, int[] parameters, int seed) {
        this.seed = seed;
        this.parameters = parameters;
        this.ram = ram;
        this.depth = parameters[0];
        this.width = parameters[1];
        this.CM = new int[depth][width];
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                CM[i][j] = 0;
            }
        }
        setting = "CountMin";
        rn = new Random();
    }

    public int[] hash(long hash_long, int depth, int width) {
        int[] hashes = new int[depth];
        // Make new hash function based on Random rn
        rn.setSeed(this.seed + 18 + hash_long);
        for (int i = 0; i < depth; i++) {
            hashes[i] = rn.nextInt(width);
        }
        return hashes;
    }

    @Override
    public void add(long[] record) {
        long[] addRecord = new long[record.length - 1];
        System.arraycopy(record, 1, addRecord, 0, record.length - 1);
        long value = Arrays.hashCode(addRecord);
        int[] hashes = hash(value, depth, width);
        for (int j = 0; j < depth; j++) {
            int w = hashes[j];
            CM[j][w] += 1;
        }
    }

    @Override
    public int query(long[] query, int numPreds) {
        long value = Arrays.hashCode(query);
        int[] hashes = hash(value, depth, width);
        int estimate = Integer.MAX_VALUE;
        for (int j = 0; j < depth; j++) {
            estimate = Math.min(estimate, CM[j][hashes[j]]);
        }
        return estimate;
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        return query(query, numPreds);
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, QueryInfo CMRow) {
        return query(query, numPreds);
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        return 0;
    }

    @Override
    public void reset() {
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                CM[i][j] = 0;
            }
        }
    }

    @Override
    public void delete(long[] r) {
        throw new UnsupportedOperationException("Delete not implemented for CountMin");
    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public void printParams() {
        System.out.println("CountMin with depth = " + depth + " and width = " + width);
    }

    @Override
    public long getMemoryUsage() {
        return (long) depth * width * 32;
    }
}

package omni.synopses.omniFactory.SampleTypes;


import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class TWOLHS implements Sample {
    private final int bitSize;
    public int[][] countSignatures;
    int repetition;
    int seed;

    HashFunction hashFunction;

    public TWOLHS(int TWOLHSRepetition, int seed){
        this.repetition = TWOLHSRepetition;
        this.seed = seed;
        bitSize = 31;
        initialize();
    }

    public void initialize() {
        countSignatures = new int[bitSize][bitSize + 1];
        for (int i =0; i < countSignatures.length; i++) {
//            countSignatures[i] = new int[bitSize - i];
//
            for (int j = 0; j < countSignatures[0].length; j++) {
                countSignatures[i][j] = 0;
            }
        }

        hashFunction = Hashing.murmur3_32_fixed(seed + repetition);
    }

    @Override
    public void ingest(int id, int sign) {
        int h = hashFunction.hashInt(id).asInt() >>> 1;
        int sketchIndex = -1;
        for (int i = 0; i < bitSize; i++) {
            int mask = 1 << i;
            int val = (h & mask);
            if (val == 0) {
                continue;
            }
            if (sketchIndex == -1) {
                sketchIndex = i;
                countSignatures[i][0] += sign;
            }
            countSignatures[sketchIndex][i+1] += sign;
        }
    }

    public boolean[] lsb(long h) {
        boolean[] bits = new boolean[bitSize];

        int lsb = bitSize - 1;
        long bit =0;
        while (h > 0) {
            bit = h % 2;
            h/= 2;
            bits[lsb] = bit == 1;
            lsb--;
        }
        return bits;
    }

    public boolean emptyBucket(int lsb) {
        return countSignatures[lsb][0] == 0;
    }

    public boolean singletonBucket(int lsb) {
        // check if singleton

        if (emptyBucket(lsb)) {
            return false;
        }

        for (int i = 1; i < countSignatures[lsb].length; i++) {
            if (countSignatures[lsb][i] > 0 && countSignatures[lsb][i] < countSignatures[lsb][0]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object query() {
        return null;
    }

    @Override
    public long getMemoryFootprint() {
        return ((long) bitSize * (bitSize + 1) * 32);
    }

    @Override
    public void reset() {

    }

    @Override
    public String getKminType() {
        return "TWOLHS";
    }

    @Override
    public int getN() {
        return 0;
    }

    @Override
    public int getCurSampleSize() {
        return 0;
    }
}

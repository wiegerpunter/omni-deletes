package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.CustomPriorityQueue.PriorityQueue;

public class KminArray implements Sample {

    private final int B;
    private final int b;
    private int n;
    private int curSampleSize;
    private int[] sample;
    private int curTreeRoot = Integer.MAX_VALUE;
    private final String setting;


    public KminArray(int B, int b, String setting) {
        this.B = B;
        this.b = b;
        this.setting = setting;
        this.n = 0;
        this.curSampleSize = 0;
        this.sample = new int[B];
    }

    // Implement the methods for KminPQ here
    // For example:
    @Override
    public void ingest(int hx, int sign) {
        // Implementation for ingesting values into the KminPQ
        if (sign == 1) {
            add(hx);
        } else {
            remove(hx);
        }
    }

    private void add(int hx) {
        n++;
        if (curSampleSize < B) {
            sample[curSampleSize] = hx;
            curSampleSize++;
        } else if (curSampleSize == B) {
            curTreeRoot = sample[sample.length - 1];
            tryInsert(hx);
        } else {
            tryInsert(hx);
        }
    }


    private void tryInsert(int hx) {
        if (hx < curTreeRoot) { // get tree root
            int index = 0;
            while (index < B && sample[index] < hx) {
                index++;
            }
            if (index < B) {
                // Shift elements to the right
                for (int i = B - 1; i > index; i--) {
                    sample[i] = sample[i - 1];
                }
                sample[index] = hx;
            }
            curTreeRoot = sample[sample.length - 1]; // Update the tree root
        }
    }

    private void remove(int hx) {
        throw new UnsupportedOperationException("Remove operation is not supported in KminPQ");
    }

    public int[] query() {
        return sample;
    }

    public long getMemoryFootprint() {
        return Formulas.ramSingleKmin(curSampleSize, b);
    }

    @Override
    public void reset() {
        sample = new int[B];
        curSampleSize = 0;
        n = 0;
        curTreeRoot = Integer.MAX_VALUE;
    }

    @Override
    public String getKminType() {
        return setting; // KminPQ type
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
    public int getCurSampleSize() {
        return curSampleSize;
    }
}

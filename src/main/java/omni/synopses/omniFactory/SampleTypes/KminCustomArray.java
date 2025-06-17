package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.ArrayWithBuffer.ArrayWithBuffer;

import java.util.Arrays;

public class KminCustomArray implements Sample {

    private final int B;
    private final int b;
    private int n;
    private int curSampleSize;
    private ArrayWithBuffer sample;
    private final String setting;

    public KminCustomArray(int B, int b, String setting) {
        this.B = B;
        this.b = b;
        this.setting = setting;
        this.n = 0;
        this.curSampleSize = 0;
        this.sample = new ArrayWithBuffer(B); // Assuming a buffer size of 200
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
        sample.insert(hx);
    }

    private void remove(int hx) {
        throw new UnsupportedOperationException("Remove operation is not supported in KminPQ");
    }

    public int[] query() {
        return sample.getK();
    }

    public long getMemoryFootprint() {
        return Formulas.ramSingleKmin(curSampleSize, b);
    }

    @Override
    public void reset() {
        sample = new ArrayWithBuffer(B); // Resetting the sample with a new buffer
        curSampleSize = 0;
        n = 0;
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
        return sample.getSize();
    }
}

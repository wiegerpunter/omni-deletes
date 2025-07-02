package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.ArrayWithBuffer.ArraySimpleBuffer;

import java.util.Arrays;

public class KminSimpleBuffer implements Sample {

    private final int B;
    private final int b;
    private int n;
    private ArraySimpleBuffer sample;
    private final String setting;
    private final double beta;

    private final int ingestionBufferSize;

    public KminSimpleBuffer(int B, int b, double beta, String setting) {
        this.b = b;
        this.setting = setting;
        this.n = 0;
        this.beta = beta;
        ingestionBufferSize = Math.max(1, B / 80);
        if (beta == 0) {
            this.B = B - ingestionBufferSize;
        } else if (beta >= 0) {
            this.B = (int) ((B - ingestionBufferSize) * beta);
        } else {
            throw new IllegalArgumentException("Beta should be >= 0");
        }
        this.sample = new ArraySimpleBuffer(this.B, ingestionBufferSize);
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
        n--;
        sample.remove(hx);
    }

    public int[] query() {
        if (beta != 0) {
            return Arrays.copyOf(sample.getK(), getCurSampleSize());
        } else {
            return sample.getK();
        }
    }

    public long getMemoryFootprint() {
        analyzeDeletes();
        return Formulas.ramSingleKmin(getCurSampleSize() + getBufferSize(), b);
    }

    @Override
    public void reset() {
        sample = new ArraySimpleBuffer(B, ingestionBufferSize); // Resetting the sample without a buffer
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
        if (beta != 0) {
            return (int) Math.min(sample.getCurSampleSize(), this.B / beta);
        }
        return sample.getCurSampleSize();
    }

    public int getBufferSize() {
        return sample.getBufferSize();
    }

    public void analyzeDeletes() {
        // check if number of deletes exceed buffer: (beta - 1) * B
        if (beta!=0 & sample.getTotalDeletes() > (beta - 1) / beta * B) {
            System.out.println("Warning: Number of deletes from buffer exceeds threshold.");
        }

    }
}

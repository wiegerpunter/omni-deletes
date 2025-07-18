package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.ArrayWithBuffer.ArrayWithIngestionBuffer;

import java.util.Arrays;

public class KminCustomArray implements Sample {

    private final int B;
    private final int ingestionBufferSize;
    private final int b;
    private int n;
    private ArrayWithIngestionBuffer sample;
    private final String setting;
    private final double beta;
    private final boolean onlyUseValidSamples;
    private final boolean pessimisticDeleteCounter; // Flag to indicate if we are using pessimistic delete counter

    public KminCustomArray(int B, int b, double beta, String setting, boolean onlyUseValidSamples, boolean pessimisticDeleteCounter) {
        this.b = b;
        this.setting = setting;
        this.n = 0;
        this.beta = beta;
        this.onlyUseValidSamples = onlyUseValidSamples;
        this.pessimisticDeleteCounter = pessimisticDeleteCounter; // Default to false, can be set via constructor if needed
        ingestionBufferSize = (int) Math.max(1, (B*beta) / 80); // todo: optimize buffer size of ingestion.
        if (beta == 0) {
            this.B = B - ingestionBufferSize;
        } else if (beta >= 0) {
            this.B = (int) ((B - ingestionBufferSize) * beta);
        } else {
            throw new IllegalArgumentException("Beta should be >= 0");
        }
        this.sample = new ArrayWithIngestionBuffer(this.B, ingestionBufferSize);
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
        sample.removeSimple(hx);
    }

    public int[] query() {
        if (beta != 0) {
            return Arrays.copyOf(sample.getK(), getCurSampleSize());
        } else {
            return sample.getK();
        }
    }

    public long getMemoryFootprint() {
//        analyzeDeletes();
        return Formulas.ramSingleKmin(getCurSampleSize() + getBufferSize(), b);
    }

    @Override
    public void reset() {
        sample = new ArrayWithIngestionBuffer(B, this.ingestionBufferSize); // Resetting the sample without a buffer
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
            if (setting.equals("KminArrayWithoutBuffer")) {
                // For KminArrayWithoutBuffer, we return the current sample size directly
                return sample.getCurSampleSize();
            }
            if (onlyUseValidSamples) {
                return (int) Math.min(sample.getCurSampleSize(), B / beta);
            } else if (pessimisticDeleteCounter){
                return (int) Math.min(sample.getCurSampleSize(), Math.max(B / beta, B - sample.getTotalDeletes()));
            } else {
                return getIndexOfMinRejectedValue();
            }
        }
        return sample.getCurSampleSize();
    }

    private int getIndexOfMinRejectedValue() {
        // get sample count before minRejectedValue
        int minRejectedValue = sample.getMinRejectedValue();
        if (minRejectedValue == Integer.MAX_VALUE) {
            return sample.getCurSampleSize();
        }
        int[] k = sample.getK();
        int index = Arrays.binarySearch(k, minRejectedValue);
        if (index < 0) {
            index = -index - 1; // Convert to insertion point
        }
        return index;
    }

    public int getBufferSize() {
        return sample.getBufferSize();
    }

    public void analyzeDeletes() {
        // check if number of deletes exceed buffer: (beta - 1) * B
        if (beta!=0 & sample.getTotalDeletes() > (beta - 1) *B) {
            System.out.println("Warning: Number of deletes from buffer with sample type " + setting
                    + " exceeds threshold."
                    + " Deletes from buffer: " + sample.getDeletesFromBuffer()
                    + ", Deletes from sample: " + sample.getDeletesFromSample()
                    + ", Total deletes: " + sample.getTotalDeletes()
                    + " budget : " + (beta - 1) * B);
        }

    }
}

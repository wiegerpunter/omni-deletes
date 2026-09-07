package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.ArrayWithBuffer.ArrayWithBufferOptimized;
import omni.synopses.omniFactory.ArrayWithBuffer.ArrayWithBufferOptimizedBatchDeletes;

import java.util.Arrays;

public class KminCustomArrayOptBatchDeletes implements Sample {

    private final int B;
    private final int ingestionBufferSize;
    private final int deleteBufferSize;
    private final int b;
    private int n;
    private ArrayWithBufferOptimizedBatchDeletes sample;
    private final String setting;
    private final double beta;
    private final boolean onlyUseValidSamples;
    private final boolean pessimisticDeleteCounter; // Flag to indicate if we are using pessimistic delete counter

    public KminCustomArrayOptBatchDeletes(int B, int b, double beta, int ingestionBufferSize, int deleteBufferSize,
                                          String setting, boolean onlyUseValidSamples, boolean pessimisticDeleteCounter) {
        this.b = b;
        this.setting = setting;
        this.n = 0;
        this.beta = beta;
        this.onlyUseValidSamples = onlyUseValidSamples;
        this.pessimisticDeleteCounter = pessimisticDeleteCounter; // Default to false, can be set via constructor if needed
        this.ingestionBufferSize = ingestionBufferSize;
        this.deleteBufferSize = deleteBufferSize;
        if (beta == 0) {
            this.B = B - ingestionBufferSize - deleteBufferSize;
        } else if (beta >= 0) {
            if (B <= ingestionBufferSize + deleteBufferSize) {
                throw new IllegalArgumentException("B must be greater than ingestionBufferSize + deleteBufferSize for B: " + B + " iBuf: " + ingestionBufferSize + " dBuf: " + deleteBufferSize);
            }
            this.B = Math.max(1, (int) ((B - ingestionBufferSize - deleteBufferSize) * beta));
        } else {
            throw new IllegalArgumentException("Beta should be >= 0");
        }
        this.sample = new ArrayWithBufferOptimizedBatchDeletes(this.B, ingestionBufferSize, deleteBufferSize);
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
        sample = new ArrayWithBufferOptimizedBatchDeletes(B, this.ingestionBufferSize, this.deleteBufferSize); // Resetting the sample without a buffer
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

package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.CustomPriorityQueue.PriorityQueue;

import java.util.Arrays;
import java.util.SortedSet;

public class KminArray implements Sample {

    private final int B;
    private final int b;
    private int n;
    private int curSampleSize;
    private int[] sample;
    private int curTreeRoot = Integer.MAX_VALUE;
    private final String setting;
    private double beta;


    public KminArray(int B, int b, double beta, String setting) {

        this.b = b;
        this.beta = beta;
        this.setting = setting;
        this.n = 0;
        this.curSampleSize = 0;
        if (beta != 0) {
            this.B = (int) (B * beta);
        } else {
            this.B = B;
        }
        this.sample = new int[this.B];

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
        } else {
            if (curSampleSize == B) {
                Arrays.sort(sample, 0, B); // ensure it's sorted once
                curTreeRoot = sample[B - 1];
            }
            tryInsert(hx);
        }
    }


    private void tryInsert(int hx) {
        if (hx < curTreeRoot) { // get tree root
            int index = Arrays.binarySearch(sample, 0, B, hx);
            if (index < 0) index = -index - 1;
//
//            while (index < B && sample[index] < hx) {
//                index++;
//            }
            if (index < B && sample[index] != hx) {
                // Shift elements to the right
                System.arraycopy(sample, index, sample, index + 1, B - index - 1);
//                for (int i = B - 1; i > index; i--) {
//                    sample[i] = sample[i - 1];
//                }
                sample[index] = hx;
            }
            curTreeRoot = sample[B - 1]; // Update the tree root
        }
    }

    private void remove(int hx) {
        n--;
        if (hx > curTreeRoot) {
            return; // No need to remove if hx is greater than the current root
        }
        int index = Arrays.binarySearch(sample, 0, B, hx);
        if (index >= 0) {
            // Element found, remove it
            System.arraycopy(sample, index + 1, sample, index, B - index - 1);
            sample[B - 1] = Integer.MAX_VALUE; // Set the last element to a large value
            curSampleSize--;
            if (curSampleSize > 0) {
                curTreeRoot = sample[B - 1]; // Update the tree root
            } else {
                curTreeRoot = Integer.MAX_VALUE; // Reset tree root if no elements left
            }
        }
        // If the element is not found, do nothing
    }

    public int[] query() {
        if (beta != 0) {
            return Arrays.copyOf(sample, (int) (curSampleSize / beta));
        }
        return Arrays.copyOf(sample, curSampleSize);
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
        if (beta != 0) {
            return (int) (curSampleSize / beta);
        }
        return curSampleSize;
    }
}

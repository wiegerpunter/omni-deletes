package omni.synopses.omniFactory.SampleTypes;

import omni.Experiments.parameterSetting.Formulas;
import omni.synopses.omniFactory.ArrayWithBuffer.ArrayWithBuffer;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

public class KminTreeSet implements Sample {
    private final int B;
    private final int b;
    private int n;
    private TreeSet<Integer> sample;
    private final String setting;
    private double beta;
    private int KInQuery;
    private long curTreeRoot = Integer.MAX_VALUE;
    private int curSampleSize = 0;


    public KminTreeSet(int B, int b, double beta, String setting) {
        this.B=B;
        this.b=b;
        this.setting = setting;
        this.n = 0;
        this.beta = beta;
        if (beta == 0) {
            this.sample = new TreeSet<>(Collections.reverseOrder());
            KInQuery = B;
        } else {
            this.sample = new TreeSet<>(Collections.reverseOrder());
            KInQuery = (int) (B / beta);
        }
    }

    @Override
    public void ingest(int hx, int sign) {
        // Implementation for ingesting values into the KminTreeSet
        if (sign == 1) {
            add(hx);
        } else {
            remove(hx);
        }
    }

    private void add(int hx) {
        n++;
        if (curSampleSize <= B - 1) {
            sample.add(hx);
            curSampleSize++;
        } else if (curSampleSize == B) {
            curTreeRoot = sample.first();
            tryInsert(hx);
        } else {
            tryInsert(hx);
        }
    }

    private void tryInsert(int hx) {
        if (hx < curTreeRoot) {
            sample.pollFirst();
            if (sample.add(hx)) {
                curTreeRoot = sample.first();
            }
        }
    }

    private void remove(int hx) {
        n--;
        if (hx > curTreeRoot) {
            return; // No need to remove if hx is greater than the current root
        }
        if (sample.contains(hx)) {
            sample.remove(hx);
            curSampleSize--;
            if (!sample.isEmpty()) {
                curTreeRoot = sample.first();
            } else {
                curTreeRoot = Integer.MAX_VALUE; // Reset if the set is empty
            }
        }
    }

    @Override
    public Object query() {
        if (beta == 1) {
            int cnt = 0;
            TreeSet<Integer> result = new TreeSet<>(Collections.reverseOrder());
            Iterator<Integer> x = sample.descendingIterator();
            while (cnt <= KInQuery && x.hasNext()) {
                int k = x.next();
                result.add(k);
                cnt++;
            }

            // check if result is sample
            if (result.size() != sample.size()) {
                throw new RuntimeException("Error in KminTreeSet query: result size does not match sample size.");
            }
            if (!result.equals(sample)) {
                throw new RuntimeException("Error in KminTreeSet query: result does not match sample.");
            }
        }

        if (beta == 0) {
            return sample;
        } else {
            int cnt = 0;
            TreeSet<Integer> result = new TreeSet<>(Collections.reverseOrder());
            Iterator<Integer> x = sample.descendingIterator();
            while (cnt <= KInQuery && x.hasNext()) {
                int k = x.next();
                result.add(k);
                cnt++;
            }
            return result;
        }
    }

    @Override
    public long getMemoryFootprint() {
        return Formulas.ramSingleKmin(sample.size(), b);
    }

    @Override
    public void reset() {
        this.sample = new TreeSet<>(Collections.reverseOrder());
        this.n = 0;
        this.curTreeRoot = Integer.MAX_VALUE;
        this.curSampleSize = 0;
    }

    @Override
    public String getKminType() {
        return setting;
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
    public int getCurSampleSize() {
        return Math.min(curSampleSize, KInQuery);
    }
}

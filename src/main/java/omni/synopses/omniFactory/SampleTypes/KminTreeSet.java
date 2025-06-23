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
    private final double beta;
    private final int KInQuery;
    private long curTreeRoot = Integer.MAX_VALUE;
    private int bufferSize = 0;


    public KminTreeSet(int B, int b, double beta, String setting) {
        this.B=B;
        this.b=b;
        this.setting = setting;
        this.n = 0;
        this.beta = beta;
        if (beta == 0) {
            this.sample = new TreeSet<>(Collections.reverseOrder());
            KInQuery = B;
        } else if (beta >= 1) {
            this.sample = new TreeSet<>(Collections.reverseOrder());
            KInQuery = B;
            bufferSize = (int) (beta - 1) * B;
        } else {
            throw new IllegalArgumentException("Beta should be >= 1");
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
        if (sample.size() <= B - 1 + bufferSize) {
            sample.add(hx);
        } else if (sample.size() == B + bufferSize) {
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
            if (!sample.isEmpty()) {
                curTreeRoot = sample.first();
            } else {
                curTreeRoot = Integer.MAX_VALUE; // Reset if the set is empty
            }
        }
    }

    @Override
    public Object query() {
        if (beta == 0) {
            return sample;
        } else {
            int cnt = 0;
            TreeSet<Integer> result = new TreeSet<>(Collections.reverseOrder());
            Iterator<Integer> x = sample.iterator();
            while (cnt <= KInQuery && x.hasNext()) {
                result.add(x.next());
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
        return Math.min(sample.size(), KInQuery);
    }
}

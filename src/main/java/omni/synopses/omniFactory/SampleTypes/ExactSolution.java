package omni.synopses.omniFactory.SampleTypes;

import java.util.HashSet;
import java.util.Set;

public class ExactSolution implements Sample {
    private Set<Integer> set;
    private int n;

    public ExactSolution() {
        this.set = new HashSet<Integer>();
    }

    public void ingest(int hx, int sign) {
        if (sign == 1) {
            set.add(hx);
        } else {
            set.remove(hx);
        }
        n = set.size();
    }

    public Set<Integer> query() {
        return set;
    }

    @Override
    public long getMemoryFootprint() {
        long size = 0;
        for (Integer value : set) {
            size += Integer.BYTES; // Size of each integer
        }
        return size + Long.BYTES; // Add size for the long n
    }

    @Override
    public void reset() {
        set.clear();
        n = 0;
    }

    @Override
    public String getKminType() {
        return "ExactSolution";
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
    public int getCurSampleSize() {
        return set.size();
    }
}

package omni.synopses.omniFactory.KminTypes;

import omni.synopses.omniFactory.CustomPriorityQueue.PriorityQueue;
import omni.Experiments.parameterSetting.Formulas;

public class KminPQ implements Kmin {

    private int B;
    private int b;
    private int n;
    private int curSampleSize;
    private PriorityQueue pq;
    private int curTreeRoot = Integer.MAX_VALUE;

    public KminPQ(int B, int b) {
        this.B = B;
        this.b = b;
        this.n = 0;
        this.curSampleSize = 0;
        this.pq = new PriorityQueue(B);
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
        if (curSampleSize < B - 1) {
            pq.add(hx);
            curSampleSize++;
        } else if (curSampleSize == B) {
            curTreeRoot = pq.peek();
            tryInsert(hx);
        } else {
            tryInsert(hx);
        }
    }


    private void tryInsert(int hx) {
        if (hx < curTreeRoot) { // get tree root
            pq.poll();
            pq.add(hx);
            curTreeRoot = pq.peek();
        }
    }

    private void remove(int hx) {
        throw new UnsupportedOperationException("Remove operation is not supported in KminPQ");
    }

    public PriorityQueue query() {
        return PriorityQueue.getCopy(pq);
    }

    public long getMemoryFootprint() {
        return Formulas.ramSingleKmin(pq.size + 1, b);
    }

    @Override
    public void reset() {
        pq.clear();
        curSampleSize = 0;
        n = 0;
        curTreeRoot = Integer.MAX_VALUE;
    }

    @Override
    public String getKminType() {
        return "KminPQ"; // KminPQ type
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

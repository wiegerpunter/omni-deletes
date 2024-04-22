package omni.omniTwoLHS;

import omni.AnalysisBaselinesRefactor;
import omni.SynopsisRefactor;

public class ReservoirSample extends SynopsisRefactor {
    private final int[] reservoir;
    private int count;
    private final int size;

    public ReservoirSample(int size) {
        this.size = size;
        this.reservoir = new int[size];
        this.count = 0;
    }

    public void add(int item) {
        if (count < size) {
            reservoir[count] = item;
        } else {
            int replace = (int) (Math.random() * count);
            if (replace < size) {
                reservoir[replace] = item;
            }
        }
        count++;
    }

    @Override
    public void add(long[] record) {

    }

    @Override
    public int query(long[] query, int numPreds) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        return 0;
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void delete(long[] r) {

    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public void printParams() {
        System.out.println("ReservoirSample");

    }
}

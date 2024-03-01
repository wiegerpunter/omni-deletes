package omni;

public abstract class SynopsisRefactor {
    public int[] maxBits;
    public int countIsZero = 0;
    public String sampleType;
    public boolean useTwoLHS;
    public boolean useAcrossRows;
    protected String setting;
    protected long ram;
    public long memUsageSynopsis;
    public int[] parameters;
    public boolean useTwoKmin;
    public boolean twoLHSFast;

    public SynopsisRefactor() {

    }

    public abstract void add(long[] record);
    public abstract int query(long[] query, int numPreds);
    public abstract int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo);

    public abstract int query(long[] query, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow);
    public abstract int rangeQuery(long[] minrange, long[] maxrange);
    public abstract void reset();

    public long getMemoryUsage() {
        return memUsageSynopsis;
    }

    public abstract void delete(long[] r);

    public int[] checkConditions(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow){
        throw new UnsupportedOperationException();
    };

    public abstract int getFilledKSamples();
}

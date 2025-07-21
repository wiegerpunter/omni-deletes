package omni.synopses;

import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;

public abstract class SynopsisRefactor {
    public int countIsZero = 0;
    public String sampleType;
    public String setting;
    public long ram;
    public long memUsageSynopsis;
    public int[] parameters;
    public boolean useBetaKmin;
    public int seed;

    public SynopsisRefactor() {

    }

    public abstract void add(Record record);
    public abstract void add(long[] record);

    public abstract void ingest(Record record, int i);
    public abstract int query(long[] query, int numPreds);

    public abstract int query(Record query, int numPreds, QueryInfo queryInfo);
    public abstract int query(long[] query, int numPreds, QueryInfo queryInfo);

    public abstract int query(long[] query, int numPreds, int unionSize, QueryInfo CMRow);
    public abstract int rangeQuery(long[] minrange, long[] maxrange);
    public abstract void reset();

    public long getMemoryUsage() {
        return memUsageSynopsis;
    }

    public abstract void delete(long[] r);
    public abstract void delete(Record r);

    public int[] checkConditions(long[] q, int numPreds, int unionSize, QueryInfo CMRow){
        throw new UnsupportedOperationException();
    };

    public abstract int getFilledKSamples();

    public String getSetting() {
        return setting;
    }

}

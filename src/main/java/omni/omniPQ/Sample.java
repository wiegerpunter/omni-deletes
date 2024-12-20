package omni.omniPQ;

public abstract class Sample {
    public int n;
    public long hashDomain;
    public int bitSize = 41;
    protected String setting;
    protected int ram;
    public int memUsageSynopsis;
    public int curSampleSize;
    public int seed;

    public Sample() {

    }
    //public abstract void add(int id);
    public abstract void add(int hx);

    public abstract void ingest(int hx, int sign);

    //public abstract Sample intersect(Sample sample);
    //public abstract Sample union(Sample sample);

    public abstract void ingest(long[] vals, int sign);

    public abstract void reset();

    public long getMemoryUsage() {
        return memUsageSynopsis;
    }
}

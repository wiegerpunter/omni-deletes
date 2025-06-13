package omni.synopses.omniFactory.SampleTypes;

public interface Sample {
    void ingest(int hx, int sign);
    Object query();
    long getMemoryFootprint();
    void reset();

    String getKminType();
    int getN();
    int getCurSampleSize();
}

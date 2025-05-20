package omni.synopses.omniFactory.KminTypes;

public interface Kmin {
    void ingest(int hx, int sign);
    Object query();
    long getMemoryFootprint();
    void reset();

    String getKminType();
    int getN();
    int getCurSampleSize();
}

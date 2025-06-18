package omni.synopses.omniFactory.OmniSketchTypes;

import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;

public abstract class OmniSketchType {
    public int width;
    public int depth;
    public int numStoredAttributes;

    public OmniSketchConfig sketchConfig;
    public abstract void initialize();

    public abstract void ingest(long[] record, int i);

    public abstract void reset();
    public abstract int query(long[] query, int numPreds, QueryInfo queryInfo);

    public abstract String getSketchType();

    public abstract long getMemoryUsage();
}
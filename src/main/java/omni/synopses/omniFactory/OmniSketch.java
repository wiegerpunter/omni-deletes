package omni.synopses.omniFactory;
import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.synopses.SynopsisRefactor;
import omni.synopses.omniFactory.OmniSketchTypes.OmniSketchType;
import omni.synopses.omniFactory.OmniSketchTypes.OmniSketchTypeFactory;

import java.util.Arrays;

public class OmniSketch extends SynopsisRefactor {
    private final OmniSketchConfig config;
    private final OmniSketchType sketch;

    public OmniSketch(OmniSketchConfig config, String sketchType) {
        this.config = config;
        this.setting = sketchType;
        this.sketch = OmniSketchTypeFactory.createOmniSketchType(sketchType, config);
        parameters = config.getParams();
        ram = config.getRam();
        // Initialize other components based on the configuration
    }

    @Override
    public void add(Record record) {
        ingest(record, 1);
    }

    @Override
    public void add(long[] record) {
        // Add logic to insert a record into the sketch
        ingest(record, 1);
    }

    public void delete(long[] record) {
        // Logic to delete a record from the sketch
        ingest(record, -1);
    }

    @Override
    public void delete(Record r) {
        ingest(r, -1);
    }

    private void ingest(long[] record, int i) {
        sketch.ingest(record, i);
    }

    public void ingest(Record record, int i) {
        sketch.ingest(record, i);
    }


    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        // Query logic to retrieve results based on the query, union size, and additional info
        return sketch.query(query, numPreds, queryInfo);
    }

    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        // Query logic to retrieve results based on the query, union size, and additional info
        return sketch.query(query, numPreds, queryInfo);
    }

    public int rangeQuery(long[] minRange, long[] maxRange) {
        // Range query logic to retrieve results within the specified range
        return 0; // Replace with actual query result
    }

    public void reset() {
        // Reset logic
        sketch.reset();
    }

    public String getSetting() {
        return sketch.getSketchType();
    }

    public void printParams() {
        System.out.println("OmniSketch Configuration: " + Arrays.toString(config.getParams()));
    }

    @Override
    public long getMemoryUsage() {
        return sketch.getMemoryUsage();
    }


    // Deprecated methods
    @Deprecated
    public int query(long[] query, int numPreds) {
        // Query logic to retrieve results based on the query
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public int query(long[] query, int numPreds, int unionSize, QueryInfo queryInfo) {
        // Query logic to retrieve results based on the query and additional info
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public int getFilledKSamples() {
        // Logic to get the number of filled K samples
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public int[] checkConditions(long[] query, int numPreds, int unionSize, QueryInfo queryInfo) {
        // Logic to check conditions based on the query and additional info
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Add other methods as needed
}
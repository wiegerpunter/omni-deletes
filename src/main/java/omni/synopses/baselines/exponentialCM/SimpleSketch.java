package omni.synopses.baselines.exponentialCM;

import omni.Main;
import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Query;
import omni.synopses.SynopsisRefactor;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleSketch extends SynopsisRefactor {
    static int depth;
    static int width;

    //CountMinBaseline[] CMSketches = new CountMinBaseline[(int) Math.pow(2, Main.numAttributes) - 1];
    HashMap<ArrayList<Boolean>, CountMinBaseline> CMSketchesMap = new HashMap<>();

    public SimpleSketch(double eps, double delta) {
        //this.eps = eps;
        //this.delta = delta;
        depth = (int) Math.ceil(Math.log(1 / delta));
        width = (int) Math.ceil(Math.E / eps);
        this.setting = "CMBaseline";
    }

    public SimpleSketch(int ram, int depth, int width) {
        this.setting = "CMBaseline";
        this.ram = ram;
        SimpleSketch.depth = depth;
        SimpleSketch.width = width;
        initSketch();
    }

    public void recurCMInit(ArrayList<Boolean> attrs, int i) {
        for (int j = i + 1; j < Main.numAttributes; j++) {
            ArrayList<Boolean> attrsCopy = new ArrayList<>();
            attrsCopy = (ArrayList<Boolean>) attrs.clone();
            attrsCopy.set(j, true);
            CMSketchesMap.put(attrsCopy, new CountMinBaseline(attrsCopy));
            cnt++;
            if (i == Main.numAttributes - 1) {
                return;
            }
            recurCMInit(attrsCopy, j);
        }

    }

    int cnt = 0;
    // Make sure only one permutation of attributes makes CM sketch
    public void initSketch() {
        // Create 2^Main.numAttributes - 1 CM sketches.
        // Each sketch has a boolean array of length Main.numAttributes.
        // Each boolean value indicates that CM sketch is made for combination of true values
        // in the boolean array.

        for (int i = 0; i < Main.numAttributes; i++) {
            ArrayList<Boolean> attrs = new ArrayList<Boolean>();
            for (int k = 0; k < Main.numAttributes; k++) {
                attrs.add(false);
            }
            attrs.set(i, true);
            CMSketchesMap.put(attrs, new CountMinBaseline(attrs));
            cnt++;
            if (i >= Main.numAttributes - 1) {
                continue;
            }
            recurCMInit(attrs, i);
        }
        if (cnt != CMSketchesMap.size()) {
            throw new RuntimeException("cnt != CMSketches.length");
        }
    }
    public int rangeQuery(Query q) {
        return 0;
    }

    public long getMemoryUsage() {
        int memoryUsage = 0;
        for (CountMinBaseline CMSketch : CMSketchesMap.values()) {
            memoryUsage += CMSketch.getMemoryUsage();
        }
        return memoryUsage;
    }

    @Override
    public void delete(long[] r) {

    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    @Override
    public void add(long[] record) {
        for (CountMinBaseline CMSketch : CMSketchesMap.values()) {
            CMSketch.add(record);
        }
    }

    @Override
    public int query(long[] query, int numPreds) {
        return 0;
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, QueryInfo CMRow) {
        return 0;
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        return 0;
    }

    @Override
    public void reset() {
        CMSketchesMap.clear();
    }
}

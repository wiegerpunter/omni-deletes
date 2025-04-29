package omni.baseline;

import omni.Main;
import omni.Record.Query;
import omni.Record.Record;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleSketch extends omni.Synopsis {
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

    // Add record to all sketches it should be added to.

    public void add(Record record) {
        for (CountMinBaseline CMSketch : CMSketchesMap.values()) {
            CMSketch.add(record);
        }
    }

    public int query(Query query) {
        // Need to map from query.pointAttrs to CMSketches index.
        // For example, if query.pointAttrs = [true, false, true, false, false]
        // then we need to find the index of CMSketches with attrs = [true, false, true, false, false]
        // and query that sketch.
        // This is done by converting query.pointAttrs to a long value.
        ArrayList<Boolean> key = new ArrayList<Boolean>();
        for (int i = 0; i < query.pointAttrs.length; i++) {
            key.add(query.pointAttrs[i]);
        }
        if (CMSketchesMap.containsKey(key)) {
            return CMSketchesMap.get(key).query(query);
        } else {
            throw new RuntimeException("CMSketch == null");
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
    public void reset() {
        CMSketchesMap.clear();
    }
}

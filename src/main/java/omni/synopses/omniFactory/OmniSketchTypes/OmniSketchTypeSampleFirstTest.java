package omni.synopses.omniFactory.OmniSketchTypes;

import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchHashSetGuava;

import java.util.Arrays;
import java.util.Comparator;
import java.util.BitSet;
import java.util.Random;

public class OmniSketchTypeSampleFirstTest extends OmniSketchType {
    private final int sampleSize;
    private AttrSketchHashSetGuava[] attributeSketches;

    private int sampleCount;
    private final Random randomInt;
    double lastTermInBoundAcrossRows;
    double lastTermInBoundPerRow;

    private double delta;
    private double eps;
    private int capacity;

    public OmniSketchTypeSampleFirstTest(OmniSketchConfig sketchConfig) {
        this.sketchConfig = sketchConfig;
        this.depth = sketchConfig.getParams()[0];
        this.width = sketchConfig.getParams()[1];
        this.numStoredAttributes = sketchConfig.getNumStoredAttributes();
        this.sampleSize = sketchConfig.getParams()[2];

        this.delta = 2/Math.pow(Math.exp(1), depth);
        this.eps = sketchConfig.getEps();
        this.randomInt = new Random(sketchConfig.getSeed());
        this.capacity = sketchConfig.getParams()[3];
        initialize();
    }

    @Override
    public void initialize() {
        // Initialize the sketch with sample-first logic
        attributeSketches = new AttrSketchHashSetGuava[numStoredAttributes];
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i] = new AttrSketchHashSetGuava(sketchConfig, depth, width, sampleSize, capacity);
        }

        lastTermInBoundAcrossRows = 3 *Math.log(4 * depth * Math.sqrt(sampleSize)/ delta) / Math.log(Math.exp(1))/(eps * eps);
        lastTermInBoundPerRow = 3 * Math.log(4 * Math.sqrt(sampleSize)/delta) / Math.log(Math.exp(1))/(eps*eps);
    }

    @Override
    public void ingest(long[] record, int sign) {
        if (sampleCount < sampleSize) {
            for (int i = 0; i < numStoredAttributes; i++) {
                attributeSketches[i].ingestFirstRecords(record[i + 1], sampleCount, sign);
            }
        } else {
            int replace = randomInt.nextInt(sampleCount);
            if (replace < sampleSize) {
                for (int i = 0; i < numStoredAttributes; i++) {
                    attributeSketches[i].ingest(record[i + 1], replace, sign);
                }
            }
        }
        sampleCount++;
    }

    @Override
    public void reset() {
        // Reset the sketch state
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        return queryHashSets(hashSetsInQuery(query, numPreds), queryInfo, numPreds);
    }

    @Override
    public String getSketchType() {
        return "OmniSketchSampleFirst";
    }

    @Override
    public long getMemoryUsage() {
        long memoryFootprint = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            memoryFootprint += attributeSketches[i].getMemoryFootprint();
        }
        return memoryFootprint;
    }

    private BitSet[] hashSetsInQuery(long[] q, int numPreds) {
        BitSet[] result = new BitSet[depth * numPreds];
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) {
                BitSet[] temp = attributeSketches[i].query(q[i]);
                if (depth >= 0) {
                    System.arraycopy(temp, 0, result, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return result;
    }


    private int queryHashSets(BitSet[] samples, QueryInfo queryInfo, int numPreds) {
        // Intersect all buckets and return the size of the intersection.

        int numJoins = samples.length;
        if (numJoins == 1) {
            return samples[0].size() * (int) (Math.max((double) sampleCount / sampleSize, 1));
        }
        Arrays.sort(samples, Comparator.comparingInt(BitSet::size));

        int intersectionCount = 0;
        boolean inIntersection = true;
        for (int i = 0; i < samples[0].size(); i++) {
            for (int j = 1; j < numJoins; j++) {
                if (!samples[j].get(i)) {
                    inIntersection = false;
                    break;
                }
                // TODO: when we're doing per row, we need to add to temp.
            }
            if (inIntersection) {
                intersectionCount++;
            }
            inIntersection = true;
        }
        queryInfo.setScap(intersectionCount, sampleCount, sampleSize, false);
        if (intersectionCount < lastTermInBoundAcrossRows + Math.log(numPreds)/Math.log(Math.exp(1))) {
            queryInfo.case1=true;
        };
        return intersectionCount * (int) (Math.max((double) sampleCount / sampleSize, 1));
    }


}

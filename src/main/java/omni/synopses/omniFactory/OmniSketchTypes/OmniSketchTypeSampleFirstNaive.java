package omni.synopses.omniFactory.OmniSketchTypes;

import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchHashSetNaive;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class OmniSketchTypeSampleFirstNaive extends OmniSketchType {
    private final int sampleSize;
    private AttrSketchHashSetNaive[] attributeSketches;

    private int sampleCount;
    private final Random randomInt;
    double lastTermInBoundAcrossRows;
    double lastTermInBoundPerRow;

    private long[] sampledIDs;

    private final double delta;
    private final double eps;

    public OmniSketchTypeSampleFirstNaive(OmniSketchConfig sketchConfig) {
        this.sketchConfig = sketchConfig;
        this.depth = sketchConfig.getParams()[0];
        this.width = sketchConfig.getParams()[1];
        this.numStoredAttributes = sketchConfig.getNumStoredAttributes();
        this.sampleSize = sketchConfig.getParams()[2];

        this.delta = 2/Math.pow(Math.exp(1), depth);
        this.eps = sketchConfig.getEps();
        this.randomInt = new Random(sketchConfig.getSeed());
        initialize();
    }

    @Override
    public void initialize() {
        sampledIDs = new long[sampleSize]; // implement naive to check acc vs the more complicated one.
        // Initialize the sketch with sample-first logic
        attributeSketches = new AttrSketchHashSetNaive[numStoredAttributes];
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i] = new AttrSketchHashSetNaive(sketchConfig, depth, width, sampleSize);
        }

        lastTermInBoundAcrossRows = 3 *Math.log(4 * depth * Math.sqrt(sampleSize)/ delta) / Math.log(Math.exp(1))/(eps * eps);
        lastTermInBoundPerRow = 3 * Math.log(4 * Math.sqrt(sampleSize)/delta) / Math.log(Math.exp(1))/(eps*eps);
    }

    @Override
    public void ingest(long[] record, int sign) {
        if (sampleCount < sampleSize) {
            sampledIDs[sampleCount] = record[0];
            for (int i = 0; i < numStoredAttributes; i++) {
                attributeSketches[i].ingest(record[i + 1], record[0], sign);
            }
        } else {
            int replace = randomInt.nextInt(sampleCount);
            if (replace < sampleSize) {
                long recToReplace = sampledIDs[replace];
                for (int i = 0; i < numStoredAttributes; i++) {
                    attributeSketches[i].ingest(record[i + 1], recToReplace, -1);
                    attributeSketches[i].ingest(record[i + 1], record[0], 1);
                }
            }
        }
        sampleCount++;
    }

    @Override
    public void ingest(Record record, int i) {
throw new UnsupportedOperationException("Ingesting Record is not supported in OmniSketchTypeSampleFirst");
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
    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        throw new UnsupportedOperationException("Querying Record is not supported in OmniSketchTypeSampleFirst");
    }

    @Override
    public String getSketchType() {
        return "OmniSketchSampleFirstNaive";
    }

    @Override
    public long getMemoryUsage() {
        long memoryFootprint = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            memoryFootprint += attributeSketches[i].getMemoryFootprint();
        }
        memoryFootprint+= sampledIDs.length* 32L;
        return memoryFootprint;
    }

    private HashSet<Integer>[] hashSetsInQuery(long[] q, int numPreds) {
        HashSet<Integer>[] result = new HashSet[depth * numPreds];
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) {
                HashSet<Integer>[] temp = attributeSketches[i].query(q[i]);
                if (depth >= 0) {
                    System.arraycopy(temp, 0, result, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return result;
    }


    private int queryHashSets(HashSet<Integer>[] samples, QueryInfo queryInfo, int numPreds) {
        // Intersect all buckets and return the size of the intersection.

        int numJoins = samples.length;
        if (numJoins == 1) {
            return samples[0].size() * (int) (Math.max((double) sampleCount / sampleSize, 1));
        }
        Arrays.sort(samples, Comparator.comparingInt(HashSet::size));

        int intersectionCount = 0;
        boolean inIntersection = true;
        for (int i : samples[0]) {
            for (int j = 1; j < numJoins; j++) {
                if (!samples[j].contains(i)) {
                    inIntersection = false;
                    break;
                }
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

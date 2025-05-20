package omni.synopses.omniFactory.OmniSketchTypes;

import omni.Experiments.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchHashSetPerRow;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class OmniSketchTypeSampleFirstPerRow extends OmniSketchType {
    private final int sampleSize;
    private AttrSketchHashSetPerRow[][] attributeSketches;

    private int sampleCount;
    private final Random[] randomInt;
    double lastTermInBoundPerRow;

    private double delta;
    private double eps;

    public OmniSketchTypeSampleFirstPerRow(OmniSketchConfig sketchConfig) {
        this.sketchConfig = sketchConfig;
        this.depth = sketchConfig.getParams()[0];
        this.width = sketchConfig.getParams()[1];
        this.numStoredAttributes = sketchConfig.getNumStoredAttributes();
        this.sampleSize = sketchConfig.getParams()[2];

        this.delta = 2/Math.pow(Math.exp(1), depth);
        this.eps = sketchConfig.getEps();
        this.randomInt = new Random[depth];
        for (int i = 0; i < depth; i++) {
            this.randomInt[i] = new Random(sketchConfig.getSeed() + i);
        }
        initialize();
    }

    @Override
    public void initialize() {
        // Initialize the sketch with sample-first logic
        attributeSketches = new AttrSketchHashSetPerRow[depth][numStoredAttributes];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < numStoredAttributes; i++) {
                attributeSketches[j][i] = new AttrSketchHashSetPerRow(sketchConfig, j, width, sampleSize);
            }
        }

        lastTermInBoundPerRow = 3 * Math.log(4 * Math.sqrt(sampleSize)/delta) / Math.log(Math.exp(1))/(eps*eps);
    }

    @Override
    public void ingest(long[] record, int sign) {
        if (sampleCount < sampleSize) {
            for (int j = 0; j < depth; j++) {
                for (int i = 0; i < numStoredAttributes; i++) {
                    attributeSketches[j][i].ingest(record[i + 1], sampleCount, sign);
                }
            }
        } else {
            for (int j = 0; j < depth; j++) {
                int replace = randomInt[j].nextInt(sampleCount);
                if (replace < sampleSize) {
                    for (int i = 0; i < numStoredAttributes; i++) {
                        attributeSketches[j][i].ingest(record[i + 1], replace, sign);
                    }
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
        return queryHashSetsPerRow(hashSetsInQueryPerRow(query, numPreds), queryInfo, numPreds);
    }

    @Override
    public String getSketchType() {
        return "OmniSketchSampleFirstPerRow";
    }

    @Override
    public long getMemoryUsage() {
        long memoryFootprint = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < numStoredAttributes; i++) {
                memoryFootprint += attributeSketches[j][i].getMemoryFootprint();
            }
        }
        return memoryFootprint;
    }

    private HashSet<Integer>[][] hashSetsInQueryPerRow(long[] q, int numPreds) {
        HashSet<Integer>[][] result = new HashSet[depth][numPreds];
        for (int j = 0; j < depth; j++) {
            int attrWithoutPred = 0;
            for (int i = 0; i < q.length; i++) {
                if (q[i] != -1) {
                    HashSet<Integer> temp = attributeSketches[j][i].query(q[i]);
                    result[j][i - attrWithoutPred] = temp;
                } else {
                    attrWithoutPred++;
                }
            }
        }
        return result;
    }

    private int queryHashSetsPerRow(HashSet<Integer>[][] samples, QueryInfo queryInfo, int numPreds) {
        // Intersect all buckets and return the size of the intersection.

        int numJoins = samples[0].length;
        if (numJoins == 1) {
            // median of sizes of the samples
            int[] estimates = new int[depth];
            for (int j = 0; j < depth; j++) {
                estimates[j] = samples[j][0].size();
            }
            Arrays.sort(estimates);
            return estimates[depth / 2] * (int) (Math.max((double) sampleCount / sampleSize, 1));
        }

        // now compute estimate per row and take the median
        QueryInfo[] estimates = new QueryInfo[depth];

        for (int j = 0; j < depth; j++) {
            HashSet<Integer>[] temp = samples[j];
            Arrays.sort(temp, Comparator.comparingInt(HashSet::size));
            int intersectionCount = 0;
            boolean inIntersection = true;
            for (int i : temp[0]) {
                for (int join = 1; join < numJoins; join++) {
                    if (!temp[join].contains(i)) {
                        inIntersection = false;
                        break;
                    }
                }
                if (inIntersection) {
                    intersectionCount++;
                }
                inIntersection = true;
            }
            estimates[j] = new QueryInfo(queryInfo);
            estimates[j].setScap(intersectionCount, sampleCount, sampleSize, false);
            if (intersectionCount < lastTermInBoundPerRow + Math.log(numPreds)/Math.log(Math.exp(1))) {
                estimates[j].case1=true;
            }
            estimates[j].setEstimate(intersectionCount * (int) (Math.max((double) sampleCount / sampleSize, 1)));
        }
        Arrays.sort(estimates, Comparator.comparingInt(QueryInfo::getEstimate));
        queryInfo.setEstimate(estimates[depth / 2].getEstimate());
        queryInfo.setScap(estimates[depth / 2].getScap(), sampleCount, sampleSize, estimates[depth / 2].isCase1());
        return estimates[depth / 2].getEstimate();
    }


}

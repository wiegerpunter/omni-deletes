package omni.synopses.omniFactory.OmniSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.Experiments.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.TWOLHS;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchTWOLHS;
import omni.synopses.omniFactory.utils.TWOLHSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OmniSketchTypeSampleLaterTWOLHS extends OmniSketchType {
    private String SampleType;
    AttrSketchTWOLHS[] attributeSketches;
    HashFunction[] xx;
    private int numTWOLHSRepetitions;
    private double epsilon;

    public OmniSketchTypeSampleLaterTWOLHS(OmniSketchConfig sketchConfig, String SampleType) {
        this.sketchConfig = sketchConfig;
        this.SampleType = SampleType;
        this.depth = sketchConfig.getParams()[0];
        this.width = sketchConfig.getParams()[1];
        this.numTWOLHSRepetitions = sketchConfig.getParams()[2];
        this.numStoredAttributes = sketchConfig.getNumStoredAttributes();
//        delta = 2/(Math.pow(Math.exp(1), depth));
        epsilon = sketchConfig.getEps();
        initialize();
    }

    @Override
    public void initialize() {
        // Initialize the sketch with sample-later logic
        attributeSketches = new AttrSketchTWOLHS[numStoredAttributes];
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i] = new AttrSketchTWOLHS(sketchConfig, depth, width, numTWOLHSRepetitions, SampleType);
        }
    }

    @Override
    public void ingest(long[] record, int sign) {
        int id = (int) record[0];
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i].ingest(record[i + 1], id, sign);
        }
    }

    @Override
    public void reset() {
        // Reset the sketch state
        for (AttrSketchTWOLHS attributeSketch : attributeSketches) {
            attributeSketch.reset();
        }
        Arrays.fill(xx, null);
    }


    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        if (sketchConfig.isUseAcrossRows()) {
            Sample[][] samples = getCellsToIntersectAcrossRows(query, numPreds);
            double u = TWOLHSUtils.setUnionEstimator((TWOLHS[][]) samples, epsilon,
                    numTWOLHSRepetitions, sketchConfig.getUseFastTWOLHS());
            double estimate = TWOLHSUtils.setIntersectEstimator((TWOLHS[][]) samples,
                    u, epsilon, sketchConfig.getUseFastTWOLHS(), queryInfo, sketchConfig);

            if (queryInfo.jaccardEstimates.isEmpty()) {
                throw new IllegalArgumentException("Jaccard estimates not set");
            }
            double jaccardEstimate = queryInfo.jaccardEstimates.get(0);
            int witnessEstimate = queryInfo.witnessEstimates.get(0);

            queryInfo.set2LHS(u, witnessEstimate, jaccardEstimate);
            return (int) estimate;
        } else {
            ArrayList<QueryInfo> queryInfos = new ArrayList<>(depth);
            Sample[][][] cellsToIntersect = getCellsToIntersectPerRow(query, numPreds);
            double[] unionEstimates = new double[depth];
            double[] estimates = new double[depth];
            for (int j = 0; j < depth; j++) {
                queryInfos.add(new QueryInfo());
                unionEstimates[j] = TWOLHSUtils.setUnionEstimator((TWOLHS[][]) cellsToIntersect[j], epsilon,
                        numTWOLHSRepetitions, sketchConfig.getUseFastTWOLHS());
                estimates[j] = TWOLHSUtils.setIntersectEstimator((TWOLHS[][]) cellsToIntersect[j],
                        unionEstimates[j], epsilon, sketchConfig.getUseFastTWOLHS(), queryInfos.get(j), sketchConfig);
                if (queryInfos.get(j).jaccardEstimates.isEmpty()) {
                    throw new IllegalArgumentException("Jaccard estimates not set");
                }
                queryInfos.get(j).set2LHS(unionEstimates[j], (int) estimates[j],
                        queryInfos.get(j).jaccardEstimates.get(0));
            }
            // sort queryInfos on estimates
            QueryInfoSorter.sortByEstimateSize(queryInfos);
            queryInfo = queryInfos.get(queryInfos.size()/2);
            return queryInfo.getEstimate();
        }
    }

    private static class QueryInfoSorter {
        public static void sortByEstimateSize(List<QueryInfo> queryInfos) {
            queryInfos.sort(Comparator.comparingInt(QueryInfo::getEstimate));
        }
    }

    private Sample[][] getCellsToIntersectAcrossRows(long[] query, int numPreds) {
        Sample[][] cellsToIntersect = new Sample[depth * numPreds][numTWOLHSRepetitions];
        int attrWithoutPred = 0;
        for (int i = 0; i < query.length; i++) {
            if (query[i] != -1) { // Find way to not take the -1s into account in query.
                Sample[][] temp = attributeSketches[i].query(query[i]);
                if (depth >= 0) {
                    System.arraycopy(temp, 0, cellsToIntersect, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return cellsToIntersect;
    }

    private Sample[][][] getCellsToIntersectPerRow(long[] query, int numPreds) {
        Sample[][][] cellsToIntersect = new Sample[depth][numPreds][numTWOLHSRepetitions];
        int numPredsFound = 0;
        for (int i = 0; i < query.length; i++) {
            if (query[i] != -1) {
                Sample[][] temp = attributeSketches[i].query(query[i]);
                for (int j = 0; j < depth; j++) {
                    cellsToIntersect[j][numPredsFound] = temp[j];
                }
                numPredsFound++;
            }
        }
        return cellsToIntersect;
    }

    @Override
    public String getSketchType() {
        String accrRows = "AcrossRows";
        if (!sketchConfig.isUseAcrossRows()) {
            accrRows = "PerRow";
        }
        return "OmniSketchSampleLater_" + SampleType + "_" + accrRows;
    }

    @Override
    public long getMemoryUsage() {
        long memoryUsage = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            memoryUsage += attributeSketches[i].getMemoryFootprint();
        }

        return memoryUsage;
    }
}

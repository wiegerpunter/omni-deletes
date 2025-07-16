package omni.synopses.omniFactory.OmniSketchTypes;

import com.google.common.hash.HashFunction;
import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchTWOLHS;
import omni.synopses.omniFactory.utils.TWOLHSUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OmniSketchTypeSampleLaterTWOLHS extends OmniSketchType {
    private String sampleType;
    AttrSketchTWOLHS[] attributeSketches;
    HashFunction[] xx;
    private int numTWOLHSRepetitions;
    private double epsilon;

    public OmniSketchTypeSampleLaterTWOLHS(OmniSketchConfig sketchConfig, String SampleType) {
        this.sketchConfig = sketchConfig;
        this.sampleType = SampleType;
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
            attributeSketches[i] = new AttrSketchTWOLHS(sketchConfig, depth, width, numTWOLHSRepetitions, sampleType);
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
        xx = new HashFunction[depth];
    }


    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        if (sketchConfig.isUseAcrossRows()) {
            Sample[][] samples = getCellsToIntersectAcrossRows(query, numPreds);
            double estimate = TWOLHSUtils.estimate(sampleType, samples, epsilon,
                    numTWOLHSRepetitions, queryInfo);
            queryInfo.setEstimate(queryInfo, (int) estimate);
            return (int) estimate;
        } else {
            ArrayList<QueryInfo> queryInfos = new ArrayList<>(depth);
            Sample[][][] cellsToIntersect = getCellsToIntersectPerRow(query, numPreds);
            for (int j = 0; j < depth; j++) {
                queryInfos.add(new QueryInfo());
                TWOLHSUtils.estimate(sampleType, cellsToIntersect[j], epsilon,
                        numTWOLHSRepetitions, queryInfos.get(j));

            }
            // sort queryInfos on estimates
            QueryInfoSorter.sortByEstimateSize(queryInfos);
            QueryInfo medianQueryInfo = queryInfos.get(queryInfos.size() / 2);
            queryInfo.setEstimate(medianQueryInfo, medianQueryInfo.getEstimate());
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
        return "OmniSketchSampleLater_" + sampleType + "_" + accrRows;
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

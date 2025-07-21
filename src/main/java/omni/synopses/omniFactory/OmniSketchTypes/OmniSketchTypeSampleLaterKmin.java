package omni.synopses.omniFactory.OmniSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.datasets.Record.RecordUtils;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.attributeSketchTypes.AttrSketchKmin;
import omni.synopses.omniFactory.utils.KminUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OmniSketchTypeSampleLaterKmin extends OmniSketchType {
    private final String KminType;
    AttrSketchKmin[] attributeSketches;
    HashFunction[] xx;
    private final int b;
    private final int B;

    private final double delta;
    private final double eps;

    double lastTermInBoundAcrossRows;
    double lastTermInBoundPerRow;

    private final int[] hx;

    public OmniSketchTypeSampleLaterKmin(OmniSketchConfig sketchConfig, String KminType) {
        this.sketchConfig = sketchConfig;
        this.KminType = KminType;
        this.depth = sketchConfig.getParams()[0];
        this.width = sketchConfig.getParams()[1];
        this.B = sketchConfig.getParams()[2];
        this.b = sketchConfig.getParams()[3];
        this.numStoredAttributes = sketchConfig.getNumStoredAttributes();
        delta = 2/(Math.pow(Math.exp(1), depth));
        eps = sketchConfig.getEps();
        this.hx = new int[depth];
        initialize();
    }

    @Override
    public void initialize() {
        // Initialize the sketch with sample-later logic
        attributeSketches = new AttrSketchKmin[numStoredAttributes];
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i] = new AttrSketchKmin(sketchConfig, depth, width, B, b, KminType);
        }

        xx = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            xx[i] = Hashing.murmur3_32_fixed(i + sketchConfig.getSeed());
        }

        lastTermInBoundAcrossRows = 3 *Math.log(4 * depth / delta) / Math.log(Math.exp(1))/(eps * eps);
        lastTermInBoundPerRow = 3 * Math.log(4/delta) / Math.log(Math.exp(1))/(eps*eps);
    }

    @Override
    public void ingest(long[] record, int sign) {
        long id = record[0];
        hx[0] = signatureHash(id, 0);
        if (sketchConfig.isUseAcrossRows()) {
            for (int i = 1; i < depth; i++) {
                hx[i] = hx[0];
            }
        } else {
            for (int i = 1; i < depth; i++) {
                hx[i] = signatureHash(id, i);
            }
        }
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i].ingest(record[i + 1], hx, sign);
        }

    }

    @Override
    public void ingest(Record record, int sign) {
        Object id = record.getValue(0);
        long hx_id;
        if (id instanceof Long) {
            hx_id = (Long) id;
        } else if (id instanceof String) {
            hx_id = Long.parseLong((String) id);
        } else {
            throw new IllegalArgumentException("ID must be of type Long or String that can be parsed to Long.");
        }
        hx[0] = signatureHash(hx_id, 0);
        if (sketchConfig.isUseAcrossRows()) {
            for (int i = 1; i < depth; i++) {
                hx[i] = hx[0];
            }
        } else {
            for (int i = 1; i < depth; i++) {
                hx[i] = signatureHash(hx_id, i);
            }
        }
        for (int i = 0; i < numStoredAttributes; i++) {
            attributeSketches[i].ingest(record.getValue(i + 1), hx, sign);
        }
    }

    private int signatureHash(long id, int row) {
        return xx[row].hashLong(id).asInt() >>> 1;
    }

    @Override
    public void reset() {
        // Reset the sketch state
        for (AttrSketchKmin attributeSketch : attributeSketches) {
            attributeSketch.reset();
        }
        Arrays.fill(xx, null);
    }


    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        if (sketchConfig.isUseAcrossRows()) {
            return KminUtils.intersectAndScale(getCellsToIntersectAcrossRows(query, numPreds), queryInfo, lastTermInBoundAcrossRows, numPreds);
        } else {
            ArrayList<QueryInfo> queryInfos = new ArrayList<>(depth);
            Sample[][] cellsToIntersect = getCellsToIntersectPerRow(query, numPreds);

            for (int j = 0; j < depth; j++) {
                queryInfos.add(new QueryInfo());
                if (cellsToIntersect[j].length == 1) {
                    // If only one predicate, we can directly use the estimate from the sample
                    queryInfos.get(j).setEstimate(cellsToIntersect[j][0].getN());
                } else {
                    // If multiple predicates, we need to intersect and scale
                    queryInfos.get(j).setEstimate(KminUtils.intersectAndScale(cellsToIntersect[j], queryInfos.get(j), lastTermInBoundPerRow, numPreds));
                }
//                queryInfos.get(j).setEstimate(KminUtils.intersectAndScale(cellsToIntersect[j], queryInfos.get(j), lastTermInBoundPerRow, numPreds));
            }
            // sort queryInfos on estimates
            QueryInfoSorter.sortByEstimateSize(queryInfos);
//            queryInfo = queryInfos.get(queryInfos.size()/2);
            queryInfo = queryInfos.get(0);
            return queryInfo.getEstimate();
        }
    }

    @Override
    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        if (sketchConfig.isUseAcrossRows()) {
            return KminUtils.intersectAndScale(getCellsToIntersectAcrossRows(query, numPreds), queryInfo, lastTermInBoundAcrossRows, numPreds);
        } else {
            ArrayList<QueryInfo> queryInfos = new ArrayList<>(depth);
            Sample[][] cellsToIntersect = getCellsToIntersectPerRow(query, numPreds);

            for (int j = 0; j < depth; j++) {
                queryInfos.add(new QueryInfo());
                if (cellsToIntersect[j].length == 1) {
                    // If only one predicate, we can directly use the estimate from the sample
                    queryInfos.get(j).setEstimate(cellsToIntersect[j][0].getN());
                } else {
                    // If multiple predicates, we need to intersect and scale
                    queryInfos.get(j).setEstimate(KminUtils.intersectAndScale(cellsToIntersect[j], queryInfos.get(j), lastTermInBoundPerRow, numPreds));
                }
//                queryInfos.get(j).setEstimate(KminUtils.intersectAndScale(cellsToIntersect[j], queryInfos.get(j), lastTermInBoundPerRow, numPreds));
            }
            // sort queryInfos on estimates
            QueryInfoSorter.sortByEstimateSize(queryInfos);
//            queryInfo = queryInfos.get(queryInfos.size()/2);
            queryInfo = queryInfos.get(0);
            return queryInfo.getEstimate();
        }
    }

    private static class QueryInfoSorter {
        public static void sortByEstimateSize(List<QueryInfo> queryInfos) {
            queryInfos.sort(Comparator.comparingInt(QueryInfo::getEstimate));
        }
    }

    private Sample[] getCellsToIntersectAcrossRows(long[] query, int numPreds) {
        Sample[] cellsToIntersect = new Sample[depth * numPreds];
        int attrWithoutPred = 0;
        for (int i = 0; i < query.length; i++) {
            if (query[i] != -1) { // Find way to not take the -1s into account in query.
                Sample[] temp = attributeSketches[i].query(query[i]);
                if (depth >= 0) {
                    System.arraycopy(temp, 0, cellsToIntersect, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return cellsToIntersect;
    }

    private Sample[] getCellsToIntersectAcrossRows(Record query, int numPreds) {
        Sample[] cellsToIntersect = new Sample[depth * numPreds];
        int attrWithoutPred = 0;
        for (int i = 0; i < query.length(); i++) {
            if (!RecordUtils.flexibleEquals(query.getValue(i),-1)) { // Find way to not take the -1s into account in query.
                Sample[] temp = attributeSketches[i].query(query.getValue(i));
                if (depth >= 0) {
                    System.arraycopy(temp, 0, cellsToIntersect, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return cellsToIntersect;
    }

    private Sample[][] getCellsToIntersectPerRow(long[] query, int numPreds) {
        Sample[][] cellsToIntersect = new Sample[depth][numPreds];
        int numPredsFound = 0;
        for (int i = 0; i < query.length; i++) {
            if (query[i] != -1) {
                Sample[] temp = attributeSketches[i].query(query[i]);
                for (int j = 0; j < depth; j++) {
                    cellsToIntersect[j][numPredsFound] = temp[j];
                }
                numPredsFound++;
            }
        }
        return cellsToIntersect;
    }

    private Sample[][] getCellsToIntersectPerRow(Record query, int numPreds) {
        Sample[][] cellsToIntersect = new Sample[depth][numPreds];
        int numPredsFound = 0;
        for (int i = 0; i < query.length(); i++) {
            if (RecordUtils.flexibleEquals(query.getValue(i),-1)) {
                Sample[] temp = attributeSketches[i].query(query.getValue(i));
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
        return "OmniSketchSampleLater_" + KminType + "_" + accrRows;
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

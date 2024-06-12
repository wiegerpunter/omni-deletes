package omni.deprecated;

import omni.AttributeStats;
import omni.Main;
import omni.Parser;
import omni.Record;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;

public class DatasetStats extends Dataset implements Serializable {


    public DatasetStats(Dataset d) {
        super(d);
    }

    public DatasetStats() {
    }

    public AttributeStats[] computeStats() {
        AttributeStats[] stats = new AttributeStats[Main.numAttributes];

        for (int i = 0; i < Main.numAttributes; i++) {
            // Get String repr of attribute via Parser
            String attrName = Parser.getAttributeName(i);
            stats[i] = statsPerAttribute(i);
        }
        return stats;
    }

    public AttributeStats statsPerAttribute(int attr) {
        AttributeStats attrStats = new AttributeStats(attr);
        //Set<Long> distinctValues = dataset.stream().map(r -> r.getRecord()[attr]).collect(Collectors.toSet());
        //long cardinality = distinctValues.size();
        //System.out.println("Attribute " + Parser.getAttributeName(attr) + " cardinality: " + cardinality);
//        if (Arrays.stream(Parser.getCatAttr()).anyMatch(x -> x == attr)) {
//
//            long cardinality;
//            // Get number of distinct values
//            //Set<Long> distinctValues = dataset.stream().map(r -> r.getRecord()[attr]).collect(Collectors.toSet());
//            //cardinality = distinctValues.size();
//            // Get most frequent value of attribute in records
//            //long mostFrequent = dataset.stream().map(r -> r.getRecord()[attr]).collect(Collectors.groupingBy(e -> e, Collectors.counting())).entrySet().stream().max((e1, e2) -> e1.getValue() > e2.getValue() ? 1 : -1).get().getKey();
//
//            //attrStats.categorical = true;
//            //attrStats.setValues(distinctValues);
//            //attrStats.setCategoricalStats(cardinality, mostFrequent);
//            //System.out.println("Attribute " + Parser.getAttributeName(attr) + " cardinality: " + cardinality);
//
//            // Categorical attribute
//        } else {
//
//            //
//
//            // Sort distinctValues on frequency in dataset
//            // FInd K distinct values with highest frequencyint
//            //int K = 10;
//            //Set<Long> kHighestFreq = distinctValues.stream().sorted((e1, e2) -> (int) (dataset.stream().filter(r -> r.getRecord()[attr] == e2).count() - dataset.stream().filter(r -> r.getRecord()[attr] == e1).count())).limit(K).collect(Collectors.toSet());
//
//            //attrStats.setValues(kHighestFreq);
//            //
//            // Numerical attribute
//            long min = Long.MAX_VALUE;
//            long max = Long.MIN_VALUE;
//            long sum = 0;
//            int validRecords = 0;
//            for (Record r : dataset) {
//                if (r.getRecord()[attr] == 0|| r.getRecord()[attr] == r.phl) {
//                    continue;
//                }
//                if (r.getRecord()[attr] < min) {
//                    min = r.getRecord()[attr];
//                }
//                if (r.getRecord()[attr] > max) {
//                    max = r.getRecord()[attr];
//                }
//                sum += r.getRecord()[attr];
//                validRecords++;
//            }
//            double avg = (double) sum / validRecords;
//            long median = 0;
//            long firstQuantile = 0;
//            long thirdQuantile;
//            Set<Long> values;
//            // Get all distinct values of attribute in values and enter them in attrStats.setValues
//            int[] notToBeAdded;
//            if (Main.datasetName.equals("SNMP")) {
//                 notToBeAdded = new int[]{Parser.attrMap("timestamp"),
//                        Parser.attrMap("sysUpTime"),
//                        Parser.attrMap("ifIndex"),
//                        Parser.attrMap("ifType"),
//                        Parser.attrMap("ifSpeed"),
//                        Parser.attrMap("awcFtBridgeSelf"),
//                        Parser.attrMap("awcFtRepeaterSelf")};
//            } else {
//                notToBeAdded = new int[]{Parser.attrMap("timestamp")};
//            }
            //thirdQuantile = dataset.stream().map(r -> r.getRecord()[attr]).sorted().skip((long) dataset.size()*3/4).findFirst().get();
            //attrStats.setNumericalStats(min, max, avg, computeVariance(attr, avg, validRecords), firstQuantile, median, thirdQuantile);
            //attrStats.categorical = false;
        //}
        return attrStats;
    }

    public double computeVariance(int attr, double avg, int validRecords) {
        double sum = 0;
        for (Record r : dataset) {
            //if (new Long(r.getRecord()[attr]) == null) continue;
            if (r.getRecord()[attr] == r.phl) {
                continue;
            }
            sum += Math.pow(r.getRecord()[attr] - avg, 2);
        }
        return sum / validRecords;
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}

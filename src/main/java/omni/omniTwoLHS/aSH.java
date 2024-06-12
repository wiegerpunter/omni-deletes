package omni.omniTwoLHS;

import omni.AnalysisBaselinesRefactor;
import omni.SynopsisRefactor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class aSH extends SynopsisRefactor{
    // Adaptive Sample and Hold. Sketch for estimating count of elements in a stream.

    HashMap<long[], Double[]> sketch; // Double[] is of form c_i, tau_i, u_i, z_i
    int count;

    int size; // The number of records seen so far.
    int maxSize; // The maximum size of the sketch.

    Random rn;
    Random rn1;
    public aSH(long ram, int numAttributes, int[] parameters, int repetition) {
        setting = "aSH";
        this.size=0;
        this.maxSize = parameters[0] + 1;
        this.parameters = parameters;
        this.ram = ram;
        this.sketch = new HashMap<long[], Double[]>(parameters[0] + 1);
        this.memUsageSynopsis = ram; // TODO: make function.
        this.seed = repetition;
        rn = new Random( 1000L *seed);
        rn1 = new Random( 10002L *seed);
    }

    @Override
    public void add(long[] record) {
        ingest(record, 1);
        count++;
    }

    public void ingest(long[] record, int sign) {
        // check if the record is already in the sketch
//        Long[] recordKey = new Long[record.length];
//        for (int i = 0; i < record.length; i++) {
//            recordKey[i] = record[i];
//        }
        if (sketch.containsKey(record)) {
            // increment the count of the record
            Double[] recordValue = sketch.get(record);
            recordValue[0] += sign;
            if (recordValue[0] <= 0) {
                sketch.remove(record);
                size--;
            } else {
                sketch.put(record, recordValue);
            }
        } else {
            if (sign > 0) {
                // add the record to the sketch
                Double[] recordValue = new Double[4];
                recordValue[0] = (double) sign; // c_i
                recordValue[1] = (double) 0; // tau_i
                recordValue[2] = rn.nextDouble(); // u_i
                recordValue[3] = Math.log(rn1.nextDouble()); // z_i
                // u_i and z_i are not set here, but in EjectOne(S)
                sketch.put(record, recordValue);
                size++;
                if (size >= maxSize) {
                    ejectOne();
                }
            }
        }
    }


    private void ejectOne() {
        double tstar = Double.MAX_VALUE;
        //Set<long[]> keys = sketch.keySet();
        double[] Tis = new double[sketch.size()];
        //double[] us = new double[keys.size()];
        //double[] zs = new double[keys.size()];
        int i = 0;
        Iterator<Double[]> iterator = sketch.values().iterator();//Set().iterator();
        boolean printFirst= true;
        while (iterator.hasNext()) {
            //long[] key = ;
            Double[] sample = iterator.next();//sketch.get(key);

            //us[i] = rn.nextDouble(); // draw from uniform distribution
            //zs[i] = rn1.nextDouble(); // can i draw only once?
//            Tis[i] = Math.max(sample[1] / us[i], sample[0] / (-Math.log(zs[i])));
            Tis[i] = Math.max(sample[1] / sample[2], sample[0] / (-sample[3]));
            tstar = Math.min(tstar, Tis[i]);
            i++;
        }
        // copy of keyset
//        Object[] keys_array = sketch.keySet().toArray();
        printFirst = true;
        i = 0;
        Iterator<long[]> iterator1 = sketch.keySet().iterator();
        while (iterator1.hasNext()) {
            long[] key = iterator1.next();
            if (Tis[i] == tstar) {
                iterator1.remove();
                size--;
            } else {
                Double[] sample = sketch.get(key);
                if (sample[1] <= tstar) {
                    if (tstar * sample[2] > sample[1]) {
                        sketch.get(key)[0] += tstar * sample[3]; // check if cost is actually updated.
                    }
                    sketch.get(key)[1] = tstar;
                }
            }
            i++;
        }
//        for (Object key : keys_array) {
//            if (Tis[i] == tstar) {
//                sketch.remove((long[]) key);
//                size--;
//            } else {
//                if (sketch.get((long[]) key)[1] <= tstar) {
//                    if (tstar * us[i] > sketch.get((long[]) key)[1]) {
//                        sketch.get((long[]) key)[0] += tstar * Math.log(zs[i]); // check if cost is actually updated.
//                    }
//                    sketch.get((long[]) key)[1] = tstar;
//                }
//            }
//            i++;
//        }
    }

    @Override
    public int query(long[] query, int numPreds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
        // go over all records in the reservoir and check if they match the query
        int queryResultCount = 0;
        Set<long[]> keys = sketch.keySet();
        for (long[] key : keys) {
            boolean match = true;
            for (int j = 0; j < query.length; j++) {
                if (query[j] != -1 && query[j] != key[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                queryResultCount+= (int) (sketch.get(key)[0] + sketch.get(key)[1]);
            }
        }
        // scale result by the size of the reservoir
        return (int) (queryResultCount * (Math.max((double) count /size, 1)));
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        sketch = new HashMap<long[], Double[]>(parameters[0] + 1);
        size = 0;
        count = 0;
    }

    @Override
    public void delete(long[] r) {
        ingest(r, -1);
        count--;
    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    public void printParams() {
        System.out.println("Adaptive Sample and Hold with size: " + maxSize);
    }
}

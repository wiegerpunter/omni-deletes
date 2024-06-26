package omni.aSH;

import omni.AnalysisBaselinesRefactor;
import omni.SynopsisRefactor;

import java.util.*;

public class aSH extends SynopsisRefactor{
    // Adaptive Sample and Hold. Sketch for estimating count of elements in a stream.
    int numAttrs;
    HashMap<long[], Double[]> sketch; // Double[] is of form c_i, tau_i, u_i, z_i
    HashMap<long[], Double[]> buffer; // Double[] is of form c_i, tau_i, u_i, z_i
    int count;

    int size; // The number of records seen so far.
    final int maxSize; // The maximum size of the sketch.
    int sketchSize; // The size of the sketch.
//    int bufferSize; // The size of the buffer.

    Random rn;
    Random rn1;
    boolean useBufferInQuery;

    public aSH(long ram, int numAttributes, int[] parameters, int repetition, boolean useBufferInQuery) {
        setting = "aSH";
        if (useBufferInQuery) {
            setting += "useBuffer";
        }
        this.size=0;
        this.maxSize = parameters[0] + parameters[1];
        this.numAttrs = numAttributes;
        this.sketchSize = parameters[0];
//        this.bufferSize = parameters[1];
        this.parameters = parameters;
        this.ram = ram;
        this.sketch = new HashMap<long[], Double[]>(parameters[0] + parameters[1]);
        //this.buffer = new HashMap<long[], Double[]>(parameters[1]);
        //this.memUsageSynopsis = ram; // TODO: make function.
        this.seed = repetition;
        this.useBufferInQuery = useBufferInQuery;
        int firstSeed = 10 +  seed;
        int secondSeed = 10 + seed;
        System.out.println("Seed: " + firstSeed + " secondSeed: " + secondSeed);
        rn = new Random(firstSeed);
        rn1 = new Random(secondSeed);

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
                Double[] recordValue = new Double[5];
                recordValue[0] = (double) sign; // c_i
                recordValue[1] = (double) 0; // tau_i
                do {
                    recordValue[2] = rn.nextDouble();
                } while (recordValue[2] == 0.0);
                do {
                    recordValue[3] = Math.log(rn.nextDouble());
                } while (recordValue[3] == Math.log(0.0));
//                recordValue[2] = rn.nextDouble(); // u_i see page 347, u is rv uniformly distributed in (0, 1].
//                recordValue[3] = Math.log(rn1.nextDouble()); // z_i. z is rv uniformly distributed in (0, 1].
                recordValue[4] = Math.max(recordValue[1] / recordValue[2], recordValue[0] / (-recordValue[3]));
                // u_i and z_i are not set here, but in EjectOne(S)
                sketch.put(record, recordValue);
                size++;
                if (size >= maxSize) {
                    eject();
                }
            }
        }
    }


    private void eject() {
        double tstar;// = Double.MAX_VALUE;
        //Set<long[]> keys = sketch.keySet();
        double[] Tis = new double[sketch.size()];
        //double[] us = new double[keys.size()];
        //double[] zs = new double[keys.size()];
        int i = 0;
        Iterator<Double[]> iterator = sketch.values().iterator();//Set().iterator();
        int numToEject = size - sketchSize;

        TreeSet<Double> bs_minvalues = new TreeSet<>();//Ascending: we want the min values to eject. Comparator.reverseOrder());

        while (iterator.hasNext()) {
            Double[] sample = iterator.next();//sketch.get(key);
            Tis[i] = Math.max(sample[1] / sample[2], sample[0] / (-sample[3]));
            bs_minvalues.add(Tis[i]);
            if (bs_minvalues.size() > numToEject) { // We only want to eject numToEject records with the smallest values. If we have more, we eject the largest ones.
                bs_minvalues.pollLast(); // Eject the largest value.
            }
            i++;
        }
        if (bs_minvalues.size() == numToEject) {
            tstar = bs_minvalues.last(); // The largest value in the set of smallest values.
        } else {
            throw new RuntimeException("Error in ejecting records from aSH");
        }
        i = 0;
        Iterator<long[]> iterator1 = sketch.keySet().iterator();
        while (iterator1.hasNext()) {
            long[] key = iterator1.next();
            if (Tis[i] <= tstar) { // tstar is threshold for ejection.
                iterator1.remove();
                size--;
            } else { // Update the records that are not ejected.
                Double[] sample = sketch.get(key);
                if (sample[1] <= tstar) {
                    if (tstar * sample[2] > sample[1]) {
                        sketch.get(key)[0] += tstar * sample[3];
                    }
                    sketch.get(key)[1] = tstar; // Try if it should be here.
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
        if (!useBufferInQuery && size > sketchSize) {
            eject();
        }

        int queryResultCount = 0;
        Set<long[]> keys = sketch.keySet();
        for (long[] key : keys) {
            boolean match = true;
            for (int j = 0; j < query.length; j++) {
                if (query[j] != -1 && query[j] != key[j + 1]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                queryResultCount+= (int) (sketch.get(key)[0] + sketch.get(key)[1]);
                // sketch.get(key)[0] is c_i and sketch.get(key)[1] is tau_i
            }
        }
        // scale result by the size of the reservoir
        return (int) (queryResultCount); //* (Math.max((double) count /size, 1)));
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
        sketch = new HashMap<long[], Double[]>(parameters[0] + parameters[1]);
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

    public long getMemoryUsage() {
        return (long) size * 32 * (numAttrs + 5); // size * (size of record + size of Double array + Tis array)
    }

    public void printParams() {
        System.out.println("Adaptive Sample and Hold with size: " + maxSize);
    }
}

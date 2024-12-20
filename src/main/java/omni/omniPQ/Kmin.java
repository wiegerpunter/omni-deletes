package omni.omniPQ;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.Formulas;
import omni.Main;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

public class Kmin extends Sample {
    private final double Beta;
    private final int allowedDeletions;
    public boolean exceedsNumberOfDeletes = false;
    //PriorityQueue<Integer> sketch;
//    public TreeSet<Long> sketch;
    public PriorityQueue<Integer> sketch;
    //int sketchSize = 0;
    public int K; // number of lowest values to store
    int KInQuery; // number of lowest values to store
    Random rn = new Random();
    int maxHash;
    boolean supportDeletes;
    int deletesFromSample = 0;
    int b;
    //Hashing.murmur3_32();
    HashFunction xx;

    int hash(long x) {
        return (xx.hashLong(x).asInt() & maxHash);
//        return ((xx.hashLong(x).asInt() % maxHash) + maxHash) % maxHash;
    }

    public Kmin(int maxSize, int b, boolean supportDeletes, double Beta, int seed) {
        this.supportDeletes = supportDeletes;
        this.K = maxSize;
        this.b = b;
        this.seed = seed;
        this.Beta = Beta;

        //TODO: check if deletesFromSample exceeds bound based on Beta and maxSize.
        // In full omnisketch, check how many of the samples exceed the bound.
        if (supportDeletes) {
            this.KInQuery = (int) (maxSize/Beta);
        } else {
            this.KInQuery = maxSize;
        }
        allowedDeletions = maxSize - KInQuery;
//        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.sketch = new PriorityQueue<>(Collections.reverseOrder());
        maxHash = (int) Math.pow(2,b) - 1;
        xx = Hashing.murmur3_32_fixed(seed);
        //System.out.println("Kmin: maxHash = " + maxHash);

    }

    public long curTreeRoot = Long.MAX_VALUE;
    int collissions = 0;
    public void add(int hx) {
        n++;
        if (curSampleSize < K - 1) {
            sketch.add(hx);
//            if (Main.countUniqueSamples) {
//                if (Main.uniqueSamples.containsKey(hx)) {
//                    Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
//                } else {
//                    Main.uniqueSamples.put(hx, 1);
//                }
//                }
            curSampleSize++;
        } else if (curSampleSize == K) {
            curTreeRoot = sketch.peek();
            if (hx < curTreeRoot) { // get tree root
                sketch.poll();
                //sketch.pollFirst();
                if (sketch.add(hx)) {
                    curTreeRoot = sketch.peek();
                } else {
                    curTreeRoot = sketch.peek();
                    collissions++;
                }
            }
        }
            // check if hx is in the sketch already, if so, do nothing
            else {
                // Only proceed if hx is smaller than the current root (curTreeRoot)
                if (hx < curTreeRoot) {
                    // Try to add hx directly, avoid a separate contains() check
                    if (sketch.add(hx)) {  // Add hx to the set, returns false if already present
                        sketch.poll();  // Remove the smallest element (previous curTreeRoot)
                        curTreeRoot = sketch.peek();  // Update curTreeRoot to the new smallest element
                    } else {
                        // Collision of signatures.
                        collissions++;
                    }
                }
            }
    }


    private void remove(long hx) {
        // TODO: For PQ
//        n--;
//        if (sketch.contains(hx)) {
//            Main.kminDeletes++;
//            deletesFromSample++;
//            if (deletesFromSample > allowedDeletions) {
//                exceedsNumberOfDeletes = true;
//            }
//            sketch.remove(hx);
//            curSampleSize--;
//            if (!sketch.isEmpty()) {
//                curTreeRoot = sketch.first();
//            }
//        }
    }

    @Override
    public void ingest(int hx, int sign) {
        if (sign == 1) {
            add(hx);
        } else {
            remove(hx);
        }
    }

    @Override
    public void ingest(long[] vals, int sign) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
//        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.curSampleSize = 0;
    }

    @Override
    public long getMemoryUsage() {
        //return (sketch.size() * (b + 32*3 + 1) + 32);
        return Formulas.ramSingleKmin(sketch.size(), b);
    }

    public PriorityQueue<Integer> getSampleToQuery() {
        if (!supportDeletes) {
            return new PriorityQueue<>(sketch);

            // we can also return a sorted list of the sketch
        } else {
//            int size = KInQuery;
//
//            // Only return the KInQuery lowest elements
//            int sizeSketch = sketch.size();
            return getHalfKmin();
        }
    }

    private PriorityQueue<Integer> getHalfKmin() {
        //TODO: Fix for PQ
        int cnt = 0;
        PriorityQueue<Integer> result = new PriorityQueue<>(Collections.reverseOrder());
        //int startAt = sizeSketch - KInQuery;
//            for (long x: sketch) {
//                if (cnt < startAt) {
//                    cnt++;
//                    continue;
//                } else {
//                    result.add(x);
//                    if (result.size() > size) {
//                        break;
//                    }
//                }
//            }
//        Iterator<Long> x = sketch.descendingIterator();
//
//        while (cnt < KInQuery && x.hasNext()) {
//            long k = x.next();
//            result.add(k);
//            cnt++;
//        }
        return result;
    }

    public void changeK(int newK) {
        this.K = newK;
        this.KInQuery = newK;
    }
}

package omni.omniHouse;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.parameterSetting.Formulas;
import omni.Main;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

public class KminTreeSet extends Sample {
    private final double Beta;
    private final int allowedDeletions;
    public boolean exceedsNumberOfDeletes = false;
    //PriorityQueue<Integer> sketch;
    public TreeSet<Integer> sketch;
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
        return ((xx.hashLong(x).asInt() % maxHash) + maxHash) % maxHash;
    }

    public KminTreeSet(int maxSize, int b, boolean supportDeletes, double Beta, int seed) {
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
        this.sketch = new TreeSet<>(Collections.reverseOrder());
        maxHash = (int) Math.pow(2,b);
        xx = Hashing.murmur3_32_fixed(seed);
        //System.out.println("Kmin: maxHash = " + maxHash);

    }

    public long curTreeRoot = Long.MAX_VALUE;
    int collissions = 0;

    public void changeK(int newK) {
        this.K = newK;
        this.KInQuery = newK;
    }

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
            curTreeRoot = sketch.first();
            if (hx < curTreeRoot) { // get tree root
                if (Main.countUniqueSamples) {
                    if (Main.uniqueSamples.containsKey(curTreeRoot)) {
                        Main.uniqueSamples.put(curTreeRoot, Main.uniqueSamples.get(curTreeRoot) - 1);
                        if (Main.uniqueSamples.get(curTreeRoot) == 0) {
                            Main.uniqueSamples.remove(curTreeRoot);
                        }
                    } else if (curTreeRoot != Long.MAX_VALUE) {
                        throw new RuntimeException("Error in Kmin: curTreeRoot not in uniqueSamples");
                    }
                }
                sketch.pollFirst();
                if (sketch.add(hx)) {
                    curTreeRoot = sketch.first();
                } else {
                    collissions++;
                }
//                if (Main.countUniqueSamples) {
//                    if (Main.uniqueSamples.containsKey(hx)) {
//                        Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
//                    } else {
//                        Main.uniqueSamples.put(hx, 1);
//                    }
//                }
            }
        }
            // check if hx is in the sketch already, if so, do nothing
            else {
                // Only proceed if hx is smaller than the current root (curTreeRoot)
                if (hx < curTreeRoot) {
                    // Try to add hx directly, avoid a separate contains() check
                    if (sketch.add(hx)) {  // Add hx to the set, returns false if already present
                        sketch.pollFirst();  // Remove the smallest element (previous curTreeRoot)
                        curTreeRoot = sketch.first();  // Update curTreeRoot to the new smallest element
                    } else {
                        // Collision of signatures.
                        collissions++;
                    }
                }
            }
//else {
//            if (hx < curTreeRoot) { // get tree root
//                if (Main.countUniqueSamples) {
//                    if (Main.uniqueSamples.containsKey(curTreeRoot)) {
//                        Main.uniqueSamples.put(curTreeRoot, Main.uniqueSamples.get(curTreeRoot) - 1);
//                        if (Main.uniqueSamples.get(curTreeRoot) == 0) {
//                            Main.uniqueSamples.remove(curTreeRoot);
//                        }
//                    } else if (curTreeRoot != Long.MAX_VALUE){
//                        throw new RuntimeException("Error in Kmin: curTreeRoot not in uniqueSamples");
//                    }
//                }
//                if (sketch.contains(hx)) {
//                    return;
//                }
//               sketch.pollFirst();
//               sketch.add(hx);
//                if (Main.countUniqueSamples) {
//                    if (Main.uniqueSamples.containsKey(hx)) {
//                        Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
//                    } else {
//                        Main.uniqueSamples.put(hx, 1);
//                    }
//                }
//               curTreeRoot = sketch.first();
//            }
    }


    private void remove(long hx) {
        n--;
        if (sketch.contains(hx)) {
            Main.kminDeletes++;
            deletesFromSample++;
            if (deletesFromSample > allowedDeletions) {
                exceedsNumberOfDeletes = true;
            }
            sketch.remove(hx);
            curSampleSize--;
            if (!sketch.isEmpty()) {
                curTreeRoot = sketch.first();
            }
        }
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
        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.curSampleSize = 0;
    }

    @Override
    public long getMemoryUsage() {
        //return (sketch.size() * (b + 32*3 + 1) + 32);
        return Formulas.ramSingleKmin(sketch.size(), b);
    }

    public TreeSet<Integer> getSampleToQuery() {
        if (!supportDeletes) {
            return sketch;
        } else {
//            int size = KInQuery;
//
//            // Only return the KInQuery lowest elements
//            int sizeSketch = sketch.size();
            return getHalfKmin();
        }
    }

    private TreeSet<Integer> getHalfKmin() {
        int cnt = 0;
        TreeSet<Integer> result = new TreeSet<>(Collections.reverseOrder());
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
        Iterator<Integer> x = sketch.descendingIterator();

        while (cnt < KInQuery && x.hasNext()) {
            int k = x.next();
            result.add(k);
            cnt++;
        }
        return result;
    }
}

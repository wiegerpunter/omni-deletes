package omni.omniTwoLHS;

import omni.Main;

import java.util.*;
import java.util.stream.Collectors;

public class Kmin extends Sample {
    private final double Beta;
    private final int allowedDeletions;
    public boolean exceedsNumberOfDeletes = false;
    //PriorityQueue<Integer> sketch;
    public TreeSet<Long> sketch;
    //int sketchSize = 0;
    int K; // number of lowest values to store
    int KInQuery; // number of lowest values to store
    Random rn = new Random();
    long maxHash;
    boolean supportDeletes;
    int deletesFromSample = 0;
    int b;
    long hash(long x) {
        long k;
        rn.setSeed(x + seed);
        // Hash function hashing x to [0, 1]^b with base = log(2m^2/delta)

        k = rn.nextLong(maxHash);

        return k;
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
        this.sketch = new TreeSet<>(Collections.reverseOrder());
        maxHash = Math.min((int) Math.pow(2, b), Integer.MAX_VALUE);

    }

    long curTreeRoot = Long.MAX_VALUE;
    public void add(long hx) {
        n++;
        if (curSampleSize < K - 1) {
            sketch.add(hx);
            if (Main.countUniqueSamples) {
                if (Main.uniqueSamples.containsKey(hx)) {
                    Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
                } else {
                    Main.uniqueSamples.put(hx, 1);
                }
                }
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
                sketch.add(hx);
                if (Main.countUniqueSamples) {
                    if (Main.uniqueSamples.containsKey(hx)) {
                        Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
                    } else {
                        Main.uniqueSamples.put(hx, 1);
                    }
                }
                curTreeRoot = sketch.first();
            }
        } else {
            if (hx < curTreeRoot) { // get tree root
                if (Main.countUniqueSamples) {
                    if (Main.uniqueSamples.containsKey(curTreeRoot)) {
                        Main.uniqueSamples.put(curTreeRoot, Main.uniqueSamples.get(curTreeRoot) - 1);
                        if (Main.uniqueSamples.get(curTreeRoot) == 0) {
                            Main.uniqueSamples.remove(curTreeRoot);
                        }
                    } else if (curTreeRoot != Long.MAX_VALUE){
                        throw new RuntimeException("Error in Kmin: curTreeRoot not in uniqueSamples");
                    }
                }
               sketch.pollFirst();
               sketch.add(hx);
                if (Main.countUniqueSamples) {
                    if (Main.uniqueSamples.containsKey(hx)) {
                        Main.uniqueSamples.put(hx, Main.uniqueSamples.get(hx) + 1);
                    } else {
                        Main.uniqueSamples.put(hx, 1);
                    }
                }
               curTreeRoot = sketch.first();
            }
        }
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
    public void ingest(long hx, int sign) {
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
        return (sketch.size() * (b + 32*3 + 1) + 32);
    }

    public TreeSet<Long> getSampleToQuery() {
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

    private TreeSet<Long> getHalfKmin() {
        int cnt = 0;
        TreeSet<Long> result = new TreeSet<>(Collections.reverseOrder());
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
        Iterator<Long> x = sketch.descendingIterator();

        while (cnt < KInQuery && x.hasNext()) {
            long k = x.next();
            result.add(k);
            cnt++;
        }
        return result;
    }
}

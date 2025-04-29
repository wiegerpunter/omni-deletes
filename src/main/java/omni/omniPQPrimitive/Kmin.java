package omni.omniPQPrimitive;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.parameterSetting.Formulas;

import omni.PriorityQueue.PriorityQueue;
import java.util.Random;

public class Kmin extends Sample {
    private final double Beta;
    private final int allowedDeletions;
    public boolean exceedsNumberOfDeletes = false;
    //PriorityQueue<Integer> sketch;
//    public TreeSet<Long> sketch;
    public PriorityQueue sketch;
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
    int curb;
    int nextbVal;
    private final boolean dynamicResizing;

    int hash(long x) {
        int hash = (xx.hashLong(x).asInt() % maxHash);
        if (hash < 0) {
            hash = hash + maxHash;
        }
        return hash;
    }

    public Kmin(int maxSize, int b, boolean supportDeletes, double Beta, boolean dynamicResizing, int seed) {
        this.supportDeletes = supportDeletes;
        this.K = maxSize;
        this.b = b;
        this.seed = seed;
        this.Beta = Beta;
        this.dynamicResizing = dynamicResizing;
        this.curb = b;
        this.nextbVal = (int) Math.pow(2, curb - 1);

        if (supportDeletes) {
            this.KInQuery = (int) (maxSize/Beta);
        } else {
            this.KInQuery = maxSize;
        }
        allowedDeletions = maxSize - KInQuery;
//        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.sketch = new PriorityQueue(K);
        maxHash = (int) Math.pow(2,b) - 1;
        xx = Hashing.murmur3_32_fixed(seed);
        //System.out.println("Kmin: maxHash = " + maxHash);

    }

    public Kmin(Kmin kmin) {
        this.supportDeletes = kmin.supportDeletes;
        this.K = kmin.K;
        this.b = kmin.b;
        this.seed = kmin.seed;
        this.Beta = kmin.Beta;
        this.dynamicResizing = kmin.dynamicResizing;
        this.curb = kmin.curb;
        this.nextbVal = kmin.nextbVal;
        this.KInQuery = kmin.KInQuery;
        this.allowedDeletions = kmin.allowedDeletions;
        this.sketch = PriorityQueue.getCopy(kmin.sketch);
        this.maxHash = kmin.maxHash;
        this.xx = Hashing.murmur3_32_fixed(seed);
        // copy everything
        this.n = kmin.n;
        this.curSampleSize = kmin.curSampleSize;
        this.curTreeRoot = kmin.curTreeRoot;
        this.collissions = kmin.collissions;
        this.deletesFromSample = kmin.deletesFromSample;
        this.exceedsNumberOfDeletes = kmin.exceedsNumberOfDeletes;
    }

    public int curTreeRoot = Integer.MAX_VALUE;
    int collissions = 0;
//    public void add(int hx) {
//        n++;
//        if (curSampleSize < K - 1) {
//            sketch.add(hx);
//            curSampleSize++;
//        } else if (curSampleSize == K) {
//            curTreeRoot = sketch.peek();
//            if (hx < curTreeRoot) { // get tree root
//                sketch.poll();
//                sketch.add(hx);
//                //sketch.pollFirst();
//                curTreeRoot = sketch.peek();
//            }
//        }
//            // check if hx is in the sketch already, if so, do nothing
//            else {
//                // Only proceed if hx is smaller than the current root (curTreeRoot)
//                if (hx < curTreeRoot) {
//                    // Try to add hx directly, avoid a separate contains() check
//                    sketch.poll();
//                    sketch.add(hx);
//                    curTreeRoot = sketch.peek();  // Update curTreeRoot to the new smallest element
//                }
//            }
//    }
//public void add(int hx) {
//    n++;
//
//    if (curSampleSize < K) {
//        // Add elements until we reach K items in the queue
//        sketch.add(hx);
//        curSampleSize++;
//        if (curSampleSize == K) {
//            // Set the current root as the largest of the K smallest elements
//            curTreeRoot = sketch.peek();
//        }
//    } else {
//        // We already have K elements, so check if `hx` is smaller than the largest of the K smallest elements
//        if (hx < curTreeRoot) {
//            sketch.poll(); // Remove the largest of the K smallest elements
//            sketch.add(hx); // Add the new element
//            curTreeRoot = sketch.peek(); // Update curTreeRoot to the new largest of the K smallest elements
//        }
//    }
//}

    private void tryInsert(int hx) {
        if (hx < curTreeRoot) { // get tree root
            sketch.poll();
            sketch.add(hx);
            curTreeRoot = sketch.peek();

            if (this.dynamicResizing) {
                // Check if we can reduce the number of bits needed to store the signature
                if (curTreeRoot < nextbVal) {
                    curb = curb - 1;
                    nextbVal = (int) Math.pow(2, curb - 1);
                    // Now we can reduce the number of bits needed to store signature to curb.
                    // This means that we can double the number of signatures we can store in the same space.
                    // First, we will do an empirical check to see how much it will help.
                    K = K * (curb + 1) / (curb);
                    PriorityQueue newSketch = new PriorityQueue(K);
                    while (!sketch.isEmpty()) {
                        newSketch.add(sketch.poll());
                    }
//                    sketch = new PriorityQueue(K, sketch);
                    sketch = newSketch;
                }
            }
        }
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
            curTreeRoot = sketch.peek();
            tryInsert(hx);
        }
        // check if hx is in the sketch already, if so, do nothing
        else {
            tryInsert(hx);
            // Only proceed if hx is smaller than the current root (curTreeRoot)
//            if (hx < curTreeRoot) {
//                // Try to add hx directly, avoid a separate contains() check
//                if (sketch.add(hx)) {  // Add hx to the set, returns false if already present
//                    sketch.poll();  // Remove the smallest element (previous curTreeRoot)
//                    curTreeRoot = sketch.peek();  // Update curTreeRoot to the new smallest element
//                } else {
//                    // Collision of signatures.
//                    collissions++;
//                }
//            }
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
        if (dynamicResizing) {
            return Formulas.ramSingleKmin(sketch.size + 1, curb);
        }
        return Formulas.ramSingleKmin(sketch.size + 1, b);
    }

    public PriorityQueue getSampleToQuery() {
        if (!supportDeletes) {
            // return completely sorted sketch

            return PriorityQueue.getCopy(sketch);

            // we can also return a sorted list of the sketch
        } else {
//            int size = KInQuery;
//
//            // Only return the KInQuery lowest elements
//            int sizeSketch = sketch.size();
            return getHalfKmin();
        }
    }

    private PriorityQueue getHalfKmin() {
        //TODO: Fix for PQ
        int cnt = 0;
        PriorityQueue result = new PriorityQueue(K);
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

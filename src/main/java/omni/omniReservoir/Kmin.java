package omni.omniReservoir;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
//import omni.PriorityQueue.PriorityQueue;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntComparators;

public class Kmin {
    public boolean exceedsNumberOfDeletes = false;

    public IntHeapPriorityQueue[] sketch;
    public int[] numInserts;

    public int K; // number of lowest values to store
    int KInQuery; // number of lowest values to store

    int maxHash;
    boolean supportDeletes;
    int b;
    int width;

    HashFunction xx;

    int curSampleSize;

    int hash(long x) {
        int hash = (xx.hashLong(x).asInt() % maxHash);
        if (hash < 0) {
            hash = hash + maxHash;
        }
        return hash;
    }

    public Kmin(int maxSize, int w, int b, boolean supportDeletes, double Beta, int seed) {
        this.supportDeletes = supportDeletes;
        this.K = maxSize;
        this.width = w;
        this.b = b;

        if (supportDeletes) {
            this.KInQuery = (int) (maxSize/Beta);
        } else {
            this.KInQuery = maxSize;
        }

        this.sketch = new IntHeapPriorityQueue[width];
        this.numInserts = new int[width];
        for (int i = 0; i < width; i++) {
            this.sketch[i] = new IntHeapPriorityQueue(IntComparators.oppositeComparator(IntComparators.NATURAL_COMPARATOR));
        }
        maxHash = (int) Math.pow(2,b) - 1;
        xx = Hashing.murmur3_32_fixed(seed);

    }

    public int[] curTreeRoot = {Integer.MAX_VALUE, 0};
    int collisions = 0;
//

    private int[] getTreeRoot() {
        int[] root = {0, 0};
        for (int i = 0; i < width; i++) {
            if (sketch[i].isEmpty()) {
                continue;
            }
            if (root[0] < sketch[i].firstInt()) { // Found new max
                root[0] = sketch[i].firstInt();
                root[1] = i;
            }
        }
        return root;
    }

    private void tryInsert(int hx, int attrHash) {
        if (hx < curTreeRoot[0]) { // get tree root
            sketch[curTreeRoot[1]].dequeueInt();
            sketch[curTreeRoot[1]].trim();
            sketch[attrHash].enqueue(hx);
            curTreeRoot = getTreeRoot();
        }
    }
    public void add(int hx, int attrHash) {
        numInserts[attrHash]++;
        if (curSampleSize <= K - 1) {
            sketch[attrHash].enqueue(hx);

            curSampleSize++;
            if (curSampleSize == K) {
                curTreeRoot = getTreeRoot();
                //tryInsert(hx, attrHash);
            }
        }
        else {
            tryInsert(hx, attrHash);
        }
    }



    private void remove(long hx, int attrHash) {
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


    public void ingest(int hx, int attrHash, int sign) {
        if (sign == 1) {
            add(hx, attrHash);
        } else {
            remove(hx, attrHash);
        }
    }

    public void ingest(long[] vals, int sign) {
        throw new UnsupportedOperationException();
    }

    public void reset() {
//        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.curSampleSize = 0;
    }

    public long getMemoryUsage() {
        //return (sketch.size() * (b + 32*3 + 1) + 32);
        long memory = 0;
        for (int i = 0; i < width; i++) {
            memory+= (long) sketch[i].size() * b;
        }
        return (long) memory + width * 32L;
        //return Formulas.ramSingleKmin(sketch.size() + 1, b);
    }


    public long getMemoryUsagePQ() {
        //return (sketch.size() * (b + 32*3 + 1) + 32);
        long memory = 0;
        for (int i = 0; i < width; i++) {
            memory+= (long) sketch[i].size()* b;
        }
        return (long) memory + width * 32L;
        //return Formulas.ramSingleKmin(sketch.size() + 1, b);
    }

    public IntHeapPriorityQueue getSampleToQuery(int attrHash) {
        if (!supportDeletes) {
            // return completely sorted sketch
            IntHeapPriorityQueue copy = new IntHeapPriorityQueue(sketch[attrHash].size(), IntComparators.oppositeComparator(IntComparators.NATURAL_COMPARATOR));
            IntHeapPriorityQueue old = new IntHeapPriorityQueue(sketch[attrHash].size(), IntComparators.oppositeComparator(IntComparators.NATURAL_COMPARATOR));
            int size = sketch[attrHash].size();
            for (int i = 0; i <size;i++) {
                int dequeued = sketch[attrHash].dequeueInt();
                copy.enqueue(dequeued);
                old.enqueue(dequeued);
            }
            sketch[attrHash] = old;
            return copy; // TODO: make sure getSampleToQuery is based on attrHAsh

            // we can also return a sorted list of the sketch
        } else {
//            int size = KInQuery;
//
//            // Only return the KInQuery lowest elements
//            int sizeSketch = sketch.size();
            return getHalfKmin();
        }
    }

    private IntHeapPriorityQueue getHalfKmin() {
        //TODO: Fix for PQ
        int cnt = 0;
        IntHeapPriorityQueue result = new IntHeapPriorityQueue(K);
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

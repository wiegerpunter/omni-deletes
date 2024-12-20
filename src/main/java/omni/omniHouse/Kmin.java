package omni.omniHouse;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
//
import java.util.Comparator;
import java.util.PriorityQueue;
//import it.unimi.dsi.fastutil.ints.IntComparators;
//import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;

public class Kmin {
    public boolean exceedsNumberOfDeletes = false;

    public PriorityQueue<Integer>[] sketch;
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

        this.sketch = new PriorityQueue[width];
        this.numInserts = new int[width];
        for (int i = 0; i < width; i++) {
            this.sketch[i] = new  PriorityQueue<Integer>(maxSize/w, Comparator.reverseOrder());
        }
        maxHash = (int) Math.pow(2,b) - 1;
        xx = Hashing.murmur3_32_fixed(seed);

    }

//    public int[] curTreeRoot = {Integer.MAX_VALUE, 0};
    public int curTreeRootValue = 0;
    public int curTreeRootColumn = 0;
    int collisions = 0;
//

    private void getTreeRoot() {
        for (int i = 0; i < width; i++) {
            if (!sketch[i].isEmpty()) {
                if (curTreeRootValue < sketch[i].peek()) { // Found new max
                    curTreeRootValue = sketch[i].peek();
                    curTreeRootColumn = i;
                }
            }
        }
    }
    private void addIntInPQ(int attrHash, Integer hxi) {
        sketch[attrHash].add(hxi);
    }
    private void addInPQ(int hx, int attrHash) {
        Integer hxi = hx;
        addIntInPQ(attrHash, hxi);

    }

    private void pollPQ() {
        sketch[curTreeRootColumn].poll();
    }

    private void tryInsert(int hx, int attrHash) {
        // get tree root

        pollPQ();
        addInPQ(hx, attrHash);
        getTreeRoot();
    }

    public void add(int hx, int attrHash) {
        numInserts[attrHash]++;
        if (curSampleSize <= K - 1) {
            sketch[attrHash].add(hx);

            curSampleSize++;
            if (curSampleSize == K) {
                getTreeRoot();
                //tryInsert(hx, attrHash);
            }
        }
        else {
            if (hx < curTreeRootValue) {
                tryInsert(hx, attrHash);
            }
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

    public PriorityQueue getSampleToQuery(int attrHash) {
        if (!supportDeletes) {
            // return deep copy of sketch
            return new PriorityQueue(sketch[attrHash]);

            // we can also return a sorted list of the sketch
        } else {
         throw new UnsupportedOperationException();
        }
    }

    public void changeK(int newK) {
        this.K = newK;
        this.KInQuery = newK;
    }
}

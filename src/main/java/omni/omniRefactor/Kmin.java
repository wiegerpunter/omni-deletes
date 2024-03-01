package omni.omniRefactor;

import omni.Main;

import java.util.Collections;
import java.util.Random;
import java.util.TreeSet;

public class Kmin extends Sample {
    //PriorityQueue<Integer> sketch;
    public TreeSet<Long> sketch;
    //int sketchSize = 0;
    int K; // number of lowest values to store
    //int n = 0;
    //int b;
    int simax = Integer.MIN_VALUE;

    int seed;
    Random rn = new Random();
    long maxHash;
    long hash(long x) {
        long k;
        rn.setSeed(x + Main.repetition);
        // Hash function hashing x to [0, 1]^b with base = log(2m^2/delta)

        k = rn.nextLong(maxHash);

        return k;
    }

    /* public static int hash(int input, int b) {
        int hash = input ^ random.nextInt(); // XOR the input with a random number
        hash = hash % (1 << b); // Modulo operation to limit the hash to b bits
        return hash;
    }*/
    public Kmin() {
        this.K = Main.maxSize;
        //this.sketch = new PriorityQueue<>(Main.maxSize, Collections.reverseOrder());
        this.sketch = new TreeSet<>(Collections.reverseOrder());

        this.delta = Main.delta /2;
        maxHash = Math.min((int) Math.pow(2, Main.b), Integer.MAX_VALUE);
        //Main.b = (int) Math.ceil(Math.log(4*Math.pow(K,(double) 5/2)/this.delta));
    }
    public Kmin(double delta, int maxSize, int b) {
        this.K = maxSize;
        this.sketch = new TreeSet<>(Collections.reverseOrder());
        maxHash = Math.min((int) Math.pow(2, b), Integer.MAX_VALUE);
        this.delta = delta;
    }

    public Kmin(Kmin other) {
        this.K = other.K;
        //this.sketch = new PriorityQueue<>(other.sketch);
        this.sketch = new TreeSet<>(other.sketch);
        this.curSampleSize = other.curSampleSize;
        this.delta = other.delta;
        this.maxHash = other.maxHash;
    }
    @Override
    public void add(int id) {
//        n++;
//        int hx = hash(id);
//        if (curSampleSize < K) {
//            sketch.add(hx);
//            curSampleSize++;
//        } else {
//            if (hx < curTreeRoot) { // get tree root
//                sketch.pollFirst();
//                sketch.add(hx);
//                curTreeRoot = sketch.first();
//            }
//        }
    }

    long curTreeRoot = Long.MAX_VALUE;
    public void add(long hx) {
        n++;
        if (curSampleSize < K) {
            sketch.add(hx);
            curSampleSize++;
        } else {
            if (hx < curTreeRoot) { // get tree root
                sketch.pollFirst();
                sketch.add(hx);
                curTreeRoot = sketch.first();
            }
        }
    }

    @Override
    public Kmin intersect(Sample sample) {
        //Intersect with another KminBaseline sketch
        Kmin other = (Kmin) sample;
        this.sketch.retainAll(other.sketch);
        this.curSampleSize = this.sketch.size();
        return this;
    }

    @Override
    public Sample union(Sample sample) {
        // Union with another KminBaseline sketch
        Kmin other = (Kmin) sample;
        Kmin result = new Kmin();
        result.K = this.K;
        result.delta = this.delta;
        result.curSampleSize = 0;
        //result.sketch = new PriorityQueue<>(this.sketch);
        result.sketch = new TreeSet<>(this.sketch);
        result.sketch.addAll(other.sketch);
        result.curSampleSize = result.sketch.size();
        while (result.curSampleSize > K) {
            result.sketch.pollFirst();
            result.curSampleSize--;
        }
        return result;
    }

    @Override
    public void reset() {
        //this.sketch = new PriorityQueue<>(Main.maxSize, Collections.reverseOrder());
        this.sketch = new TreeSet<>(Collections.reverseOrder());
        this.curSampleSize = 0;
    }

    @Override
    public long getMemoryUsage() {
        return (sketch.size() * (Main.b + 32*3 + 1) + 32);
    }
}

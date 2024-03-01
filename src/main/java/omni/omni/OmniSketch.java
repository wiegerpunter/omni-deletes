package omni.omni;
import omni.*;
import omni.Record;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class OmniSketch extends Synopsis {
    CountMin[] CMSketches = new CountMin[Main.numAttributes];
    CountMinDyad[][] CMSketchesRange = new CountMinDyad[Main.numAttributes][Main.dyadicRangeBits + 1];
    //ArrayList<DieHash> dieHashFunctions = new ArrayList<>();
    boolean[] hasPredicate;

    public OmniSketch(boolean[] hasPredicates) {
        super();
        setting = "OmniSketch";
        System.out.println("Epsilon: " + Main.eps);
        System.out.println("Delta: " + Main.delta);
        System.out.println("Depth: " + Main.depth);
        System.out.println("Width: " + Main.width);
        System.out.println("Max Sample Size: " + Main.maxSize);
        System.out.println("b: " + Main.b);
        this.parameters = new int[]{Main.depth, Main.width, Main.maxSize, Main.b};
        this.hasPredicate = ArrayUtils.clone(hasPredicates);
        this.maxBits = new int[Main.numAttributes];
        if (!Main.rangeQueries) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicates[i]) {
                    CMSketches[i] = new CountMin(i);
                } else {
                    CMSketches[i] = null;
                }
                //CMSketches.add(new CountMin(dieHashFunctions, i));
            }
        } else {
            // Make dyadic intervals per attribute sketch
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicates[i]) {
                    for (int j = Main.dyadicRangeBits; j >-1; j--) {
                        CMSketchesRange[i][Main.dyadicRangeBits - j] = new CountMinDyad(i, j);
                    }
                } else {
                    CMSketches[i] = null;
                }
            }
        }
    }

    public OmniSketch(boolean[] hasPredicates, long ram) {
        this.setting = "OmniSketch";
        this.ram = ram;
        System.out.println("Depth: " + Main.depth);
        System.out.println("Width: " + Main.width);
        System.out.println("Max Sample Size: " + Main.maxSize);
        System.out.println("b: " + Main.b);
        this.parameters = new int[]{Main.depth, Main.width, Main.maxSize, Main.b};
        this.hasPredicate = ArrayUtils.clone(hasPredicates);
        this.maxBits = new int[Main.numAttributes];

        if (!Main.rangeQueries) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicates[i]) {
                    CMSketches[i] = new CountMin(i);
                } else {
                    CMSketches[i] = null;
                }
                //CMSketches.add(new CountMin(dieHashFunctions, i));
            }
        } else {
            // Make dyadic intervals per attribute sketch
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicates[i]) {
                    for (int j = Main.dyadicRangeBits; j >-1; j--) {
                        CMSketchesRange[i][Main.dyadicRangeBits - j] = new CountMinDyad(i, j);
                    }
                } else {
                    CMSketches[i] = null;
                }
            }
        }
    }

    public OmniSketch() {
        for (int i = 0; i < Main.numAttributes; i++) {
            CMSketches[i] = new CountMin(i);
        }
    }

//    public void initDieHashes() {
//        for (int i = 0; i < Main.depth; i++) {
//            dieHashFunctions.add(new DieHash(Main.maxLevel, 1));
//        }
//    }

    Kmin kminTmp = new Kmin();
    public void add(Record record) {
        int id = record.getId();
        long hx = kminTmp.hash(id);
        //long start = System.currentTimeMillis();

        long[] recordArray = record.getRecord();

        if (!Main.rangeQueries) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicate[i]) {
                    CMSketches[i].add(id, recordArray[i], hx);
                }
            }
        } else {
            // Compute all dyadic ranges it belongs to.
            // Insert in all those ranges.
            for (int i = 0; i < Main.numAttributes; i++) {
                if (hasPredicate[i]) {
                    long[][] ranges = wrapperInitLogRanges(recordArray[i]);
                    for (int j = 0; j < ranges[2].length; j++) {
                        CMSketchesRange[i][j].add(ranges[1][j], ranges[2][j], hx);
                    }
                }
            }
        }
    }

    private long[][] wrapperInitLogRanges(long l) {
        //l += (long) Math.pow(2, Main.dyadicRangeBits - 1); // shift to positive
        if (l < 0) {
            System.out.println("Error: l < 0");
            System.exit(1);
        }
        long[][] ranges = getLogRanges(l + 1);
        for (int i = 0; i < ranges[2].length; i++) {
            ranges[1][i] = ranges[1][i] - 1L ;//- (long) Math.pow(2, Main.dyadicRangeBits - 1);
            ranges[2][i] = ranges[2][i] - 1L ;//- (long) Math.pow(2, Main.dyadicRangeBits - 1);
        }
        return ranges;
    }

    public static long[][] getLogRanges(long inputKey) {
        long[] coeff      = new long[Main.dyadicRangeBits];
        long[] lowerBound = new long[Main.dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)
        long[] upperBound = new long[Main.dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)

        long halfPoint = (long) Math.pow(2,Main.dyadicRangeBits -1);
        if (inputKey < halfPoint) {
            coeff[0]=0;
            lowerBound[0] = 1; // inclusive, starting from 1
            upperBound[0] = halfPoint; // inclusive
        } else {
            coeff[0]=1;
            lowerBound[0] = halfPoint; //inclusive, starting from 1
            upperBound[0] = halfPoint*2; // inclusive
        }

        long pow = halfPoint;
        for (int i = 1; i<Main.dyadicRangeBits -1; i++) {
            long prevCoeff = coeff[i-1];
            long newCoeffLower = prevCoeff*2;
            pow/=2;
            if ((newCoeffLower + 1) *pow < inputKey) {
                newCoeffLower++;
                coeff[i] = newCoeffLower;
            } else {
                coeff[i] = newCoeffLower;
            }
            lowerBound[i] = coeff[i]*pow+1;
            upperBound[i] = (coeff[i]+1)*pow;
        }
        lowerBound[Main.dyadicRangeBits -1] = inputKey;
        upperBound[Main.dyadicRangeBits -1] = inputKey;
        coeff[Main.dyadicRangeBits -1] = inputKey;

        long[][] result = new long[3][];
        result[0] = coeff;
        result[1] = lowerBound;
        result[2] = upperBound;
        return result;
    }

    int minCMRow = 0;

    //int[] test_ids = {1215, 1216, 1217, 2452, 2453, 6531, 6532, 7767};
    public int withinConstraint= 0;
    public int outsideConstraint = 0;
    public int query(Query q) {
        if (Main.LTO) {
            return queryLeaveTwoOut(q);
        } else if (Main.LOO) {
            return queryLeaveOneOut(q);
        } else {
            return queryAllRows(q);
        }
    }
    public int queryAllRows(Query q) {
        double S_cap = 0;
        int n_max = 0;
        Sample[] samples = new Kmin[q.predAttrs.size() * Main.depth];
        if (Main.useDS) {
           throw new RuntimeException("Not implemented");
        } else {
          for (int i = 0; i < q.predAttrs.size(); i++) {
              Sample[] temp = CMSketches[q.predAttrs.get(i)].query(q.getRecord()[q.predAttrs.get(i)]);
              if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
          }
          n_max = getNmax((Kmin[]) samples);
          S_cap = getAltEstKMV((Kmin[]) samples);;
        }
        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(Main.maxSize))
                / Main.delta)/(Main.eps * Main.eps);

        q.case2Estimate = Math.ceil(S_cap * n_max / Main.maxSize);
        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                Math.sqrt(Main.maxSize)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
        if (Main.maxSize >= BConstraint) {
            q.ratioCondition = true;
        }
        q.intersectSize = S_cap;
        if (S_cap < constraint) {
            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                    Math.sqrt(Main.maxSize)) / Main.delta)/(Main.maxSize * Main.eps * Main.eps)));
        } else {
            q.thrm33Case2 = true;
            return (int) Math.ceil(S_cap * n_max / Main.maxSize);
        }
    }

    public int queryLeaveOneOut(Query q) {
        double S_cap = 0;
        int n_max = 0;
        Sample[] samples = new Kmin[q.predAttrs.size() * (Main.depth - 1)];
        if (Main.useDS) {
            throw new RuntimeException("Not implemented");
        } else {
            for (int i = 0; i < q.predAttrs.size(); i++) {

                Sample[] temp = CMSketches[q.predAttrs.get(i)].query(q.getRecord()[q.predAttrs.get(i)]);
                // get the sample with highest n
                int maxN = 0;
                int maxNIndex = 0;
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j].n > maxN) {
                        maxN = temp[j].n;
                        maxNIndex = j;
                    }
                }
                // Copy every sample except the one with highest n
                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 1), maxNIndex);
                if (Main.depth >= 0) System.arraycopy(temp, maxNIndex + 1, samples, i * (Main.depth - 1) + maxNIndex,
                        Main.depth - maxNIndex - 1);

                //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
            }
            // At every attribute, leave one out that has highest n
            n_max = getNmax((Kmin[]) samples);
            S_cap = getAltEstKMV((Kmin[]) samples);;
        }
        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(Main.maxSize))
                / Main.delta)/(Main.eps * Main.eps);

        q.case2Estimate = Math.ceil(S_cap * n_max / Main.maxSize);
        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                Math.sqrt(Main.maxSize)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
        if (Main.maxSize >= BConstraint) {
            q.ratioCondition = true;
        }
        q.intersectSize = S_cap;
        if (S_cap < constraint) {
            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                    Math.sqrt(Main.maxSize)) / Main.delta)/(Main.maxSize * Main.eps * Main.eps)));
        } else {
            q.thrm33Case2 = true;
            return (int) Math.ceil(S_cap * n_max / Main.maxSize);
        }

    }
    public int queryLeaveTwoOut(Query q) {
        double S_cap = 0;
        int n_max = 0;
        Sample[] samples = new Kmin[q.predAttrs.size() * (Main.depth - 2)];
        if (Main.useDS) {
            throw new RuntimeException("Not implemented");
        } else {
            for (int i = 0; i < q.predAttrs.size(); i++) {

                Sample[] temp = CMSketches[q.predAttrs.get(i)].query(q.getRecord()[q.predAttrs.get(i)]);
                // get the sample with highest n
                int maxN = 0;
                int maxNIndex = 0;
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j].n > maxN) {
                        maxN = temp[j].n;
                        maxNIndex = j;
                    }
                }
                // find the second highest n
                int secondMaxN = 0;
                int secondMaxNIndex = 0;
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j].n > secondMaxN && j != maxNIndex) {
                        secondMaxN = temp[j].n;
                        secondMaxNIndex = j;
                    }
                }
                // Copy every sample except the one with highest n or second highest n
                int minIndex = Math.min(maxNIndex, secondMaxNIndex);
                int maxIndex = Math.max(maxNIndex, secondMaxNIndex);

                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 2), minIndex);
                if (Main.depth >= 0) System.arraycopy(temp, minIndex + 1, samples,
                        i * (Main.depth - 2) + minIndex,maxIndex - minIndex - 1);
                if (Main.depth >= 0) System.arraycopy(temp, maxIndex + 1, samples,
                        i * (Main.depth - 2) + (maxIndex - 1), Main.depth - 1 - maxIndex);
//                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 1), maxNIndex);
//                if (Main.depth >= 0) System.arraycopy(temp, maxNIndex + 1, samples, i * (Main.depth - 1) + maxNIndex,
//                        Main.depth - maxNIndex - 1);

                //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
            }
            // At every attribute, leave one out that has highest n
            n_max = getNmax((Kmin[]) samples);
            S_cap = getAltEstKMV((Kmin[]) samples);;
        }
        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(Main.maxSize))
                / Main.delta)/(Main.eps * Main.eps);

        q.case2Estimate = Math.ceil(S_cap * n_max / Main.maxSize);
        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                Math.sqrt(Main.maxSize)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
        if (Main.maxSize >= BConstraint) {
            q.ratioCondition = true;
        }
        q.intersectSize = S_cap;
        if (S_cap < constraint) {
            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                    Math.sqrt(Main.maxSize)) / Main.delta)/(Main.maxSize * Main.eps * Main.eps)));
        } else {
            q.thrm33Case2 = true;
            return (int) Math.ceil(S_cap * n_max / Main.maxSize);
        }

    }

    private int getNmax(Kmin[] samples) {
        int n_max = 0;
        for (Kmin sample : samples) {
            if (sample.n > n_max) {
                n_max = sample.n;
            }
        }
        return n_max;
    }

    private double getAltEstKMV(Kmin[] samples) {
        int numJoins = samples.length;
        int c = 0;
        Iterator<Long> iter = samples[0].sketch.iterator();
        while (iter != null && iter.hasNext()) {
            boolean found = true;
            Long i = iter.next();
            for (int j = 1; j < numJoins; j++) {
                Long otherElement = samples[j].sketch.ceiling(i);
                if (otherElement == null) {
                    found = false;
                    iter = null;
                    break;
                } // not contained
                else if (otherElement.equals(i)) continue; // is contained
                else {
                    iter = samples[0].sketch.tailSet(otherElement).iterator(); // fast forward iter0
                    found = false;
                    break; // but now you need to start from iter.hasNext() again
                }
            }
            if (found) c++;

        }
        return c;
    }

    private double getAltEstKMV(TreeSet<Long>[] samples) {
        int numJoins = samples.length;
        int c = 0;
        Iterator<Long> iter = samples[0].iterator();
        while (iter != null && iter.hasNext()) {
            boolean found = true;
            Long i = iter.next();
            for (int j = 1; j < numJoins; j++) {
                Long otherElement = samples[j].ceiling(i);
                if (otherElement == null) {
                    found = false;
                    iter = null;
                    break;
                } // not contained
                else if (otherElement.equals(i)) continue; // is contained
                else {
                    iter = samples[0].tailSet(otherElement).iterator(); // fast forward iter0
                    found = false;
                    break; // but now you need to start from iter.hasNext() again
                }
            }
            if (found) c++;

        }
        return c;
    }

    public int[][] ns = new int[Main.numAttributes][Main.depth];

    public int[][] Bs = new int[Main.numAttributes][Main.depth];

    public int getNmax() {
        int n_max = 0;
        for (int i = 0; i < Main.numAttributes; i++) {
            for (int j = 0; j < Main.depth; j++) {
                if (ns[i][j] > n_max) {
                    n_max = ns[i][j];
                }
            }
        }
        return n_max;
    }
    public int rangeQuery(Query q) {
        double S_cap = 0;
        int n_max = 0;
        int B_virtual = 0;
        TreeSet<Long>[] samples = new TreeSet[q.predAttrs.size() * Main.depth];
        // Empty ns;
        for (int i = 0; i < Main.numAttributes; i++) {
            for (int j = 0; j < Main.depth; j++) {
                ns[i][j] = 0;
            }
        }
        for (int i = 0; i < q.predAttrs.size(); i++) {
            int attr = q.predAttrs.get(i);
            //long[][] ranges = getLogRanges(q.lower[i], q.upper[i]);
            ArrayList<long[]> rangesList = wrapLogRanges(q.lower[attr], q.upper[attr]);
            TreeSet<Long>[] set = new TreeSet[Main.depth];
            B_virtual = rangesList.size() * Main.maxSize;
            for (int j = 0; j < rangesList.size(); j++) {
                 CountMinDyad cm = CMSketchesRange[q.predAttrs.get(i)][getIndexOfRange(rangesList.get(j))];
                 TreeSet<Long>[] rangeSet = cm.rangeQuery(rangesList.get(j)[0], rangesList.get(j)[1], ns[attr]);
                 for (int d = 0; d < Main.depth; d++) {
                    if (set[d] == null) {
                        set[d] = rangeSet[d];
                    } else {
                        set[d].addAll(rangeSet[d]);
                    }
                 }

            }
            if (Main.depth >= 0) System.arraycopy(set, 0, samples, i * Main.depth, Main.depth);
        }
        n_max = getNmax();
        S_cap = getAltEstKMV(samples); //Instead of getAltEstKMV
        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(B_virtual))
                / Main.delta)/(Main.eps * Main.eps);
        q.case2Estimate = Math.ceil(S_cap * n_max / B_virtual);
        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                Math.sqrt(B_virtual)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
        if (B_virtual >= BConstraint) {
            q.ratioCondition = true;
        }
        q.intersectSize = S_cap;
        if (S_cap < constraint) {
            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
                    Math.sqrt(B_virtual)) / Main.delta)/(B_virtual * Main.eps * Main.eps)));
        } else {
            q.thrm33Case2 = true;
            return (int) Math.ceil(S_cap * n_max / B_virtual);
        }
    }

    private ArrayList<long[]> wrapLogRanges(long low, long up) {
        ArrayList<long[]> temp;
        //low += (long) Math.pow(2, Main.dyadicRangeBits - 1);
        //up += (long) Math.pow(2, Main.dyadicRangeBits - 1);
        if (low < 0 || up < 0) {
            System.out.println("Error because low or up < 0: low: " + low + " up: " + up);
            System.exit(1);
        }
        temp = getLogRangesArrList(low + 1, up + 1);
        for (long[] i: temp) {
            i[0] = i[0] - 1L;// - (long) Math.pow(2, Main.dyadicRangeBits - 1);
            i[1] = i[1] - 1L;// - (long) Math.pow(2, Main.dyadicRangeBits - 1);
        }
        return temp;
    }

    private int getIndexOfRange(long[] longs) {
        // get distance between the two elements in longs.
        return (int) (Math.log(longs[1] - longs[0] + 1) / Math.log(2));
    }

    public static ArrayList<long[]> getLogRangesArrList(long startInclusive, long stopInclusive) {
        startInclusive--;stopInclusive--;
        long initDiff=stopInclusive-startInclusive+1;
        ArrayList<long[]> result = new ArrayList<>();
        long totalSum = 0;
        long pow = 1;
        for (int j = 0; j <= Main.dyadicRangeBits - 1; j++) {
            if (startInclusive+pow-1>stopInclusive)
                break;
            else if (startInclusive%(pow*2)==0 && startInclusive+pow-1<=stopInclusive) ;
                // do nothing, increase the power further
            else {
                result.add(new long[]{1+startInclusive, 1+startInclusive + pow - 1});
                totalSum+=pow;
                startInclusive+=pow;
            }
            pow*=2;
        }

        pow= (long) Math.pow(2,Main.dyadicRangeBits);
        for (int j=Main.dyadicRangeBits;j>=0;j--) {
            if (startInclusive%pow==0L && startInclusive+pow-1<=stopInclusive) {
                result.add(new long[]{1+startInclusive, 1+startInclusive + pow - 1});
                totalSum+=pow;
                startInclusive+=pow;
            }
            pow/=2;
        }

        if (totalSum != initDiff) {
            System.err.println("Error - no full coverage");
        }
        return result;
    }

    public long getMemoryUsage() {
        long maxMemoryUsage;
        int predsUsed = 0;
        for (int i = 0; i < Main.numAttributes; i++) {
            if (hasPredicate[i]) {
                predsUsed++;
            }
        }

        if (Main.rangeQueries) {
            maxMemoryUsage = (long) Main.depth * Main.width * (long) predsUsed * (Main.maxSize * (Main.dyadicRangeBits + 1) * (Main.b + 3 *32 + 1) + 32);
        } else {
            maxMemoryUsage = (long) Main.depth * Main.width * (long) predsUsed * (Main.maxSize * (Main.b + 3 *32 + 1) + 32);
        }

        long memoryUsage = 0;
        long memUsageSketch = 0;
        long memUsageArray = 0;
        long totalSavedByArrays = 0;
        long totalSavedByMaxBits = 0;
        for (int i = 0; i < Main.numAttributes; i++) {
            if (hasPredicate[i]) {
                if (Main.rangeQueries) {
                    for (int j = 0; j < Main.dyadicRangeBits + 1; j++) {
                        if (maxBits[i] + 1 < Main.dyadicRangeBits - j) { // no need to store empty ranges
                            totalSavedByMaxBits += CMSketchesRange[i][j].getMemoryUsage();
                            //System.out.println("No need to store empty range " + (Main.dyadicRangeBits - j) + " for attribute " + i);
                            continue;
                        }
                        memUsageSketch = CMSketchesRange[i][j].getMemoryUsage();
                        memUsageArray  = getMemUsageArray(j);
                        if (memUsageArray < memUsageSketch) {
                            totalSavedByArrays += memUsageSketch - memUsageArray;
                            /*System.out.println("Array is better for attribute "
                                    + i + " and range " + j + " by "
                                    + (memUsageSketch - memUsageArray) + " bits");
                            System.out.println("Array: " + memUsageArray + " bits");
                            System.out.println("Sketch: " + memUsageSketch + " bits");*/
                        }
                        memoryUsage += Math.min(memUsageArray, memUsageSketch);
                    }
                } else {
                    memoryUsage += CMSketches[i].getMemoryUsage();
                }
            }
        }
        System.out.println("Total saved by arrays: " + totalSavedByArrays + " bits");
        System.out.println("Total saved by max bits: " + totalSavedByMaxBits + " bits");

        if (memoryUsage > maxMemoryUsage) {
            System.err.println("Memory usage is " + (memoryUsage) + " bytes and max memory usage is " + maxMemoryUsage + " bytes");
        }
        memUsageSynopsis = memoryUsage;
        return memoryUsage;
    }

    private long getMemUsageArray(int j) {
        long memUsageArray = 0;

        long C = (long) Math.pow(2, j);
        memUsageArray = C * Main.maxSize * (Main.b + 3 * 32 + 1) + 32;
        return memUsageArray;
    }

    public void reset() {
        for (int i = 0; i < Main.numAttributes; i++) {
            if (hasPredicate[i]) {
                if (Main.rangeQueries) {
                    for (int j = 0; j < Main.dyadicRangeBits + 1; j++) {
                        CMSketchesRange[i][j].reset();
                    }
                } else {
                    CMSketches[i].reset();
                }
            }
        }
    }


}

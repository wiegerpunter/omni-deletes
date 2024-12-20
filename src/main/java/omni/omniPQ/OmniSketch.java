package omni.omniPQ;

import omni.AnalysisBaselinesRefactor;
import omni.Formulas;
import omni.Main;
import omni.SynopsisRefactor;

import java.util.*;

import static java.lang.Math.*;

public class OmniSketch extends SynopsisRefactor {
    private final boolean dynamicSampleSizes;
    public AttributeSketch[] CMSketches;
    CountMinDyad[][] CMSketchesRange;
    CountMinS0[] CMSketchesS0;
    public final int depth;
    public final int width;
    private final int maxSize;
    private final int b;
    private final int numTwoLHSReps;
    public final int numStoredAttributes;
    private final int dyadicRangeBits;
    private final boolean checkExactUnion2LHS;
    Kmin kminTmp;
    TWOLHS[] TWOLHSTmp;
    boolean rangeQueries;
    Random randomHashG;
    final double BetaKmin;

    public OmniSketch(long ram, int numStoredAttributes, int[] parameters, int dyadicBits,
                      boolean useTwoLHS, boolean useAcrossRows,
                      boolean rangeQueries, boolean useBetaKmin,
                      boolean useFastTwoLHS, boolean useInvDistPaper2LHS,
                      boolean useMinEstimate, boolean checkExactUnion2LHS,
                      double BetaKmin, boolean dynamicSampleSizes, int seed) {
        System.out.println("OmniSketch has stored attributes: " + numStoredAttributes);
        this.seed = seed;
        this.parameters = parameters;
        this.ram = ram;
        this.dyadicRangeBits = dyadicBits;
        this.useTwoLHS = useTwoLHS;
        this.useAcrossRows = useAcrossRows;
        this.numStoredAttributes = numStoredAttributes;
        this.rangeQueries = rangeQueries;
        this.useBetaKmin = useBetaKmin;
        this.useFastTwoLHS = useFastTwoLHS;
        this.useInvDistPaper2LHS = useInvDistPaper2LHS;
        this.useMinEstimate = useMinEstimate;
        this.checkExactUnion2LHS = checkExactUnion2LHS;
        this.randomHashG = new Random(seed);
        this.BetaKmin = BetaKmin;
        this.dynamicSampleSizes = dynamicSampleSizes;

        depth = parameters[0];
        width = parameters[1];
        if (useTwoLHS) {
            StringBuilder sb = new StringBuilder();
            sb.append("TWOLHS");
            if (useAcrossRows) {
                sb.append("AcrossRows");
            } else {
                sb.append("PerRow");
            }
            if (useFastTwoLHS) {
                sb.append("Fast");
            } else {
                sb.append("Slow");
            }
            if (useInvDistPaper2LHS) {
                sb.append("InvDist");
            } else {
                sb.append("Normal");
            }
            if (useMinEstimate) {
                sb.append("MinEst");
            } else {
                sb.append("MedianEst");
            }
            sampleType = sb.toString();

            numTwoLHSReps = parameters[2];
            maxSize = -1;
            b = -1;
        } else {
            sampleType = "Kmin" + Double.toString(BetaKmin);
            maxSize = parameters[2];
            b = parameters[3];
            numTwoLHSReps = -1;
        }
        System.out.println("Parameters: " + Arrays.toString(parameters));
        initSketch();
    }

    /**
     * Initialize the sketch
     */

    public void initSketch() {
        setting = "OmniSketchPQ_" + dynamicSampleSizes;
        Main.kminDeletes = 0;

        if (useTwoLHS) {
            TWOLHSTmp = new TWOLHS[numTwoLHSReps];
            for (int i = 0; i < numTwoLHSReps; i++) {
                TWOLHSTmp[i] = new TWOLHS(seed, i);
            }
        } else {
            kminTmp = new Kmin(maxSize, b, useBetaKmin, BetaKmin, seed);
        }
        this.maxBits = new int[numStoredAttributes];

        if (rangeQueries) {
            CMSketchesRange = new CountMinDyad[numStoredAttributes][dyadicRangeBits + 1];
            // Make dyadic intervals per attribute sketch
            for (int i = 0; i < numStoredAttributes; i++) {
                for (int j = dyadicRangeBits; j >-1; j--) {
                    CMSketchesRange[i][dyadicRangeBits - j] = new CountMinDyad(i, j, dyadicRangeBits, parameters, useTwoLHS, BetaKmin, seed);
                }
            }

        } else {
            CMSketches = new AttributeSketch[numStoredAttributes];
            for (int i = 0; i < numStoredAttributes; i++) {
                CMSketches[i] = new AttributeSketch(i, parameters, useTwoLHS, useBetaKmin, BetaKmin, useFastTwoLHS, dynamicSampleSizes, seed);
            }
            if (checkExactUnion2LHS) {
                CMSketchesS0 = new CountMinS0[numStoredAttributes];
                for (int i = 0; i < numStoredAttributes; i++) {
                    CMSketchesS0[i] = new CountMinS0(i, parameters, seed);
                }
            }

        }
    }

    // region INGESTION
    /*
     * Ingest a record into the sketch
     */

    public void delete(long[] record) {
        ingest(record, -1);
    }
    public void add(long[] record) {
        ingest(record, 1);
    }

    public void ingest(long[] record, int sign) {
        long id = record[0];

        // test where the id is coming from record[1] - record[record.length -1]
//        long[] attrRecord = new long[record.length - 1];
//        System.arraycopy(record, 1, attrRecord, 0, record.length - 1);
//        long id = Arrays.hashCode(attrRecord);
        int hx = (int) id;
        long[] hx_2lhs = null;
        long[][] vals = null;
        int hashG = -1;

        if (!useTwoLHS){
            hx = kminTmp.hash(id);
        } else {
            if (useFastTwoLHS) {
                hashG = hashG(id);
            } else {
                long hx_i;
                //hx_2lhs = new long[numTwoLHSReps];
                vals = new long[numTwoLHSReps][TWOLHSTmp[0].bitSize];
                for (int i = 0; i < numTwoLHSReps; i++) {
                    hx_i = TWOLHSTmp[i].hashH(hx);
                    for (int j = 0; j < TWOLHSTmp[0].bitSize; j++) {
                        int mask = 1 << j;
                        long val = (hx_i & mask);
                        if (val == 0) {
                            vals[i][j] = 0;
                        } else {
                            vals[i][j] = 1;
                        }
                    }
                }
            }
        }

        if (rangeQueries) {
            // Compute all dyadic ranges it belongs to.
            // Insert in all those ranges.
            for (int i = 0; i < numStoredAttributes; i++) {
                long[][] ranges = wrapperInitLogRanges(record[i + 1]);
                for (int j = 0; j < ranges[2].length; j++) {
                    CMSketchesRange[i][j].add(ranges[1][j], ranges[2][j], hx);
                }
            }
        } else {
            for (int i = 0; i < numStoredAttributes; i++) {
                CMSketches[i].ingest(record[i + 1], hx, vals, hashG, sign);
            }
            if (checkExactUnion2LHS) {
                for (int i = 0; i < numStoredAttributes; i++) {
                    CMSketchesS0[i].ingest(record[i + 1], hx, sign);
                }
            }
        }
    }

    // endregion

    /*
     * G Hash function for TwoLHS
     */

    private int hashG(long id) {
        randomHashG.setSeed(id + this.seed);
        return randomHashG.nextInt(numTwoLHSReps);
    }

    //region Point Query General
    /* Query general */

    @Override
    public int query(long[] query, int numPreds) {
        throw new IllegalArgumentException("OmniSketch does not support this query method");
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        throw new IllegalArgumentException("OmniSketch does not support this query method");
    }


//    public int query(long[] q, int numPreds, int unionSize) {
//        throw new IllegalArgumentException("OmniSketch does not support this query method");
//    }


    @Override
    public int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
        // Get estimate per depth. Take minimum of all estimates.

        if (useTwoLHS) {
            int result;
            if (useAcrossRows) {
                result = queryEstAcrossRowsTwoLHS(getSamplesTwoLHSAcrossRows(query, numPreds), queryInfo);
            } else {
                result = queryEstPerRowTwoLHSWithInfo(getSamplesTwoLHSPerRow(query, numPreds), queryInfo);
            }
            if (checkExactUnion2LHS) {
                int[] res = checkConditions(query, numPreds, -1, queryInfo);
                queryInfo.exactUnion = res[0];
                queryInfo.exactIntersection = res[1];
            }
            return result;
        } else {
            if (useAcrossRows) {
                return queryKmin(getSamplesKmin(query, numPreds), queryInfo);
            } else {
                // return median of rows
                return queryEstPerRowKmin(getSamplesPerRowKmin(query, numPreds), queryInfo);
            }
        }
    }



//    public int query(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        if (Main.estPerRow) {
//            return queryEstPerRow(q, numPreds, unionSize, CMRow);
//        } else {
//            int est = queryEstOverAllRows(q, numPreds, unionSize, CMRow);
//            System.out.println("Estimate: " + est);
//            return est;// queryEstOverAllRows(q, numPreds, unionSize, CMRow);
//        }
//    }

    //endregion


    //region Minwise Query
    /*
     * Query minwise
     */

    public Kmin[] getSamplesKmin(long[] q, int numPreds) {
        Kmin[] samples = new Kmin[numPreds * depth];
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) { // Find way to not take the -1s into account in query.
                Kmin[] temp = CMSketches[i].queryKmin(q[i]);
                if (depth >= 0) {
                    System.arraycopy(temp, 0, samples, (i - attrWithoutPred) * depth, depth);
                }
            } else {
                attrWithoutPred++;
            }
        }
        return samples;
    }

    public Kmin[][] getSamplesPerRowKmin(long[] q, int numPreds) {
        Kmin[][] samples = new Kmin[depth][numPreds];
        int numPredsFound = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) {
                Kmin[] temp = CMSketches[i].queryKmin(q[i]);
                for (int j = 0; j < depth; j++) {
                    samples[j][numPredsFound] = temp[j];
                }
                numPredsFound++;
            }
        }
        return samples;
    }

    private double getAltEstKMV(PriorityQueue<Integer>[] samples) {
        int numJoins = samples.length;
        int intersectionCount = 0;

        // Array to track the current value from each iterator
        Integer[] currentValues = new Integer[numJoins];

        // Initialize each queue's current value
        for (int i = 0; i < numJoins; i++) {
            if (!samples[i].isEmpty()) {
                currentValues[i] = samples[i].peek();
            } else {
                return 0; // If any queue is empty, intersection count is zero
            }
        }

        while (true) {
            // Find the maximum value among the current values
            Integer minCurrent = currentValues[0];
            for (int i = 1; i < numJoins; i++) {
                if (currentValues[i] < minCurrent) {
                    minCurrent = currentValues[i];
                }
            }

            // Check if all current values match the maxCurrent
            boolean allMatch = true;
            for (int i = 0; i < numJoins; i++) {
                if (!currentValues[i].equals(minCurrent)) {
                    allMatch = false;
                    break;
                }
            }

            // If all queues have the same current value, count it as an intersection
            if (allMatch) {
                intersectionCount++;
                // Advance all iterators to the next element
                for (int i = 0; i < numJoins; i++) {
                    samples[i].poll();
                    if (!samples[i].isEmpty()) {
                        currentValues[i] = samples[i].peek();
                    } else {
                        return intersectionCount; // End if any queue is exhausted
                    }
                }
            } else {
                // Advance only iterators of queues with current values < maxCurrent
                for (int i = 0; i < numJoins; i++) {
                    while (currentValues[i] > minCurrent) {
                        samples[i].poll();
                        if (!samples[i].isEmpty()) {
                            currentValues[i] = samples[i].peek();
                        } else {
                            return intersectionCount; // End if any queue is exhausted
                        }
                    }
                }
            }
        }
    }


//    private double getAltEstKMV(PriorityQueue<Long>[] samples) {
//        int numJoins = samples.length;
//        int intersectionCount = 0;
//
//        // Clone each PriorityQueue to avoid modifying the original queues
//
//        // Initialize an iterator for each cloned PriorityQueue
//        Iterator<Long>[] iterators = new Iterator[numJoins];
//        for (int i = 0; i < numJoins; i++) {
//            iterators[i] = samples[i].iterator();
//        }
//
//        // Iterate through the first cloned queue as the "base" for comparison
//        Iterator<Long> baseIter = iterators[0];
//        while (baseIter.hasNext()) {
//            Long current = baseIter.next();
//            boolean isInAllQueues = true;
//
//            // Check if the current element is present in each of the other cloned queues
//            for (int j = 1; j < numJoins; j++) {
//                Long otherCurrent = null;
//                while (iterators[j].hasNext()) {
//                    otherCurrent = iterators[j].next();
//                    // Since queues are in descending order, break when otherCurrent <= current
//                    if (otherCurrent <= current) break;
//                }
//
//                // If no match or `otherCurrent` < `current`, it's not in all queues
//                if (otherCurrent == null || !otherCurrent.equals(current)) {
//                    isInAllQueues = false;
//                    break;
//                }
//            }
//
//            // If element is present in all queues, increase the count
//            if (isInAllQueues) {
//                intersectionCount++;
//            }
//        }
//
//        return intersectionCount;
//    }

//    private double getAltEstKMV(PriorityQueue<Long>[] samples) {
//        int numJoins = samples.length;
//        int intersectionCount = 0;
//
//        // Initialize an iterator for each PriorityQueue
//        Iterator<Long>[] iterators = new Iterator[numJoins];
//        for (int i = 0; i < numJoins; i++) {
//            iterators[i] = samples[i].iterator();
//        }
//
//        // Iterate through the first queue as the "base" for comparison
//        Iterator<Long> baseIter = iterators[0];
//        while (baseIter.hasNext()) {
//            Long current = baseIter.next();
//            boolean isInAllQueues = true;
//
//            // Check if the current element is present in each of the other queues
//            for (int j = 1; j < numJoins; j++) {
//                Long otherCurrent = null;
//                while (iterators[j].hasNext()) {
//                    otherCurrent = iterators[j].next();
//                    // Since queues are in descending order, break when otherCurrent <= current
//                    if (otherCurrent <= current) break;
//                }
//
//                // If no match or `otherCurrent` < `current`, it's not in all queues
//                if (otherCurrent == null || !otherCurrent.equals(current)) {
//                    isInAllQueues = false;
//                    break;
//                }
//            }
//
//            // If element is present in all queues, increase the count
//            if (isInAllQueues) {
//                intersectionCount++;
//            }
//        }
//
//        return intersectionCount;
//    }


    private int[] getNmax(Kmin[] samples) {
        int[] nmax = new int[2];
        for (Kmin kmin : samples) {
            if (kmin.n > nmax[0]) {
                nmax[0] = kmin.n;
                nmax[1] = kmin.curSampleSize;
            }
        }
        return nmax;
    }


    private int queryEstPerRowKmin(Kmin[][] samplesPerRowKminTreeSet, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
        // Do queryKmin per row and take median.
        int[] estimates = new int[depth];
        int min_nmax = Integer.MAX_VALUE;
        int final_SCap = 0;
        int final_nmax = 0;
        int final_maxSize = 0;
        int estimate = 0;
        boolean case1 = false;
        for (int i = 0; i < depth; i++) {
            estimates[i] = queryKmin(samplesPerRowKminTreeSet[i], queryInfo);
            if (queryInfo.nmax < min_nmax) { // Return estimate with lowest nmax.
                min_nmax = queryInfo.nmax;
                estimate = estimates[i];
                final_SCap = queryInfo.Scap;
                final_nmax = queryInfo.nmax;
                final_maxSize = queryInfo.maxSize;
                case1 = queryInfo.case1;
            }
        }
        queryInfo.setScap(final_SCap, final_nmax, final_maxSize, case1);
        return estimate;

    }

    private int queryKmin(Kmin[] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {

        double S_cap = 0;
        int[] nmax;
        int exceedBounds = 0;
        PriorityQueue<Integer>[] flatSamples = new PriorityQueue[samples.length];
        for (int i = 0; i < samples.length; i++) {
            if (samples[i].exceedsNumberOfDeletes) {
                exceedBounds++;
            }
            PriorityQueue<Integer> kmin = samples[i].getSampleToQuery();
            flatSamples[i] = kmin;
        }
        nmax = getNmax(samples);
        S_cap = getAltEstKMV(flatSamples);
        queryInfo.setScap((int) S_cap, nmax[0], nmax[1], queryInfo.case1);
        queryInfo.numberOfKmins = samples.length;
        queryInfo.numberOfKminsExceedingBound = exceedBounds;


//        if (maxSize > Main.streamSize) {
//            throw new IllegalArgumentException("maxSize > streamSize");
//        }
        if (useBetaKmin) {
            return (int) ceil(S_cap * nmax[0] / ((double) nmax[1] /BetaKmin)); // K/2 because we have deletes.
        } else {
            return (int) ceil(S_cap * nmax[0] / nmax[1]);
        }

    }

    public int getFilledKSamples() {
        int count = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            count += CMSketches[i].getFilledKSamples();
        }
        return count;
    }

    //endregion


    //region QUERY 2LHS COMMON
    /* Query TwoLHS */

    /* 2LHS COMMON */

    private int setUnionEstimator(TWOLHS[][] samples, double eps) {
        double f =(1 + eps) * numTwoLHSReps / 8;
        int index =0;
        int count;
        while(true) {
            count = 0;
            boolean incrCount = false;
            for (int i = 0; i < numTwoLHSReps; i++) {
                incrCount = false;
                for (TWOLHS[] sample : samples) {
                    if (!sample[i].emptyBucket(index)) {
                        incrCount = true;
                        break;
                    }
                }
                if (incrCount) {
                    count++;
                }
            }
            if (count <= f) {
                break;
            } else {
                index++;
            }
        }
        double phat = (double) (count + 1) /(numTwoLHSReps + 1);//samples.length;
        double R = pow(2, index + 1);
        double S = (log(1 - phat)/log(2)) / (log(1 - 1 / R)/log(2));
        if (useFastTwoLHS) {
            return (int) ceil(S) * numTwoLHSReps;
        } else {
            return (int) ceil(S);
        }
    }

    private int setIntersectEstimator(TWOLHS[][] samples, double unionEstimate, double eps,
                                      int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {

        if (this.useInvDistPaper2LHS) {
            return setIntersectEstimatorInverseDist(samples, unionEstimate, eps, unionSizeExact, CMRow);
        } else {
            return setIntersectEstimatorTwoLHS(samples, unionEstimate, eps, unionSizeExact, CMRow);
        }

    }

    private boolean identicalSingletonBucket(TWOLHS[] samples, int lsb) {
        for (TWOLHS sample : samples) {
            if (!sample.singletonBucket(lsb)) {
                return false;
            }
        }
        int j = 1;
        while (j < samples[0].countSignatures[lsb].length) {
            // Check if no sample has a count of 0 and the other has a count > 0.
            for (int i = 0; i < samples.length - 1; i++) {
                for (int k = i + 1; k < samples.length; k++) {
                    if ((samples[i].countSignatures[lsb][j] > 0) != (samples[k].countSignatures[lsb][j] > 0)) { // true if they contain same sinleton element.
                        // if one is 0 and the other is not, they are not identical.
                        // They do not have to have the same count.
                        return false;
                    }
                }
            }
            j++;
        }
        return true;
    }


    private boolean singletonUnionBucket(TWOLHS[][] samples, int repetition, int lsb) {
        // check if union of buckets is singleton.
        // either one is empty and the other is singleton, or they are identical singleton buckets.
        // sample must be either empty or must be singleton.
        // samples is samples[predicates][2lhsreps]
        // repetiotn is the repetition of the 2lhs.
        // lsb is the least significant bit.
        ArrayList<TWOLHS> singletonElements = new ArrayList<>();
        for (TWOLHS[] sample : samples) {
            if (sample[repetition].singletonBucket(lsb)) {
                singletonElements.add(sample[repetition]);
            } else if (!sample[repetition].emptyBucket(lsb)) {
                return false;
            }
        }
        // Now check if all singleton elements are identical.
        if (!singletonElements.isEmpty()) {
            TWOLHS[] singletons =  new TWOLHS[singletonElements.size()];
            for (int i=0;i<singletonElements.size();i++){
                singletons[i] = singletonElements.get(i);
            }
            return identicalSingletonBucket(singletons, lsb);
        } else {
            return false;
        }
    }
    //endregion

    //region QUERY 2LHS INV DIST PAPER

    /* 2LHS INVERSE DIST PAPER APPROACH */

    private int[] bucketDiffEstimatorInverseDist(TWOLHS[][] samples, int repetition, int unionSizeExact) {
        //int index;
        int sum=0;// witness count
        int count =0; // total count
        // Instead of doing it for one index, we want to check every index.
        int countSignaturesLength = samples[0][repetition].countSignatures.length;
//        if (repetition == 0) {
//            System.out.println("CountSignaturesLength: " + countSignaturesLength);
//        }
        for (int index=0; index < countSignaturesLength; index++) {
            if (singletonUnionBucket(samples, repetition, index)) {
                boolean witnessFound = true;
                for (TWOLHS[] sample : samples) {
                    if (!sample[repetition].singletonBucket(index)) {
                        witnessFound = false;
                    }
                }
                if (witnessFound) {
                    sum++;
                }
                count++;
            }
        }
        return new int[]{sum, count};
    }

    private int setIntersectEstimatorInverseDist(TWOLHS[][] samples, double unionEstimate, double eps, int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < numTwoLHSReps; i++) {
            int[] bde = bucketDiffEstimatorInverseDist(samples, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
            sum += bde[0];
            count+=bde[1];
        }
        if (count == 0){
            countIsZero++;
        }
        double result;
        if (Main.useExactUnionSize) {
            result = ceil(((double) sum / count) * unionSizeExact);
        } else {
            result = ceil(((double) sum / count) * unionEstimate);
        }
        CMRow.addJaccardEstimate((double) sum / count, sum);
        int resultInt = (int) result;
        if (resultInt < 0) {
            System.out.println("Error: result > unionSize");
            System.exit(1);
        }

        return resultInt;
    }

    //endregion

    //region QUERY 2LHS NORMAL
    /* 2LHS NORMAL APPROACH */

    private int bucketDiffEstimator(TWOLHS[][] samples, double unionEstimate, double eps, int repetition, int unionSizeExact) {
        int index;
        if (useFastTwoLHS) {
            if (Main.useExactUnionSize) {
                index = (int) ceil(log((2 * unionSizeExact) / (numTwoLHSReps * pow((1 - eps), 2))) / log(2));
            } else {
                index = (int) ceil(log((2 * unionEstimate) / (numTwoLHSReps * pow((1 - eps), 2))) / log(2));
            }
        } else {
            double Beta = 1.5;
            index =(int) ceil(log((Beta * unionEstimate) / (1 - eps)) / log(2));

        }
//        System.out.println("LSB: " + index);
        if (index < 0) {
            throw new IllegalArgumentException("Index <= 0");
        }
        // SingletonUnionBucket with multiple samples.
        if (!singletonUnionBucket(samples, repetition, index)) {
            return -1;
        }
        // if all are singletons, found witness
        for (TWOLHS[] sample : samples) {
            if (!sample[repetition].singletonBucket(index)) {
                return 0; // no witness found
            }
        }
        return 1; // witness found of intersection.
    }

    private int setIntersectEstimatorTwoLHS(TWOLHS[][] samples, double unionEstimate, double eps,
                                            int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < numTwoLHSReps; i++) {
            int diff = bucketDiffEstimator(samples, unionEstimate, eps, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
            if (diff != -1) {
                sum += diff;
                count++;
            }
        }
        if (count == 0){
            countIsZero++;
        }
        double result;
        if (Main.useExactUnionSize) {
            result = ceil(((double) sum / count) * unionSizeExact);
        } else {
            result = ceil(((double) sum / count) * unionEstimate);
        }
        CMRow.addJaccardEstimate((double) sum / count, sum);
        int resultInt = (int) result;
        if (resultInt < 0) {
            System.out.println("Error: result > unionSize");
            System.exit(1);
        }

        return resultInt;
    }
    //endregion

    //region QUERY 2LHS ACROSS ROWS

    /* 2LHS ACROSS ROWS */


    public TWOLHS[][] getSamplesTwoLHSAcrossRows(long[] q, int numPreds) {
        TWOLHS[][] samples = new TWOLHS[depth*numPreds][];
        // initialize arrays
        for (int i = 0; i < samples.length; i++) {
            samples[i] = new TWOLHS[numTwoLHSReps];
        }
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) { // Find way to not take the -1s into account in query.
                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
                if (depth >= 0) {
                    for (int j = 0; j < depth; j++) {
                        System.arraycopy(temp[j], 0, samples[(i - attrWithoutPred) * depth + j], 0, numTwoLHSReps);
                    }
                }
            } else {
                attrWithoutPred++;
            }
        }
        return samples;
    }

    private int queryEstAcrossRowsTwoLHS(TWOLHS[][] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {

        double u = setUnionEstimator(samples, Main.eps/3); // Est of union size. //TODO: Using Main.eps, but should use eps.
        //System.out.println("u: " + u + ", unionSize: " + unionSize + " diff: " + (u - unionSize));
        int estimate = setIntersectEstimator(samples, u, Main.eps/3, -1,queryInfo); //TODO: Using Main.eps, but should use eps.

        if (queryInfo.jaccardEstimates.isEmpty() && useTwoLHS) {
            throw new IllegalArgumentException("Jaccard estimates not set");
        } else if (queryInfo.jaccardEstimates.size() > 1) {
            throw new IllegalArgumentException("Jaccard estimates size > 1");
        }
        double jaccardEstimate = queryInfo.jaccardEstimates.get(0);
        int witnessEstimate = queryInfo.witnessEstimates.get(0);

        queryInfo.set2LHS(u, witnessEstimate, jaccardEstimate);
        return estimate;
    }
    //endregion

    //region QUERY 2LHS PER ROW
    /* 2LHS PER ROW */

    public TWOLHS[][][] getSamplesTwoLHSPerRow(long[] q, int numPreds) {
        TWOLHS[][][] samples = new TWOLHS[depth][numPreds][];
        // initialize arrays
        for (int i = 0; i < depth; i++) {
            samples[i] = new TWOLHS[numPreds][];
            for (int j = 0; j < numPreds; j++) {
                samples[i][j] = new TWOLHS[numTwoLHSReps];
            }

        }
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) { // Find way to not take the -1s into account in query.
                // temp is Sample[depth][numTwoLHSReps]
                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
                // want to add to samples per depth, such that we can call unionEstimate
                if (depth>0) {
                    for (int j = 0; j < depth; j++) {
                        System.arraycopy(temp[j], 0, samples[j][(i - attrWithoutPred)], 0, temp[j].length);
                    }
                }
            } else {
                attrWithoutPred++;
            }
        }
        return samples;
    }

    private int minEstimate(int[] estimates, double[] unionEstimates, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < estimates.length; i ++) {
            if (estimates[i] < min) {
                CMRow.setCMRow(i);
                min = estimates[i];
                if (CMRow.jaccardEstimates.isEmpty() && useTwoLHS) {
                    throw new IllegalArgumentException("Jaccard estimates not set");
                }
                double jaccardEstimate = CMRow.jaccardEstimates.get(i);
                int witnessEstimate = CMRow.witnessEstimates.get(i);
                CMRow.set2LHS(unionEstimates[i], witnessEstimate, jaccardEstimate);

            }
        }
        return min;
    }

    private int medianEstimate(int[] estimates) {
        int median;
        int[] sorted = estimates.clone();
        Arrays.sort(sorted);
        if (sorted.length % 2 == 0) {
            median = (sorted[sorted.length/2] + sorted[sorted.length/2 - 1])/2;
        } else {
            median = sorted[sorted.length/2];
        }
        return median;
    }


    private int queryEstPerRowTwoLHSWithInfo(TWOLHS[][][] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
        int[] estimates = new int[depth];
        double[] unionEstimates = new double[depth];
        for (int i = 0; i < depth; i++) {
            unionEstimates[i] = setUnionEstimator( samples[i], Main.eps/3); // Est of union size.
            estimates[i] = setIntersectEstimator(samples[i], unionEstimates[i], Main.eps/3, -1, queryInfo);
        }
        if (useMinEstimate) {
            return minEstimate(estimates, unionEstimates, queryInfo);
        } else {
            return medianEstimate(estimates);
        }
    }
    //endregion
    /*
     * End 2LHS
     */


//region COMMENTED OUT
//



    //TODO: Can I deletE?
//    private int queryEstOverAllRows(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        TWOLHS[][] samples = new TWOLHS[numPreds * depth][];
//        // initialize arrays
//        for (int i = 0; i < numPreds * depth; i++) {
//            samples[i] = new TWOLHS[numTwoLHSReps];
//        }
//
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
//                if (depth >= 0) {
//                    for (int j = 0; j < depth; j++) {
//                        System.arraycopy(temp[j], 0, samples[(i - attrWithoutPred) * depth + j], 0, numTwoLHSReps);
//                    }
//                }
//            } else {
//                attrWithoutPred++;
//            }
//        }
//        double u = setUnionEstimator((TWOLHS[][]) samples, Main.eps/3); // Est of union size.
//        return setIntersectEstimator((TWOLHS[][]) samples, u, Main.eps/3, unionSize, CMRow);
//    }

//    private int queryEstPerRowTwoLHS(TWOLHS[][][] samples) {
//        int[] estimates = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            double u = setUnionEstimator( samples[i], Main.eps/3); // Est of union size.
//            estimates[i] = setIntersectEstimator(samples[i], u, Main.eps/3, -1, null);
//        }
//        if (Main.minEstimate) {
//            return minEstimate(estimates);
//        } else {
//            return medianEstimate(estimates);
//        }
//    }





//    private int queryEstPerRow(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        TWOLHS[][][] samples = (TWOLHS[][][]) getSamplesTwoLHSPerRow(q, numPreds);
//        // Get estimate per depth. Take minimum of all estimates.
//        int[] estimates = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            double u = setUnionEstimator((TWOLHS[][]) samples[i], Main.eps/3); // Est of union size.
//            estimates[i] = setIntersectEstimator((TWOLHS[][]) samples[i], u, Main.eps/3, unionSize, CMRow);
//        }
//        if (Main.minEstimate) {
//            return minEstimate(estimates, CMRow);
//        } else {
//            return medianEstimate(estimates);
//        }

////    }
//    private int minEstimate(int[] estimates, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        int min = Integer.MAX_VALUE;
//        for (int i = 0; i < estimates.length; i ++) {
//            if (estimates[i] < min) {
//                CMRow.setCMRow(i);
//                min = estimates[i];
//
//            }
//        }
//        return min;
//    }
//
//
//
//    private int minEstimate(int[] estimates) {
//        int min = Integer.MAX_VALUE;
//        for (int estimate : estimates) {
//            if (estimate < min) {
//                min = estimate;
//            }
//        }
//        return min;
//    }
//    private double[] minEstimateInfo(double[][] estimates) {
//        int min = Integer.MAX_VALUE;
//        double[] minEstimate = new double[4];
//        for (int i = 0; i<estimates.length; i++) {
//            if (estimates[i][0] < min) {
//                min = (int) estimates[i][0];
//                minEstimate = estimates[i];
//            }
//        }
//        return minEstimate;
//    }



//
//    private int[] bucketDiffEstimatorGreedy(TWOLHS[][] samples, double unionEstimate, double eps, int repetition, int unionSizeExact) {
//        int index;
//        int count =0;
//        int sum=0;
//
//        for (int i = 0; i < TWOLHSTmp[0].bitSize; i++) {
//            if (!singletonUnionBucket(samples, repetition, i)) {
//                continue;
//            }
//            boolean witnessFound = true;
//            for (TWOLHS[] sample : samples) {
//                if (!sample[repetition].singletonBucket(i)) {
//                    witnessFound = false;
//                }
//            }
//            if (witnessFound) {
//                sum++;
//            }
//            count++;
//        }
//        return new int[]{sum, count};
//    }








//
//
//    private double[] setIntersectEstimatorInfo(TWOLHS[][] samples, double unionEstimate, double eps, int unionSizeExact) {
//        int sum = 0;
//        int count = 0;
//        for (int i = 0; i < numTwoLHSReps; i++) {
//            int diff = bucketDiffEstimator(samples, unionEstimate, eps, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
//            if (diff != -1) {
//                sum += diff;
//                count++;
//            }
//        }
//        if (count == 0){
//            countIsZero++;
//        }
//        double result;
//        if (Main.useExactUnionSize) {
//            result = ceil(((double) sum / count) * unionSizeExact);
//        } else {
//            result = ceil(((double) sum / count) * unionEstimate);
//        }
//        int resultInt = (int) result;
//        if (resultInt < 0) {
//            System.out.println("Error: result > unionSize");
//            System.exit(1);
//        }
//
//        return new double[]{resultInt, sum, count, unionEstimate};
//    }




//    private double getAltEstKMV(Kmin[] samples) {
//        int numJoins = samples.length;
//        int c = 0;
//        Iterator<Long> iter = samples[0].sketch.iterator();
//        while (iter != null && iter.hasNext()) {
//            boolean found = true;
//            Long i = iter.next();
//            for (int j = 1; j < numJoins; j++) {
//                Long otherElement = samples[j].sketch.ceiling(i);
//                if (otherElement == null) {
//                    found = false;
//                    iter = null;
//                    break;
//                } // not contained
//                else if (otherElement.equals(i)) continue; // is contained
//                else {
//                    iter = samples[0].sketch.tailSet(otherElement).iterator(); // fast forward iter0
//                    found = false;
//                    break; // but now you need to start from iter.hasNext() again
//                }
//            }
//            if (found) c++;
//
//        }
//        return c;
//    }


    //public int[][] Bs = new int[Main.numStoredAttributes][Main.depth];




// endregion



    //region SLOW INSERTION
    /* Query method for 'slow' updates */

    private int atomicDiffEstimator(TWOLHS[][] samples, double u, double eps, int repetition) {
        double beta=1.5;
        int index = max((int) ceil(log((beta * u)/(1-eps))/log(2)), 0);
        // SingletonUnionBucket with multiple samples.
        if (!singletonUnionBucket(samples, repetition, index)) {
            return -1;
        }
        // if all are singletons, found witness
        for (TWOLHS[] sample : samples) {
            if (!sample[repetition].singletonBucket(index)) {
                return 0; // no witness found
            }
        }
        return 1; // witness found of intersection.
    }
    //endregion



    //region MEMORY USAGE AND RESET
    /* Common helpers */

    public void printParams() {
        System.out.println("Omnisketch parameters:");
        if (useTwoLHS) {
            System.out.println("depth: " + depth + ", width: " + width + ", numTwoLHS: " + numTwoLHSReps);
        } else {
            System.out.println("depth: " + depth + ", width: " + width + ", maxSize: " + maxSize + ", b: " + b);
        }
    }

    public long getMemoryUsage() {
        long maxMemoryUsage;
        maxMemoryUsage = (long) this.depth * this.width * (long) numStoredAttributes;
        if (rangeQueries) {
            if (useTwoLHS) {
                maxMemoryUsage = maxMemoryUsage * ((
                        ((long) numTwoLHSReps * CMSketches[0].CMTwoLHS[0][0][0].bitSize * (
                                CMSketches[0].CMTwoLHS[0][0][0].bitSize + 1) * 32)) * (dyadicRangeBits + 1) * 32);
            } else {
                maxMemoryUsage = maxMemoryUsage * (
                        maxSize * (dyadicRangeBits + 1) * (b + 3 *32 + 1) + 32);
            }
        } else {
            if (useTwoLHS) {
                maxMemoryUsage = maxMemoryUsage * (
                        ((long) numTwoLHSReps * CMSketches[0].CMTwoLHS[0][0][0].bitSize * (
                                CMSketches[0].CMTwoLHS[0][0][0].bitSize + 1) * 32));
            } else {
                maxMemoryUsage = maxMemoryUsage * Formulas.ramSingleKmin(maxSize,b);
                //(maxSize * (b + 3 *32 + 1) + 32));
            }
        }

        long memoryUsage = 0;
        long memUsageSketch = 0;
        long memUsageArray = 0;
        long totalSavedByArrays = 0;
        long totalSavedByMaxBits = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            if (rangeQueries) {
                for (int j = 0; j < dyadicRangeBits + 1; j++) {
                    if (maxBits[i] + 1 < dyadicRangeBits - j) { // no need to store empty ranges
                        totalSavedByMaxBits += CMSketchesRange[i][j].getMemoryUsage();
                        continue;
                    }
                    memUsageSketch = CMSketchesRange[i][j].getMemoryUsage();
                    memUsageArray  = getMemUsageArray(j);
                    if (memUsageArray < memUsageSketch) {
                        totalSavedByArrays += memUsageSketch - memUsageArray;
                    }
                    memoryUsage += min(memUsageArray, memUsageSketch);
                }
            } else {
                memoryUsage += CMSketches[i].getMemoryUsage();
            }
        }

        if (memoryUsage > maxMemoryUsage) {
            System.err.println("Memory usage is " + (memoryUsage) + " bytes and max memory usage is " + maxMemoryUsage + " bytes");
        }
        memUsageSynopsis = memoryUsage;
        return memoryUsage;
    }

    private long getMemUsageArray(int j) {
        long memUsageArray = 0;

        long C = (long) pow(2, j);
        memUsageArray = C * Formulas.ramSingleKmin(maxSize, b);//maxSize * (b + 3 * 32 + 1) + 32;
        return memUsageArray;
    }


    public void reset() {
        for (int i = 0; i < numStoredAttributes; i++) {
            if (rangeQueries) {
                for (int j = 0; j < dyadicRangeBits + 1; j++) {
                    CMSketchesRange[i][j].reset();
                }
            } else {
                CMSketches[i].reset();
            }
        }
    }

    //endregion

    //region RANGE QUERIES
    /*
     * Range Queries
     */



    public int[][] ns;
    public int getNmaxRange() {
        int n_max = 0;
        for (int i = 0; i < numStoredAttributes; i++) {
            for (int j = 0; j < depth; j++) {
                if (ns[i][j] > n_max) {
                    n_max = ns[i][j];
                }
            }
        }
        return n_max;
    }


    public int rangeQuery(long[] minranges, long[] maxranges) {
        double S_cap = 0;
        int n_max = 0;
        int B_virtual = 0;
        PriorityQueue<Integer>[] samples = new PriorityQueue[numStoredAttributes * depth];
        // Empty ns;
        ns = new int[numStoredAttributes][depth];

        for (int i = 0; i < numStoredAttributes; i++) { // Used to be Main.numAttributes.
            //long[][] ranges = getLogRanges(q.lower[i], q.upper[i]);
            ArrayList<long[]> rangesList = wrapLogRanges(minranges[i + 1], minranges[i + 1]);
            PriorityQueue<Integer>[] set = new PriorityQueue[depth];
            B_virtual = rangesList.size() * maxSize;
            for (int j = 0; j < rangesList.size(); j++) {
                CountMinDyad cm = CMSketchesRange[i][getIndexOfRange(rangesList.get(j))];
                PriorityQueue<Integer>[] rangeSet = cm.rangeQuery(rangesList.get(j)[0], rangesList.get(j)[1], ns[i]);
                for (int d = 0; d < depth; d++) {
                    if (set[d] == null) {
                        set[d] = rangeSet[d];
                    } else {
                        set[d].addAll(rangeSet[d]);
                    }
                }

            }
            if (depth > 0) System.arraycopy(set, 0, samples, i * depth,depth);
        }
        n_max = getNmaxRange();
        S_cap = getAltEstKMV(samples); //Instead of getAltEstKMV
        double constraint = 3 * log((4 * numStoredAttributes * depth * sqrt(B_virtual))
                / Main.delta)/(Main.eps * Main.eps);
        return (int) ceil(S_cap * n_max / B_virtual);
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

    public long[][] getLogRanges(long inputKey) {
        long[] coeff      = new long[dyadicRangeBits];
        long[] lowerBound = new long[dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)
        long[] upperBound = new long[dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)

        long halfPoint = (long) pow(2,dyadicRangeBits -1);
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
        for (int i = 1; i<dyadicRangeBits -1; i++) {
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
        lowerBound[dyadicRangeBits -1] = inputKey;
        upperBound[dyadicRangeBits -1] = inputKey;
        coeff[dyadicRangeBits -1] = inputKey;

        long[][] result = new long[3][];
        result[0] = coeff;
        result[1] = lowerBound;
        result[2] = upperBound;
        return result;
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
        return (int) (log(longs[1] - longs[0] + 1) / log(2));
    }

    public ArrayList<long[]> getLogRangesArrList(long startInclusive, long stopInclusive) {
        startInclusive--;stopInclusive--;
        long initDiff=stopInclusive-startInclusive+1;
        ArrayList<long[]> result = new ArrayList<>();
        long totalSum = 0;
        long pow = 1;
        for (int j = 0; j <= dyadicRangeBits - 1; j++) {
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

        pow= (long) pow(2,dyadicRangeBits);
        for (int j=dyadicRangeBits;j>=0;j--) {
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

    /* End of range queries */
    //endregion


    //region CHECK CONDITIONS
    /*
     * Check conditions
     */

    public int unionSize(TreeSet<Long>[] lists) {
        if (lists.length == 0) {
            return 0;
        } else if (lists.length == 1) {
            return lists[0].size();
        }
        // union on sorted arraylist
        TreeSet<Long> set = new TreeSet<>();
        for (TreeSet<Long> list : lists) {
            set.addAll(list);
        }

        return set.size();
    }

    private int intersection (TreeSet<Long>[] samples) {
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

    @Override
    public int[] checkConditions(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
        TreeSet<Long>[][] samples = new TreeSet[depth][numPreds];
        // initialize arrays
        for (int i = 0; i < depth; i++) {
            samples[i] = new TreeSet[numPreds];

        }
        int attrWithoutPred = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] != -1) { // Find way to not take the -1s into account in query.
                // temp is Sample[depth][numTwoLHSReps]
                TreeSet<Long>[] temp = CMSketchesS0[i].query(q[i]);
                // want to add to samples per depth, such that we can call unionEstimate
                if (depth>=0) {
                    for (int j = 0; j < depth; j++) {
                        samples[j][(i - attrWithoutPred)] = temp[j];
                        //System.arraycopy(temp[j], 0, samples[j][(i - attrWithoutPred)],0, temp[j].size());
                    }
                }
            } else {
                attrWithoutPred++;
            }
        }
        int[] result = new int[2];
        if (numPreds == 0) {
            return new int[]{0, 0};
        }
        // get union and intersection size
        result[0] = unionSize(samples[CMRow.CMRow]);
        result[1] = intersection(samples[CMRow.CMRow]);
        //System.out.println("In CheckConditions | Union size: " + result[0] + ", intersection size: " + result[1]);
        return result;
    }
    //endregion

}




//region OLD OMNI

//--------------------------------------------------------------------------------------------------------------------------------
//package omni.omniTwoLHS;
//
//import omni.*;
//
//import java.util.*;
//
//import static java.lang.Math.*;
//
//public class OmniSketch extends SynopsisRefactor {
//    CountMin[] CMSketches;
//    CountMinDyad[][] CMSketchesRange;
//    //ArrayList<DieHash> dieHashFunctions = new ArrayList<>();
//    CountMinS0[] CMSketchesS0;
//
//    private final int depth;
//    private final int width;
//    private final int maxSize;
//    private final int b;
//    private final int numTwoLHSReps;
//    private final int numStoredAttributes;
//    private final int dyadicRangeBits;
//    Kmin kminTmp;
//    TWOLHS[] TWOLHSTmp;
//    //boolean useTwoLHS;
//    boolean rangeQueries;
//    Random randomHashG;
//
//    //boolean useTwoKmin;
//
//
////    public OmniSketch(boolean useTwoLHS) {
////        super();
////        this.useTwoLHS = useTwoLHS;
////        if (useTwoLHS) {
////            sampleType = "TWOLHS";
////            this.parameters = new int[]{Main.depth, Main.width, Main.numTwoLHSReps};
////        } else {
////            sampleType = "Kmin";
////            this.parameters = new int[]{Main.depth, Main.width, Main.maxSize, Main.b};
////        }
////        depth = Main.depth;
////        width = Main.width;
////        maxSize = Main.maxSize;
////        b = Main.b;
////        numTwoLHSReps = Main.numTwoLHSReps;
////        initSketch();
////    }
//
//    public OmniSketch(int[] parameters, boolean useTwoLHS) {
//        super();
//        throw new IllegalArgumentException("OmniSketch does not support this constructor");
////        this.useTwoLHS = useTwoLHS;
////        this.parameters = parameters;
////        depth = parameters[0];
////        width = parameters[1];
////        maxSize = parameters[2];
////        b = parameters[3];
////        initSketch();
//
//    }
//
////    public OmniSketch(long ram, boolean useTwoLHS) {
////        this.ram = ram;
////        this.useTwoLHS = useTwoLHS;
////        if (useTwoLHS) {
////            sampleType = "TWOLHS";
////            this.parameters = new int[]{Main.depth, Main.width, Main.numTwoLHSReps};
////        } else {
////            sampleType = "Kmin";
////            this.parameters = new int[]{Main.depth, Main.width, Main.maxSize, Main.b};
////        }
////        depth = Main.depth;
////        width = Main.width;
////        maxSize = Main.maxSize;
////        b = Main.b;
////        numTwoLHSReps = Main.numTwoLHSReps;
////        initSketch();
////    }
//
//    public OmniSketch(long ram, int numStoredAttributes, int[] parameters, int dyadicBits,
//                      boolean useTwoLHS, boolean useAcrossRows, boolean rangeQueries, boolean useTwoKmin, boolean twoLHSFast, int seed) {
//        System.out.println("OmniSketch has stored attributes: " + numStoredAttributes);
//        this.seed = seed;
//        this.parameters = parameters;
//        this.ram = ram;
//        this.dyadicRangeBits = dyadicBits;
//        this.useTwoLHS = useTwoLHS;
//        this.useAcrossRows = useAcrossRows;
//        this.numStoredAttributes = numStoredAttributes;
//        this.rangeQueries = rangeQueries;
//        this.useTwoKmin = useTwoKmin;
//        this.twoLHSFast = twoLHSFast;
//
//
//
//        this.randomHashG = new Random(seed);
//        depth = parameters[0];
//        width = parameters[1];
//        if (useTwoLHS) {
//            sampleType = "TWOLHS";
//            numTwoLHSReps = parameters[2];
//            maxSize = -1;
//            b = -1;
//        } else {
//            sampleType = "Kmin";
//            maxSize = parameters[2];
//            b = parameters[3];
//            numTwoLHSReps = -1;
//        }
//        initSketch();
//    }
//
//    public void initSketch() {
//        setting = "OmniSketch";
//        Main.kminDeletes = 0;
//
//        if (!useTwoLHS) {
//            kminTmp = new Kmin(maxSize, b, useTwoKmin, seed);
//        } else {
//            TWOLHSTmp = new TWOLHS[numTwoLHSReps];
//            for (int i = 0; i < numTwoLHSReps; i++) {
//                TWOLHSTmp[i] = new TWOLHS(seed, i);
//            }
//        }
//        this.maxBits = new int[numStoredAttributes];
//
//        if (!rangeQueries) {
//            CMSketches = new CountMin[numStoredAttributes];
//            for (int i = 0; i < numStoredAttributes; i++) {
//                CMSketches[i] = new CountMin(i, parameters, useTwoLHS, useTwoKmin, twoLHSFast, seed);
//            }
//            if (Main.checkConditions) {
//                CMSketchesS0 = new CountMinS0[numStoredAttributes];
//                for (int i = 0; i < numStoredAttributes; i++) {
//                    CMSketchesS0[i] = new CountMinS0(i);
//                }
//            }
//        } else {
//            CMSketchesRange = new CountMinDyad[numStoredAttributes][dyadicRangeBits + 1];
//            // Make dyadic intervals per attribute sketch
//            for (int i = 0; i < numStoredAttributes; i++) {
//                for (int j = dyadicRangeBits; j >-1; j--) {
//                    CMSketchesRange[i][dyadicRangeBits - j] = new CountMinDyad(i, j, dyadicRangeBits, parameters, useTwoLHS, seed);
//                }
//            }
//
//        }
//    }
//
//    public void printParams() {
//        System.out.println("Omnisketch parameters:");
//        if (useTwoLHS) {
//            System.out.println("depth: " + depth + ", width: " + width + ", numTwoLHS: " + numTwoLHSReps);
//        } else {
//            System.out.println("depth: " + depth + ", width: " + width + ", maxSize: " + maxSize + ", b: " + b);
//        }
//    }
//
//    public void delete(long[] record) {
//        ingest(record, -1);
//    }
//    public void add(long[] record) {
//        ingest(record, 1);
//    }
//
//    public void ingest(long[] record, int sign) {
//        long id = record[0];
//        long hx = id;
//        long[] hx_2lhs = null;
//        long[][] vals = null;
//        int hashG = -1;
//
//        if (!useTwoLHS){
//            hx = kminTmp.hash(id);
//        } else {
//            if (twoLHSFast) {
//                hashG = hashG(id);
//            } else {
//                long hx_i;
//                //hx_2lhs = new long[numTwoLHSReps];
//                vals = new long[numTwoLHSReps][TWOLHSTmp[0].bitSize];
//                for (int i = 0; i < numTwoLHSReps; i++) {
//                    hx_i = TWOLHSTmp[i].hashH(hx);
//                    for (int j = 0; j < TWOLHSTmp[0].bitSize; j++) {
//                        int mask = 1 << j;
//                        long val = (hx_i & mask);
//                        if (val == 0) {
//                            vals[i][j] = 0;
//                        } else {
//                            vals[i][j] = 1;
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!rangeQueries) {
//            for (int i = 0; i < numStoredAttributes; i++) {
//                CMSketches[i].ingest(record[i + 1], hx, vals, hashG, sign);
//            }
//            if (Main.checkConditions) {
//                for (int i = 0; i < numStoredAttributes; i++) {
//                    CMSketchesS0[i].ingest(record[i + 1], hx, sign);
//                }
//            }
//        } else {
//            // Compute all dyadic ranges it belongs to.
//            // Insert in all those ranges.
//            for (int i = 0; i < numStoredAttributes; i++) {
//                long[][] ranges = wrapperInitLogRanges(record[i + 1]);
//                for (int j = 0; j < ranges[2].length; j++) {
//                    CMSketchesRange[i][j].add(ranges[1][j], ranges[2][j], hx);
//                }
//            }
//        }
//    }
//    private int hashG(long id) {
//        randomHashG.setSeed(id + this.seed);
//        return randomHashG.nextInt(numTwoLHSReps);
//    }
//    private long[][] wrapperInitLogRanges(long l) {
//        //l += (long) Math.pow(2, Main.dyadicRangeBits - 1); // shift to positive
//        if (l < 0) {
//            System.out.println("Error: l < 0");
//            System.exit(1);
//        }
//        long[][] ranges = getLogRanges(l + 1);
//        for (int i = 0; i < ranges[2].length; i++) {
//            ranges[1][i] = ranges[1][i] - 1L ;//- (long) Math.pow(2, Main.dyadicRangeBits - 1);
//            ranges[2][i] = ranges[2][i] - 1L ;//- (long) Math.pow(2, Main.dyadicRangeBits - 1);
//        }
//        return ranges;
//    }
//
//    public long[][] getLogRanges(long inputKey) {
//        long[] coeff      = new long[dyadicRangeBits];
//        long[] lowerBound = new long[dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)
//        long[] upperBound = new long[dyadicRangeBits]; // at pos i we store the x for ranges of size 2^(maxSize-i)
//
//        long halfPoint = (long) pow(2,dyadicRangeBits -1);
//        if (inputKey < halfPoint) {
//            coeff[0]=0;
//            lowerBound[0] = 1; // inclusive, starting from 1
//            upperBound[0] = halfPoint; // inclusive
//        } else {
//            coeff[0]=1;
//            lowerBound[0] = halfPoint; //inclusive, starting from 1
//            upperBound[0] = halfPoint*2; // inclusive
//        }
//
//        long pow = halfPoint;
//        for (int i = 1; i<dyadicRangeBits -1; i++) {
//            long prevCoeff = coeff[i-1];
//            long newCoeffLower = prevCoeff*2;
//            pow/=2;
//            if ((newCoeffLower + 1) *pow < inputKey) {
//                newCoeffLower++;
//                coeff[i] = newCoeffLower;
//            } else {
//                coeff[i] = newCoeffLower;
//            }
//            lowerBound[i] = coeff[i]*pow+1;
//            upperBound[i] = (coeff[i]+1)*pow;
//        }
//        lowerBound[dyadicRangeBits -1] = inputKey;
//        upperBound[dyadicRangeBits -1] = inputKey;
//        coeff[dyadicRangeBits -1] = inputKey;
//
//        long[][] result = new long[3][];
//        result[0] = coeff;
//        result[1] = lowerBound;
//        result[2] = upperBound;
//        return result;
//    }
//
////    public int query(long[] q, int numPreds) {
////        return queryAllRows(q, numPreds);
////        // If we want to use the other query methods, we need to change the way we store the sketches.
////        //if (Main.LTO) {
////        //            return queryLeaveTwoOut(q);
////        //        } else if (Main.LOO) {
////        //            return queryLeaveOneOut(q);
////        //        } else {
////        //            return queryAllRows(q);
////        //        }
////    }
//    @Override
//    public int[] checkConditions(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//
//
//        TreeSet<Long>[][] samples = new TreeSet[depth][numPreds];
//        // initialize arrays
//        for (int i = 0; i < depth; i++) {
//            samples[i] = new TreeSet[numPreds];
//
//        }
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                // temp is Sample[depth][numTwoLHSReps]
//                TreeSet<Long>[] temp = CMSketchesS0[i].query(q[i]);
//                // want to add to samples per depth, such that we can call unionEstimate
//                if (depth>=0) {
//                    for (int j = 0; j < depth; j++) {
//                        samples[j][(i - attrWithoutPred)] = temp[j];
//                        //System.arraycopy(temp[j], 0, samples[j][(i - attrWithoutPred)],0, temp[j].size());
//                    }
//                }
//            } else {
//                attrWithoutPred++;
//            }
//        }
//        int[] result = new int[2];
//        if (numPreds == 0) {
//            return new int[]{0, 0};
//        }
//        // get union and intersection size
//        result[0] = unionSize(samples[CMRow.CMRow]);
//        result[1] = intersection(samples[CMRow.CMRow]);
//        //System.out.println("In CheckConditions | Union size: " + result[0] + ", intersection size: " + result[1]);
//        return result;
//    }
//
//    public int unionSize(TreeSet<Long>[] lists) {
//        if (lists.length == 0) {
//            return 0;
//        } else if (lists.length == 1) {
//            return lists[0].size();
//        }
//        // union on sorted arraylist
//        TreeSet<Long> set = new TreeSet<>();
//        for (TreeSet<Long> list : lists) {
//            set.addAll(list);
//        }
//
//        return set.size();
//    }
//
//    private int intersection (TreeSet<Long>[] samples) {
//        int numJoins = samples.length;
//        int c = 0;
//        Iterator<Long> iter = samples[0].iterator();
//        while (iter != null && iter.hasNext()) {
//            boolean found = true;
//            Long i = iter.next();
//            for (int j = 1; j < numJoins; j++) {
//                Long otherElement = samples[j].ceiling(i);
//                if (otherElement == null) {
//                    found = false;
//                    iter = null;
//                    break;
//                } // not contained
//                else if (otherElement.equals(i)) continue; // is contained
//                else {
//                    iter = samples[0].tailSet(otherElement).iterator(); // fast forward iter0
//                    found = false;
//                    break; // but now you need to start from iter.hasNext() again
//                }
//            }
//            if (found) c++;
//
//        }
//        return c;
//    }
//    @Override
//    public int query(long[] query, int numPreds) {
//        throw new IllegalArgumentException("OmniSketch does not support this query method");
//    }
//
//    @Override
//    public int query(long[] query, int numPreds, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
//        // Get estimate per depth. Take minimum of all estimates.
//        if (useTwoLHS) {
//            if (useAcrossRows) {
//                return queryEstAcrossRowsTwoLHS(getSamplesTwoLHSAcrossRows(query, numPreds), queryInfo);
//            } else {
//                return queryEstPerRowTwoLHSWithInfo(getSamplesTwoLHSPerRow(query, numPreds), queryInfo);
//            }
//        } else {
//            return queryKmin(getSamplesKmin(query, numPreds), queryInfo);
//        }
//    }
//
//    private int queryKmin(Kmin[] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
//        double S_cap = 0;
//        int n_max = 0;
//        TreeSet<Long>[] flatSamples = new TreeSet[samples.length];
//        for (int i = 0; i < samples.length; i++) {
//            TreeSet<Long> kmin = samples[i].getSampleToQuery();
//            flatSamples[i] = kmin;
//        }
//        n_max = getNmax(samples);
//        S_cap = getAltEstKMV(flatSamples);
//        queryInfo.setScap((int) S_cap, n_max);
////        double constraint = 3 * Math.log((4 * numPreds * Main.depth * Math.sqrt(Main.maxSize))
////                / Main.delta)/(Main.eps * Main.eps);
//
//        if (maxSize > Main.streamSize) {
//            throw new IllegalArgumentException("maxSize > streamSize");
//            //return (int) Math.ceil(S_cap);
//        }
//        if (useTwoKmin) {
//            return (int) ceil(S_cap * n_max / ((double) maxSize /2)); // K/2 because we have deletes.
//        } else {
//            return (int) ceil(S_cap * n_max / maxSize);
//        }
//
//    }
//
//    private int queryEstPerRowTwoLHS(TWOLHS[][][] samples) {
//        int[] estimates = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            double u = setUnionEstimator( samples[i], Main.eps/3); // Est of union size.
//            estimates[i] = setIntersectEstimator(samples[i], u, Main.eps/3, -1, null);
//        }
//        if (Main.minEstimate) {
//            return minEstimate(estimates);
//        } else {
//            return medianEstimate(estimates);
//        }
//    }
//    private int queryEstPerRowTwoLHSWithInfo(TWOLHS[][][] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
//        int[] estimates = new int[depth];
//        double[] unionEstimates = new double[depth];
//        for (int i = 0; i < depth; i++) {
//            unionEstimates[i] = setUnionEstimator( samples[i], Main.eps/3); // Est of union size.
//            estimates[i] = setIntersectEstimator(samples[i], unionEstimates[i], Main.eps/3, -1, queryInfo);
//        }
//        if (Main.minEstimate) {
//            return minEstimate(estimates, unionEstimates, queryInfo);
//        } else {
//            return medianEstimate(estimates);
//        }
//    }
//    private int queryEstAcrossRowsTwoLHS(TWOLHS[][] samples, AnalysisBaselinesRefactor.QueryInfo queryInfo) {
//
//        double u = setUnionEstimator(samples, Main.eps/3); // Est of union size. //TODO: Using Main.eps, but should use eps.
//        //System.out.println("u: " + u + ", unionSize: " + unionSize + " diff: " + (u - unionSize));
//        int estimate = setIntersectEstimator(samples, u, Main.eps/3, -1,queryInfo); //TODO: Using Main.eps, but should use eps.
//
//        if (queryInfo.jaccardEstimates.isEmpty() && useTwoLHS) {
//            throw new IllegalArgumentException("Jaccard estimates not set");
//        } else if (queryInfo.jaccardEstimates.size() > 1) {
//            throw new IllegalArgumentException("Jaccard estimates size > 1");
//        }
//        double jaccardEstimate = queryInfo.jaccardEstimates.get(0);
//        int witnessEstimate = queryInfo.witnessEstimates.get(0);
//
//        queryInfo.set2LHS(u, witnessEstimate, jaccardEstimate);
//        return estimate;
//    }
//
//    public int query(long[] q, int numPreds, int unionSize) {
//        throw new IllegalArgumentException("OmniSketch does not support this query method");
//    }
//
//    public int query(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        if (Main.estPerRow) {
//            return queryEstPerRow(q, numPreds, unionSize, CMRow);
//        } else {
//            int est = queryEstOverAllRows(q, numPreds, unionSize, CMRow);
//            System.out.println("Estimate: " + est);
//            return est;// queryEstOverAllRows(q, numPreds, unionSize, CMRow);
//        }
//    }
//
//    public TWOLHS[][][] getSamplesTwoLHSPerRow(long[] q, int numPreds) {
//        TWOLHS[][][] samples = new TWOLHS[depth][numPreds][];
//        // initialize arrays
//        for (int i = 0; i < depth; i++) {
//            samples[i] = new TWOLHS[numPreds][];
//            for (int j = 0; j < numPreds; j++) {
//                samples[i][j] = new TWOLHS[numTwoLHSReps];
//            }
//
//        }
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                // temp is Sample[depth][numTwoLHSReps]
//                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
//                // want to add to samples per depth, such that we can call unionEstimate
//                if (depth>0) {
//                    for (int j = 0; j < depth; j++) {
//                        System.arraycopy(temp[j], 0, samples[j][(i - attrWithoutPred)], 0, temp[j].length);
//                    }
//                }
//            } else {
//                attrWithoutPred++;
//            }
//        }
//        return samples;
//    }
//
//    public TWOLHS[][] getSamplesTwoLHSAcrossRows(long[] q, int numPreds) {
//        TWOLHS[][] samples = new TWOLHS[depth*numPreds][];
//        // initialize arrays
//        for (int i = 0; i < samples.length; i++) {
//            samples[i] = new TWOLHS[numTwoLHSReps];
//        }
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
//                if (depth >= 0) {
//                    for (int j = 0; j < depth; j++) {
//                        System.arraycopy(temp[j], 0, samples[(i - attrWithoutPred) * depth + j], 0, numTwoLHSReps);
//                    }
//                }
//            } else {
//                attrWithoutPred++;
//            }
//        }
//        return samples;
//    }
//
//
//    public Kmin[] getSamplesKmin(long[] q, int numPreds) {
//        Kmin[] samples = new Kmin[numPreds * depth];
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                Kmin[] temp = CMSketches[i].queryKmin(q[i]);
//                if (depth >= 0) {
//                    //System.arraycopy(temp[j], 0, samples, (i - attrWithoutPred) * Main.depth + j, 1);
//                    System.arraycopy(temp, 0, samples, (i - attrWithoutPred) * depth, depth);
//                }
//            } else {
//                attrWithoutPred++;
//            }
//        }
//            //Sample[] temp = CMSketches[i].query(q[i]);
//            //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
////        }
////        int attrWithoutPred = 0;
////        for (int i = 0; i < q.length; i++) {
////            if (q[i] != -1) { // Find way to not take the -1s into account in query.
////                // temp is Sample[depth][numTwoLHSReps]
////                Sample[][] temp = CMSketches[i].query(q[i]);
////                // want to add to samples per depth, such that we can call unionEstimate
////                if (Main.depth>=0) {
////                    for (int j = 0; j < Main.depth; j++) {
////                        System.arraycopy(temp[j], 0, samples[j*(i - attrWithoutPred)], 0, temp[j].length);
////                    }
////                }
////            } else {
////                attrWithoutPred++;
////            }
////        }
//        return samples;
//    }
//
//    private int queryEstOverAllRows(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        TWOLHS[][] samples = new TWOLHS[numPreds * depth][];
//        // initialize arrays
//        for (int i = 0; i < numPreds * depth; i++) {
//            samples[i] = new TWOLHS[numTwoLHSReps];
//        }
//
//        int attrWithoutPred = 0;
//        for (int i = 0; i < q.length; i++) {
//            if (q[i] != -1) { // Find way to not take the -1s into account in query.
//                TWOLHS[][] temp = CMSketches[i].queryTwoLHS(q[i]);
//                if (depth >= 0) {
//                    for (int j = 0; j < depth; j++) {
//                        System.arraycopy(temp[j], 0, samples[(i - attrWithoutPred) * depth + j], 0, numTwoLHSReps);
//                    }
//                }
//            } else {
//                attrWithoutPred++;
//            }
//            //Sample[] temp = CMSketches[i].query(q[i]);
//            //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
//        }
//        double u = setUnionEstimator((TWOLHS[][]) samples, Main.eps/3); // Est of union size.
//        //System.out.println("u: " + u + ", unionSize: " + unionSize + " diff: " + (u - unionSize));
//        return setIntersectEstimator((TWOLHS[][]) samples, u, Main.eps/3, unionSize, CMRow);
//    }
//
//    private int queryEstPerRow(long[] q, int numPreds, int unionSize, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        TWOLHS[][][] samples = (TWOLHS[][][]) getSamplesTwoLHSPerRow(q, numPreds);
//        // Get estimate per depth. Take minimum of all estimates.
//        int[] estimates = new int[depth];
//        for (int i = 0; i < depth; i++) {
//            double u = setUnionEstimator((TWOLHS[][]) samples[i], Main.eps/3); // Est of union size.
//            estimates[i] = setIntersectEstimator((TWOLHS[][]) samples[i], u, Main.eps/3, unionSize, CMRow);
//        }
//        if (Main.minEstimate) {
//            return minEstimate(estimates, CMRow);
//        } else {
//            return medianEstimate(estimates);
//        }
//
//    }
//    private int minEstimate(int[] estimates, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        int min = Integer.MAX_VALUE;
//        for (int i = 0; i < estimates.length; i ++) {
//            if (estimates[i] < min) {
//                CMRow.setCMRow(i);
//                min = estimates[i];
//
//            }
//        }
//        return min;
//    }
//
//    private int minEstimate(int[] estimates, double[] unionEstimates, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        int min = Integer.MAX_VALUE;
//        for (int i = 0; i < estimates.length; i ++) {
//            if (estimates[i] < min) {
//                CMRow.setCMRow(i);
//                min = estimates[i];
//                if (CMRow.jaccardEstimates.isEmpty() && useTwoLHS) {
//                    System.out.println("Jaccard estimates not set");
//                    throw new IllegalArgumentException("Jaccard estimates not set");
//                }
//                double jaccardEstimate = CMRow.jaccardEstimates.get(i);
//                int witnessEstimate = CMRow.witnessEstimates.get(i);
//                if (jaccardEstimate > 0) {
//                    System.out.println("Jaccard estimate at OmniSketch.minEstimate: " + jaccardEstimate);
//                }
//                CMRow.set2LHS(unionEstimates[i], witnessEstimate, jaccardEstimate);
//
//            }
//        }
//        return min;
//    }
//
//    private int minEstimate(int[] estimates) {
//        int min = Integer.MAX_VALUE;
//        for (int estimate : estimates) {
//            if (estimate < min) {
//                min = estimate;
//            }
//        }
//        return min;
//    }
//    private double[] minEstimateInfo(double[][] estimates) {
//        int min = Integer.MAX_VALUE;
//        double[] minEstimate = new double[4];
//        for (int i = 0; i<estimates.length; i++) {
//            if (estimates[i][0] < min) {
//                min = (int) estimates[i][0];
//                minEstimate = estimates[i];
//            }
//        }
//        return minEstimate;
//    }
//    private int medianEstimate(int[] estimates) {
//        int median = 0;
//        int[] sorted = estimates.clone();
//        Arrays.sort(sorted);
//        if (sorted.length % 2 == 0) {
//            median = (sorted[sorted.length/2] + sorted[sorted.length/2 - 1])/2;
//        } else {
//            median = sorted[sorted.length/2];
//        }
//        return median;
//    }
//
//
//    private boolean identicalSingletonBucket(TWOLHS[] samples, int lsb) {
//        for (TWOLHS sample : samples) {
//            if (!sample.singletonBucket(lsb)) {
//                return false;
//            }
//        }
//        int j = 1;
//        while (j < samples[0].countSignatures[lsb].length) {
//            // Check if no sample has a count of 0 and the other has a count > 0.
//            for (int i = 0; i < samples.length - 1; i++) {
//                for (int k = i + 1; k < samples.length; k++) {
//                    if ((samples[i].countSignatures[lsb][j] > 0) != (samples[k].countSignatures[lsb][j] > 0)) { // true if they contain same sinleton element.
//                        // if one is 0 and the other is not, they are not identical.
//                        // They do not have to have the same count.
//                        return false;
//                    }
//                }
//            }
//            j++;
//        }
//        return true;
//    }
//
//    private boolean singletonUnionBucket(TWOLHS[][] samples, int repetition, int lsb) {
//        // check if union of buckets is singleton.
//        // either one is empty and the other is singleton, or they are identical singleton buckets.
//        // sample must be either empty or must be singleton.
//        ArrayList<TWOLHS> singletonElements = new ArrayList<>();
//        for (TWOLHS[] sample : samples) {
//            if (sample[repetition].singletonBucket(lsb)) {
//                singletonElements.add(sample[repetition]);
//            } else if (!sample[repetition].emptyBucket(lsb)) {
//                return false;
//            }
//        }
//        // Now check if all singleton elements are identical.
//        if (!singletonElements.isEmpty()) {
//            TWOLHS[] singletons =  new TWOLHS[singletonElements.size()];
//            for (int i=0;i<singletonElements.size();i++){
//                singletons[i] = singletonElements.get(i);
//            }
//            return identicalSingletonBucket(singletons, lsb);
//        } else {
//            return false;
//        }
//    }
//
//    private int setUnionEstimator(TWOLHS[][] samples, double eps) {
//        double f =(1 + eps) * numTwoLHSReps / 8;
//        int index =0;
//        int count;
//        while(true) {
//            count = 0;
//            boolean incrCount = false;
//            for (int i = 0; i < numTwoLHSReps; i++) {
//                incrCount = false;
//                for (TWOLHS[] sample : samples) {
//                    if (!sample[i].emptyBucket(index)) {
//                        incrCount = true;
//                        break;
//                    }
//                }
//                if (incrCount) {
//                    count++;
//                }
//            }
//            if (count <= f) {
//                break;
//            } else {
//                index++;
//            }
//        }
//        double phat = (double) (count + 1) /(numTwoLHSReps + 1);//samples.length;
//        double R = pow(2, index + 1);
//        double S = (log(1 - phat)/log(2)) / (log(1 - 1 / R)/log(2));
//        if (twoLHSFast) {
//            return (int) ceil(S) * numTwoLHSReps;
//        } else {
//            return (int) ceil(S);
//        }
//    }
//
//    private int atomicDiffEstimator(TWOLHS[][] samples, double u, double eps, int repetition) {
//        double beta=1.5;
//        int index = max((int) ceil(log((beta * u)/(1-eps))/log(2)), 0);
//        // SingletonUnionBucket with multiple samples.
//        if (!singletonUnionBucket(samples, repetition, index)) {
//            return -1;
//        }
//        // if all are singletons, found witness
//        for (TWOLHS[] sample : samples) {
//            if (!sample[repetition].singletonBucket(index)) {
//                return 0; // no witness found
//            }
//        }
//        return 1; // witness found of intersection.
//    }
//
//    private int[] bucketDiffEstimatorInverseDist(TWOLHS[][] samples, int i, int unionSizeExact) {
//        //int index;
//        int sum=0;
//        int count =0;
//        // Instead of doing it for one index, we want to check every index.
//       for (int index=0; index < samples.length; index++) {
//           if (singletonUnionBucket(samples, i, index)) {
//               boolean witnessFound = true;
//               for (TWOLHS[] sample : samples) {
//                   if (!sample[i].singletonBucket(index)) {
//                       witnessFound = false;
//                   }
//               }
//               if (witnessFound) {
//                   sum++;
//               }
//               count++;
//           }
//       }
//       return new int[]{sum, count};
//    }
//    private int bucketDiffEstimator(TWOLHS[][] samples, double unionEstimate, double eps, int repetition, int unionSizeExact) {
//        int index;
//        if (twoLHSFast) {
//            if (Main.useExactUnionSize) {
//                index = (int) ceil(log((2 * unionSizeExact) / (numTwoLHSReps * pow((1 - eps), 2))) / log(2));
//            } else {
//                index = (int) ceil(log((2 * unionEstimate) / (numTwoLHSReps * pow((1 - eps), 2))) / log(2));
//            }
//        } else {
//            double Beta = 1.5;
//            index =(int) ceil(log((Beta * unionEstimate) / (1 - eps)) / log(2));
//
//        }
//        if (index < 0) {
//            throw new IllegalArgumentException("Index <= 0");
//        }
//        // SingletonUnionBucket with multiple samples.
//        if (!singletonUnionBucket(samples, repetition, index)) {
//            return -1;
//        }
//        // if all are singletons, found witness
//        for (TWOLHS[] sample : samples) {
//            if (!sample[repetition].singletonBucket(index)) {
//                return 0; // no witness found
//            }
//        }
//        return 1; // witness found of intersection.
//    }
//
//    private int[] bucketDiffEstimatorGreedy(TWOLHS[][] samples, double unionEstimate, double eps, int repetition, int unionSizeExact) {
//        int index;
//        int count =0;
//        int sum=0;
//
//        for (int i = 0; i < TWOLHSTmp[0].bitSize; i++) {
//            if (!singletonUnionBucket(samples, repetition, i)) {
//                continue;
//            }
//            boolean witnessFound = true;
//            for (TWOLHS[] sample : samples) {
//                if (!sample[repetition].singletonBucket(i)) {
//                    witnessFound = false;
//                }
//            }
//            if (witnessFound) {
//                sum++;
//            }
//            count++;
//        }
//        return new int[]{sum, count};
//    }
//
//
//    private int setIntersectEstimator(TWOLHS[][] samples, double unionEstimate, double eps,
//                                      int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//
//        if (Main.inverseDistPaperApproach) {
//            return setIntersectEstimatorInverseDist(samples, unionEstimate, eps, unionSizeExact, CMRow);
//        } else {
//            return setIntersectEstimatorTwoLHS(samples, unionEstimate, eps, unionSizeExact, CMRow);
//        }
//
//    }
//
//    private int setIntersectEstimatorInverseDist(TWOLHS[][] samples, double unionEstimate, double eps, int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        int sum = 0;
//        int count = 0;
//        for (int i = 0; i < numTwoLHSReps; i++) {
//            int[] bde = bucketDiffEstimatorInverseDist(samples, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
//            sum += bde[0];
//            count+=bde[1];
//        }
//        if (count == 0){
//            countIsZero++;
//        }
//        double result;
//        if (Main.useExactUnionSize) {
//            result = ceil(((double) sum / count) * unionSizeExact);
//        } else {
//            result = ceil(((double) sum / count) * unionEstimate);
//        }
//        CMRow.addJaccardEstimate((double) sum / count, sum);
//        int resultInt = (int) result;
//        if (resultInt < 0) {
//            System.out.println("Error: result > unionSize");
//            System.exit(1);
//        }
//
//        return resultInt;
//    }
//
//
//
//    private int setIntersectEstimatorTwoLHS(TWOLHS[][] samples, double unionEstimate, double eps,
//                                           int unionSizeExact, AnalysisBaselinesRefactor.QueryInfo CMRow) {
//        int sum = 0;
//        int count = 0;
//        for (int i = 0; i < numTwoLHSReps; i++) {
//            int diff = bucketDiffEstimator(samples, unionEstimate, eps, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
//            if (diff != -1) {
//                sum += diff;
//                count++;
//            }
//        }
//        if (count == 0){
//            countIsZero++;
//        }
//        double result;
//        if (Main.useExactUnionSize) {
//            result = ceil(((double) sum / count) * unionSizeExact);
//        } else {
//            result = ceil(((double) sum / count) * unionEstimate);
//        }
//        CMRow.addJaccardEstimate((double) sum / count, sum);
//        int resultInt = (int) result;
//        if (resultInt < 0) {
//            System.out.println("Error: result > unionSize");
//            System.exit(1);
//        }
//
//        return resultInt;
//    }
//
//    private double[] setIntersectEstimatorInfo(TWOLHS[][] samples, double unionEstimate, double eps, int unionSizeExact) {
//        int sum = 0;
//        int count = 0;
//        for (int i = 0; i < numTwoLHSReps; i++) {
//            int diff = bucketDiffEstimator(samples, unionEstimate, eps, i, unionSizeExact); // atomicDiffEstimator if section 3 of paper.
//            if (diff != -1) {
//                sum += diff;
//                count++;
//            }
//        }
//        if (count == 0){
//            countIsZero++;
//        }
//        double result;
//        if (Main.useExactUnionSize) {
//            result = ceil(((double) sum / count) * unionSizeExact);
//        } else {
//            result = ceil(((double) sum / count) * unionEstimate);
//        }
//        int resultInt = (int) result;
//        if (resultInt < 0) {
//            System.out.println("Error: result > unionSize");
//            System.exit(1);
//        }
//
//        return new double[]{resultInt, sum, count, unionEstimate};
//    }
//
//
//    private int getNmax(Kmin[] samples) {
//        int n_max = 0;
//        for (Kmin kmin : samples) {
//            if (kmin.n > n_max) {
//               n_max = kmin.n;
//            }
//        }
//        return n_max;
//    }
//
//    private double getAltEstKMV(Kmin[] samples) {
//        int numJoins = samples.length;
//        int c = 0;
//        Iterator<Long> iter = samples[0].sketch.iterator();
//        while (iter != null && iter.hasNext()) {
//            boolean found = true;
//            Long i = iter.next();
//            for (int j = 1; j < numJoins; j++) {
//                Long otherElement = samples[j].sketch.ceiling(i);
//                if (otherElement == null) {
//                    found = false;
//                    iter = null;
//                    break;
//                } // not contained
//                else if (otherElement.equals(i)) continue; // is contained
//                else {
//                    iter = samples[0].sketch.tailSet(otherElement).iterator(); // fast forward iter0
//                    found = false;
//                    break; // but now you need to start from iter.hasNext() again
//                }
//            }
//            if (found) c++;
//
//        }
//        return c;
//    }
//
//    private double getAltEstKMV(TreeSet<Long>[] samples) {
//        int numJoins = samples.length;
//        int c = 0;
//        Iterator<Long> iter = samples[0].iterator();
//        while (iter != null && iter.hasNext()) {
//            boolean found = true;
//            Long i = iter.next();
//            for (int j = 1; j < numJoins; j++) {
//                Long otherElement = samples[j].ceiling(i);
//                if (otherElement == null) {
//                    found = false;
//                    iter = null;
//                    break;
//                } // not contained
//                else if (otherElement.equals(i)) continue; // is contained
//                else {
//                    iter = samples[0].tailSet(otherElement).iterator(); // fast forward iter0
//                    found = false;
//                    break; // but now you need to start from iter.hasNext() again
//                }
//            }
//            if (found) c++;
//
//        }
//        return c;
//    }
//
//    public int[][] ns;
//
//    //public int[][] Bs = new int[Main.numStoredAttributes][Main.depth];
//
//    public int getNmaxRange() {
//        int n_max = 0;
//        for (int i = 0; i < numStoredAttributes; i++) {
//            for (int j = 0; j < depth; j++) {
//                if (ns[i][j] > n_max) {
//                    n_max = ns[i][j];
//                }
//            }
//        }
//        return n_max;
//    }
//    public int rangeQuery(long[] minranges, long[] maxranges) {
//        double S_cap = 0;
//        int n_max = 0;
//        int B_virtual = 0;
//        TreeSet<Long>[] samples = new TreeSet[numStoredAttributes * depth];
//        // Empty ns;
//        ns = new int[numStoredAttributes][depth];
//
//        for (int i = 0; i < Main.numAttributes; i++) {
//            //long[][] ranges = getLogRanges(q.lower[i], q.upper[i]);
//            ArrayList<long[]> rangesList = wrapLogRanges(minranges[i + 1], minranges[i + 1]);
//            TreeSet<Long>[] set = new TreeSet[depth];
//            B_virtual = rangesList.size() * maxSize;
//            for (int j = 0; j < rangesList.size(); j++) {
//                 CountMinDyad cm = CMSketchesRange[i][getIndexOfRange(rangesList.get(j))];
//                 TreeSet<Long>[] rangeSet = cm.rangeQuery(rangesList.get(j)[0], rangesList.get(j)[1], ns[i]);
//                 for (int d = 0; d < depth; d++) {
//                    if (set[d] == null) {
//                        set[d] = rangeSet[d];
//                    } else {
//                        set[d].addAll(rangeSet[d]);
//                    }
//                 }
//
//            }
//            if (depth > 0) System.arraycopy(set, 0, samples, i * depth,depth);
//        }
//        n_max = getNmaxRange();
//        S_cap = getAltEstKMV(samples); //Instead of getAltEstKMV
//        double constraint = 3 * log((4 * numStoredAttributes * depth * sqrt(B_virtual))
//                / Main.delta)/(Main.eps * Main.eps);
//        return (int) ceil(S_cap * n_max / B_virtual);
////        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                Math.sqrt(B_virtual)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
////        if (B_virtual >= BConstraint) {
////            q.ratioCondition = true;
////        }
////        q.intersectSize = S_cap;
////        if (S_cap < constraint) {
////            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                    Math.sqrt(B_virtual)) / Main.delta)/(B_virtual * Main.eps * Main.eps)));
////        } else {
////            q.thrm33Case2 = true;
////            return (int) Math.ceil(S_cap * n_max / B_virtual);
////        }
//    }
//
//    private ArrayList<long[]> wrapLogRanges(long low, long up) {
//        ArrayList<long[]> temp;
//        //low += (long) Math.pow(2, Main.dyadicRangeBits - 1);
//        //up += (long) Math.pow(2, Main.dyadicRangeBits - 1);
//        if (low < 0 || up < 0) {
//            System.out.println("Error because low or up < 0: low: " + low + " up: " + up);
//            System.exit(1);
//        }
//        temp = getLogRangesArrList(low + 1, up + 1);
//        for (long[] i: temp) {
//            i[0] = i[0] - 1L;// - (long) Math.pow(2, Main.dyadicRangeBits - 1);
//            i[1] = i[1] - 1L;// - (long) Math.pow(2, Main.dyadicRangeBits - 1);
//        }
//        return temp;
//    }
//
//    private int getIndexOfRange(long[] longs) {
//        // get distance between the two elements in longs.
//        return (int) (log(longs[1] - longs[0] + 1) / log(2));
//    }
//
//    public ArrayList<long[]> getLogRangesArrList(long startInclusive, long stopInclusive) {
//        startInclusive--;stopInclusive--;
//        long initDiff=stopInclusive-startInclusive+1;
//        ArrayList<long[]> result = new ArrayList<>();
//        long totalSum = 0;
//        long pow = 1;
//        for (int j = 0; j <= dyadicRangeBits - 1; j++) {
//            if (startInclusive+pow-1>stopInclusive)
//                break;
//            else if (startInclusive%(pow*2)==0 && startInclusive+pow-1<=stopInclusive) ;
//                // do nothing, increase the power further
//            else {
//                result.add(new long[]{1+startInclusive, 1+startInclusive + pow - 1});
//                totalSum+=pow;
//                startInclusive+=pow;
//            }
//            pow*=2;
//        }
//
//        pow= (long) pow(2,dyadicRangeBits);
//        for (int j=dyadicRangeBits;j>=0;j--) {
//            if (startInclusive%pow==0L && startInclusive+pow-1<=stopInclusive) {
//                result.add(new long[]{1+startInclusive, 1+startInclusive + pow - 1});
//                totalSum+=pow;
//                startInclusive+=pow;
//            }
//            pow/=2;
//        }
//
//        if (totalSum != initDiff) {
//            System.err.println("Error - no full coverage");
//        }
//        return result;
//    }
//
//    public int getFilledKSamples() {
//        int count = 0;
//        for (int i = 0; i < numStoredAttributes; i++) {
//            count += CMSketches[i].getFilledKSamples();
//        }
//        return count;
//    }
//
//    public long getMemoryUsage() {
//        long maxMemoryUsage;
//        maxMemoryUsage = (long) this.depth * this.width * (long) numStoredAttributes;
//        if (rangeQueries) {
//            if (useTwoLHS) {
//                maxMemoryUsage = maxMemoryUsage * ((
//                        ((long) numTwoLHSReps * CMSketches[0].CMTwoLHS[0][0][0].bitSize * (
//                                CMSketches[0].CMTwoLHS[0][0][0].bitSize + 1) * 32)) * (dyadicRangeBits + 1) * 32);
//            } else {
//                maxMemoryUsage = maxMemoryUsage * (
//                        maxSize * (dyadicRangeBits + 1) * (b + 3 *32 + 1) + 32);
//            }
//        } else {
//            if (useTwoLHS) {
//                maxMemoryUsage = maxMemoryUsage * (
//                        ((long) numTwoLHSReps * CMSketches[0].CMTwoLHS[0][0][0].bitSize * (
//                                CMSketches[0].CMTwoLHS[0][0][0].bitSize + 1) * 32));
//            } else {
//                maxMemoryUsage = maxMemoryUsage * Formulas.ramSingleKmin(maxSize,b);
//                        //(maxSize * (b + 3 *32 + 1) + 32));
//            }
//        }
//
//        long memoryUsage = 0;
//        long memUsageSketch = 0;
//        long memUsageArray = 0;
//        long totalSavedByArrays = 0;
//        long totalSavedByMaxBits = 0;
//        for (int i = 0; i < numStoredAttributes; i++) {
//            if (rangeQueries) {
//                for (int j = 0; j < dyadicRangeBits + 1; j++) {
//                    if (maxBits[i] + 1 < dyadicRangeBits - j) { // no need to store empty ranges
//                        totalSavedByMaxBits += CMSketchesRange[i][j].getMemoryUsage();
//                            //System.out.println("No need to store empty range " + (Main.dyadicRangeBits - j) + " for attribute " + i);
//                        continue;
//                    }
//                    memUsageSketch = CMSketchesRange[i][j].getMemoryUsage();
//                    memUsageArray  = getMemUsageArray(j);
//                    if (memUsageArray < memUsageSketch) {
//                        totalSavedByArrays += memUsageSketch - memUsageArray;
//                        /*System.out.println("Array is better for attribute "
//                                + i + " and range " + j + " by "
//                                + (memUsageSketch - memUsageArray) + " bits");
//                        System.out.println("Array: " + memUsageArray + " bits");
//                        System.out.println("Sketch: " + memUsageSketch + " bits");*/
//                    }
//                    memoryUsage += min(memUsageArray, memUsageSketch);
//                }
//            } else {
//                memoryUsage += CMSketches[i].getMemoryUsage();
//            }
//        }
//        //System.out.println("Total saved by arrays: " + totalSavedByArrays + " bits");
//        //System.out.println("Total saved by max bits: " + totalSavedByMaxBits + " bits");
//
//        if (memoryUsage > maxMemoryUsage) {
//            System.err.println("Memory usage is " + (memoryUsage) + " bytes and max memory usage is " + maxMemoryUsage + " bytes");
//        }
//        memUsageSynopsis = memoryUsage;
//        return memoryUsage;
//    }
//
//    private long getMemUsageArray(int j) {
//        long memUsageArray = 0;
//
//        long C = (long) pow(2, j);
//        memUsageArray = C * maxSize * (b + 3 * 32 + 1) + 32;
//        return memUsageArray;
//    }
//
//    public void reset() {
//        for (int i = 0; i < numStoredAttributes; i++) {
//            if (rangeQueries) {
//                for (int j = 0; j < dyadicRangeBits + 1; j++) {
//                    CMSketchesRange[i][j].reset();
//                }
//            } else {
//                CMSketches[i].reset();
//            }
//            }
//        }
//
//
//}
//
/////public int queryLeaveOneOut(Query q) {
////        double S_cap = 0;
////        int n_max = 0;
////        Sample[] samples = new Kmin[q.predAttrs.size() * (Main.depth - 1)];
////        if (Main.useDS) {
////            throw new RuntimeException("Not implemented");
////        } else {
////            for (int i = 0; i < q.predAttrs.size(); i++) {
////
////                Sample[] temp = CMSketches[q.predAttrs.get(i)].query(q.getRecord()[q.predAttrs.get(i)]);
////                // get the sample with highest n
////                int maxN = 0;
////                int maxNIndex = 0;
////                for (int j = 0; j < temp.length; j++) {
////                    if (temp[j].n > maxN) {
////                        maxN = temp[j].n;
////                        maxNIndex = j;
////                    }
////                }
////                // Copy every sample except the one with highest n
////                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 1), maxNIndex);
////                if (Main.depth >= 0) System.arraycopy(temp, maxNIndex + 1, samples, i * (Main.depth - 1) + maxNIndex,
////                        Main.depth - maxNIndex - 1);
////
////                //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
////            }
////            // At every attribute, leave one out that has highest n
////            n_max = getNmax((Kmin[]) samples);
////            S_cap = getAltEstKMV((Kmin[]) samples);;
////        }
////        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(Main.maxSize))
////                / Main.delta)/(Main.eps * Main.eps);
////
////        q.case2Estimate = Math.ceil(S_cap * n_max / Main.maxSize);
////        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                Math.sqrt(Main.maxSize)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
////        if (Main.maxSize >= BConstraint) {
////            q.ratioCondition = true;
////        }
////        q.intersectSize = S_cap;
////        if (S_cap < constraint) {
////            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                    Math.sqrt(Main.maxSize)) / Main.delta)/(Main.maxSize * Main.eps * Main.eps)));
////        } else {
////            q.thrm33Case2 = true;
////            return (int) Math.ceil(S_cap * n_max / Main.maxSize);
////        }
////
////    }
////    public int queryLeaveTwoOut(Query q) {
////        double S_cap = 0;
////        int n_max = 0;
////        Sample[] samples = new Kmin[q.predAttrs.size() * (Main.depth - 2)];
////        if (Main.useDS) {
////            throw new RuntimeException("Not implemented");
////        } else {
////            for (int i = 0; i < q.predAttrs.size(); i++) {
////
////                Sample[] temp = CMSketches[q.predAttrs.get(i)].query(q.getRecord()[q.predAttrs.get(i)]);
////                // get the sample with highest n
////                int maxN = 0;
////                int maxNIndex = 0;
////                for (int j = 0; j < temp.length; j++) {
////                    if (temp[j].n > maxN) {
////                        maxN = temp[j].n;
////                        maxNIndex = j;
////                    }
////                }
////                // find the second highest n
////                int secondMaxN = 0;
////                int secondMaxNIndex = 0;
////                for (int j = 0; j < temp.length; j++) {
////                    if (temp[j].n > secondMaxN && j != maxNIndex) {
////                        secondMaxN = temp[j].n;
////                        secondMaxNIndex = j;
////                    }
////                }
////                // Copy every sample except the one with highest n or second highest n
////                int minIndex = Math.min(maxNIndex, secondMaxNIndex);
////                int maxIndex = Math.max(maxNIndex, secondMaxNIndex);
////
////                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 2), minIndex);
////                if (Main.depth >= 0) System.arraycopy(temp, minIndex + 1, samples,
////                        i * (Main.depth - 2) + minIndex,maxIndex - minIndex - 1);
////                if (Main.depth >= 0) System.arraycopy(temp, maxIndex + 1, samples,
////                        i * (Main.depth - 2) + (maxIndex - 1), Main.depth - 1 - maxIndex);
//////                if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * (Main.depth - 1), maxNIndex);
//////                if (Main.depth >= 0) System.arraycopy(temp, maxNIndex + 1, samples, i * (Main.depth - 1) + maxNIndex,
//////                        Main.depth - maxNIndex - 1);
////
////                //if (Main.depth >= 0) System.arraycopy(temp, 0, samples, i * Main.depth, Main.depth);
////            }
////            // At every attribute, leave one out that has highest n
////            n_max = getNmax((Kmin[]) samples);
////            S_cap = getAltEstKMV((Kmin[]) samples);;
////        }
////        double constraint = 3 * Math.log((4 * q.predAttrs.size() * Main.depth * Math.sqrt(Main.maxSize))
////                / Main.delta)/(Main.eps * Main.eps);
////
////        q.case2Estimate = Math.ceil(S_cap * n_max / Main.maxSize);
////        double BConstraint =  2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                Math.sqrt(Main.maxSize)) / Main.delta)/(q.exactAnswer * Main.eps * Main.eps);
////        if (Main.maxSize >= BConstraint) {
////            q.ratioCondition = true;
////        }
////        q.intersectSize = S_cap;
////        if (S_cap < constraint) {
////            return (int) (Math.ceil(2 * n_max * Math.log((4 * q.predAttrs.size() * Main.depth *
////                    Math.sqrt(Main.maxSize)) / Main.delta)/(Main.maxSize * Main.eps * Main.eps)));
////        } else {
////            q.thrm33Case2 = true;
////            return (int) Math.ceil(S_cap * n_max / Main.maxSize);
////        }
////
////    }
//endregion
package omni.synopses.omniFactory.utils;

import omni.datasets.Record.Query;
import omni.synopses.omniFactory.CustomPriorityQueue.PriorityQueue;
import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.SampleTypes.Sample;

import java.util.*;

public class KminUtils {
    public static int intersectAndScale(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        if (kminArray == null || kminArray.length == 0) {
            throw new IllegalArgumentException("kminArray is null or empty");
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminPQ")) {
            return estimatePriorityQueue(kminArray, queryInfo, bound, numPreds);
        }
        if (Objects.equals(kminArray[0].getKminType(), "KminArray") || Objects.equals(kminArray[0].getKminType(), "KminArrayRegBuffer")) {
            return estimateArray(kminArray, queryInfo, bound, numPreds);
        }
        if (Objects.equals(kminArray[0].getKminType(), "KminArrayWithBuffer")) {
            return estimateArray(kminArray, queryInfo, bound, numPreds);
        }
        if (Objects.equals(kminArray[0].getKminType(), "KminArrayWithoutBuffer")) {
            return estimateArray(kminArray, queryInfo, bound, numPreds);
        }
        if ((Objects.equals(kminArray[0].getKminType(), "KminSimpleBuffer")
                || Objects.equals(kminArray[0].getKminType(), "KminSimpleBufferNoDeleteBuffer"))) {
            return estimateArray(kminArray, queryInfo, bound, numPreds);
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminPQOptimized")) {
            return estimatePriorityQueueOptimized(kminArray, queryInfo, bound, numPreds);
        }
        if (Objects.equals(kminArray[0].getKminType(), "KminPQOptimizedOnlyNew")) {
            return estimatePriorityQueueOptimizedOnlyNew(kminArray, queryInfo, bound, numPreds);
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminHashSet")) {
            return intersectHashSet((HashSet[]) kminArray);
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminTreeSet")) {
            return estimateTreeSet(kminArray, queryInfo, bound, numPreds);
        }
        if (Objects.equals(kminArray[0].getKminType(), "KminTreeSetWithoutBuffer")) {
            return estimateTreeSet(kminArray, queryInfo, bound, numPreds);
        }
        throw new IllegalArgumentException("Unsupported kmin type: " + kminArray[0].getKminType());
    }

    private static int intersectHashSet(HashSet[] kminArray) {
        throw new UnsupportedOperationException("HashSet intersection not implemented yet");
    }

    public static int estimateTreeSet(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        int S_cap;
        int[] n_max;
        TreeSet<Integer>[] flatSamples = new TreeSet[kminArray.length];
        for (int i = 0; i < kminArray.length; i++) {
            flatSamples[i] = (TreeSet<Integer>) kminArray[i].query();
        }

        n_max= getNmax(kminArray);
        S_cap = intersectionTreeSet(flatSamples);
        queryInfo.setScap(S_cap, n_max[0], n_max[1], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[1]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        return (int) ((long) S_cap * n_max[0] / n_max[1]);
    }

    private static int intersectionTreeSet(TreeSet<Integer>[] samples) {
            int numJoins = samples.length;
            int c = 0;
            Iterator<Integer> iter = samples[0].iterator();
            while (iter != null && iter.hasNext()) {
                boolean found = true;
                Integer i = iter.next();
                for (int j = 1; j < numJoins; j++) {
                    Integer otherElement = samples[j].ceiling(i);
                    if (otherElement == null) {
                        found = false;
                        iter = null;
                        break;
                    } // not contained
                    else if (otherElement.equals(i)) {
                    } // is contained
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

    private static int estimatePriorityQueue(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        // Implement the logic to intersect and scale PriorityQueue kminArray
        int S_cap;
        //queryInfo.expSetting.setNumSets(kminArray.length);
        PriorityQueue[] flatSamples = new PriorityQueue[kminArray.length];
        for (int i = 0; i < kminArray.length; i++) {
//            if (kminArray[i].getN() < queryInfo.expSetting.getIntersectionSize()) {
//                throw new IllegalArgumentException("N is less than intersection size for kminArray[" + i + "]");
//            }
//            queryInfo.expSetting.setSetInfo(i, kminArray[i].getN(), kminArray[i].getCurSampleSize());
            flatSamples[i] = (PriorityQueue) kminArray[i].query();
        }

        int[] n_max = getNmax(kminArray);
        S_cap = intersectionPQ(flatSamples);

        queryInfo.setScap(S_cap, n_max[0], n_max[1], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[1]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        return (int) ((long) S_cap * n_max[0] / n_max[1]);
    }

    public static int estimateArray(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        // Implement the logic to intersect and scale kminArray
        int S_cap;
        int[] n_max;

        int[][] flatSamples = new int[kminArray.length][];
        for (int i = 0; i < kminArray.length; i++) {
            flatSamples[i] = (int[]) kminArray[i].query();
        }

        n_max = getNmax(kminArray);
        S_cap = intersectionArray(flatSamples);

        queryInfo.setScap(S_cap, n_max[0], n_max[1], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[1]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        return (int) ((long) S_cap * n_max[0] / n_max[1]);
    }

    public static int estimateSetExact(Sample[] kminSets, QueryInfo queryInfo) {
        if (kminSets == null || kminSets.length == 0) {
            throw new IllegalArgumentException("kminSets is null or empty");
        }

        if (Objects.equals(kminSets[0].getKminType(), "ExactSolution")) {
            int intersection;
            Set<Integer> intersectionSet = new HashSet<>((Set<Integer>) kminSets[0].query());

            for (int i = 1; i < kminSets.length; i++) {
                intersectionSet.retainAll((Set<Integer>) kminSets[i].query());
            }

            intersection = intersectionSet.size();
            queryInfo.setScap(intersection, kminSets[0].getN(), kminSets[0].getCurSampleSize(), false);
            return intersection;
        } else {
            throw new IllegalArgumentException("Unsupported kmin type: " + kminSets[0].getKminType());
        }
    }

    private static int intersectionArray(int[][] arrays) {
        if (arrays == null || arrays.length == 0) return 0;

        if (arrays.length == 1) {
            return arrays[0].length; // If there's only one array, return its length
        }

        // Find the index of the shortest array
        int minIndex = 0;
        for (int i = 1; i < arrays.length; i++) {
            if (arrays[i].length < arrays[minIndex].length) {
                minIndex = i;
            }
        }

        int[] base = arrays[minIndex];
        int count = 0;
        int prev = Integer.MIN_VALUE;

        for (int i = 0; i < base.length; i++) {
            int val = base[i];

            // Skip duplicates in base array
            if (i > 0 && val == prev) continue;
            prev = val;

            boolean presentInAll = true;
            for (int j = 0; j < arrays.length; j++) {
                if (j == minIndex) continue;
                if (Arrays.binarySearch(arrays[j], val) < 0) { //TODO: we can search less space by using iterator for the non-base-sets.
                    presentInAll = false;
                    break;
                }
            }

            if (presentInAll) {
                count++;
            }
        }

        return count;
    }


    private static int estimatePriorityQueueOptimized(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        // Implement the logic to intersect and scale PriorityQueue kminArray
        int S_cap;
        int[] n_max = new int[4];
        PriorityQueue[] flatSamples = new PriorityQueue[kminArray.length];
        for (int i = 0; i < kminArray.length; i++) {
            flatSamples[i] = (PriorityQueue) kminArray[i].query();
        }

        int[] nres = getNCountUsingB(kminArray);
        n_max[0] = nres[0];
        n_max[1] = nres[1];
        n_max[2] = nres[2];
        int[] temp = intersectionPQCountNewB(flatSamples, nres[4]);
        S_cap = temp[0];
        n_max[3] = temp[1];

        queryInfo.setScap(S_cap, n_max[0], n_max[1], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[1]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        int estimate1 = (int) ((long) S_cap * n_max[0] / n_max[1]);
        int estimate2 = (int) ((long) S_cap * n_max[2] / n_max[3]);
        return Math.min(estimate1, estimate2);
    }

    private static int estimatePriorityQueueOptimizedOnlyNew(Sample[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        // Implement the logic to intersect and scale PriorityQueue kminArray
        int S_cap;
        int[] n_max = new int[4];
        PriorityQueue[] flatSamples = new PriorityQueue[kminArray.length];
        for (int i = 0; i < kminArray.length; i++) {
            flatSamples[i] = (PriorityQueue) kminArray[i].query();
        }

        int[] nres = getNCountUsingB(kminArray);
        n_max[0] = nres[0];
        n_max[1] = nres[1];
        n_max[2] = nres[2];
        int[] temp = intersectionPQCountNewB(flatSamples, nres[4]);
        S_cap = temp[0];
        n_max[3] = temp[1];

        queryInfo.setScap(S_cap, n_max[2], n_max[3], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[3]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        return (int) ((long) S_cap * n_max[2] / n_max[3]);
    }

    private static int[] getNmax(Sample[] kminArray) {
        int[] nmax = new int[2];
        for (Sample kmin : kminArray) {
            if (kmin.getN() > nmax[0]) {
                nmax[0] = kmin.getN();
                nmax[1] = kmin.getCurSampleSize();
                if (nmax[1] == 0) {
                    System.out.println(kmin.getCurSampleSize());
                }
            }
        }
        return nmax;
    }

    private static int[] getNCountUsingB(Sample[] kminArray) {
        int[] nmin = new int[5];
        nmin[0] = Integer.MIN_VALUE;
        nmin[2] = Integer.MAX_VALUE;
        for (int i = 0; i < kminArray.length; i++) {
            Sample kmin = kminArray[i];
            if (kmin.getN() > nmin[0]) {
                nmin[0] = kmin.getN();
                nmin[1] = kmin.getCurSampleSize();
            }
            if (kmin.getN() < nmin[2]) {
                nmin[2] = kmin.getN();
                nmin[3] = kmin.getCurSampleSize();
                nmin[4] = i;
            }
        }
        return nmin;
    }


    private static int intersectionPQ(omni.synopses.omniFactory.CustomPriorityQueue.PriorityQueue[] samples) {
        int numJoins = samples.length;
        if (samples.length == 1) {
            return samples[0].size + 1; // One predicate and either per row or one row.
        }
        int intersectionCount = 0;

        Arrays.sort(samples, Comparator.comparingInt(PriorityQueue::size));

        // Array to track the current value from each iterator
        int[] currentValues = new int[numJoins];

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
            int minCurrent = currentValues[0];
            for (int i = 1; i < numJoins; i++) { // We can skip the first one, since we just set it to minCurrent.
                if (currentValues[i] < minCurrent) {
                    minCurrent = currentValues[i];
                }
            }

            // Check if all current values match the maxCurrent
            boolean allMatch = true;
            for (int i = 0; i < numJoins; i++) {
                if (currentValues[i] != minCurrent) {
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


    private static int[] intersectionPQCountNewB(PriorityQueue[] samples, int index_min_n) {
        int numJoins = samples.length;
        if (samples.length == 1) {
            return new int[]{samples[0].size + 1, samples[0].size + 1}; // One predicate and either per row or one row.
        }
        int intersectionCount = 0;
        int n_skipped_in_min_n = 0;
        int old_B = samples[index_min_n].size;
        int min_n_value = samples[index_min_n].peek();

        // Sort the samples based on their size
        Arrays.sort(samples, Comparator.comparingInt(PriorityQueue::size));

        // get new index of min_n
        int index_min_n_new = 0;
        // Array to track the current value from each iterator
        int[] currentValues = new int[numJoins];
        int initial_min_current = Integer.MAX_VALUE;
        // Initialize each queue's current value
        for (int i = 0; i < numJoins; i++) {
            if (samples[i].size == old_B && samples[i].peek() == min_n_value) {
                index_min_n_new = i;
            }
            if (!samples[i].isEmpty()) {
                currentValues[i] = samples[i].peek();
                if (currentValues[i] < initial_min_current) {
                    initial_min_current = currentValues[i];
                }
            } else {
                return new int[]{0, 1}; // If any queue is empty, intersection count is zero
            }
        }

        // Update index_min_n to the new index
        index_min_n = index_min_n_new;
        old_B++; // size starts with -1


        int first_iterator = 0;
        while (true) {
            // Find the maximum value among the current values
            int minCurrent = currentValues[0];
            for (int i = 1; i < numJoins; i++) { // We can skip the first one, since we just set it to minCurrent.
                if (currentValues[i] < minCurrent) {
                    minCurrent = currentValues[i];
                }
            }


            // Check if all current values match the maxCurrent
            boolean allMatch = true;
            for (int i = 0; i < numJoins; i++) {
                if (currentValues[i] != minCurrent) {
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
                        return new int[]{intersectionCount, old_B - n_skipped_in_min_n}; // End if any queue is exhausted
                    }
                }
            } else {
                // Advance only iterators of queues with current values < maxCurrent
                for (int i = 0; i < numJoins; i++) {
                    while (currentValues[i] > minCurrent) {
                        samples[i].poll();
                        if (first_iterator == 0 && i == index_min_n && currentValues[i] > initial_min_current) { // count the number of skipped elements in the min_n
                            n_skipped_in_min_n++;
                        }
                        if (!samples[i].isEmpty()) {
                            currentValues[i] = samples[i].peek();
                        } else {
                            return new int[]{intersectionCount, old_B - n_skipped_in_min_n}; // End if any queue is exhausted
                        }
                    }
                }
            }
            first_iterator++;
        }
    }

}

package omni.omniFactory.utils;

import omni.PriorityQueue.PriorityQueue;
import omni.QueryInfo;
import omni.omniFactory.KminTypes.Kmin;
import omni.omniFactory.OmniSketchConfig;

import java.util.*;

public class KminUtils {
    public static int intersectAndScale(Kmin[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        if (kminArray == null || kminArray.length == 0) {
            throw new IllegalArgumentException("kminArray is null or empty");
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminPQ")) {
            return estimatePriorityQueue(kminArray, queryInfo, bound, numPreds);
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminHashSet")) {
            return intersectHashSet((HashSet[]) kminArray);
        }

        if (Objects.equals(kminArray[0].getKminType(), "KminTreeSet")) {
            return intersectTreeSet((TreeSet[]) kminArray);
        }
        throw new IllegalArgumentException("Unsupported kmin type: " + kminArray[0].getKminType());
    }

    private static int intersectHashSet(HashSet[] kminArray) {
        throw new UnsupportedOperationException("HashSet intersection not implemented yet");
    }

    private static int intersectTreeSet(TreeSet[] kminArray) {
        throw new UnsupportedOperationException("TreeSet intersection not implemented yet");
    }

    private static int estimatePriorityQueue(Kmin[] kminArray, QueryInfo queryInfo, double bound, int numPreds) {
        // Implement the logic to intersect and scale PriorityQueue kminArray
        int S_cap = 0;
        int[] n_max;
        PriorityQueue[] flatSamples = new PriorityQueue[kminArray.length];
        for (int i = 0; i < kminArray.length; i++) {
            flatSamples[i] = (PriorityQueue) kminArray[i].query();
        }

        n_max = getNmax(kminArray);
        S_cap = intersectionPQ(flatSamples);

        queryInfo.setScap(S_cap, n_max[0], n_max[1], false);
        bound = Math.ceil(bound + Math.log(Math.sqrt(n_max[1]) * numPreds));
        if (S_cap < bound) {
            queryInfo.case1 = true;
        }
        return (int) ((long) S_cap * n_max[0] / n_max[1]);
    }

    private static int[] getNmax(Kmin[] kminArray) {
        int[] nmax = new int[2];
        for (Kmin kmin : kminArray) {
            if (kmin.getN() > nmax[0]) {
                nmax[0] = kmin.getN();
                nmax[1] = kmin.getCurSampleSize();
            }
        }
        return nmax;
    }

    private static int intersectionPQ(omni.PriorityQueue.PriorityQueue[] samples) {
        int numJoins = samples.length;
        if (samples.length == 1) {
            return samples[0].size + 1; // One predicate and either per row or one row.
        }
        int intersectionCount = -1;

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

}

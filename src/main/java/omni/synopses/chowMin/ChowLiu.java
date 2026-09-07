package omni.synopses.chowMin;

import omni.Config;
import omni.Experiments.RunExperiments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ChowLiu {

    public static class EdgeWithWeight {
        int u, v;
        double weight;
        EdgeWithWeight(int u, int v, double w) {
            this.u = u;
            this.v = v;
            this.weight = w;
        }
    }

    static class DSU {
        int[] parent, rank;
        DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }
        boolean union(int u, int v) {
            int rx = find(u), ry = find(v);
            if (rx == ry) return false;
            if (rank[rx] < rank[ry]) parent[rx] = ry;
            else if (rank[rx] > rank[ry]) parent[ry] = rx;
            else { parent[ry] = rx; rank[rx]++; }
            return true;
        }
    }

    /**
     * Build maximum spanning tree edges using MI matrix.
     * @param MI symmetric matrix of mutual information.
     * @return list of edges (u,v) in the Chow–Liu tree.
     */
    public static List<Edge> chowLiu(double[][] MI, int numAttributes) {
        List<EdgeWithWeight> edges = new ArrayList<>();

        for (double[] doubles : MI) {
            edges.add(new EdgeWithWeight((int) doubles[0], (int) doubles[1], doubles[2]));
        }
        // sort by descending MI
        edges.sort((a, b) -> Double.compare(b.weight, a.weight));

        DSU dsu = new DSU(numAttributes);
        List<Edge> tree = new ArrayList<>();

        for (EdgeWithWeight e : edges) {
            if (dsu.union(e.u, e.v)) {
                tree.add(new  Edge(e.u, e.v));
                if (tree.size() == numAttributes - 1) break;
            }
        }
        return tree;
    }

    public static List<Edge> chowLiuWithPredicates(double[][] MI, Set<Integer> active, int numAttributes) {
        List<EdgeWithWeight> edges = new ArrayList<>();

        for (double[] doubles : MI) {
            if (active.contains((int) doubles[0]) && active.contains((int) doubles[1]))
                edges.add(new EdgeWithWeight((int) doubles[0], (int) doubles[1], doubles[2]));
        }
        // sort by descending MI
        edges.sort((a, b) -> Double.compare(b.weight, a.weight));

        DSU dsu = new DSU(numAttributes);
        List<Edge> tree = new ArrayList<>();

        if (edges.size() ==1) {
            tree.add(new Edge(edges.get(0).u, edges.get(0).v));
            return tree;
        }
        for (EdgeWithWeight e : edges) {
            if (dsu.union(e.u, e.v)) {
                tree.add(new  Edge(e.u, e.v));
                if (tree.size() == numAttributes - 1) break;
            }
        }
        return tree;
    }


    static List<Edge> computeChowLiu(double[][] MIs, int numAttributes) {
        // goal: compute exact chow liu tree.

        List<Edge> chowLiuTree = chowLiu(MIs, numAttributes);
        for (Edge e : chowLiuTree) {
            System.out.printf("Edge (%d, %d)", e.getS(), e.getT());
        }
        // get max span tree for ChowLiu:
        return chowLiuTree;

    }

    public static List<Edge> computeChowLiuWithPredicates(double[][] MIs, Set<Integer> active, int numAttributes) {
        // goal: compute exact chow liu tree.
        List<Edge> chowLiuTree = chowLiuWithPredicates(MIs, active, numAttributes);
        for (Edge e : chowLiuTree) {
            System.out.printf("Edge (%d, %d)", e.getS(), e.getT());
        }
        // get max span tree for ChowLiu:
        return chowLiuTree;

    }

    public static double[][] computeExactMIsFromDisk(RunExperiments runExperiments, Config config, int numAttributes)
            throws IOException {
        // For each pair of columns (i,j), store:
        //   count(x_i, x_j)
        //
        // pairCounts[i][j] is only used for j > i.
//        @SuppressWarnings("unchecked")
        HashMap<LongPair, Long>[][] pairCounts =
                new HashMap[numAttributes][numAttributes];

        // Marginal counts for each column.
//        @SuppressWarnings("unchecked")
        HashMap<Long, Long>[] marginalCounts =
                new HashMap[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            marginalCounts[i] = new HashMap<>();
            for (int j = i + 1; j < numAttributes; j++) {
                pairCounts[i][j] = new HashMap<>();
            }
        }

        long N = 0;

        long numUpdates = 0;
        long numDeletes = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(runExperiments.cd.datasetReaderName))) {

            br.readLine(); // skip header

            String line;

            while ((line = br.readLine()) != null) {

                String[] values = line.split(",");

                long[] record = new long[config.numStoredAttributes + 1];

                for (int i = 0; i < record.length; i++) {
                    record[i] = Long.parseLong(values[i]);
                }

                int sign = Integer.parseInt(values[values.length -1]);

                if (sign != 1 && sign != -1) {
                    throw new RuntimeException("Invalid sign value: " + sign);
                }

                // Update marginals
                for (int i = 0; i < numAttributes; i++) {

                    long value = record[i];

                    HashMap<Long, Long> counts = marginalCounts[i];

                    long oldCount = counts.getOrDefault(value, 0L);
                    long newCount = oldCount + sign;

                    if (newCount == 0) {
                        counts.remove(value);
                    } else {
                        counts.put(value, newCount);
                    }
                }

                // Update joint counts
                for (int i = 0; i < numAttributes; i++) {
                    for (int j = i + 1; j < numAttributes; j++) {

                        LongPair pair = new LongPair(record[i], record[j]);

                        HashMap<LongPair, Long> counts = pairCounts[i][j];

                        long oldCount = counts.getOrDefault(pair, 0L);
                        long newCount = oldCount + sign;

                        if (newCount == 0) {
                            counts.remove(pair);
                        } else {
                            counts.put(pair, newCount);
                        }
                    }
                }

                N += sign;

                if (sign == 1) {
                    numUpdates++;
                } else {
                    numDeletes++;
                }

                if ((numUpdates + numDeletes) % 1_000_000 == 0) {
                    double progress =
                            (double) (numUpdates + numDeletes)
                                    / (numUpdates + numDeletes) * 100.0;

                    System.out.printf(
                            "\rProcessed %,d records (updates %,d, deletes %,d)",
                            numUpdates + numDeletes,
                            numUpdates,
                            numDeletes
                    );
                }
            }
        }

        System.out.println();
        System.out.println("Updates: " + numUpdates);
        System.out.println("Deletes: " + numDeletes);
        System.out.println("Final N: " + N);

        if (N <= 0) {
            throw new IllegalStateException("Final dataset is empty.");
        }
        int numPairs = numAttributes * (numAttributes - 1) / 2;

        double[][] MIs = new double[numPairs][3];

        int count = 0;

        for (int i = 0; i < numAttributes; i++) {
            for (int j = i + 1; j < numAttributes; j++) {

                double mi = computeMI(
                        pairCounts[i][j],
                        marginalCounts[i],
                        marginalCounts[j],
                        N
                );

                MIs[count] = new double[]{i, j, mi};

                System.out.println(
                        "MI " + i + " , " + j + " = " + mi
                );

                count++;
            }
        }

        return MIs;
    }

    public static class LongPair {

        private final long first;
        private final long second;

        public LongPair(long first, long second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof LongPair)) {
                return false;
            }

            LongPair other = (LongPair) o;

            return first == other.first
                    && second == other.second;
        }

        @Override
        public int hashCode() {
            return 31 * Long.hashCode(first) + Long.hashCode(second);
        }
    }

    private static double computeMI(
            HashMap<LongPair, Long> jointCounts,
            HashMap<Long, Long> v1Counts,
            HashMap<Long, Long> v2Counts,
            long N) {

        double mi = 0.0;

        for (Map.Entry<LongPair, Long> entry : jointCounts.entrySet()) {

            LongPair pair = entry.getKey();

            long jointCount = entry.getValue();

            long v1Value = pair.first;
            long v2Value = pair.second;

            long v1Count = v1Counts.get(v1Value);
            long v2Count = v2Counts.get(v2Value);

            double p_v1_v2 = (double) jointCount / N;
            double p_v1 = (double) v1Count / N;
            double p_v2 = (double) v2Count / N;

            mi += p_v1_v2
                    * Math.log(p_v1_v2 / (p_v1 * p_v2))
                    / Math.log(2);
        }

        return mi;
    }

    // --- Example usage ---
    public static void main(String[] args) {
        // Example MI matrix (symmetric, zero diagonal)
        double[][] MI = {
                {0, 0.90475275, 1.73688301, 2.70838433},
                {0.90475275, 0, 0.24704629, 0.39246054},
                {1.73688301, 0.24704629, 0, 0.75886671},
                {2.70838433, 0.39246054, 0.75886671, 0}
        };

        List<Edge> tree = chowLiu(MI, 4);
        for (Edge e : tree) {
            System.out.printf("Edge (%d, %d)", e.getS(), e.getT());
        }
    }
}
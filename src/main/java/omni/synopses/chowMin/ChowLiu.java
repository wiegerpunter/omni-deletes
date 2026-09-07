package omni.synopses.chowMin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
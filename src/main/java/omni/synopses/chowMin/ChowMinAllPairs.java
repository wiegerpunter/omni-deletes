package omni.synopses.chowMin;

import omni.Experiments.utils.QueryInfo;
import omni.datasets.Record.Record;
import omni.datasets.Record.StringRecord;
import omni.synopses.SynopsisRefactor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

import static omni.synopses.chowMin.TWODCM.mix;


public class ChowMinAllPairs extends SynopsisRefactor {
    private static int N = 0;
    /*
         Pairwise summary for edges in ChowLiu tree. plus currently a single CM for every attribute.
         Pairwise summary updates counter at cell [h_x(x), h_y(y)].
         */
    TWODCM[] sketches;
    private final int numAttributes;
    private final int depth;
    HashMap<Integer, Integer> widthMap;
    private final int[][] seeds;
    private final int[][] hashes;
    private double[][] MIs;
    final public String[] columns;

    public ChowMinAllPairs(long ram, int numAttributes, int[] params) {
        this.columns = makeColumnsSingleTable(numAttributes);
        this.ram = ram;
        this.numAttributes = numAttributes;
        this.parameters = params;
        this.depth = params[0];
        this.widthMap = makeWidthMapSingleTable(numAttributes, params[1]); // only when using joins

        sketches = new TWODCM[numAttributes*(numAttributes-1)/2];
        for (int a1 = 0; a1 < numAttributes-1; a1++) { // summary for every pair in upper triangle
            for (int a2 = a1+1; a2<numAttributes; a2++) {
                sketches[sketch_idx(a1, a2)] = new TWODCM(this.depth, this.widthMap.get(a1), this.widthMap.get(a2), a1, a2,
                        getAttrSpecHashCode(a1, true), getAttrSpecHashCode(a2, true));
            }
        }
        seeds = new int[numAttributes][this.depth];
        for (int i = 0; i < numAttributes; i++) {
            int attrSpecSeed = getAttrSpecHashCode(i,true);
            for (int j = 0; j < seeds[i].length; j++) {
                seeds[i][j] = (j+1)*attrSpecSeed;
            }
        }
        hashes = new int[numAttributes][this.depth];
    }

    private HashMap<Integer, Integer> makeWidthMapSingleTable(int numAttributes, int width) {
        widthMap = new HashMap<>();
        for (int i = 0; i < numAttributes; i++) {
            widthMap.put(i, width);
        }
        return widthMap;
    }

    private String[] makeColumnsSingleTable(int numAttributes) {
        String[] columns = new String[numAttributes];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = String.valueOf(i);
        }
        return columns;
    }

    private HashMap<Integer, Integer> getWidthMap(HashMap<String, Integer> stringWidthMap, String[] columns) {
        HashMap<Integer, Integer> widthMap = new HashMap<>();
        for (int i = 0; i < columns.length; i++){
            int width = stringWidthMap.get(columns[i]);
            widthMap.put(i, width);
        }
        return widthMap;
    }

    private int getAttrSpecHashCode(int colidx, boolean singleTable) {
        if (singleTable) {
            return String.valueOf(colidx).hashCode();
        } else {
            throw new IllegalArgumentException("Not single table");
        }
    }

    private int sketch_idx(int a1, int a2) {
        if (a1 == a2) {
            throw new IllegalArgumentException("Same attribute.");
        }
        if (a1 > a2) {
            int tmp = a1;
            a1 = a2;
            a2 = tmp;
        }
        return a1 * (numAttributes - 1) - (a1 * (a1-1))/2 + (a2 - a1-1);
    }



    public int sketch_idx_first(int attr) {
        if (attr<0 || attr >= numAttributes) {
            throw new IllegalArgumentException("Out of range");
        }
        if (attr == 0) {
            return 0;
        }
        return attr - 1;
    }

    public void computeHashes(int att, long attrValue, int[] hashes, int[] seeds) {
        for (int i = 0; i < depth; i++) {
            hashes[i] = mix(attrValue, seeds[i], widthMap.get(att));
        }
    }

    public void computeHashes(int att, String attrValue, int[] hashes, int[] seeds) {
        for (int i = 0; i < depth; i++) {
            if (attrValue == null) {
                hashes[i] = -1;
            } else {
                hashes[i] = mix(attrValue.hashCode(), seeds[i], widthMap.get(att));
            }
        }
    }

    public void printParams() {
        System.out.println("ChowMin with d: "+ depth + ", width: " + widthMap.get(0));
    }

    @Override
    public void add(Record record) {
        ingest(record, 1);
    }

    @Override
    public void add(long[] record) {

        ingest(record, 1);

    }
    public void ingest(long[] record, int sign) {
        N += sign;
        for (int att = 0; att < numAttributes; att++) {
            int[] h = hashes[att];
            int[] s = seeds[att];
            long v = record[att];
            computeHashes(att, v, h, s);
        }
        ingestToSketch(null, sign);
    }

    @Override
    public void ingest(Record record, int sign) {
        N += sign;
        for (int att = 0; att < numAttributes; att++) {
            int[] h = hashes[att];
            int[] s = seeds[att];
            String v = (record.getValue(att+1)).toString();
            computeHashes(att, v, h, s);
        }
        ingestToSketch(null, sign);
    }

    @Override
    public int query(long[] query, int numPreds) {
        return 0;
    }

    @Override
    public int query(Record query, int numPreds, QueryInfo queryInfo) {
        ArrayList<Integer> active = new ArrayList<>(numAttributes);
        checkActive(active, (String[]) query.getQueryData(numAttributes));

        if (active.isEmpty()) {
            return N;
        }

        if (active.size() == 1) {
            return marginal(active.get(0), query.getValue(active.get(0)).toString());
        }

        HashSet<Integer> attrsInPath = getAttrsInPath(active);

        int root_cl = active.get(0);
        List<Edge> f = buildFactorization(attrsInPath, root_cl, numAttributes, MIs);
        // query the pairwise CMs to get to estimate.

        return queryFactors(f, (String[]) query.getQueryData(numAttributes), attrsInPath);
    }

    @Override
    public int query(long[] query, int numPreds, QueryInfo queryInfo) {
        ArrayList<Integer> active = new ArrayList<>(numAttributes);
        checkActive(active, query);

        if (active.isEmpty()) {
            return N;
        }

        if (active.size() == 1) {
            return marginal(active.get(0), query[active.get(0)]);
        }

        HashSet<Integer> attrsInPath = getAttrsInPath(active);

        int root_cl = active.get(0);
        List<Edge> f = buildFactorization(attrsInPath, root_cl, numAttributes, MIs);
        // query the pairwise CMs to get to estimate.

        return queryFactors(f, query, attrsInPath);
    }

    @Override
    public int query(long[] query, int numPreds, int unionSize, QueryInfo CMRow) {
        return 0;
    }

    @Override
    public int rangeQuery(long[] minrange, long[] maxrange) {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void delete(long[] r) {
        ingest(r, -1);
    }

    @Override
    public void delete(Record r) {
        ingest(r, -1);
    }

    @Override
    public int getFilledKSamples() {
        return 0;
    }

    private void ingestToSketch(boolean[] isHeavy, int sign) {
        for (int a1 = 0; a1 < numAttributes-1; a1++) { // summary for every pair in upper triangle
            for (int a2 = a1 + 1; a2 < numAttributes; a2++) {
                if (isHeavy!= null && (isHeavy[a1] || isHeavy[a2])) {
                    continue;
                }
                int[] ha1 = hashes[a1];
                int[] ha2 = hashes[a2];
                int sid = sketch_idx(a1, a2);
                for (int j = 0; j < depth; j++) {
                    if (ha1[j] == -1 || ha2[j] == -1) {
                        continue; // ignore null attribute values. Future version: don't make pairwise sketches with attributes that are all null.
                    }
                    sketches[sid].CMR[sketches[sid].idx(j,ha1[j],ha2[j])] +=sign;
                }
            }
        }
    }

    public int query(String[] record) {
        ArrayList<Integer> active = new ArrayList<>(numAttributes);
        checkActive(active, record);

        if (active.isEmpty()) {
            return N;
        }

        if (active.size() == 1) {
            return marginal(active.get(0), record[active.get(0)]);
        }


        HashSet<Integer> attrsInPath = getAttrsInPath(active);

        int root_cl = active.get(0);
        List<Edge> f = buildFactorization(attrsInPath, root_cl, numAttributes, MIs);
        // query the pairwise CMs to get to estimate.

        return queryFactors(f, record, attrsInPath);
    }

    private void checkActive(ArrayList<Integer> active, long[] record) {
        for (int i = 0; i < record.length; i++) {
            if (record[i] != -1) {
                active.add(i);
            }
        }
    }

    private void checkActive(ArrayList<Integer> active, String[] record) {
        for (int i = 0; i < record.length; i++) {
            if (!record[i].equals("-1")) {
                active.add(i);
            }
        }
    }

    private HashSet<Integer> getAttrsInPath(ArrayList<Integer> active) {
        HashSet<Integer> attrs= new HashSet<Integer>(active.size());
        attrs.addAll(active);
        return attrs;
    }
    private int marginal(Integer attr, String predicate) {
        return sketches[sketch_idx_first(attr)].queryMarginal(predicate, attr);
    }
    private int marginal(Integer attr, long predicate) {
        return sketches[sketch_idx_first(attr)].queryMarginal(predicate, attr);
    }

    ArrayList<AttributeDimension> attributeDimensions = new ArrayList<>();
    private int queryFactors(List<Edge> f, long[] record, HashSet<Integer> attrsInPath) {
        constructDimensions(record, attrsInPath);
        int numDimensions = attributeDimensions.size();
        int[] indices = new int[numDimensions];
        return pathQuery(numDimensions, indices, f);
    }
    private int queryFactors(List<Edge> f, String[] record, HashSet<Integer> attrsInPath) {
        constructDimensions(record, attrsInPath);
        int numDimensions = attributeDimensions.size();
        int[] indices = new int[numDimensions];
        return pathQuery(numDimensions, indices, f);
    }

    private int pathQuery(int numDimensions, int[] indices, List<Edge> f) {
        double sum = 0;
        int[][] k = new int[depth][numAttributes];
        while (true) {
            for (int i = 0; i < numDimensions; i++) {
                AttributeDimension dim = attributeDimensions.get(i);
                int codomainIndex = indices[i];
                for (int j = 0; j < depth; j++) {
                    k[j][dim.attribute] = dim.valuesPerCodomain[j][codomainIndex];
                }
            }

            double product_p = 0;
            for (Edge edge : f) {
                int s = edge.getS();
                int t = edge.getT();
                int mine = Math.min(s,t);
                int maxe = Math.max(s,t);
                if (s == t) {
                    product_p = sketches[sketch_idx_first(s)].queryMarginalCodomainWOLatent(s, k);

                } else {
                    double jointFreq = sketches[sketch_idx(mine, maxe)].queryCodomainWOLatent(k,mine, maxe);
                    double denominator = sketches[sketch_idx_first(s)].queryMarginalCodomainWOLatent(s, k);
                    product_p *= jointFreq / denominator;
                }
            }
            if (product_p > 0) {
                sum += product_p;
            }

            int pos = numDimensions - 1;
            while (pos >= 0) {
                indices[pos]++;
                if (indices[pos] < attributeDimensions.get(pos).codomainSize) {
                    break;
                }
                indices[pos] = 0;
                pos--;
            }

            if (pos < 0) {
                break; // all combinations exhausted
            }
        }
        return (int) (sum);
    }

    // Test marginals
    public boolean testMarginals(int j, int s, int w) {
        List<Integer> sketchesWithS = new ArrayList<>();
        for (int i = 0; i < sketches.length; i++) {
            TWODCM sketch = sketches[i];
            if (sketch.attrS == s || sketch.attrT == s) {
                sketchesWithS.add(i);
            }
        }
        int[] margs = new int[sketchesWithS.size()];
        for (int i = 0; i < margs.length; i++) {
            int est = sketches[sketchesWithS.get(i)].marginalOneRep(j,s,w);
            margs[i] = est;
            if (i > 1 && margs[i] != margs[i - 1]) {
                throw new RuntimeException("Margs not equal");
            }
        }
        // check ig all margs are same;
        return true;
    }

    public double[][] estimateMIs() {
//        double[] MIs = new HashMap<>();
        double[][] MIs = new double[numAttributes*(numAttributes-1)/2][2+this.depth];
        int maxWidth = 0;
        for (int w : widthMap.values()) {
            if (w > maxWidth) {
                maxWidth = w;
            }
        }
        double[] marg1 = new double[maxWidth];
        double[] marg2 = new double[maxWidth];
        for (int a1 = 0; a1 < numAttributes-1; a1++) {
            for (int a2 = a1+1; a2 < numAttributes; a2++) {
                int sketch_idx = sketch_idx(a1, a2);
                double[] MIsList = new double[this.depth];
                for (int j = 0; j < depth; j++) {
                    double mi = estMI(widthMap.get(a1), widthMap.get(a2),a1, a2, sketch_idx, j, marg1, marg2);
                    MIsList[j] = mi;
                }
                Arrays.sort(MIsList);
                double medianMI = MIsList[MIsList.length/2];
                MIs[sketch_idx] = new double[]{a1,a2,medianMI};
            }
        }
        return MIs;
    }

    private double estMI(int width1, int width2, int a1, int a2, int sketch_idx, int j, double[] marg1, double[] marg2) {
        for (int w=0; w<width1; w++) {
            marg1[w] = sketches[sketch_idx_first(a1)]
                    .queryMarginalCodomain(a1, w, j);
        }
        for (int w = 0; w < width2; w++) {
            marg2[w] = sketches[sketch_idx_first(a2)]
                    .queryMarginalCodomain(a2, w, j);
        }

        double mi = 0;
        for (int w1 = 0; w1 < width1; w1++) {
            for (int w2 = 0; w2 < width2; w2++) {
                double joint = this.sketches[sketch_idx].jointCodomain(w1, w2,j);
                if (joint == 0) {
                    continue;
                }
                double mi_iter = joint/N * Math.log(joint/ (marg1[w1] * marg2[w2]));
                if (!Double.isNaN(mi_iter)) {
                    mi += mi_iter;
                }
            }
        }
        return mi;
    }

    void constructDimensions(long[] record, HashSet<Integer> attrsInPath) {
        attributeDimensions = new ArrayList<>();
        for (int i = 0; i < numAttributes; i++) {
            boolean isInPath = attrsInPath.contains(i);
            if (!isInPath) {
                continue;
            }
            int[] codomain;
            codomain = getCodomain(i, record[i]); // format int[d rows]
            int[][] valuesCodomain = new int[depth][1];
            for (int j = 0; j < codomain.length; j++) {
                valuesCodomain[j] = new int[]{codomain[j]};
            }
            attributeDimensions.add(new AttributeDimension(i, valuesCodomain));
        }
    }

    void constructDimensions(String[] record, HashSet<Integer> attrsInPath) {
        attributeDimensions = new ArrayList<>();
        for (int i = 0; i < numAttributes; i++) {
            boolean isInPath = attrsInPath.contains(i);
            if (!isInPath) {
                continue;
            }
            int[] codomain;
            codomain = getCodomain(i, record[i]); // format int[d rows]
            int[][] valuesCodomain = new int[depth][1];
            for (int j = 0; j < codomain.length; j++) {
                valuesCodomain[j] = new int[]{codomain[j]};
            }
            attributeDimensions.add(new AttributeDimension(i, valuesCodomain));
        }
    }

    private int[] getCodomain(int attr, long predicate) {
        return sketches[sketch_idx_first(attr)].getCodomain(attr, predicate);
    }
    private int[] getCodomain(int attr, String predicate) {
        return sketches[sketch_idx_first(attr)].getCodomain(attr, predicate);
    }

    public long getMemoryUsage() {
        long ram = 0;
        for (TWODCM cm : sketches) {
            ram += cm.memory();
        }
        return ram;
    }

    /**
     * Find path between two nodes in a tree using BFS.
     */

    public static List<Edge> buildFactorization(Set<Integer> subtree, int root, int numAttributes, double[][] MIs) {
        List<Edge> factors = new ArrayList<>();
        List<List<Integer>> adj = getChowLiuAdjMatrix(subtree, numAttributes, MIs);

        boolean[] visited = new boolean[adj.size()];
        Deque<Integer> stack = new ArrayDeque<>();
        // if root not in query, try something else of subtree
        //no root in all pairwise
        if (subtree.contains(root)) {
            stack.push(root);
            visited[root] = true;
            factors.add(new Edge(root, root));
        } else {
            throw new RuntimeException("Subtree has no root.");
        }


        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v :adj.get(u)) {
                if (subtree.contains(v) && !visited[v]) {
                    visited[v] = true;
                    stack.push(v);
//                    factors.add(new Edge(Math.min(v,u), Math.max(v,u)));
                    factors.add(new Edge(u, v));
                }
            }
        }
        return factors;
    }

    public static List<Edge> chowLiuWithPredicates(double[][] MI, Set<Integer> active, int numAttributes) {
        List<ChowLiu.EdgeWithWeight> edges = new ArrayList<>();

        for (double[] doubles : MI) {
            if (active.contains((int) doubles[0]) && active.contains((int) doubles[1]))
                edges.add(new ChowLiu.EdgeWithWeight((int) doubles[0], (int) doubles[1], doubles[2]));
        }
        // sort by descending MI
        edges.sort((a, b) -> Double.compare(b.weight, a.weight));

        ChowLiu.DSU dsu = new ChowLiu.DSU(numAttributes);
        List<Edge> tree = new ArrayList<>();

        if (edges.size() ==1) {
            tree.add(new Edge(edges.get(0).u, edges.get(0).v));
            return tree;
        }
        for (ChowLiu.EdgeWithWeight e : edges) {
            if (dsu.union(e.u, e.v)) {
                tree.add(new  Edge(e.u, e.v));
                if (tree.size() == numAttributes - 1) break;
            }
        }
        return tree;
    }

    private static List<List<Integer>> getChowLiuAdjMatrix(Set<Integer> subtree, int numAttributes, double[][] MIs) {
        List<Edge> chowLiu = chowLiuWithPredicates(MIs, subtree, numAttributes);
        return getLists(chowLiu, numAttributes);
    }

    @NonNull
    static List<List<Integer>> getLists(List<Edge> edges, int numAttributes) {
        List<List<Integer>> adjMatrix = new ArrayList<>(numAttributes);
        for (int i = 0; i < numAttributes; i++) adjMatrix.add(new ArrayList<>());
        if (edges.size()==1){
            adjMatrix.get(edges.get(0).getS()).add(edges.get(0).getT());
        }
        for (Edge e : edges) {
            adjMatrix.get(e.getS()).add(e.getT());
            adjMatrix.get(e.getT()).add(e.getS());
        }
        return adjMatrix;
    }

    public int getN() {return N;}

    public void setMIs(double[][] MIs, boolean estMIs) {
        this.MIs = MIs;
        if (estMIs) {
            this.setting = "ChowM2DAllPairs_estMIs";
        } else {
            this.setting = "ChowM2DAllPairs";
        }
    }
}

package omni.Old;

import omni.Dataset;
import omni.Main;
import omni.Query;

public class ExpWorkload {

    Query[] randChosenQueries;
    Dataset d;
    boolean[] attributesUsed;
    public ExpWorkload(Dataset d) {
        this.d = d;
        generateWorkload();
    }

    public ExpWorkload() {
    }

    public void generateWorkload() {
        randChosenQueries = new Query[d.queries.size()];
        for (int i = 0; i < randChosenQueries.length; i++) { // Choose Main.numQueries random queries;
            if (randChosenQueries[i] == null) {
                randChosenQueries[i] = d.queries.get(i);
            }
        }
        attributesUsed = new boolean[Main.numAttributes];
        for (Query q : randChosenQueries) {
            q.idxHighestAttr = 0;
            if (maxPredPerQuery < q.predAttrs.size()) {
                maxPredPerQuery = q.predAttrs.size();
            }
            for (int i = 0; i < q.predAttrs.size(); i++) {
                if (q.predAttrs.get(i) >= Main.numAttributes) {
                    continue;
                }
                attributesUsed[q.predAttrs.get(i)] = true;
                if (q.predAttrs.get(i) > q.idxHighestAttr) {
                    q.idxHighestAttr = q.predAttrs.get(i);
                }
            }
        }
    }
    int maxPredPerQuery = 0;


}

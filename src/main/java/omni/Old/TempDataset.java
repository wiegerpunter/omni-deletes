package omni.Old;

import omni.Query;
import omni.Record;

import java.util.ArrayList;

public class TempDataset {
    // Final class should replace Dataset class.
    // Per dataset, we create a reader class that reads the dataset and creates a TempDataset object.


    String name;
    public ArrayList<Record> records = new ArrayList<>();

    public TempDataset(String name) {
        this.name = name;
    }

    public void batchExactRange(long[] rec, ArrayList<Query> queries) {
        for (Query q : queries) {
            // check if record all higher than q.lower and all lower than q.higher
            boolean valid = true;
            for (int i = 0; i < q.predAttrs.size(); i++) {
                int attr = q.predAttrs.get(i);
                if (rec[attr] < q.lower[attr] || rec[attr] > q.upper[attr]) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                q.exactAnswer++;
            }
        }
    }
}

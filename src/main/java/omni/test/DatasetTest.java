package omni.test;

import omni.Dataset;
import omni.Record;

public class DatasetTest {
    Dataset d;
    public DatasetTest(Dataset d) {
        this.d = d;
    }


    public void testPrimaryKey() {
        for (Record r: d.dataset) {
            for (Record s: d.dataset) {
                if (r.equals(s) && r.getId() != s.getId()) {
                    System.err.println("Primary key violated");
                }
            }
        }
    }
}

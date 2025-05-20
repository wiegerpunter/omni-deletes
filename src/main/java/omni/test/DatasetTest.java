package omni.test;

import omni.datasets.DatasetRefactor;
import omni.datasets.Record.Record;

public class DatasetTest {
    DatasetRefactor d;
    public DatasetTest(DatasetRefactor d) {
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

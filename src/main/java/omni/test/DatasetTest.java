package omni.test;

import omni.DatasetRefactor;
import omni.Record.Record;

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

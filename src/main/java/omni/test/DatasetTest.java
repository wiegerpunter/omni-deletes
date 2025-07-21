package omni.test;

import omni.datasets.DatasetRefactor;
import omni.datasets.ReadRecord.ReadRecord;

public class DatasetTest {
    DatasetRefactor d;
    public DatasetTest(DatasetRefactor d) {
        this.d = d;
    }


    public void testPrimaryKey() {
        for (ReadRecord r: d.dataset) {
            for (ReadRecord s: d.dataset) {
                if (r.equals(s) && r.getId() != s.getId()) {
                    System.err.println("Primary key violated");
                }
            }
        }
    }
}

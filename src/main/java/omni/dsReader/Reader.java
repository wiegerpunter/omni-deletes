package omni.dsReader;

import com.opencsv.exceptions.CsvValidationException;
import omni.Main;
import omni.TempDataset;

import java.io.IOException;

public class Reader {
    // static class that reads a dataset and creates a TempDataset object.
    public static TempDataset main() throws CsvValidationException, IOException {
        if (Main.datasetName.equals("SNMP")) {
            return ReaderSNMP.reader(Main.fileStartCondition);
        } else if (Main.datasetName.equals("CAIDA")) {
            return ReaderCAIDA.reader(Main.fileStartCondition);
        } else {
            System.out.println("Dataset not found.");
        }
        return null;
    }
}

package omni.dsReader;

import omni.Main;
import omni.Record;
import omni.Old.TempDataset;
import omni.dataGeneration.ProcessedStreamLoaderGeneric;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ReaderCAIDA {
    // Makes a TempDataset object from a dataset. This is the only class that should be used to read the SNMP dataset.

    public static TempDataset reader(String condition) {
        int numFiles = Integer.parseInt(condition);
        TempDataset dataset = new TempDataset("CAIDA");
        ArrayList<Record> records = new ArrayList<Record>();
        int bits = 32;
        // CAIDA can only read from scratch, loading from file is not needed.
        readFromScratch(records, numFiles);

        dataset.records = records;
        Main.dyadicRangeBits = bits;
        return dataset;

    }


    private static void readFromScratch(ArrayList<Record> records, int numFiles) {
        int recId = 0;
        String path = Main.outputFolder + "/CAIDA/csv/";
        File dir = new File(path);
        File[] filesList = dir.listFiles();

        int filesSeen = 0;
        if (filesList != null) {
            Arrays.sort(filesList, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
            for (File child : filesList) {
                if (child.getName().endsWith(".pcap.csv")) {
                    Main.logger.info("Reading file: " + child.getName());
                    System.out.println(child.getName());
                    ProcessedStreamLoaderGeneric psl = new ProcessedStreamLoaderGeneric(child.getPath(), false);
                    psl.reset();
                    int cnt = 0;

                    String firstLine = psl.readFirst();
                    String nextLine = psl.readRecord(firstLine, recId, records);
                    recId++;

                    while (!(nextLine == null)) {
                        nextLine = psl.readRecord(nextLine, recId, records); // Returns null at end of file.
                        recId++;
                        cnt++;
                        if (cnt == 9999999) {
                            throw new RuntimeException("Too many records");
                        }
                    }
                    psl.close();
                    filesSeen++;
                    if (filesSeen >= numFiles) {
                        break;
                    }
                }
            }
        }
    }


}

package omni.dsReader;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import omni.Main;
import omni.Record;
import omni.RecordSNMP;
import omni.Old.TempDataset;
import omni.dataGeneration.ProcessedStreamLoaderGeneric;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ReaderSNMP {
    // Makes a TempDataset object from a dataset. This is the only class that should be used to read the SNMP dataset.

    public static TempDataset reader(String condition) throws CsvValidationException, IOException {
        TempDataset dataset = new TempDataset("SNMP");
        ArrayList<Record> records = new ArrayList<Record>();
        int bits = 32;
        // check if file exists
        String CSV_FILE_NAME = Main.outputFolder + "SNMPDataset_" + Main.datasetName + "_" + condition + ".csv";
        File f = new File(CSV_FILE_NAME);
        if (f.exists() && !f.isDirectory()) {
            readFromExisting(condition, records);
        } else {
            // read from original file
            readFromScratch(condition, records);

            preprocess(records);
            // write to file
            writeToFile(condition, records);
        }

        dataset.records = records;
        Main.dyadicRangeBits = bits;

        return dataset;

    }

    private static void writeToFile(String condition, ArrayList<Record> records) throws IOException {
        String CSV_FILE_NAME = Main.outputFolder + "SNMPDataset_" + Main.datasetName + "_"+ condition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"id", "timestamp", "AP", "sysUpTime", "sysDescr", "ifIndex", "ifDescr", "ifType", "ifSpeed",
                "ifInOctets", "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets", "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards",
                "awcDot11AssociatedStationCount", "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount", "awcDot11DeauthenicateCount",
                "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"};

        writer.writeNext(header);
        for (Record r : records) {
            writer.writeNext(((RecordSNMP) r).writeRecord());
        }
        writer.close();
    }

    private static void preprocess(ArrayList<Record> records) {
        ArrayList<Record> validRecords = new ArrayList<>();
        boolean validRecord;
        HashSet<Integer> invalidIndices = new HashSet<>(Arrays.asList(3, 16, 17, 18, 19, 20));

        for (Record record : records) {
            validRecord = true;

            long[] recordData = record.getRecord();
            for (int i = 0; i < Main.numAttributes; i++) {
                // if i is not 3, 16, 17, 18, 19, 20.
                if (!invalidIndices.contains(i) && recordData[i] == -999) {
                    validRecord = false;
                    break;
                }
            }
            if (validRecord) {
                validRecords.add(record);
            }
        }

        // Replace the original dataset with the new validRecords ArrayList
        records = validRecords;
    }


    private static void readFromScratch(String condition, ArrayList<Record> records) {
        String directory = Main.readFolder + "/SNMP/";
        File dir = new File(directory);

        String[] directoryListing = dir.list((current, name) -> new File(current, name).isDirectory());
        int recId = 0;
        if (directoryListing != null) {
            for (String child : directoryListing) {
                String path = directory + child;
                if (condition.contains("_OR_")) {
                    String[] conditions = condition.split("_OR_");
                    boolean skip = true;
                    for (String c : conditions) {
                        if (child.startsWith(c)) {
                            skip = false;
                            break;
                        }
                    }
                    if (skip) {
                        continue;
                    }
                } else if (!child.startsWith(condition)) {
                    continue;
                }
                readFile(path, records, recId);
            }
        } else {
            throw new RuntimeException("No directories found");
        }
        Main.logger.info("All files read.");
        System.out.println("All files read.");

    }

    private static void readFile(String path, ArrayList<Record> records, int recId) {
        File dir = new File(path);
        File[] filesList = dir.listFiles();
        if (filesList != null) {
            for (File child : filesList) {
                if (child.getName().endsWith(".snmp.gz")) {

                    ProcessedStreamLoaderGeneric psl = new ProcessedStreamLoaderGeneric(child.getPath(), true);
                    psl.reset();
                    int cnt = 0;
                    String firstLine = psl.readFirst();
                    String nextLine = psl.readRecord(firstLine, recId, records);
                    recId++;

                    while (!(nextLine == null)) {
                        nextLine = psl.readRecord(nextLine, recId, records); // Returns null at end of file.
                        recId++;
                        cnt++;
                        if (cnt == 999999) {
                            throw new RuntimeException("Too many records in one file");
                        }
                    }
                    psl.close();
                }
            }
        }

    }

    private static void readFromExisting(String condition, ArrayList<Record> records) throws IOException, CsvValidationException {
        String CSV_FILE_NAME = Main.outputFolder + "SNMPDataset_" + Main.datasetName + "_"+ condition + ".csv";
        System.out.println("Reading from file: " + CSV_FILE_NAME);
        CSVReader reader = new CSVReader(new FileReader(CSV_FILE_NAME));
        String[] nextLine;
        //reader.readNext(); // skip header
        //System.out.println("Header: " + Arrays.toString(reader.readNext()));
        long largestValue = 0;
        nextLine = reader.readNext();
        try {
            while ((nextLine) != null) {
                long ID = Long.parseLong(nextLine[0]);
                long timestamp = Long.parseLong(nextLine[1]);
                long[] record = new long[]{Long.parseLong(nextLine[2]), Long.parseLong(nextLine[3]), Long.parseLong(nextLine[4]),
                        Long.parseLong(nextLine[5]), Long.parseLong(nextLine[6]), Long.parseLong(nextLine[7]), Long.parseLong(nextLine[8]),
                        Long.parseLong(nextLine[9]), Long.parseLong(nextLine[10]), Long.parseLong(nextLine[11]), Long.parseLong(nextLine[12]),
                        Long.parseLong(nextLine[13]), Long.parseLong(nextLine[14]), Long.parseLong(nextLine[15]), Long.parseLong(nextLine[16]),
                        Long.parseLong(nextLine[17]), Long.parseLong(nextLine[18]), Long.parseLong(nextLine[19]), Long.parseLong(nextLine[20]),
                        Long.parseLong(nextLine[21]), Long.parseLong(nextLine[22]), Long.parseLong(nextLine[23]), Long.parseLong(nextLine[24])};

                final Record r = new RecordSNMP(ID,timestamp,record);
                for (long l : record) {
                    if (l > largestValue) {
                        largestValue = l;
                    }
                }
                records.add(r);
                nextLine = reader.readNext();
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        Main.dyadicRangeBits = printBitsNeeded(largestValue);
    }


    private static int printBitsNeeded(long largestValue) {
        int thrBits = (int) Math.ceil(Math.log(largestValue)/Math.log(2));
        int bitsNeeded = 0;
        while (largestValue > 0) {
            largestValue = largestValue >> 1;
            bitsNeeded++;
        }

        if (thrBits != bitsNeeded)
            System.out.println("# bits not same: " + thrBits + " " + bitsNeeded);
        else
            System.out.println("Bits needed: " + bitsNeeded);

        return bitsNeeded;
    }

}

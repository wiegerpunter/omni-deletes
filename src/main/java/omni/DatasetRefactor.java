package omni;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import omni.Record.Record;
import omni.Record.RecordSNMP;
import omni.Record.RecordSynth;
import omni.dataGeneration.ProcessedStreamLoaderGenericRefactor;

import java.io.*;
import java.util.*;


public class DatasetRefactor {
    int size;
    public ArrayList<omni.Record.Record> dataset = new ArrayList<>();
    private int totalRead;

    public DatasetRefactor(String setting) throws IOException, CsvValidationException {
        switch (setting) {
            case "SNMP" -> {
                // Check if file exists
                String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName + "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (!f.exists()) {
                    SNMPDataset();
                    Main.writeSNMP = false;
                } else {
                    System.out.println("Loading dataset from file, name: " + CSV_FILE_NAME);
                    dataset = Main.h.readSNMPDataset();
                    System.out.println("Loaded size: " + dataset.size());
                }
            }
            case "CAIDA" -> {
                CAIDADataset();
            }
            default -> {
//                Main.logger.severe("Invalid setting");
                System.out.println("Invalid setting");
                System.exit(0);
            }
        }
        this.size = dataset.size();
        dataset.sort(Comparator.comparingLong(Record::getTimestamp));
        System.out.println("Dataset loaded with size: " + dataset.size());
    }

    private int totalSkip = 0;

    int recId = 0;


    public void CAIDADataset() throws RuntimeException {
        String dir;
        dir = Main.inputFolder + "/data/" + Main.datasetName;
        loadCAIDAFile(dir);
    }

    private void loadCAIDAFile(String path) throws RuntimeException {
        File dir = new File(path);//File subDir = new File(directory + child);
        File[] filesList = dir.listFiles();

        //int id = 0;
        int filesSeen = 0;
        if (filesList != null) {
            Arrays.sort(filesList, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
            for (File child : filesList) {
                if (child.getName().endsWith(".pcap.csv")) {
                    System.out.println(child.getName());
                    ProcessedStreamLoaderGenericRefactor psl = new ProcessedStreamLoaderGenericRefactor(child.getPath(), false);
                    psl.reset();
                    int cnt = 0;

                    StringBuilder firstLine = psl.readFirst();
                    StringBuilder nextLine = psl.readRecord(firstLine, recId, dataset);
                    recId++;

                    while (!(nextLine == null)) {
                        nextLine = psl.readRecord(nextLine, recId, dataset); // Returns null at end of file.
                        recId++;
                        cnt++;
                        if (cnt == 9999999) {
                            throw new RuntimeException("Too many records");
                        }
                    }
                    this.totalSkip += psl.skip; //Number of records skipped.
                    this.totalRead += psl.linesSeen;
                    psl.close();
                    filesSeen++;
                    if (filesSeen >= Main.numFiles) {
                        break;
                    }
                    if (!Main.readAllFiles) {
                        break;
                    }
                }
            }
        }
    }

    public void SNMPDataset() throws RuntimeException, IOException {
        if (Main.readAllFiles) {
            //String directory = "C:/Users/s162378/OneDrive - TU Eindhoven/Documents/GitHub/DSCM/fall03.tar/fall03/fall03/";
            String directory = Main.inputFolder +"/data/" + Main.datasetName;
            File dir = new File(directory);

            String[] directoryListing = dir.list((current, name) -> new File(current, name).isDirectory());

            if (directoryListing != null) {
                for (String child : directoryListing) {
                    String path = directory + child;
                    if (Main.fileStartCondition.contains("_OR_")) {
                        String[] conditions = Main.fileStartCondition.split("_OR_");
                        boolean skip = true;
                        for (String condition : conditions) {
                            if (child.startsWith(condition)) {
                                skip = false;
                                break;
                            }
                        }
                        if (skip) {
                            continue;
                        }
                    } else if (!child.startsWith(Main.fileStartCondition)) {
                        continue;
                    }
                    loadFileSNMP(path);
                }
            } else {
                throw new RuntimeException("No directories found");
            }
//            Main.logger.info("All files read.");
            System.out.println("All files read.");
        } else {
            String smallPath = Main.readFolder + "/SNMP/031101/";
            loadFileSNMP(smallPath);
        }

        //Check for whole dataset how many records have value -999 per attribute:
        int[] count = new int[Main.numAttributes];
        for (omni.Record.Record r : dataset) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (r.getRecord()[i] == -999) {
                    count[i]++;
                }
            }
        }
        for (int i = 0; i < Main.numAttributes; i++) {
            System.out.println("Attribute " + i + " has " + count[i] + " records with value -999");
        }


        ArrayList<omni.Record.Record> validRecords = new ArrayList<>();
        boolean validRecord;
        HashSet<Integer> invalidIndices = new HashSet<>(Arrays.asList(3, 16, 17, 18, 19, 20));

        for (omni.Record.Record record : dataset) {
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
        dataset = validRecords;
        initSNMPDataset(this);
        for (omni.Record.Record r : dataset) {
            writeSNMPDataset(((RecordSNMP) r).writeRecord());
        }
        System.out.println("Skipped " + totalSkip + " records");
        System.out.println("Read " + totalRead + " records");
        System.out.println("Size: " + dataset.size());
    }

    private void loadFileSNMP(String path) {
        File dir = new File(path);//File subDir = new File(directory + child);
        File[] filesList = dir.listFiles();
        //int id = 0;
        if (filesList != null) {
            for (File child : filesList) {
                if (child.getName().endsWith(".snmp.gz")) {
                    //System.out.println("Processing " + child.getPath());
                    ProcessedStreamLoaderGenericRefactor psl = new ProcessedStreamLoaderGenericRefactor(child.getPath(), true);
                    psl.reset();
                    //psl.readHeader();

                    int cnt = 0;

                    StringBuilder firstLine = psl.readFirst();
                    StringBuilder nextLine = psl.readRecord(firstLine, recId, dataset);
                    recId++;

                    while (!(nextLine == null)) {
                        nextLine = psl.readRecord(nextLine, recId, dataset); // Returns null at end of file.
                        recId++;
                        cnt++;
                        if (cnt == 999999) {
                            throw new RuntimeException("Too many records");
                        }
                    }
                    this.totalSkip += psl.skip; //Number of records skipped.
                    this.totalRead += psl.linesSeen;
                psl.close();
                }
            }
        }
    }

    public static void initSNMPDataset(DatasetRefactor d) throws IOException {
        //initialize csv file
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        String[] header;
        header = new String[]{"id", "timestamp", "AP", "sysUpTime", "sysDescr", "ifIndex", "ifDescr", "ifType", "ifSpeed",
                "ifInOctets", "ifInUcastPkts", "ifInErrors", "ifInDiscards", "ifOutOctets", "ifOutUcastPkts", "ifOutErrors", "ifOutDiscards",
                "awcDot11AssociatedStationCount", "awcDot11ReassociatedStationCount", "awcDot11RoamedStationCount", "awcDot11DeauthenicateCount",
                "awcDot11DisassociateCount", "awcFtClientSTASelf", "awcFtBridgeSelf", "awcFtRepeaterSelf"};

        writer.writeNext(header);
    }


    public static void writeSNMPDataset(String[] data) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName +
                "/SNMPDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));
        writer.writeNext(data);
        writer.close();
    }

}

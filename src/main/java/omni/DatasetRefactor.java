package omni;

import com.opencsv.exceptions.CsvValidationException;
import omni.dataGeneration.ProcessedStreamLoaderGenericRefactor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class DatasetRefactor {
    //OmniSketch s;
    int size;
    String setting;
    public ArrayList<Record> dataset = new ArrayList<>();
    private int totalRead;
    ArrayList<Integer> usedIDs = new ArrayList<>();
    Helper h;
    int numZipfAttrs;
    double zipfAlpha;

    public DatasetRefactor(String setting, Helper h) throws IOException, CsvValidationException {
        //this.s = s;
        this.h = h;
        this.setting = setting;
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
            case "wc98" -> {
                // Check if file exists
                String CSV_FILE_NAME = Main.inputFolder + "/data/" + Main.datasetName + "/WCDataset_" + Main.datasetName + "_"+ Main.fileStartCondition + ".csv";
                File f = new File(CSV_FILE_NAME);

                if (!f.exists()) {
                    WCDataset();
                    Main.writeSNMP = false;
                } else {
                    System.out.println("Loading dataset from file, name: " + CSV_FILE_NAME);
                    dataset = Main.h.readWCDataset();
                    System.out.println("Loaded size: " + dataset.size());
                }
            }
            case "CAIDA" -> {
                CAIDADataset();
            }
            case "synthzipf1" -> {
                numZipfAttrs = 5;
                zipfAlpha = 1; // Zipf distribution parameter
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            } case "synthzipf1-3" -> {
                numZipfAttrs = 5;
                zipfAlpha = 1.3; // Zipf distribution parameter
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            }
            case "synthzipf1-5" -> {
                numZipfAttrs = 5;
                zipfAlpha = 1.5; // Zipf distribution parameter
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            }case "synthzipf1-7" -> {
                numZipfAttrs = 5;
                zipfAlpha = 1.7; // Zipf distribution parameter
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            }
            case "synthUniform" -> {
                numZipfAttrs = 0;
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            }
            case "synthEquiDepthQueryBins"-> {
                numZipfAttrs = 0;
                boolean differAlphas = false;
                String CSV_FILE_NAME = Main.outputFolder + "synthDataset_" + Main.datasetName + "_N="+ Main.sensitivityNumberOfRecords +
                        "_ZipfAttrs="+ numZipfAttrs + "_zipfAlpha="+ zipfAlpha + "_differAlphas="+ differAlphas + ".csv";
                File f = new File(CSV_FILE_NAME);
                if (f.exists()) {
                    dataset = h.readSynthDataset(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                } else {
                    synthEquiDepth(Main.sensitivityNumberOfRecords, numZipfAttrs, zipfAlpha, false);
                }
            }
            default -> {
//                Main.logger.severe("Invalid setting");
                System.out.println("Invalid setting");
                System.exit(0);
            }
        }
        this.size = dataset.size();
        dataset.sort(Comparator.comparingLong(o -> o.timestamp));
        System.out.println("Dataset loaded with size: " + dataset.size());
    }



    public DatasetRefactor() {
    }

    public DatasetRefactor(DatasetRefactor d) {
        this.size = d.size;
        this.setting = d.setting;
        this.dataset = d.dataset;
        this.totalRead = d.totalRead;
        this.totalSkip = d.totalSkip;
        this.dStats = null;
        this.recId = d.recId;
        this.usedIDs = d.usedIDs;
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
        for (Record r : dataset) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (r.getRecord()[i] == -999) {
                    count[i]++;
                }
            }
        }
        for (int i = 0; i < Main.numAttributes; i++) {
            System.out.println("Attribute " + i + " has " + count[i] + " records with value -999");
        }


        ArrayList<Record> validRecords = new ArrayList<>();
        boolean validRecord;
        HashSet<Integer> invalidIndices = new HashSet<>(Arrays.asList(3, 16, 17, 18, 19, 20));

        for (Record record : dataset) {
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
        dataset = validRecords;
        // delete records with value -999
//        for (int i = 0; i < Main.numAttributes; i++) {
//            // if i is not 3, 16, 17, 18, 19, 20.
//            if (i != 3 && i != 16 && i != 17 && i != 18 && i != 19 && i != 20) {
//                for (int j = 0; j < dataset.size(); j++) {
//                    if (dataset.get(j).getRecord()[i] == -999) {
//                        dataset.remove(j);
//                        j--;
//                    }
//                }
//            }
//        }

        // write dataset to file
        h.initSNMPDataset(this);
        for (Record r : dataset) {
            h.writeSNMPDataset(((RecordSNMP) r).writeRecord());
        }


//
//        Main.logger.info("Skipped " + totalSkip + " records");
//        Main.logger.info("Read " + totalRead + " records");
//        Main.logger.info("Size: " + dataset.size());
        System.out.println("Skipped " + totalSkip + " records");
        System.out.println("Read " + totalRead + " records");
        System.out.println("Size: " + dataset.size());

    }

    public void WCDataset() throws RuntimeException, IOException {
        if (Main.readAllFiles) {
            //String directory = "C:/Users/s162378/OneDrive - TU Eindhoven/Documents/GitHub/DSCM/fall03.tar/fall03/fall03/";
            String directory = Main.readFolder + "wc98/";
            File dir = new File(directory);
            if (dir != null) {
                for (String path: Objects.requireNonNull(dir.list())) {
                    loadFileWC(Main.readFolder + "wc98/" + path);
                }
            } else {
                throw new RuntimeException("No directories found");
            }
//            Main.logger.info("All files read.");
            System.out.println("All files read.");
        } else {
            String smallPath = Main.readFolder + "/wc98/031101/";
            loadFileSNMP(smallPath);
        }


        //Check for whole dataset how many records have value -999 per attribute:
        int[] count = new int[Main.numAttributes];
        for (Record r : dataset) {
            for (int i = 0; i < Main.numAttributes; i++) {
                if (r.getRecord()[i] == -999) {
                    count[i]++;
                }
            }
        }
        for (int i = 0; i < Main.numAttributes; i++) {
            System.out.println("Attribute " + i + " has " + count[i] + " records with value -999");
        }


        ArrayList<Record> validRecords = new ArrayList<>();
        boolean validRecord;
        HashSet<Integer> invalidIndices = new HashSet<>(Arrays.asList(3, 16, 17, 18, 19, 20));

        for (Record record : dataset) {
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
        dataset = validRecords;
        // delete records with value -999
//        for (int i = 0; i < Main.numAttributes; i++) {
//            // if i is not 3, 16, 17, 18, 19, 20.
//            if (i != 3 && i != 16 && i != 17 && i != 18 && i != 19 && i != 20) {
//                for (int j = 0; j < dataset.size(); j++) {
//                    if (dataset.get(j).getRecord()[i] == -999) {
//                        dataset.remove(j);
//                        j--;
//                    }
//                }
//            }
//        }

        // write dataset to file
        h.initSNMPDataset(this);
        for (Record r : dataset) {
            h.writeSNMPDataset(((RecordSNMP) r).writeRecord());
        }


//
//        Main.logger.info("Skipped " + totalSkip + " records");
//        Main.logger.info("Read " + totalRead + " records");
//        Main.logger.info("Size: " + dataset.size());
        System.out.println("Skipped " + totalSkip + " records");
        System.out.println("Read " + totalRead + " records");
        System.out.println("Size: " + dataset.size());

    }

    private void loadFileWC(String path) {
        File file = new File(path);//File subDir = new File(directory + child);
        //File[] filesList = dir.listFiles();
        //int id = 0;
        if (file.getName().endsWith(".gz")) {
            //System.out.println("Processing " + child.getPath());
            ProcessedStreamLoaderGenericRefactor psl = new ProcessedStreamLoaderGenericRefactor(file.getPath(), true);
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
    AttributeStats[] dStats;

    public String[] synthInfoToWrite(int id, long[] values) {
        String[] data = new String[Main.numAttributes + 1];
        data[0] = Integer.toString(dataset.size());
        data[1] = Integer.toString(Main.numAttributes);
        data[2] = Integer.toString(numZipfAttrs);
        data[3] = Double.toString(zipfAlpha);
        data[4] = Integer.toString(id);
        data[5] = Arrays.toString(values);
        return data;
    }

    public void synthDataset(int numRecords, int numZipfianAttributes, double zipfAlpha, boolean differAlphas) throws IOException {

        //long[] zipfianSumArray = new long[0];
        
        long[][] zipfData = new long[0][];
        long[][] unifData = new long[0][];
        if (numZipfianAttributes>0){
            zipfData=ZipfGenerator.zipfData(numRecords, numZipfianAttributes, 10000, zipfAlpha);
        }
        if (Main.numAttributes - numZipfianAttributes > 0) {
            unifData = new long[numRecords][Main.numAttributes - numZipfianAttributes];
        }
        Random random = new Random(1);
        for (int i = 0; i < numRecords; i++) {
            for (int j = 0; j < Main.numAttributes - numZipfianAttributes; j++) {
                unifData[i][j] = (long) (random.nextLong(10000000));
            }
        }

//        Random random = new Random();
//        System.out.println("Generating synthetic dataset");
//        //zipfianSumArray = generateZipfianSumArray(numRecords, zipfAlpha);
        int rec_id = 0;
        for (int i = 0; i < numRecords; i++) {
            long[] values = new long[Main.numAttributes];
            if (numZipfianAttributes > 0) System.arraycopy(zipfData[i], 0, values, 0, numZipfianAttributes);
            if (Main.numAttributes - numZipfianAttributes > 0) {
                System.arraycopy(unifData[i], 0, values, numZipfianAttributes, Main.numAttributes - numZipfianAttributes);
            }
            for (int j = 0; j < 15; j++) {

                this.dataset.add(new RecordSynth(rec_id, values));
                rec_id++;
            }
            String[] data = synthInfoToWrite(rec_id, values);
            h.initSynthDataset(data, numRecords, numZipfianAttributes, zipfAlpha, differAlphas);
            h.writeSynthDataset(data, numRecords, numZipfianAttributes, zipfAlpha, differAlphas);

            }
        System.out.println("Dataset size Synthetic set: " + dataset.size());
    }

    private void synthEquiDepth(int sensitivityNumberOfRecords, int numZipfAttrs, double zipfAlpha, boolean b) {
        // This will be a queries first -> dataset later approach.
        // We have queries in 10 bins of 100 queries each.
        // All queries in different bins are on distinct domains. No overlap.
        // When we have the queries, we can generate the dataset.
        // We want bin 0 to have answer size [2^0, 2^1], bin 1 [2^1, 2^2], bin 2 [2^2, 2^3], etc.
        // We want the queries to be on distinct domains, so we can generate the dataset in one go.
        

    }

    public String getMemoryUsage() {
        long usg = (long) dataset.size() * (Main.numAttributes * 32L + 32); // unit is bits
        return Long.toString(usg);
    }

}

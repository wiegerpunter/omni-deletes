package omni;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CleanDataset {

    public int numDeletes;
    Helper h;

    // Input dataset and make new dataset with new records, omitting attributes that are not valid.
    DatasetRefactor original;
    String name;
    long[][] dataset;
    long[][] datasetNegUpdates;
    long[][] datasetResidu;
    long[][] noiseUpdates;
//    long[][] ingestionDataset;
//    long[][] warmupDataset;

    long[][][] rangeQueries;
    long[][] pointQueries;
    int[] pointQueryBinNumber;
    public int[] pointQueriesNumAttrs; // number of attributes in each point query.
    int[] pointQueryAnswers;
    int[] pointQueryAnswersDeletes;
    int[] rangeQueryAnswers;
    public int[] pointQueryUnion;
    public int[] pointQueryUnionDeletes;
    int size;
    int[] cleanIds;
    boolean[] neverDeleted;
    public ArrayList<Integer>[] toDelete;
    public long[][] datasetInsertsSpreadOut;
    public boolean[] isDelete;
    public long[][] datasetAllSpreadOut;

    long[][] pointQueriesEmpty;
    int[] pointQueryBinNumberEmpty;
    int[] pointQueryAnswersEmpty;

    double sizeFactor = 1;
    private int repetition;
    private final int numPredicates;
    private final int numBins;

    //int[] attributesToChoose;
//    private void getAttrsToChoose(int repetition) {
//        // Repetition is used to get the same attributes for each repetition, but different for each repetition.
//        if (Objects.equals(name, "SNMP")) {
//            attributesToChoose = new int[]{9,3,2,8,10,1,5,6,7,0,4};
//        } else if (Objects.equals(name, "CAIDA")) {
//            attributesToChoose = new int[]{0,1,2,3,4};
//        } else {
//            attributesToChoose = new int[dataset[0].length];
//            for (int i = 0; i < dataset[0].length; i++) {
//                attributesToChoose[i] = i;
//            }
//        }
//        // shuffle attributesToChoose based on repetition.
//        Random rng = new Random(repetition);
//        for (int i = 0; i < attributesToChoose.length; i++) {
//            int randomPosition = rng.nextInt(attributesToChoose.length);
//            int temp = attributesToChoose[i];
//            attributesToChoose[i] = attributesToChoose[randomPosition];
//            attributesToChoose[randomPosition] = temp;
//        }
//        System.out.println("Attributes to choose: " + Arrays.toString(attributesToChoose) + " for repetition " + repetition);
//    }

    public CleanDataset(Helper h, int numBins, int numPredicates) {
        this.h = h;
        this.name = Main.datasetName;
        this.numBins = numBins;
        this.numPredicates = numPredicates;
//        if (Objects.equals(name, "SNMP")) {
//            cleanIds = new int[]{12, 14, 11, 1, 5, 13, 9, 8, 10, 21, 15};//{0, 1, 2, 3, 4, 5, 6, 7, 8, 16, 17};
//            //int[] order = {12, 14, 11, 1, 5, 13, 9, 8, 10, 21, 15};//21, 19, 18, 1, 16, 12, 15, 5, 20,11, 17, 8, 13, 10};
//        } else if (Objects.equals(name, "CAIDA")) {
//            cleanIds = new int[]{5, 6, 8, 9, 10}; // 4 & 7 are also ok but correlate with 5 6 8 9.
//            // print names of vars used:
//            for (int i = 0; i < cleanIds.length; i++) {
//                System.out.println(Parser.getAttributeName(cleanIds[i]));
//            }
//        } else if (Objects.equals(name, "wc98")) {
//            throw new RuntimeException("Not implemented");
//        } else if (name.contains("synth")) {
//            cleanIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7,8,9}; //,5,6,7,8,9,10,11};
//        }else {
//            throw new RuntimeException("Unknown dataset name");
//        }
    }

    public void setAttributes(int repetition) {
        this.repetition = repetition;
        if (Objects.equals(name, "SNMP")) {
            cleanIds = new int[]{12, 14, 11, 1, 5, 13, 9, 8, 10, 21, 15};//{0, 1, 2, 3, 4, 5, 6, 7, 8, 16, 17};
            //int[] order = {12, 14, 11, 1, 5, 13, 9, 8, 10, 21, 15};//21, 19, 18, 1, 16, 12, 15, 5, 20,11, 17, 8, 13, 10};
        } else if (Objects.equals(name, "CAIDA")) {
            cleanIds = new int[]{5, 6, 8, 9, 10}; // 4 & 7 are also ok but correlate with 5 6 8 9.
        } else if (Objects.equals(name, "wc98")) {
            throw new RuntimeException("Not implemented");
        } else if (name.contains("synth")) {
            cleanIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}; //,5,6,7,8,9,10,11};
            return; // no need to shuffle cleanIds, as already synthetic data.
        }else {
            throw new RuntimeException("Unknown dataset name");
        }
        // shuffle clean ids based on repetition
        Random rng = new Random(repetition);
        for (int i = 0; i < cleanIds.length; i++) {
            int randomPosition = rng.nextInt(cleanIds.length);
            int temp = cleanIds[i];
            cleanIds[i] = cleanIds[randomPosition];
            cleanIds[randomPosition] = temp;
        }
        System.out.println("Clean ids: " + Arrays.toString(cleanIds) + " for repetition " + repetition);
    }

    public void cleanDataset(DatasetRefactor d, double percToDelete, double maxPercToDelete) throws CsvValidationException, IOException {
        this.original = d;
        dataset = new long[original.dataset.size()][];
        clean();
        // warmupDataset is a subset of dataset, first Main.warmupNumber records.
        int numToKeep = splitDeletes(percToDelete);
        if (Main.rangeQueries) {
            getRangeQueries();
        } else {
            getPointQueries(percToDelete, maxPercToDelete);
        }
    }

    private int splitDeletes(double percToDelete) {
//        warmupDataset = Arrays.copyOfRange(dataset, 0, Math.min(Main.warmupNumber, dataset.length));
//        ingestionDataset = Arrays.copyOfRange(dataset, Math.min(Main.warmupNumber, dataset.length), dataset.length);
        if (!Main.withDeletes) {
            datasetResidu = dataset;
            datasetNegUpdates = new long[0][];
            Main.streamSize = dataset.length;
            return dataset.length;
        }
        int numToKeep=0;
        long time_start = System.currentTimeMillis();
//        if (Main.spreadOutDeletes) {
//            neverDeleted = new boolean[dataset.length];
//            toDelete = new ArrayList[dataset.length];
//
//            Random rn = new Random(1);
//            for (int i = 0; i < dataset.length; i++) {
//                toDelete[i] = new ArrayList<Integer>();
//            }
//            for (int i = 0; i < dataset.length; i++) {
//                rn.setSeed(i);
//                int rand = rn.nextInt(100);
//
//                if (rand < percToDelete * 100) {
//                    // add to deletion queue to be deleted at certain i in future of loop.
//                    int rand2 = rn.nextInt(dataset.length - i);
//                    toDelete[i + rand2].add(i);
//                } else {
//                    // never delete
//                    neverDeleted[i] = true;
//                }
//
//            }
//            numToKeep = setDatasets(neverDeleted);
//        } else {
        numToKeep = (int) Math.ceil((1 - percToDelete) * dataset.length);
        datasetNegUpdates = new long[dataset.length - numToKeep][]; // datasetNegUpdates is a subset of dataset, first numToDelete records.
        datasetResidu = new long[numToKeep][]; // datasetAfterDeletes is a subset of dataset, all records except first numToDelete records.

        // fill datasetNegUpdates with first numToDelete records from dataset.
        System.arraycopy(dataset, 0, datasetNegUpdates, 0, dataset.length - numToKeep);
        // fill datasetAfterDeletes with records from dataset after numToDelete records.
        System.arraycopy(dataset, dataset.length - numToKeep, datasetResidu, 0, numToKeep);
//        }
        long time_end = System.currentTimeMillis();
        System.out.println("Time to set deletes: " + (time_end - time_start)/1000 + " s");
        Main.streamSize = numToKeep;
        System.out.println("Dataset neg updates size: " + datasetNegUpdates.length);
        System.out.println("Dataset after deletes size: " + datasetResidu.length);
        return numToKeep;
    }

    public static void shuffleArray(long[][] array) {
        Random r = new Random(0);
        for (int i = array.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            long[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private boolean queryFileExists() throws CsvValidationException, IOException {
        String filenameToMatch = getQueryFileName(false);
        // check if there exists a file where first part of name matches filenameToMatch
        String queriesDir = Main.inputFolder + "pointQueries/" + Main.datasetName + "/";
        File[] files = new File(queriesDir).listFiles();
        assert files != null;
        String filename = fileExists(filenameToMatch, files);
        if (filename != null) {
            System.out.println("File " + filename + " exists. Reading queries with exact frequency.");
            readWorkloadWithExact(queriesDir + filename);
            return true;
        }
        return false;
    }
    public void cleanDatasetEquiDepthBins(double percToDelete,
                                          double sizeFactor) throws IOException, CsvValidationException {
        // This will be a queries first -> dataset later approach.
        // We have queries in 10 bins of 100 queries each.
        // All queries in different bins are on distinct domains. No overlap.
        // When we have the queries, we can generate the dataset.
        // We want bin 0 to have answer size [2^0, 2^1], bin 1 [2^1, 2^2], bin 2 [2^2, 2^3], etc.
        // We want the queries to be on distinct domains, so we can generate the dataset in one go.
        this.sizeFactor = sizeFactor;

        // Generate queries
        Main.numQueries = 100 * numBins;
        int numQueries = Main.numQueries;
        int numAttrs = Main.numAttributes; // Change to numStoredAttributes if needed.

        int queriesPerBin = numQueries / numBins;
        int[] binStartDomain = new int[numBins];
        int[] binEndDomain = new int[numBins];

        for (int i = 0; i < numBins; i++) {
            binStartDomain[i] = i * 1000;
            binEndDomain[i] = (i + 1) * 1000;
        }
        int noiseDomainStart=binEndDomain[numBins-1] + 1000;
        int noiseDomainEnd=binEndDomain[numBins-1] + 2000;

        pointQueries = new long[numQueries][];

        Random predRng = new Random(this.repetition);
        Random domainRng = new Random(this.repetition + 1);
        pointQueryBinNumber = new int[numQueries];

        /*if (queryFileExists()) {
            // read queries and read dataset.
            readDatasetFromFile();
            int numToKeep = splitDeletes(percToDelete);
            return;
        }*/

        // Generate queries for each bin within the domain.
        for (int i = 0; i < numBins; i++) {
            for (int j = 0; j < queriesPerBin; j++) {
                pointQueries[i * queriesPerBin + j] = new long[numAttrs];
                //initialize with -1
                for (int k = 0; k < numAttrs; k++) {
                    pointQueries[i * queriesPerBin + j][k] = -1;
                }

                for (int p = 0; p < numPredicates; p++) {
                    pointQueries[i * queriesPerBin + j][p] = (long) (binStartDomain[i] +
                            (domainRng.nextDouble()) * (double) (binEndDomain[i] - binStartDomain[i]));
                }
//                int hasPredicate = 0;
//                while (hasPredicate < numPredicates) {
//                    int attr = predRng.nextInt(numAttrs);
//                    if (pointQueries[i * queriesPerBin + j][attr] == -1) {
//                        pointQueries[i * queriesPerBin + j][attr] = (long) (binStartDomain[i] +
//                                (domainRng.nextDouble()) * (double) (binEndDomain[i] - binStartDomain[i]));
//                        hasPredicate++;
//                    }
//                    }
                pointQueryBinNumber[i * queriesPerBin + j] = i;
            }
        }


        // Generate dataset
        ArrayList<Long[]> potDataset = new ArrayList<>();
        long id = 0;
        for (int i = 0; i < numBins; i++) {
            // Generate dataset for each bin.
            int numRecords = (int) ((int) Math.pow(2, i*8) * this.sizeFactor); //10
            // go over queries in bin i and add numRecords times.
            for (int j = 0; j < queriesPerBin; j++) {
                for (int k = 0; k < numRecords; k++) {
                    Long[] record = new Long[numAttrs + 1];
                    record[0] = id;
                    for (int l = 0; l < numAttrs; l++) {
                        if (pointQueries[i * queriesPerBin + j][l] != -1) {
                            record[l + 1] = pointQueries[i * queriesPerBin + j][l];
                        } else {
                            record[l + 1] = (long) (noiseDomainStart + (domainRng.nextDouble()) * (double) (noiseDomainEnd - noiseDomainStart));
                        }
                    }
                    potDataset.add(record);
                    id++;
                }
            }
        }
        System.out.println("Num queried records: " + potDataset.size());
        // Add noise
        //int numNoiseRecords = (int) Math.pow(2, numBins*2) * 2;
        int numNoiseRecords = (int) ((int) Math.pow(2, numBins*8) * this.sizeFactor);

        System.out.println("Num never queried records: " + numNoiseRecords);
        for (int i = 0; i < numNoiseRecords; i++) {
            Long[] record = new Long[numAttrs + 1];
            record[0] = id;
            for (int j = 0; j < numAttrs; j++) {
                record[j + 1] = (long) (noiseDomainStart + (domainRng.nextDouble()) * (double) (noiseDomainEnd - noiseDomainStart));
            }
            potDataset.add(record);
            id++;
        }

        dataset = new long[potDataset.size()][];
        for (int i = 0; i < potDataset.size(); i++) {
            dataset[i] = new long[numAttrs + 1];
            for (int j = 0; j < numAttrs + 1; j++) {
                dataset[i][j] = potDataset.get(i)[j];
            }
            potDataset.set(i, null); // Clear memory.
        }

        potDataset.clear();
        // dump dataset.
        System.gc();

        shuffleArray(dataset);
        int numToKeep = splitDeletes(percToDelete);

        computeExactPoint(datasetResidu);

        // Check for every point query the number of attributes that are not -1.
        pointQueriesNumAttrs = new int[pointQueries.length];
        for (int i = 0; i < pointQueries.length; i++) {
            for (int j = 0; j < numAttrs; j++) {
                if (pointQueries[i] == null) {
                    continue;
                }
                if (pointQueries[i][j] != -1) {
                    pointQueriesNumAttrs[i]++;
                }
            }
        }
        //writePointQueriesToFile();
        //writeDatasetToFile();
    }

    private void writeDatasetToFile() {
        String datasetName = getDatasetName();
        String datasetDir = Main.inputFolder + "data/" + Main.datasetName + "/";
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(datasetDir + datasetName));
            for (long[] longs : dataset) {
                String[] record = new String[longs.length];
                for (int j = 0; j < longs.length; j++) {
                    record[j] = String.valueOf(longs[j]);
                }
                writer.writeNext(record);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDatasetFromFile() {
        // read dataset
        String datasetName = getDatasetName();
        String datasetDir = Main.inputFolder + "data/" + Main.datasetName + "/";
        File f = new File(datasetDir + datasetName);
        if (!f.exists()) {
            throw new RuntimeException("Dataset file does not exist.");
        }
        readDataset(datasetName);

    }


    private void readDataset(String datasetName) {
        try {
            CSVReader reader = new CSVReader(new FileReader(datasetName));
            //BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datasetName));
            int numRecords = 0;
            String[] record = reader.readNext();
            ArrayList<String[]> records = new ArrayList<>();
            while (record != null) {
                records.add(record);
                record = reader.readNext();
                numRecords++;
            }
            dataset = new long[numRecords][];
            for (int i = 0; i < numRecords; i++) {
                dataset[i] = new long[records.get(i).length];
                for (int j = 0; j < records.get(i).length; j++) {
                    dataset[i][j] = Long.parseLong(records.get(i)[j]);
                }
            }
            records.clear();
            System.out.println("Read dataset with " + numRecords + " records.");
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private String fileExists(String filenameToMatch, File[] files) {
        for (File file : files) {
            if (file.getName().startsWith(filenameToMatch)) {
                return file.getName();
            }
        }
        return null;
    }


    private void getPointQueries(double percToDelete, double maxPercToDelete) throws CsvValidationException, IOException {
        if (queryFileExists()) {
            return;
        }
        if (maxPercToDelete == percToDelete) {
            generateQueries();
        } else {
            throw new RuntimeException("Queries do not exist and maxPercToDelete is different from percToDelete. Not implemented.");
        }
//        String queriesDir = Main.inputFolder + "pointQueries/" + Main.datasetName + "/";
//        // num Queries unknown beforehand
//        String filenameToMatch = getQueryFileName();
//
////        "queries_" + deletes + Main.datasetName  +
////                "_numStoredAttrs_" + Main.numStoredAttributes + "_queriesFromDomain_" + Main.queriesFromDomain +
////                "_orgN" + dataset.length + "_numQueries_";
//        // check if there exists a file where first part of name matches filenameToMatch
//        File[] files = new File(queriesDir).listFiles();
//        assert files != null;
//        String filename = fileExists(filenameToMatch, files);
//        if (filename != null) {
//            System.out.println("File " + filename + " exists. Reading queries with exact frequency.");
//            // parse numQueries
//            readWorkloadWithExact(queriesDir + filename);
//            return;
//        }

        //deletes = deletes(numToKeep, maxPercToDelete, maxPercToDelete);
//        String filenameToMatchMaxPerc = "queries_" + deletes + Main.datasetName +
//                    "_numStoredAttrs_" + Main.numStoredAttributes + "_queriesFromDomain_" + Main.queriesFromDomain + "_orgN" + dataset.length + "_numQueries_";
//        filename = fileExists(filenameToMatchMaxPerc, files);
//        if (filename != null) {
//            System.out.println("File " + filename + " exists. Reading queries for different # deletes, but same N.");
//            // parse numQueries
//            numQueries = Integer.parseInt(filename.substring(filenameToMatchMaxPerc.length(), filename.length() - 4));
//            Main.numQueries = numQueries;
//            System.out.println("NumQueries: " + numQueries);
//            readWorkloadWithoutExact(queriesDir + filename, numToKeep, percToDelete, maxPercToDelete);
//            return;
//        }

//        String filenameToMatchDiffN = "queries_" + deletes + Main.datasetName +
//                "_numStoredAttrs_" + Main.numStoredAttributes + "_queriesFromDomain_" + Main.queriesFromDomain + "_numQueries_";
//        filename = fileExists(filenameToMatchDiffN, files);
//        if (filename != null) {
//            String subString = filename.substring(filenameToMatchDiffN.length(), filename.length() - 4);
//
//            Main.numQueries = Integer.parseInt(subString);
//            System.out.println("NumQueries: " + Main.numQueries);
//            readWorkloadWithoutExact(queriesDir + filename, numToKeep, percToDelete, maxPercToDelete);
//            return;
//        }

    }

    public void generateQueries() throws IOException {
        if (Main.rangeQueries) {
            generateRangeQueries();
            computeExactRange();
            h.writeRangeQueriesToFile();
        } else {
            generatePointQueriesAlt(datasetResidu);// todo: queryGenProb based on ratio numToKeep / dataset.length
            System.out.println("Generated queries, computing exact answers.");
            computeExactPoint(datasetResidu);
            // recompute exact answers based on neverDeleted

            writePointQueriesToFile();
        }
    }

    private void getRangeQueries() {
    }


    private void clean() {
        for (int i = 0; i <original.dataset.size(); i++) {
            long[] newRecord = cleanRecord(original.dataset.get(i));
            dataset[i] = newRecord;
        }
    }

    private void shuffleAttributes(int repetition) {
    }


    private long[] cleanRecord(Record r) {

        long[] newRecord = new long[1 + Main.numStoredAttributes];
        newRecord[0] = r.id;
        int i = 1;
        for (int j = 0; j < cleanIds.length; j++) {
            if (j >= Main.numStoredAttributes) {
                break;
            }
            newRecord[i] = r.record[cleanIds[j]];
            i++;
        }
        return newRecord;
    }

//    private String deletes(double percToDelete, double maxPercToDelete) {
//        return deletes(0, percToDelete, maxPercToDelete);
//    }

//    private String deletes(int numToKeep, double percToDelete, double maxPercToDelete) {
//        if (Main.withDeletes) {
//            if (Main.spreadOutDeletes) {
//                return "withDeletes_spreadOut_actDelPerc=" + percToDelete + "_maxDelPerc=" + maxPercToDelete + "_";
//            } else {
//                return "withDeletes_" + numToKeep + "_";
//            }
//        } else {
//            return "";
//        }
//    }
    // Generate queries on the clean dataset.

    private void writePointQueriesToFile() throws IOException {
        boolean init = false;
        String queriesWithExactName = getQueryFileName(true);
        /*+ "queries_" + deletes + Main.datasetName +
                "_numStoredAttrs_" + Main.numStoredAttributes + "_sizeFactor_" + sizeFactor +
                "_queriesFromDomain_" + Main.queriesFromDomain +
                "_numQueries_" + Main.numQueries + ".csv";*/
//        String queriesWithoutExactName = queriesDir + "queries_" + deletes + Main.datasetName +
//                "_numStoredAttrs_" + Main.numStoredAttributes + "_sizeFactor_" + sizeFactor +
//                "_queriesFromDomain_" + Main.queriesFromDomain + "_numQueries_" +
//                Main.numQueries +".csv";
        String queryDir = Main.inputFolder + "pointQueries/" + Main.datasetName + "/";
        File f = new File(queryDir + queriesWithExactName);
        if (!f.exists()) {
            init = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(queryDir + queriesWithExactName, true));

        if (init) { // Initialize file with header.
            String[] header = new String[]{"numStoredAttributes","queryId", "query", "numPreds","binNumber",
                    "exactAnswer", "unionSize", "exactDeletes", "unionDeletes"};
            writer.writeNext(header);
        }

        for (int i = 0; i < Main.numQueries; i++) {
            String[] nextLine = new String[9];
            nextLine[0] = String.valueOf(Main.numStoredAttributes);
            nextLine[1] = String.valueOf(i);
            nextLine[2] = parsePointQueryToString(pointQueries[i]);
            nextLine[3] = String.valueOf(pointQueriesNumAttrs[i]);
            nextLine[4] = String.valueOf(pointQueryBinNumber[i]);
            nextLine[5] = String.valueOf(pointQueryAnswers[i]);
            nextLine[6] = String.valueOf(pointQueryUnion[i]);
            nextLine[7] = String.valueOf(pointQueryAnswersDeletes[i]);
            nextLine[8] = String.valueOf(pointQueryUnionDeletes[i]);
            writer.writeNext(nextLine);
        }
        writer.close();
    }

    private String getQueryFileName(boolean withNumberOfQueries) {
        //String queriesDir = Main.inputFolder + "pointQueries/" + Main.datasetName + "/";
        String filename = "queries_" +
                Main.datasetName +
                "_sizeFactor_" + this.sizeFactor +
                "_repetition_" + this.repetition +
                "_numPredicates_" + this.numPredicates +
                "_numBins_" + this.numBins;
        if (withNumberOfQueries) {
            return filename + "_numQueries_" + Main.numQueries + ".csv";
        } else {
            return filename;
        }
    }


    private String getDatasetName() {
        return "dataset_" +
                Main.datasetName +
                "_sizeFactor_" + this.sizeFactor +
                "_repetition_" + this.repetition +
                "_numStoredAttrs_" + Main.numStoredAttributes +
                ".csv";
    }

    public void computeExactPoint(long[][] dataset) {
        pointQueryAnswers = new int[pointQueries.length];
        pointQueryAnswersDeletes = new int[pointQueries.length];
        pointQueryUnion = new int[pointQueries.length];
        pointQueryUnionDeletes = new int[pointQueries.length];
        // loop over dataset, for each record, check if it is in one of the queries, if so, add to answer.
        computeGroundTruth(dataset, pointQueryAnswers, pointQueryUnion);

        //        computeGroundTruth(datasetNegUpdates, pointQueryAnswersDeletes, pointQueryUnionDeletes);
    }

    private void computeGroundTruth(long[][] dataset, int[] pointQueryAnswers, int[] pointQueryUnion) {
        // make pointQueryAnswers 0
        System.out.println("Computing exact answers");
        for (int i = 0; i < pointQueryAnswers.length; i++) {
            pointQueryAnswers[i] = 0;
            pointQueryUnion[i] = 0;
        }

        for (long[] longs : dataset) {
            for (int j = 0; j < pointQueries.length; j++) {
                boolean inQuery = true;
                boolean inUnion = false;
                for (int k = 0; k < dataset[0].length - 1; k++) {
                    if (pointQueries[j][k] != -1 && pointQueries[j][k] != longs[k + 1]) {
                        inQuery = false;
                        break;
                    }
                    if (pointQueries[j][k] != -1 && pointQueries[j][k] == longs[k + 1]) {
                        inUnion = true;
                        //break;
                    }
                }
                if (inQuery) {
                    pointQueryAnswers[j]++;
                }
                if (inUnion) {
                    pointQueryUnion[j]++;
                }
            }
        }
    }

    private void computeExactRange() {
        rangeQueryAnswers = new int[Main.numQueries];
        // loop over dataset, for each record, check if it is in one of the queries, if so, add to answer.
        for (long[] longs : dataset) {
            for (int j = 0; j < rangeQueries.length; j++) {
                boolean inQuery = true;
                for (int k = 0; k < Main.numStoredAttributes; k++) {
                    if (rangeQueries[j][0][k] != -1 && rangeQueries[j][0][k] > longs[k + 1]) {
                        inQuery = false;
                        break;
                    }
                    if (rangeQueries[j][1][k] != -1 && rangeQueries[j][1][k] < longs[k + 1]) {
                        inQuery = false;
                        break;
                    }
                }
                if (inQuery) {
                    rangeQueryAnswers[j]++;
                }
            }
        }
    }

    private void generateRangeQueries() {
        throw new RuntimeException("Not implemented");
        //TODO: implement
    }

    private void generatePointQueries(long[][] dataset, double queryGenProb) {
        // Make point queries on the clean dataset.
        pointQueries = new long[Main.numQueries][];
        // initialize with -1s
        for (int i = 0; i < Main.numQueries; i++) {
            pointQueries[i] = new long[Main.numStoredAttributes];
            for (int j = 0; j < Main.numStoredAttributes; j++) {
                pointQueries[i][j] = -1;
            }
        }
        int generatedQueries = 0;
        Random rng = new Random(0);

        for (int j = 0; j < dataset.length; j++) {
                // Select random record from dataset with probability 0.05%.
                if (rng.nextDouble() < queryGenProb) {
                    // For each possible combination of predicates, generate a query.
                    for (int k = 1; k < Math.pow(2, Main.numStoredAttributes); k++) {
                        long[] query = new long[Main.numStoredAttributes];
                        // Convert k to binary string.
                        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(k));
                        // Pad with zeros.
                        while (binaryString.length() < Main.numStoredAttributes) {
                            binaryString.insert(0, "0");
                        }
                        // Make query.
                        for (int l = 0; l < Main.numStoredAttributes; l++) {
                            if (binaryString.charAt(l) == '1') {
                                query[l] = dataset[j][l + 1];
                            } else {
                                query[l] = -1;
                            }
                        }
                        // Check if query is already in list of queries.
                        boolean alreadyInList = false;
                        for (int m = 0; m < generatedQueries; m++) {
                            if (Arrays.equals(query, pointQueries[m])) {
                                alreadyInList = true;
                                break;
                            }
                        }
                        if (!alreadyInList) {
                            pointQueries[generatedQueries] = query;
                            generatedQueries++;
                            if (generatedQueries == Main.numQueries) {
                                break;
                            }
                        }
                    }
                    if (generatedQueries >= Main.numQueries) {
                        break;
                    }
                }
        }

        // Check for every point query the number of attributes that are not -1.
        pointQueriesNumAttrs = new int[Main.numQueries];
        for (int i = 0; i < generatedQueries; i++) {
            for (int j = 0; j < Main.numStoredAttributes; j++) {
                if (pointQueries[i][j] != -1) {
                    pointQueriesNumAttrs[i]++;
                }
            }
        }
    }


    private void generatePointQueriesAlt(long[][] dataset) {
        // Make point queries on the clean dataset.
        //ArrayList<Long[]> potentialQueries = new ArrayList<>();

        //getAttrsToChoose(repetition);
        HashMap<Long[], Integer> potQueries = new HashMap<>();
        //ArrayList<Integer> potentialQueriesExactAnswer = new ArrayList<>();

        // initialize with -1s
        int generatedQueries = 0;

        for (int j = 0; j < dataset.length; j++) {
            // Select random record from dataset with probability 0.05%.
            Long[] query = new Long[Main.numStoredAttributes];
                // Convert k to binary string.

            for (int l = 0; l < Main.numStoredAttributes; l++) {
                if (l < numPredicates) {
                    query[l] = dataset[j][l + 1];
                } else {
                    query[l] = (long) -1;
                }
            }
            // Check if query is already in list of queries.
            boolean alreadyInList = false;
            // check if query in set

            for (Long[] key : potQueries.keySet()) {
                if (Arrays.equals(key, query)) {
                    alreadyInList = true;
                    potQueries.put(key, potQueries.get(key) + 1);
                }
            }

            if (!alreadyInList) {
                potQueries.put(query, 1);
                //potentialQueries.add(query);
                //potentialQueriesExactAnswer.add(1);
                generatedQueries++;
            }

        }
        System.out.println("Generated " + generatedQueries + " potential queries.");

        // Sort queries by frequency of occurrence.
        //
        if (Main.numQueries > generatedQueries) {
            throw new RuntimeException("Number of queries to generate is larger than number of potential queries.");
        }
        // Sort queries by frequency of occurrence.
        // select Main.numQueries uniformly over distribution of the number of times a query occurs, so that every frequency quantile is represented equally.

        // Sort queries by frequency of occurrence as indicated in potentialQueriesExactAnswer.



        long[][] potPointQueries = new long[generatedQueries][];
        int[] potPointQueryAnswers = new int[generatedQueries];
        Set<Long[]> keys = potQueries.keySet();
        Long[][] keys_array = new Long[keys.size()][];
        keys.toArray(keys_array);
        for (int i = 0; i < generatedQueries; i++) {
            potPointQueries[i] = new long[Main.numStoredAttributes];

            potPointQueryAnswers[i] = potQueries.get(keys_array[i]);
            for (int j = 0; j < Main.numStoredAttributes; j++) {
                potPointQueries[i][j] = (long) keys_array[i][j];
            }
        }

        Integer[] indices = new Integer[potPointQueryAnswers.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingInt(i -> potPointQueryAnswers[i]));
        //List<long[]> selectedQueries = new ArrayList<>();
        //List<Integer> selectedAnswers = new ArrayList<>();
        int minDomain = potPointQueryAnswers[indices[0]];
        int maxDomain = potPointQueryAnswers[indices[indices.length - 1]];
        int domain = maxDomain - minDomain;
        int numQueriesPerBin = 100;


        Random random = new Random(0);
        //if (Main.queriesFromDomain){
        pointQueriesEmpty = new long[numBins * numQueriesPerBin][];
        pointQueryAnswersEmpty = new int[numBins * numQueriesPerBin];
        pointQueryBinNumberEmpty = new int[numBins * numQueriesPerBin];

        int domainBinSize = domain/ numBins;
        int[] splitPoints = new int[numBins - 1];
        for (int i = 0; i < numBins - 1; i++) {
            splitPoints[i] = minDomain + (i + 1) * domainBinSize;
        }
        // go over sorted indices, and add to pointQueries and pointQueryAnswers.
        // choose query randomly from bin,
        // bin size based on potPointQueryAnswers
        int[] splitIndices = new int[numBins];
        int splitIndex = 0;
        for (int i : indices) { // potPointQueryAnswers indices not sorted.
            if (splitIndex>=splitPoints.length){
                break;
            }
            if (potPointQueryAnswers[i] > splitPoints[splitIndex]) {
                splitIndices[splitIndex + 1] = splitIndices[splitIndex];
                splitIndex++;
            }
            splitIndices[splitIndex]++;
        }

        Main.numQueries = 0;
        for (int i = 0; i < numBins; i++) {
            int binSize = 0;
            if (i == 0) {
                binSize = splitIndices[i];
            } else {
                binSize = splitIndices[i] - splitIndices[i - 1];
            }
            addRandomQueries(numQueriesPerBin, i, splitIndices, indices, binSize, potPointQueries, potPointQueryAnswers, random);
        }
        // Now we know actual # queries, resize pointQueries array to be pointQueries[Main.numQueries]
        pointQueries = new long[Main.numQueries][];
        pointQueryAnswers = new int[Main.numQueries];
        pointQueryBinNumber = new int[Main.numQueries];
        System.arraycopy(pointQueriesEmpty, 0, pointQueries, 0, Main.numQueries);
        System.arraycopy(pointQueryAnswersEmpty, 0, pointQueryAnswers, 0, Main.numQueries);
        System.arraycopy(pointQueryBinNumberEmpty, 0, pointQueryBinNumber, 0, Main.numQueries);

//        } else {
//            pointQueries = new long[Main.numQueries][];
//            pointQueryAnswers = new int[Main.numQueries];
//            int binSize = indices.length / Main.numQueries;
//            for (int i = 0; i < Main.numQueries; i++) {
//                int startIndex = i * binSize;
//                int endIndex = (i + 1) * binSize;
//
//                int randomIndex = indices[random.nextInt(endIndex - startIndex) + startIndex];
//
//                pointQueries[i] = potPointQueries[randomIndex];
//                pointQueryAnswers[i] = potPointQueryAnswers[randomIndex];
//            }
//        }

        //sort map by value
        // add query of every bin to pointQueries
        // we have Main.numQueries bins
        // Check for every point query the number of attributes that are not -1.
        pointQueriesNumAttrs = new int[pointQueries.length];
        for (int i = 0; i < pointQueriesNumAttrs.length; i++) {
            for (int j = 0; j < Main.numStoredAttributes; j++) {
                if (pointQueries[i] == null) {
                    continue;
                }
                if (pointQueries[i][j] != -1) {
                    pointQueriesNumAttrs[i]++;
                }
            }
        }
    }

    private void addRandomQueries(int numQueriesPerBin, int i, int[] splitIndices, Integer[] indices, int binSize,
                                  long[][] potPointQueries, int[] potPointQueryAnswers, Random random) {
        int startPos = 0;
        if (i > 0) {
            startPos = splitIndices[i - 1];
        }
        if (binSize <= 0) {
            return;
        }
        int[] randomIndices;
        if (numQueriesPerBin >= binSize) {
            // add all queries from bin
            randomIndices = new int[binSize];
            for (int j = 0; j < binSize; j++) {
                randomIndices[j] = startPos + j;
            }
        } else {
            randomIndices = new int[numQueriesPerBin];
            for (int j = 0; j < numQueriesPerBin; j++) {
                int toAdd = startPos + random.nextInt(binSize);
                boolean alreadyIn = false;
                alreadyIn = Arrays.stream(randomIndices).anyMatch(x -> x == toAdd);
                if (alreadyIn) {
                    j--;
                } else {
                    randomIndices[j] = toAdd;
                }
            }
        }
        for (int randomIndex : randomIndices) {
            pointQueriesEmpty[Main.numQueries] = potPointQueries[indices[randomIndex]];
            pointQueryAnswersEmpty[Main.numQueries] = potPointQueryAnswers[indices[randomIndex]];
            pointQueryBinNumberEmpty[Main.numQueries] = i;
            Main.numQueries++;
        }
    }
    // Parser from index in new record to name of attribute in original record.
    public String parseIndex(int i) {

        if (i == 0) {
            return "id";
        } else {
            return Parser.getAttributeName(cleanIds[i - 1]);
        }
    }

    public int parseName(String name) {
        int org_pos;
        if (name.equals("id")) {
            org_pos = 0;
        } else {
            org_pos = Parser.attrMap(name);// + 1;
        }
        for (int i = 0; i < cleanIds.length; i++) {
            if (cleanIds[i] == org_pos) {
                return i;
            }
        }
        throw new RuntimeException("Attribute not found in cleanIds");
    }

    public void getDistributions() {
        // Use HashSet for faster lookup of unique values
        HashSet<Long>[] unique = new HashSet[Main.numStoredAttributes];
        for (int i = 0; i < unique.length; i++) {
            unique[i] = new HashSet<>();
        }

        // Count unique values for each attribute
        for (int i = 0; i < dataset.length; i++) {
            for (int j = 0; j < Main.numStoredAttributes; j++) {
                if (dataset[i][j + 1] != -1) {
                    unique[j].add(dataset[i][j + 1]);
                }
            }
        }

        // Print number of unique values for each attribute
        for (int i = 0; i < Main.numStoredAttributes; i++) {
            System.out.println("Attribute with index " + i + " ," + parseIndex(i + 1) + " has " + unique[i].size() + " unique values.");
        }
    }


    public long getMemoryUsage() {
        long usg = (long) dataset.length * (Main.numStoredAttributes + 1) * 32L;
        return usg;
    }


    private void readWorkloadWithExact(String fileName) throws IOException, CsvValidationException {
        // get numQueries from filename, it is between "_numQueries_" and ".csv"; not a fixed length.
        int start = fileName.indexOf("_numQueries_") + 12;
        int end = fileName.indexOf(".csv");
        Main.numQueries = Integer.parseInt(fileName.substring(start, end));
        System.out.println("NumQueries: " + Main.numQueries);
        pointQueries = new long[Main.numQueries][];
        pointQueryAnswers = new int[pointQueries.length];
        pointQueryBinNumber = new int[pointQueries.length];
        pointQueriesNumAttrs = new int[pointQueries.length];
        pointQueryUnion = new int[pointQueries.length];
        pointQueryAnswersDeletes = new int[pointQueries.length];
        pointQueryUnionDeletes = new int[pointQueries.length];
        // Read workload from file.
        CSVReader reader = new CSVReader(new FileReader(fileName));
        String[] nextLine;
        reader.readNext(); // skip header
        try {
            while ((nextLine = reader.readNext()) != null) {
                int numStoredAttributesInFile = Integer.parseInt(nextLine[0]);
//                if (numStoredAttributesInFile != Main.numStoredAttributes) {
//                    throw new RuntimeException("numStoredAttributes in workload file does not match numStoredAttributes in Main");
//                }
                int queryId = Integer.parseInt(nextLine[1]);
                String query = nextLine[2];
                // Method that fills query with "-1" for difference numStoredAttributes - Main.numStoredAttributes
                //query = fillQuery(query, numStoredAttributesInFile); //TODO: fill query with -1 for missing attributes
                int numPreds = Integer.parseInt(nextLine[3]);
                int exactAnswer = Integer.parseInt(nextLine[5]);
                pointQueries[queryId] = parseStringToPointQuery(query);
                pointQueriesNumAttrs[queryId] = numPreds;
                pointQueryBinNumber[queryId] = Integer.parseInt(nextLine[4]);
                pointQueryAnswers[queryId] = exactAnswer;
                pointQueryUnion[queryId] = Integer.parseInt(nextLine[6]);
                pointQueryAnswersDeletes[queryId] = Integer.parseInt(nextLine[7]);
                pointQueryUnionDeletes[queryId] = Integer.parseInt(nextLine[8]);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        reader.close();

    }

    private String fillQuery(String query, int numStoredAttributesInFile) {
        StringBuilder result = new StringBuilder(query);
        for (int i = numStoredAttributesInFile; i < Main.numStoredAttributes; i++) {
            result.append(parseIndex(i + 1)).append("=-1").append(", ");
        }
        return result.toString();

    }

    private void readWorkloadWithoutExact(String fileName, int numToKeep, double percToDelete, double maxPercToDelete) throws IOException, CsvValidationException {
        pointQueries = new long[Main.numQueries][];
        pointQueriesNumAttrs = new int[Main.numQueries];
        pointQueryBinNumber = new int[Main.numQueries];
        // Read workload from file.
        CSVReader reader = new CSVReader(new FileReader(fileName));
        String[] nextLine;
        reader.readNext(); // skip header
        try {
            while ((nextLine = reader.readNext()) != null) {
                int numStoredAttributes = Integer.parseInt(nextLine[0]);
                if (numStoredAttributes != Main.numStoredAttributes) {
                    throw new RuntimeException("numStoredAttributes in workload file does not match numStoredAttributes in Main");
                }
                int queryId = Integer.parseInt(nextLine[1]);
                String query = nextLine[2];
                int numPreds = Integer.parseInt(nextLine[3]);
                pointQueries[queryId] = parseStringToPointQuery(query);
                pointQueriesNumAttrs[queryId] = numPreds;
                pointQueryBinNumber[queryId] = Integer.parseInt(nextLine[4]);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        reader.close();
        if (fileName.contains("Deletes")) {
            computeExactPoint(datasetResidu);
        } else {
            computeExactPoint(dataset);
        }
        writePointQueriesToFile();

    }

    public String parsePointQueryToString(long[] pointQuery) {
        // Convert point query to string with attribute name + value if val != -1.
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Main.numStoredAttributes; i++) {
            if (pointQuery[i] != -1) {
                result.append(parseIndex(i + 1)).append("=").append(pointQuery[i]).append(", ");
            }
        }
        return result.toString();
    }

    public long[] parseStringToPointQuery(String query) {
        // Convert string to point query.
        String[] predicates = query.split(", ");
        long[] pointQuery = new long[Main.numStoredAttributes];
        for (int i = 0; i < Main.numStoredAttributes; i++) {
            pointQuery[i] = -1;
        }
        for (String s : predicates) {
            String[] predicate = s.split("=");
            int index = parseName(predicate[0]);
            pointQuery[index] = Long.parseLong(predicate[1]);
        }
        return pointQuery;
    }

    public void setNoiseUpdates(int numNoiseUpdates) {
        // fill noiseUpdates with all numNoiseUpdates at end of negUpdates
        if (numNoiseUpdates > datasetNegUpdates.length) {
            throw new RuntimeException("numNoiseUpdates > numToDelete, namely "
                    + numNoiseUpdates + " > " + datasetNegUpdates.length);
        }
        noiseUpdates = new long[numNoiseUpdates][];
        System.arraycopy(datasetNegUpdates, datasetNegUpdates.length
                - numNoiseUpdates, noiseUpdates, 0, numNoiseUpdates);
        System.out.println("Noise updates size: " + noiseUpdates.length);
        //computeGroundTruth(noiseUpdates, pointQueryAnswersDeletes, pointQueryUnionDeletes);
        if (Main.spreadOutDeletes) {
            // Add all recs to datasetInsertsSpreadOut, shuffle, and decide when we can delete all noiseUpdates.
            // make array with indices of noiseUpdates
            long[] noiseIndices = Arrays.stream(noiseUpdates)
                    .mapToLong(arr -> arr[0])
                    .sorted()
                    .toArray();//new long[noiseUpdates.length];
//            for (int i = 0; i < noiseUpdates.length; i++) {
//                noiseIndices[i] = noiseUpdates[i][0];
//            }
//            // sort noiseIndices
//            Arrays.sort(noiseIndices);

            this.datasetInsertsSpreadOut = new long[noiseUpdates.length + datasetResidu.length][];
            System.arraycopy(datasetResidu, 0, datasetInsertsSpreadOut, 0, datasetResidu.length);
            System.arraycopy(noiseUpdates, 0, datasetInsertsSpreadOut, datasetResidu.length, noiseUpdates.length);
            // shuffle datasetInsertsSpreadOut
            shuffleArray(datasetInsertsSpreadOut);
            datasetAllSpreadOut = new long[datasetResidu.length + 2 * noiseUpdates.length][];
            isDelete = new boolean[datasetResidu.length + 2 * noiseUpdates.length];
            Arrays.fill(isDelete, false);
            //ArrayList<long[]> deleteQueue = new ArrayList<>();
            //ArrayList<long[]> deleteQueue = new ArrayList<>();
            //HashMap<Integer, Integer> indicesToRemove = new HashMap<>();
            int c = 0;
            Random r = new Random(0);
            for (long[] record : datasetInsertsSpreadOut) {
                boolean inserted = false;
                while (!inserted && c < datasetAllSpreadOut.length) {
                    if (datasetAllSpreadOut[c] == null) {
                        datasetAllSpreadOut[c] = record;
                        inserted = true;
                    }
                    c++;
                }

                // check if datasetInsertsSpreadOut[i] is in noiseUpdates
                if (Arrays.binarySearch(noiseIndices, record[0]) >= 0) {
                    // get random index between c and datasetInsertsSpreadOut.length
                    // if datasetInsertsSpreadOut[index] == null, we can insert the delete here.
                    // if not, we choose another random index.
                    boolean insertedDelete = false;
                    int maxTries = 100;
                    while (!insertedDelete && maxTries > 0) {
                        int index = r.nextInt(c, datasetAllSpreadOut.length);
                        if (datasetAllSpreadOut[index] == null) {
                            datasetAllSpreadOut[index] = record;
                            isDelete[index] = true;
                            insertedDelete = true;
                        }
                        maxTries--;
                    }
                    if (!insertedDelete) {
                        // reverse over datasetAllSpreadOut and insert delete at first empty spot.
                        for (int j = datasetAllSpreadOut.length - 1; j >= c; j--) {
                            if (datasetAllSpreadOut[j] == null) {
                                datasetAllSpreadOut[j] = record;
                                isDelete[j] = true;
                                break;
                            }
                            if (j == c) {
                                throw new RuntimeException("Could not insert delete in datasetAllSpreadOut.");
                            }
                        }
                    }

                }
//
//                if (!deleteQueue.isEmpty() && r.nextDouble() < 0.5) {
//                    int indexToDelete = r.nextInt(0, deleteQueue.size());
//                    if (!indicesToRemove.containsKey(indexToDelete)) {
//                        indicesToRemove.put(indexToDelete, c);
//                        c++;
//                    }
//                }
//
//                if (indicesToRemove.size() > 1000000) {
//                    System.out.println("Removing " + indicesToRemove.size() + " records from datasetAllSpreadOut.");
//                    System.out.println("Delete Queue size: " + deleteQueue.size());
//                    for (int index_c: indicesToRemove.keySet()) {
//                        datasetAllSpreadOut[indicesToRemove.get(index_c)] = deleteQueue.get(index_c);
//                        isDelete[indicesToRemove.get(index_c)] = true;
//                    }
//                    deleteQueue.removeAll(indicesToRemove.keySet().stream().map(deleteQueue::get).toList());
//                    indicesToRemove.clear();
//                    System.out.println("DeleteQueue size after deletes: " + deleteQueue.size());
//                }
//            }
//            // add remaining deletes
//            for (long[] record : deleteQueue) {
//                datasetAllSpreadOut[c] = record;
//                isDelete[c] = true;
//                c++;
//            }
            }
        }
    }

    public int setDatasets(boolean[] neverDeleted) {
        // reset datasetAfterDeletes with neverDeleted
        int numToKeep = 0;
        for (boolean b : neverDeleted) {
            if (b) {
                numToKeep++;
            }
        }
        datasetResidu = new long[numToKeep][];
        datasetNegUpdates = new long[dataset.length - numToKeep][];
        int j = 0;
        for (int i = 0; i < neverDeleted.length; i++) {
            if (neverDeleted[i]) {
                datasetResidu[j] = dataset[i];
                j++;
            } else {
                datasetNegUpdates[i - j] = dataset[i];
            }
        }
        return numToKeep;
    }

}

package omni.datasets.caida;

import omni.Config;
import omni.datasets.Record.RecordUtils;

import java.io.*;
import java.util.*;

public class CAIDADataset {
    private final Config config;
    private String datasetFileName;
    private int datasetResiduSize;
    private int noiseSize;
    private String queryFileName;
    private long[][] pointQueries;
    private int[] pointQueryAnswers;
    private int[] pointQueryUnion;

    private int[] pointQueriesNumAttrs;
    private int[] pointQueryBinNumber;

    public CAIDADataset(Config config) {
        this.config = config;
    }

    private int countCsvRecords(String subfolder) {
        File countFile = new File(subfolder, "count.txt");
        if (countFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(countFile))) {
                return Integer.parseInt(br.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Count file does not exist");
    }


    private void setupResiduDataset(int sizeFactor) {
        String subfolder = config.readFolder + "input/data/CAIDA/" + sizeFactor + "/" + 0.0 + "/";
        datasetFileName = subfolder + "final_stream_" + 0.0 + ".csv";
//        datasetFileName = config.readFolder + "input/data/" + config.datasetName + "/" + config.datasetName +".csv";
        datasetResiduSize = countCsvRecords(subfolder);
    }

    private void setupDataset(double perc, int sizeFactor) {
        String subfolder = config.readFolder + "input/data/CAIDA/" + sizeFactor + "/" + perc+ "/";
        datasetFileName = subfolder + "final_stream_" + perc + ".csv";
        if (datasetResiduSize == 0) {
            throw new RuntimeException("Dataset Residu size is zero");
        }
        noiseSize = (countCsvRecords(subfolder) - datasetResiduSize)/2;
    }

    public void generateQueries(int sizeFactor) throws IOException {
        setupResiduDataset(sizeFactor); // Use 0.0 to generate queries on the residu dataset
        if (config.pvldb_queries) {
            generateQueriesPVLDB();
        } else {
            generateQueriesVLDBJ();
        }
    }

    public void loader(int sizeFactor) {
        setupResiduDataset(sizeFactor);
        loadQueries();
    }

    private void loadPointQueryArrays(ArrayList<long[]> pointQueriesList, ArrayList<Integer> pointQueryAnswersList, ArrayList<Integer> pointQueryUnionList, int numAttrs) {
        pointQueries = new long[pointQueriesList.size()][];
        pointQueryAnswers = new int[pointQueries.length];
        pointQueryUnion = new int[pointQueries.length];
        pointQueriesNumAttrs = new int[pointQueries.length];
        pointQueryBinNumber = new int[pointQueries.length];
        for (int i = 0; i < pointQueriesList.size(); i++) {
            pointQueries[i] = pointQueriesList.get(i);
            pointQueryAnswers[i] = pointQueryAnswersList.get(i);
            pointQueryUnion[i] = pointQueryUnionList.get(i);
        }

        computeQueryStats(numAttrs);
    }

    private void computeQueryStats(int numAttrs) {
        for (int i = 0; i < pointQueries.length; i++) {
            for (int j = 0; j < numAttrs; j++) {
                if (!RecordUtils.flexibleEquals(pointQueries[i][j], -1)) {
                    pointQueriesNumAttrs[i]++;
                }
            }
            pointQueryBinNumber[i] = i;
        }
    }

    private void loadQueries() {
        ArrayList<long[]> pointQueriesList = new ArrayList<>();
        ArrayList<Integer> pointQueryAnswersList = new ArrayList<>();
        ArrayList<Integer> pointQueryUnionList = new ArrayList<>();
        queryFileName = setQueryFileName(datasetFileName, config.pvldb_queries);
        int numAttrs = config.numStoredAttributes;
        // load queries and pointQueryAnswers from file
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFileName))) {
            String line;
            int queryCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id")) continue; // Skip header line

                String[] parts = line.split(",");
                if (parts.length != numAttrs + 3) { // +2 for answer and union
                    throw new RuntimeException("Record does not match expected number of attributes: " + line);
                }
                long[] query = new long[numAttrs];
                for (int i = 0; i < numAttrs; i++) {
                    query[i] = Long.parseLong(parts[i + 1]); // Skip id
                }
                pointQueriesList.add(query);
                pointQueryAnswersList.add(Integer.parseInt(parts[numAttrs + 1]));
                pointQueryUnionList.add(Integer.parseInt(parts[numAttrs + 2]));
                queryCount++;
            }
        } catch (IOException e) {
            System.err.println("Error reading dataset file: " + datasetFileName);
            e.printStackTrace();
        }

        // Convert ArrayLists to arrays
        loadPointQueryArrays(pointQueriesList, pointQueryAnswersList, pointQueryUnionList, numAttrs);
    }

    private void generateQueriesVLDBJ() throws IOException {
        int numAttrs = config.numStoredAttributes;
        pointQueries = new long[config.numSelectedRecForQueries * config.numPredicates][];

        Set<Integer> selectedIndices = selectRandomIndices(datasetResiduSize, config.numSelectedRecForQueries);
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetFileName))) {
            populatePointQueriesString(reader, numAttrs, selectedIndices);
        } catch (IOException e) {
            System.err.println("Error opening file for queries: " + datasetFileName);
            throw e;
        }

        pointQueryAnswers = new int[pointQueries.length];
        pointQueryUnion = new int[pointQueries.length];
        pointQueriesNumAttrs = new int[pointQueries.length];
        pointQueryBinNumber = new int[pointQueries.length];
        applyPredicates(numAttrs);
        deduplicateQueries(numAttrs);
        computeExactAnswers();
        computeQueryStats(numAttrs);
    }

    private void generateQueriesPVLDB() throws IOException {
        int numAttrs = config.numStoredAttributes;
        pointQueries = new long[config.numSelectedRecForQueries * config.numPredicates][];

        Set<Integer> selectedIndices = selectRandomIndices(datasetResiduSize, config.numSelectedRecForQueries);
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetFileName))) {
            populatePointQueriesString(reader, numAttrs, selectedIndices);
        } catch (IOException e) {
            System.err.println("Error opening file for queries: " + datasetFileName);
            throw e;
        }

        pointQueryAnswers = new int[pointQueries.length];
        pointQueryUnion = new int[pointQueries.length];
        pointQueriesNumAttrs = new int[pointQueries.length];
        pointQueryBinNumber = new int[pointQueries.length];
        applyPredicates(numAttrs);
        deduplicateQueries(numAttrs);
        computeExactAnswers();
        computeQueryStats(numAttrs);
    }

    private Set<Integer> selectRandomIndices(int datasetSize, int numQueries) {
        Set<Integer> randomIndices = new HashSet<>();
        Random random = new Random(0);
        while (randomIndices.size() < numQueries && randomIndices.size() < datasetSize) {
            int index = random.nextInt(datasetSize);
            randomIndices.add(index);
        }
        if (randomIndices.size() < numQueries) {
            config.numSelectedRecForQueries = randomIndices.size(); // Adjust numQueries if not enough unique indices
        }
        return randomIndices;
    }

    private void populatePointQueriesString(BufferedReader reader, int numAttrs, Set<Integer> selectedIndices) throws IOException {
        String line;
        int added = 0;
        while ((line = reader.readLine()) != null && added < pointQueries.length) {
            if (line.startsWith("id")) continue;

            long[] record = readRecord(line, numAttrs);
            int id = (int) record[0];
            if (selectedIndices.contains(id)) {
                for (int p = 0; p < config.numPredicates; p++) {
                    pointQueries[added] = new long[numAttrs];
                    System.arraycopy(record, 1, pointQueries[added], 0, numAttrs);
                    added++;
                }
            }
        }

        // shrink the pointQueries array to the actual number of queries added
        pointQueries = Arrays.copyOf(pointQueries, added);
    }

    private long[] readRecord(String line, int numAttrs) {
        String[] parts = line.split(",");
        if (parts.length < numAttrs + 2) {
            throw new IllegalArgumentException("Record does not match expected number of attributes: " + line);
        }
        long[] record = new long[numAttrs + 2];
        for (int i = 0; i < numAttrs + 1; i++) {
            record[i] = Long.parseLong(parts[i]);
        }
        record[numAttrs + 1] = Long.parseLong(parts[parts.length - 1]); // Last part is sign
        return record;
    }

    private void applyPredicates(int numAttrs) {
        Random randomQueries = new Random(0);

        for (int p = 0; p < config.numPredicates; p++) {
            for (int i = p * config.numSelectedRecForQueries; i < (p + 1) * config.numSelectedRecForQueries; i++) {
                int curNumPreds = 0;
                while (curNumPreds < numAttrs - (p + 1)) {
                    int index = randomQueries.nextInt(numAttrs);
                    if (pointQueries[i][index] !=-1) {
                        pointQueries[i][index] = -1;
                        curNumPreds++;
                    }
                }
            }
        }
    }

    private void deduplicateQueries(int numAttrs) {
        // Deduplication process
        Set<String> uniqueQueries = new HashSet<>();
        int uniqueCount = 0;

        for (long[] query : pointQueries) {
            String key = buildQueryKey(query, numAttrs);

            if (uniqueQueries.add(key)) {
                pointQueries[uniqueCount] = query;
                uniqueCount++;
            }
        }
        // Resize the pointQueries array to contain only unique entries
        pointQueries = Arrays.copyOf(pointQueries, uniqueCount);
    }

    private String buildQueryKey(long[] query, int numAttrs) {
        StringBuilder keyBuilder = new StringBuilder();
        for (int j = 0; j < numAttrs; j++) {
            if (query[j] !=-1) {
                keyBuilder.append(j).append(":").append(query[j]).append(",");
            }
        }
        return keyBuilder.toString();
    }

    private void computeExactAnswers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetFileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id")) continue;

                long[] record = readRecord(line, pointQueries[0].length);
                updateAnswers(record);
            }
        } catch (IOException e) {
            System.err.println("Error computing exact answers.");
            e.printStackTrace();
        }

        writeQueriesToFile();
    }

    private void updateAnswers(long[] record) {
        for (int i = 0; i < pointQueries.length; i++) {
            boolean match = true;
            boolean unionMatch = false;

            for (int j = 0; j < pointQueries[i].length; j++) {
                long queryVal = pointQueries[i][j];
                long recordVal = record[j + 1];

                if (queryVal != -1 && queryVal != recordVal) {
                    match = false;
                    break;
                }
                if (queryVal != -1 && queryVal == recordVal) {
                    unionMatch = true;
                }
            }

            if (match) pointQueryAnswers[i]++;
            if (unionMatch) pointQueryUnion[i]++;
        }
    }

    private void writeQueriesToFile() {
        queryFileName = setQueryFileName(datasetFileName, config.pvldb_queries);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(queryFileName))) {
            writeHeaderQueryFile(writer, pointQueries[0].length);
            for (int i = 0; i < pointQueries.length; i++) {
                StringBuilder line = new StringBuilder();
                line.append(i);

                for (int j = 0; j < pointQueries[i].length; j++) {
                    line.append(",").append(pointQueries[i][j]);
                }
                line.append(",").append(pointQueryAnswers[i])
                        .append(",").append(pointQueryUnion[i]);

                writer.write(line.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error writing queries to file.");
            e.printStackTrace();
        }
    }


    private void writeHeaderQueryFile(BufferedWriter writer, int numAttrs) {
        String[] header = new String[numAttrs + 3];
        header[0] = "id";
        for (int i = 1; i <= numAttrs; i++) {
            header[i] = "attr" + i;
        }
        header[header.length - 2] = "answer"; // Append answer at the end
        header[header.length - 1] = "union"; // Append union at the end
        try {
            writeLine(writer, header);
        } catch (IOException e) {
            System.err.println("Error writing header to file: " + datasetFileName);
            e.printStackTrace();
        }
    }

    private void writeLine(BufferedWriter writer, String... values) throws IOException {
        writer.write(String.join(",", values));
        writer.newLine();
    }

    private String setQueryFileName(String datasetFileName, boolean pvldb_queries) {
//        String queryFileName = datasetFileName.replace(".csv", "_" + config.numStoredAttributes +"_queries.csv");
        if (pvldb_queries) {
            return datasetFileName.replace(".csv", "_pvldb.csv");
        }
        return datasetFileName.replace(".csv", "_" + config.numStoredAttributes +"_queries.csv");
    }

    public int[] getPointQueryAnswers() {
        return pointQueryAnswers;
    }

    public int[] getPointQueriesNumAttrs() {
        return pointQueriesNumAttrs;
    }

    public int[] getPointQueryBinNumber() {
        return pointQueryBinNumber;
    }

    public int[] getPointQueryUnion() {
        return pointQueryUnion;
    }

    public long[][] getPointQueries() {
        return pointQueries;
    }

    public String getDatasetReaderName(double perc, int sizeFactor) {
        setupDataset(perc, sizeFactor);
        return datasetFileName;
    }

    public int getDatasetResiduSize() {
        return datasetResiduSize;
    }

    public boolean queriesNotExistCAIDA(int sizeFactor) {
        setupResiduDataset(sizeFactor); // Use 0.0 queries are generated on residu.
        String queryFileName = setQueryFileName(datasetFileName, config.pvldb_queries);
        File queryFile = new File(queryFileName);
        return !queryFile.exists() || queryFile.length() == 0;
    }

    public int getDatasetNoiseSize() {
        return noiseSize;
    }

}

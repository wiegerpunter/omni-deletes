package omni.datasets.stringData;

import omni.Config;
import omni.datasets.Record.Record;
import omni.datasets.Record.RecordUtils;
import omni.datasets.Record.StringRecord;

import java.io.*;
import java.util.*;

public class StringDataset {
    private Config config;
    private String datasetFileName;
    private int datasetResiduSize;
    private int noiseSize;
    private String queryFileName;
    private Record[] pointQueriesObj;
    private int[] pointQueryAnswers;
    private int[] pointQueryUnion;

    private int[] pointQueriesNumAttrs;
    private int[] pointQueryBinNumber;

    public StringDataset(Config config) {
        this.config = config;
    }
    private int countCsvRecords(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            return Math.max(0, lines - 1); // Subtract 1 for header
        }
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
        String subfolder = config.readFolder + "input/data/tpc-ds/" + sizeFactor + "/" + 0.0 + "/";
        datasetFileName = subfolder + "final_stream_" + 0.0 + ".csv";
//        datasetFileName = config.readFolder + "input/data/" + config.datasetName + "/" + config.datasetName +".csv";
        datasetResiduSize = countCsvRecords(subfolder);
    }

    private void setupDataset(double perc, int sizeFactor) {
        String subfolder = config.readFolder + "input/data/tpc-ds/" + sizeFactor + "/" + perc+ "/";
        datasetFileName = subfolder + "final_stream_" + perc + ".csv";
        if (datasetResiduSize == 0) {
            throw new RuntimeException("Dataset Residu size is zero");
        }
        noiseSize = (countCsvRecords(subfolder) - datasetResiduSize)/2;
    }

    public void generateQueries(int sizeFactor) throws IOException {
        setupResiduDataset(sizeFactor); // Use 0.0 to generate queries on the residu dataset
        generateQueriesString();
    }

    public void loader(int sizeFactor) {
        setupResiduDataset(sizeFactor);
        loadQueriesString();
    }

    private void loadPointQueryArrays(ArrayList<omni.datasets.Record.Record> pointQueriesList, ArrayList<Integer> pointQueryAnswersList, ArrayList<Integer> pointQueryUnionList, int numAttrs) {
        pointQueriesObj = new omni.datasets.Record.Record[pointQueriesList.size()];
        pointQueryAnswers = new int[pointQueriesObj.length];
        pointQueryUnion = new int[pointQueriesObj.length];
        pointQueriesNumAttrs = new int[pointQueriesObj.length];
        pointQueryBinNumber = new int[pointQueriesObj.length];
        for (int i = 0; i < pointQueriesList.size(); i++) {
            pointQueriesObj[i] = pointQueriesList.get(i);
            pointQueryAnswers[i] = pointQueryAnswersList.get(i);
            pointQueryUnion[i] = pointQueryUnionList.get(i);
        }

        computeQueryStatsObj(numAttrs);
    }

    private void computeQueryStatsObj(int numAttrs) {
        for (int i = 0; i < pointQueriesObj.length; i++) {
            for (int j = 0; j < numAttrs; j++) {
                if (!RecordUtils.flexibleEquals(pointQueriesObj[i].getValue(j), -1)) {
                    pointQueriesNumAttrs[i]++;
                }
            }
            pointQueryBinNumber[i] = i;
        }
    }

    private void loadQueriesString() {
        ArrayList<Record> pointQueriesList = new ArrayList<>();
        ArrayList<Integer> pointQueryAnswersList = new ArrayList<>();
        ArrayList<Integer> pointQueryUnionList = new ArrayList<>();

        int numAttrs = config.numStoredAttributes;
        queryFileName = setQueryFileName(datasetFileName);
        // load queries and pointQueryAnswers from file
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFileName))) {
            String line;
            int queryCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id")) continue; // Skip header line

                String[] parts = line.split(",");
                if (parts.length!= numAttrs + 3) { // +2 for answer and union
                    throw new RuntimeException("Record too short, not matching expected number of attributes: " + line);
                }
                String[] query = new String[numAttrs];
                // Skip id
                System.arraycopy(parts, 1, query, 0, numAttrs);
                pointQueriesList.add(new StringRecord(query));
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

    private void generateQueriesString() throws IOException {
        int numAttrs = config.numStoredAttributes;
        pointQueriesObj = new omni.datasets.Record.Record[config.numQueries * config.numPredicates];

        Set<Integer> selectedIndices = selectRandomIndices(datasetResiduSize, config.numQueries);
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetFileName))) {
            populatePointQueriesString(reader, numAttrs, selectedIndices);
        } catch (IOException e) {
            System.err.println("Error opening file for queries: " + datasetFileName);
            throw e;
        }

        pointQueryAnswers = new int[pointQueriesObj.length];
        pointQueryUnion = new int[pointQueriesObj.length];
        pointQueriesNumAttrs = new int[pointQueriesObj.length];
        pointQueryBinNumber = new int[pointQueriesObj.length];
        applyPredicatesObj(numAttrs);
        deduplicateQueriesObj(numAttrs);
        computeExactAnswersObj();
        computeQueryStatsObj(numAttrs);
    }

    private Set<Integer> selectRandomIndices(int datasetSize, int numQueries) {
        Set<Integer> randomIndices = new HashSet<>();
        Random random = new Random(0);
        while (randomIndices.size() < numQueries && randomIndices.size() < datasetSize) {
            int index = random.nextInt(datasetSize);
            randomIndices.add(index);
        }
        if (randomIndices.size() < numQueries) {
            config.numQueries = randomIndices.size(); // Adjust numQueries if not enough unique indices
        }
        return randomIndices;
    }

    private void populatePointQueriesString(BufferedReader reader, int numAttrs, Set<Integer> selectedIndices) throws IOException {
        String line;
        int added = 0;
        while ((line = reader.readLine()) != null && added < pointQueriesObj.length) {
            if (line.startsWith("id")) continue;

            Record record = readRecordString(line, numAttrs);
            int id = Integer.parseInt((String) record.getValue(0));
            if (selectedIndices.contains(id)) {
                for (int p = 0; p < config.numPredicates; p++) {
                    String[] queryData = new String[numAttrs];
                    // copy the numAttrs from the record, skipping the first value (id)
                    if (added >= pointQueriesObj.length) {
                        break; // Prevent ArrayIndexOutOfBoundsException
                    }

                    System.arraycopy(record.getData(numAttrs), 1, queryData, 0, numAttrs);
                    pointQueriesObj[added] = new StringRecord(queryData);
                    added++;
                }
            }
        }

        // shrink the pointQueries array to the actual number of queries added
        pointQueriesObj = Arrays.copyOf(pointQueriesObj, added);
    }

    private omni.datasets.Record.Record readRecordString(String line, int numAttrs) {
        String[] parts = line.split(",");
        if (parts.length < numAttrs + 2) {
            throw new IllegalArgumentException("Record does not match expected number of attributes: " + line);
        }
        return new StringRecord(parts);
    }

    private void applyPredicatesObj(int numAttrs) {
        Random randomQueries = new Random(0);

        for (int p = 0; p < config.numPredicates; p++) {
            for (int i = p * config.numQueries; i < (p + 1) * config.numQueries; i++) {
                int curNumPreds = 0;
                while (curNumPreds < numAttrs - (p + 1)) {
                    int index = randomQueries.nextInt(numAttrs);
                    if (!RecordUtils.flexibleEquals(pointQueriesObj[i].getValue(index),-1)) {
                        pointQueriesObj[i].setValue(index, -1);
                        curNumPreds++;
                    }
                }
            }
        }
    }

    private void deduplicateQueriesObj(int numAttrs) {
        // Deduplication process
        Set<String> uniqueQueries = new HashSet<>();
        int uniqueCount = 0;

        for (Record query : pointQueriesObj) {
            String key = buildQueryKey(query, numAttrs);

            if (uniqueQueries.add(key)) {
                pointQueriesObj[uniqueCount] = query;
                uniqueCount++;
            }
        }
        // Resize the pointQueries array to contain only unique entries
        pointQueriesObj = Arrays.copyOf(pointQueriesObj, uniqueCount);
    }

    private String buildQueryKey(Record query, int numAttrs) {
        StringBuilder keyBuilder = new StringBuilder();
        for (int j = 0; j < numAttrs; j++) {
            if (!query.getValue(j).equals(-1)) {
                keyBuilder.append(j).append(":").append(query.getValue(j)).append(",");
            }
        }
        return keyBuilder.toString();
    }

    private void computeExactAnswersObj() {
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetFileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id")) continue;

                Record record = readRecordString(line, pointQueriesObj[0].length());
                updateAnswersObj(record);
            }
        } catch (IOException e) {
            System.err.println("Error computing exact answers.");
            e.printStackTrace();
        }

        writeQueriesToFile();
    }

    private void updateAnswersObj(Record record) {
        for (int i = 0; i < pointQueriesObj.length; i++) {
            boolean match = true;
            boolean unionMatch = false;

            for (int j = 0; j < pointQueriesObj[i].length(); j++) {
                Object queryVal = pointQueriesObj[i].getValue(j);
                Object recordVal = record.getValue(j + 1);

                if (!RecordUtils.flexibleEquals(queryVal,-1) && !queryVal.equals(recordVal)) {
                    match = false;
                    break;
                }
                if (!RecordUtils.flexibleEquals(queryVal,-1) && queryVal.equals(recordVal)) {
                    unionMatch = true;
                }
            }

            if (match) pointQueryAnswers[i]++;
            if (unionMatch) pointQueryUnion[i]++;
        }
    }

    private void writeQueriesToFile() {
        queryFileName = setQueryFileName(datasetFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(queryFileName))) {
            writeHeaderQueryFile(writer, pointQueriesObj[0].length());
            for (int i = 0; i < pointQueriesObj.length; i++) {
                StringBuilder line = new StringBuilder();
                line.append(i);

                for (int j = 0; j < pointQueriesObj[i].length(); j++) {
                    line.append(",").append(pointQueriesObj[i].getValue(j));
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

    private String setQueryFileName(String datasetFileName) {
        return datasetFileName.replace(".csv", "_" + config.numStoredAttributes + "_queries.csv");
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

    public Record[] getPointQueriesObj() {
        return pointQueriesObj;
    }

    public String getDatasetReaderName(double perc, int sizeFactor) {
        setupDataset(perc, sizeFactor);
        return datasetFileName;
    }

    public int getDatasetResiduSize() {
        return datasetResiduSize;
    }

    public boolean queriesNotExistTPCDS(int sizeFactor) {
        setupResiduDataset(sizeFactor); // Use 0.0 queries are generated on residu.
        String queryFileName = setQueryFileName(datasetFileName);
        File queryFile = new File(queryFileName);
        return !queryFile.exists() || queryFile.length() == 0;
    }

    public int getDatasetNoiseSize() {
        return noiseSize;
    }

}

package omni.datasets.stringData;

import omni.Config;
import omni.datasets.Record.Record;
import omni.datasets.Record.RecordUtils;
import omni.datasets.Record.StringRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class StringDataset {
    private Config config;
    private String datasetFileName;
    private String queryFileName;
    private Record[] pointQueriesObj;
    private int[] pointQueryAnswers;
    private int[] pointQueryUnion;

    private int[] pointQueriesNumAttrs;
    private int[] pointQueryBinNumber;

    public StringDataset(Config config) {
        this.config = config;
    }

    private void setupDataset() {
        datasetFileName = config.readFolder + "input/data/" + config.datasetName + "/" + config.datasetName +".csv";
    }

    public void loader() {
        setupDataset();
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

        computeQueryStatsObj(numAttrs, config.numZipfAttributes);
    }

    private void computeQueryStatsObj(int numAttrs, int numZipfAttributes) {
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
        queryFileName = setQueryFileName(datasetFileName);
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

    private String setQueryFileName(String datasetFileName) {
        return datasetFileName.replace(".csv", "_queries.csv");
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

    public String getDatasetReaderName() {
        return datasetFileName;
    }

    public int getDatasetResiduSize() {
        return 3;
    }
}

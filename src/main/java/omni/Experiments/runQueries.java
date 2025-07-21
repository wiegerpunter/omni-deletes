package omni.Experiments;
//

import com.opencsv.CSVWriter;
import omni.Config;
import omni.Experiments.utils.QueryInfo;
import omni.Main;
import omni.datasets.CleanDataset;
import omni.datasets.Record.Record;
import omni.synopses.SynopsisRefactor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class runQueries {
    Config config;
    SynopsisRefactor s;
    CleanDataset d;
    //ExpWorkload ew;

    int[] estimatedAnswersPointQuery;
    public int[] intersectionOfR;
    public int[] unionOfR;
    public ExpSetting[] expSettings;

    int[] estimatedAnswersRangeQuery;
    int[] SCap;
    double[] jaccardEstimates2LHS;
    int[] witness2LHS;
    double[] unionEstimates2LHS;
    int[] unionExact;
    int[] numberOfKmins;
    int[] numberOfKminsExceedingBounds;
    boolean[] case1;
    double[] bound;

    int[] NMax;
    int[] usedMaxSizes;
    long[] queryExecutionTime;

    long totalTime;
    int totalExecQueries;
    ArrayList<Double> error;

    int totalQueriesZero = 0;
    int totalEstimatesZero=0;
    long ingestionTime;
    int collisions;
    int repetition;

    public runQueries(SynopsisRefactor s, CleanDataset cd,
                      long timepassed, int collisions, int repetition, Config config) throws IOException {
        this.config = config;
        this.s = s;
        this.d = cd;
        this.ingestionTime = timepassed;
        this.collisions = collisions;
        this.repetition = repetition;
        estimatedAnswersPointQuery = new int[cd.pointQueriesObj.length];
        estimatedAnswersRangeQuery = new int[cd.pointQueriesObj.length];
        SCap = new int[cd.pointQueriesObj.length];
        NMax = new int[cd.pointQueriesObj.length];
        usedMaxSizes = new int[cd.pointQueriesObj.length];
        jaccardEstimates2LHS = new double[cd.pointQueriesObj.length];
        unionEstimates2LHS = new double[cd.pointQueriesObj.length];
        unionExact = new int[cd.pointQueriesObj.length];
        witness2LHS = new int[cd.pointQueriesObj.length];
        queryExecutionTime = new long[cd.pointQueriesObj.length];
        intersectionOfR = new int[cd.pointQueriesObj.length];
        unionOfR = new int[cd.pointQueriesObj.length];
        expSettings = new ExpSetting[cd.pointQueriesObj.length];
        numberOfKmins = new int[cd.pointQueriesObj.length];
        numberOfKminsExceedingBounds = new int[cd.pointQueriesObj.length];
        bound = new double[cd.pointQueriesObj.length];
        case1 = new boolean[cd.pointQueriesObj.length];
        Arrays.fill(case1, false);

    }

    public void run() throws IOException {
        // Count number of collisions in Omnisketch.

        error = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        if (Main.rangeQueries) {
            for (int i = 0; i< d.rangeQueries.length; i++) {
                computeErrorRangeQuery(i, d.rangeQueries[i], d.rangeQueryAnswers[i]);
                //h.addQueryResult(q);
            }
        } else {
            for (int i = 0; i< d.pointQueries.length; i++) {
                computeErrorPointQuery(i, d.pointQueries[i], d.pointQueriesNumAttrs[i], d.pointQueryAnswers[i], d.pointQueryUnion[i]);
                //h.addQueryResult(q);
                if (i % 1000 == 0) {
                    System.out.println("\r" + i + " queries done");
                }
            }
            System.out.println("Memory usage is " + s.getMemoryUsage());
            // test for Synopsis if it has more memory than the computed mem usage.

        }
        long endTime = System.currentTimeMillis();
        long totalQueryExecutionTime = endTime - startTime;

        long totalExecTime = 0;
        for (long l : queryExecutionTime) {
            totalExecTime = totalExecTime + l;
        }
        System.out.println("Total execution time: " + totalExecTime + " ms, average: "
                + (double) totalExecTime / d.pointQueries.length + " ms");

        System.out.println("Difference between total time and execution time per query: "
                + (totalQueryExecutionTime - totalExecTime));


        // Write results to file
        if (config.rangeQueries) {
            throw new RuntimeException("Range queries file not implemented yet");
        } else {
//            if (s.setting.contains("SampleLater")) {
//                writeLoggedSetSizes(expSettings);
//            }
            writeResultsToFilePointQuery(repetition, s, d, ingestionTime, collisions, estimatedAnswersPointQuery,
                    SCap, NMax, usedMaxSizes, jaccardEstimates2LHS, unionEstimates2LHS, unionExact, witness2LHS, queryExecutionTime, totalQueryExecutionTime,
                    numberOfKmins, numberOfKminsExceedingBounds, case1, bound);
        }
        System.out.println("Total queries: " + d.pointQueries.length);
        System.out.println("Total queries with zero empty or singleton witnesses: " + s.countIsZero);
        System.out.println("Total queries with zero estimate: " + totalEstimatesZero);
    }

    public void runObj() throws IOException {
        // Count number of collisions in Omnisketch.

        error = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        if (Main.rangeQueries) {
            for (int i = 0; i< d.rangeQueries.length; i++) {
                computeErrorRangeQuery(i, d.rangeQueries[i], d.rangeQueryAnswers[i]);
                //h.addQueryResult(q);
            }
        } else {
            for (int i = 0; i< d.pointQueriesObj.length; i++) {
                computeErrorPointQuery(i, d.pointQueriesObj[i], d.pointQueriesNumAttrs[i], d.pointQueryAnswers[i], d.pointQueryUnion[i]);
                //h.addQueryResult(q);
                if (i % 1000 == 0) {
                    System.out.println("\r" + i + " queries done");
                }
            }
            System.out.println("Memory usage is " + s.getMemoryUsage());
            // test for Synopsis if it has more memory than the computed mem usage.

        }
        long endTime = System.currentTimeMillis();
        long totalQueryExecutionTime = endTime - startTime;

        long totalExecTime = 0;
        for (long l : queryExecutionTime) {
            totalExecTime = totalExecTime + l;
        }
        System.out.println("Total execution time: " + totalExecTime + " ms, average: "
                + (double) totalExecTime / d.pointQueriesObj.length + " ms");

        System.out.println("Difference between total time and execution time per query: "
                + (totalQueryExecutionTime - totalExecTime));


        // Write results to file
        if (config.rangeQueries) {
            throw new RuntimeException("Range queries file not implemented yet");
        } else {
//            if (s.setting.contains("SampleLater")) {
//                writeLoggedSetSizes(expSettings);
//            }
            writeResultsToFilePointQuery(repetition + 1, s, d, ingestionTime, collisions, estimatedAnswersPointQuery,
                    SCap, NMax, usedMaxSizes, jaccardEstimates2LHS, unionEstimates2LHS, unionExact, witness2LHS, queryExecutionTime, totalQueryExecutionTime,
                    numberOfKmins, numberOfKminsExceedingBounds, case1, bound);
        }
        System.out.println("Total queries: " + d.pointQueriesObj.length);
        System.out.println("Total queries with zero empty or singleton witnesses: " + s.countIsZero);
        System.out.println("Total queries with zero estimate: " + totalEstimatesZero);
    }

    private void writeLoggedSetSizes(ExpSetting[] expSettings) throws IOException {
        String CSV_FILE_NAME = config.getOutputFolder() + "/pointQueries/" + config.datasetName + "/" + config.currentDate + "_" + config.experimentName + "_"
                + config.setting + "_dataset_" + config.datasetName +
                "_loggedSetSizes.csv";
        boolean init = false;
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if (!new File(CSV_FILE_NAME).exists()) {
            init = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));

        if (init) {
            String[] header = new String[]{"intersectionSize", "B", "setSizes"};
            writer.writeNext(header);
        }
        for (ExpSetting expSetting : expSettings) {
            String[] result = new String[3];
            result[0] = String.valueOf(expSetting.getIntersectionSize());
            result[1] = Arrays.toString(expSetting.getB());
            result[2] = Arrays.toString(expSetting.getSetSizes());
            writer.writeNext(result);
        }

    }

    public void testQueryInfoIsolation() {
        QueryInfo queryInfo1 = new QueryInfo();
        QueryInfo queryInfo2 = new QueryInfo();
        queryInfo1.case1 = true;
        if (!queryInfo2.case1) {
            System.out.println("QueryInfo objects are independent");
        } else {
            throw new RuntimeException("QueryInfo objects are not independent");
        }
    }

   // = new QueryInfo(0);
    public void computeErrorPointQuery (int queryId, long[] q, int numPreds, int exactAnswer, int unionSize) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        QueryInfo queryInfo = new QueryInfo();
        long startTime = System.currentTimeMillis();
        estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, queryInfo);
        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        if (estimatedAnswersPointQuery[queryId] == 0) {
            totalEstimatesZero++;
        }

        QueryInfo copy = new QueryInfo(queryInfo);
        unionOfR[queryId] = copy.exactUnion;
//        expSettings[queryId] = copy.expSetting;
        intersectionOfR[queryId] = copy.exactIntersection;
        totalTime = totalTime + queryExecutionTime[queryId];
        totalExecQueries++;
        SCap[queryId] = copy.Scap;
        NMax[queryId] = copy.nmax;
        case1[queryId] = copy.case1;
        bound[queryId] = copy.bound;
        usedMaxSizes[queryId] = copy.maxSize;
        jaccardEstimates2LHS[queryId] = copy.jaccardEstimate;
        unionEstimates2LHS[queryId] = copy.unionEstimate;
        unionExact[queryId] = unionSize;
        witness2LHS[queryId] = copy.witness2LHS;
        numberOfKmins[queryId] = copy.numberOfKmins;
        numberOfKminsExceedingBounds[queryId] = copy.numberOfKminsExceedingBound;
    }

    public void computeErrorPointQuery (int queryId, Record q, int numPreds, int exactAnswer, int unionSize) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        QueryInfo queryInfo = new QueryInfo();
        long startTime = System.currentTimeMillis();
        estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, queryInfo);
        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        if (estimatedAnswersPointQuery[queryId] == 0) {
            totalEstimatesZero++;
        }

        QueryInfo copy = new QueryInfo(queryInfo);
        unionOfR[queryId] = copy.exactUnion;
//        expSettings[queryId] = copy.expSetting;
        intersectionOfR[queryId] = copy.exactIntersection;
        totalTime = totalTime + queryExecutionTime[queryId];
        totalExecQueries++;
        SCap[queryId] = copy.Scap;
        NMax[queryId] = copy.nmax;
        case1[queryId] = copy.case1;
        bound[queryId] = copy.bound;
        usedMaxSizes[queryId] = copy.maxSize;
        jaccardEstimates2LHS[queryId] = copy.jaccardEstimate;
        unionEstimates2LHS[queryId] = copy.unionEstimate;
        unionExact[queryId] = unionSize;
        witness2LHS[queryId] = copy.witness2LHS;
        numberOfKmins[queryId] = copy.numberOfKmins;
        numberOfKminsExceedingBounds[queryId] = copy.numberOfKminsExceedingBound;
    }

    public void computeErrorRangeQuery (int queryId, long[][] q, int exactAnswer) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        long startTime = System.currentTimeMillis();
        estimatedAnswersRangeQuery[queryId] = s.rangeQuery(q[0], q[1]);

        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        totalTime = totalTime + queryExecutionTime[queryId];
        totalExecQueries++;
        boolean queryWithinBound;
//        if (q.thrm33Case2) {
//            queryWithinBound = Math.abs(q.estimate - q.exactAnswer) < Main.eps * d.size;
//        }  else {
//            queryWithinBound = q.exactAnswer <= q.estimate * 2;
//        }
//        q.setResult(queryWithinBound);
//        q.setBound(d.size * Main.eps);
//        q.setEpsError(d.dataset.size());
    }

    private void writeResultsToFilePointQuery(int repetition, SynopsisRefactor s, CleanDataset d,
                                             long timePassed, int collisions, int[] estimatedAnswersPointQuery,
                                             int[] SCap, int[] NMax, int[] usedMaxSizes, double[] jaccardEstimates2LHS,
                                             double[] unionEstimates2LHS, int[] unionExact, int[] witness2LHS, long[] queryExecutionTime,
                                             long totalQueryExecutionTime,
                                             int[] numberOfKmins, int[] numberOfKminsExceedingBounds, boolean[] case1, double[] bound) throws IOException {
        // write string[] result to csvOutputFile using BufferedWriter
        boolean init = false;
        //String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String CSV_FILE_NAME = config.getOutputFolder() + "/pointQueries/" + config.datasetName + "/" + config.currentDate + "_" + config.experimentName + "_"
                + config.setting + "_dataset_" + config.datasetName +
                ".csv";
        if (!new File(CSV_FILE_NAME).exists()) {
            init = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_NAME, true));

        if (init) {
            String[] header = new String[]{"repetition","totalStreamSize", "noiseUpdates", "streamSizeAfterDeletes",
                    "numAttributes", "memUsageDataset", "RAM", "setting", "sampleType",
                    "Avg Update Time", "memUsageSynopsis", "parameters", "numAttributesInWorkload",
                    "queryID", "numPredicates","numZipfianPredicates","binNumber",
                    "exactAnswer", "estimate", "absError",
                    "relError", "epsError", "withinThreshold",
                    "estTime",  "queryText", "SCap", "NMax","usedMaxSize","jacEstimate2LHS", "unionEstimate2LHS","unionExact","witness2LHS",
                    "intersectionSizeOfR","unionSizeOfR",
                    "meanQueryTime","BetaKmin","numKSamples","KminDeletes","exactInDeletes","exactUnionDeletes","uniqueSamples",
                    "numKmins","numKminsExceedingBounds","measuredSignatureCollisions","zipfAlpha", "case1", "bound","bufferDeletesMinwise","domain"};
            writer.writeNext(header);
        }


        String memUsageSyn = String.valueOf(s.getMemoryUsage());
        for (int i = 0; i < d.pointQueriesObj.length; i++) {
            String[] result = new String[49];
            // Dataset specific info;
            result[0] = String.valueOf(repetition);
            result[1] = String.valueOf(d.getDatasetSize());
            if (d.noiseUpdates == null && !config.readFromDisk) {
                result[2] = String.valueOf(0);
            } else {
                result[2] = String.valueOf(d.getNoiseSize());
            }
            result[3] = String.valueOf(d.getDatasetResiduSize());
            result[4] = String.valueOf(config.numAttributes);

            result[5] = String.valueOf(d.getMemoryUsage());
            result[6] = String.valueOf(s.ram);
            result[7] = s.getSetting();
            result[8] = s.sampleType;
            result[9] = String.valueOf(timePassed);
            result[10] = memUsageSyn;
            result[11] = Arrays.toString(s.parameters);
            result[12] = String.valueOf(config.numStoredAttributes);
            result[13] = String.valueOf(i);
            result[14] = String.valueOf(d.pointQueriesNumAttrs[i]);
            if (d.pointQueriesNumZipfian != null) {
                result[15] = String.valueOf(d.pointQueriesNumZipfian[i]);
            }
            result[16] = String.valueOf(d.pointQueryBinNumber[i]);
            result[17] = String.valueOf(d.pointQueryAnswers[i]);
            result[18] = String.valueOf(estimatedAnswersPointQuery[i]);
            result[19] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]));
            result[20] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.pointQueryAnswers[i]);
            result[21] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.getDatasetResiduSize());
            result[22] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) <= Main.eps * d.getDatasetResiduSize());
            result[23] = String.valueOf(queryExecutionTime[i]);
            result[24] = d.parsePointQueryToString(d.pointQueriesObj[i]);
            result[25] = String.valueOf(SCap[i]);
            result[26] = String.valueOf(NMax[i]);
            result[27] = String.valueOf(usedMaxSizes[i]);
            result[28] = String.valueOf(jaccardEstimates2LHS[i]);
            result[29] = String.valueOf(unionEstimates2LHS[i]);
            result[30] = String.valueOf(unionExact[i]);
            result[31] = String.valueOf(witness2LHS[i]);
            if (config.checkConditions) {
                result[32] = String.valueOf(intersectionOfR[i]);
                result[33] = String.valueOf(unionOfR[i]);
            };
            result[34] = String.valueOf(totalQueryExecutionTime);
            result[35] = String.valueOf(s.useBetaKmin);
            result[36] = String.valueOf(0);
            result[37] = String.valueOf(Main.kminDeletes);
            result[38] = String.valueOf(d.pointQueryAnswersDeletes[i]);
            result[39] = String.valueOf(d.pointQueryUnionDeletes[i]);
            if (!Main.countUniqueSamples) {
                result[40] = "0";
            } else {
                if (s.setting.contains("OmniSketch")) {
                    result[40] = String.valueOf(Main.uniqueSamples.size());
                } else {
                    result[40] = String.valueOf(Main.uniqueSamplesReservoir.size());
                }
            }
            result[41] = String.valueOf(numberOfKmins[i]);
            result[42] = String.valueOf(numberOfKminsExceedingBounds[i]);
            result[43] = String.valueOf(collisions);
            result[44] = String.valueOf(d.zipfAlpha);
            result[45] = String.valueOf(case1[i]);
            result[46] = String.valueOf(bound[i]);
            result[47] = String.valueOf(config.bufferDeletesMinwise);
            result[48] = String.valueOf(config.domain); // Assuming domain_sizes is an array of integers, and we want the first element.
            writer.writeNext(result);
        }

        writer.close();
    }

}

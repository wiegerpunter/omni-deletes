package omni;
//

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AnalysisBaselinesRefactor {
    Config config;
    SynopsisRefactor s;
    CleanDataset d;
    //ExpWorkload ew;

    int[] estimatedAnswersPointQuery;
    public int[] intersectionOfR;
    public int[] unionOfR;

    int[] estimatedAnswersRangeQuery;
    int[] SCap;
    double[] jaccardEstimates2LHS;
    int[] witness2LHS;
    double[] unionEstimates2LHS;
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

    public AnalysisBaselinesRefactor(SynopsisRefactor s, CleanDataset cd,
                                     long timepassed, int collisions, int repetition, Config config) throws IOException {
        this.config = config;
        this.s = s;
        this.d = cd;
        this.ingestionTime = timepassed;
        this.collisions = collisions;
        this.repetition = repetition;
        estimatedAnswersPointQuery = new int[cd.pointQueries.length];
        estimatedAnswersRangeQuery = new int[cd.pointQueries.length];
        SCap = new int[cd.pointQueries.length];
        NMax = new int[cd.pointQueries.length];
        usedMaxSizes = new int[cd.pointQueries.length];
        jaccardEstimates2LHS = new double[cd.pointQueries.length];
        unionEstimates2LHS = new double[cd.pointQueries.length];
        witness2LHS = new int[cd.pointQueries.length];
        queryExecutionTime = new long[cd.pointQueries.length];
        intersectionOfR = new int[cd.pointQueries.length];
        unionOfR = new int[cd.pointQueries.length];
        numberOfKmins = new int[cd.pointQueries.length];
        numberOfKminsExceedingBounds = new int[cd.pointQueries.length];
        bound = new double[cd.pointQueries.length];
        case1 = new boolean[cd.pointQueries.length];
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
                if (i % 10000 == 0) {
                    System.out.println(i + " queries done");
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
            writeResultsToFilePointQuery(repetition, s, d, ingestionTime, collisions, estimatedAnswersPointQuery,
                    SCap, NMax, usedMaxSizes, jaccardEstimates2LHS, unionEstimates2LHS, witness2LHS, queryExecutionTime, totalQueryExecutionTime,
                    numberOfKmins, numberOfKminsExceedingBounds, case1, bound);
        }
        System.out.println("Total queries: " + d.pointQueries.length);
        System.out.println("Total queries with zero empty or singleton witnesses: " + s.countIsZero);
        System.out.println("Total queries with zero estimate: " + totalEstimatesZero);
    }

//    private void printmaxB() {
//
//        // Look at sample and print the max K value in each cell, plus its potential signature size
//        // signature size is number of bits needed to express this number
//        int maxSigsize = 0;
//        int minSigsize = Integer.MAX_VALUE;
//        long maxK = 0;
//        long minK = 0;
//        for (int i = 0; i < ((OmniSketch) s).numStoredAttributes; i++) {
//            for (int j = 0; j < ((OmniSketch) s).width; j++) {
//                for (int r = 0; r < ((OmniSketch) s).depth; r++) {
//                    long K = ((OmniSketch) s).CMSketches[i].CMKminTreeSet[r][j].curTreeRoot;
//                    int sigSize = (int) Math.ceil(Math.log(K) / Math.log(2)); // number of bits needed to express this number
//                    //System.out.println("K for attribute " + i + " in cell " + j + " in row " + r + ": " + K + " with signature size: " + sigSize);
//                    if (sigSize > maxSigsize) {
//                        maxSigsize = sigSize;
//                        maxK = K;
//                    }
//                    if (sigSize < minSigsize) {
//                        minSigsize = sigSize;
//                        minK = K;
//                    }
//                    if (((OmniSketch) s).CMSketches[i].CMKminTreeSet[r][j].K > K) {
//                        System.out.println("K exceeds bound in cell " + j + " in row " + r + " for attribute " + i + ": " + K);
//                    }
//                }
//            }
//        }
//        System.out.println("Max K: " + maxK + " with signature size: " + maxSigsize);
//        System.out.println("Min K: " + minK + " with signature size: " + minSigsize);
//    }



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
//        if (Main.checkConditions ) { //&& s.ram == Main.ramVals[0]
//            estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, unionSize, queryInfo);
//            res = s.checkConditions(q, numPreds, unionSize, queryInfo);
//        } else {
        estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, queryInfo);
//        }
        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        if (estimatedAnswersPointQuery[queryId] == 0) {
            totalEstimatesZero++;
        }

        QueryInfo copy = new QueryInfo(queryInfo);
        unionOfR[queryId] = copy.exactUnion;
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
        witness2LHS[queryId] = copy.witness2LHS;
        numberOfKmins[queryId] = copy.numberOfKmins;
        numberOfKminsExceedingBounds[queryId] = copy.numberOfKminsExceedingBound;
//        if (s.useTwoLHS) {
//            if (estimatedAnswersPointQuery[queryId] > 0) {
//                System.out.println("Estimated answer: " + estimatedAnswersPointQuery[queryId]);
//                System.out.println("query info: " + queryInfo.CMRow + " union est: " + queryInfo.unionEstimate +
//                        " witness 2lhs: " + queryInfo.witness2LHS + " Jaccard similarity" + queryInfo.jaccardEstimate);
//            }
//        }
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
                                             double[] unionEstimates2LHS, int[] witness2LHS, long[] queryExecutionTime,
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
                    "estTime",  "queryText", "SCap", "NMax","usedMaxSize","jacEstimate2LHS", "unionEstimate2LHS","witness2LHS",
                    "intersectionSizeOfR","unionSizeOfR",
                    "meanQueryTime","BetaKmin","numKSamples","KminDeletes","exactInDeletes","exactUnionDeletes","uniqueSamples",
                    "numKmins","numKminsExceedingBounds","measuredSignatureCollisions","zipfAlpha", "case1", "bound"};
            writer.writeNext(header);
        }


        String memUsageSyn = String.valueOf(s.getMemoryUsage());
        for (int i = 0; i < d.pointQueries.length; i++) {
            String[] result = new String[46];
            // Dataset specific info;
            result[0] = String.valueOf(repetition);
            result[1] = String.valueOf(d.dataset.length);
            if (d.noiseUpdates == null) {
                result[2] = String.valueOf(d.datasetNegUpdates.length);
            } else {
                result[2] = String.valueOf(d.noiseUpdates.length);
            }
            result[3] = String.valueOf(d.datasetResidu.length);
            result[4] = String.valueOf(config.numAttributes);

            result[5] = String.valueOf(d.getMemoryUsage());
            result[6] = String.valueOf(s.ram);
            result[7] = s.setting;
            result[8] = s.sampleType;
            result[9] = String.valueOf(timePassed);
            result[10] = memUsageSyn;
            result[11] = Arrays.toString(s.parameters);
            result[12] = String.valueOf(config.numStoredAttributes);
            result[13] = String.valueOf(i);
            result[14] = String.valueOf(d.pointQueriesNumAttrs[i]);
            result[15] = String.valueOf(d.pointQueriesNumZipfian[i]);
            result[16] = String.valueOf(d.pointQueryBinNumber[i]);
            result[17] = String.valueOf(d.pointQueryAnswers[i]);
            result[18] = String.valueOf(estimatedAnswersPointQuery[i]);
            result[19] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]));
            result[20] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.pointQueryAnswers[i]);
            result[21] = String.valueOf((double) Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) / d.datasetResidu.length);
            result[22] = String.valueOf(Math.abs(d.pointQueryAnswers[i] - estimatedAnswersPointQuery[i]) <= Main.eps * d.datasetResidu.length);
            result[23] = String.valueOf(queryExecutionTime[i]);
            result[24] = d.parsePointQueryToString(d.pointQueries[i]);
            result[25] = String.valueOf(SCap[i]);
            result[26] = String.valueOf(NMax[i]);
            result[27] = String.valueOf(usedMaxSizes[i]);
            result[28] = String.valueOf(jaccardEstimates2LHS[i]);
            result[29] = String.valueOf(unionEstimates2LHS[i]);
            result[30] = String.valueOf(witness2LHS[i]);
            if (config.checkConditions) {
                result[31] = String.valueOf(intersectionOfR[i]);
                result[32] = String.valueOf(unionOfR[i]);
            };
            result[33] = String.valueOf(totalQueryExecutionTime);
            result[34] = String.valueOf(s.useBetaKmin);
            int numKSamples = 0;
            if (!s.useTwoLHS & !s.useS0) {
                numKSamples = s.getFilledKSamples();
            }
            result[35] = String.valueOf(numKSamples);
            result[36] = String.valueOf(Main.kminDeletes);
            result[37] = String.valueOf(d.pointQueryAnswersDeletes[i]);
            result[38] = String.valueOf(d.pointQueryUnionDeletes[i]);
            if (!Main.countUniqueSamples) {
                result[39] = "0";
            } else {
                if (s.setting.contains("OmniSketch")) {
                    result[39] = String.valueOf(Main.uniqueSamples.keySet().size());
                } else {
                    result[39] = String.valueOf(Main.uniqueSamplesReservoir.size());
                }
            }
            result[40] = String.valueOf(numberOfKmins[i]);
            result[41] = String.valueOf(numberOfKminsExceedingBounds[i]);
            result[42] = String.valueOf(collisions);
            result[43] = String.valueOf(d.zipfAlpha);
            result[44] = String.valueOf(case1[i]);
            result[45] = String.valueOf(bound[i]);
            writer.writeNext(result);
        }

        writer.close();
    }

}

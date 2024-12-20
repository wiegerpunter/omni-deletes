package omni;
//

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AnalysisBaselinesRefactor {
    SynopsisRefactor s;
    CleanDataset d;
    Helper h;
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

    public AnalysisBaselinesRefactor(SynopsisRefactor s, CleanDataset cd, Helper h, long timepassed, int collisions, int repetition) throws IOException {
        this.s = s;
        this.d = cd;
        this.h = h;
        this.ingestionTime = timepassed;
        this.collisions = collisions;
        this.repetition = repetition;
        //this.ew = ew;
        h.setRamSettingInfo(d, s, timepassed);
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
        if (Main.rangeQueries) {
            h.writeResultsToFileRangeQuery(s, d, ingestionTime, estimatedAnswersRangeQuery,
                    queryExecutionTime);
            //TODO: write range query results to file here
        } else {
            h.setConditionInfo(intersectionOfR, unionOfR);
            h.writeResultsToFilePointQuery(repetition, s, d, ingestionTime, collisions, estimatedAnswersPointQuery,
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
        if (queryId == 76) {
            System.out.println("Query 76");
        }
        if (queryId == 77) {
            System.out.println("Query 77");
        }
        if (queryId == 78) {
            System.out.println("Query 78");
        }
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

}

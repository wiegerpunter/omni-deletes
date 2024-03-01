package omni;

import java.io.IOException;
import java.util.ArrayList;

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

    int[] NMax;
    long[] queryExecutionTime;

    long totalTime;
    int totalExecQueries;
    ArrayList<Double> error;

    int totalQueriesZero = 0;
    int totalEstimatesZero=0;
    long ingestionTime;

    public AnalysisBaselinesRefactor(SynopsisRefactor s, CleanDataset cd, Helper h, long timepassed) throws IOException {
        this.s = s;
        this.d = cd;
        this.h = h;
        this.ingestionTime = timepassed;
        //this.ew = ew;
        h.setRamSettingInfo(d, s, timepassed);
        estimatedAnswersPointQuery = new int[Main.numQueries];
        estimatedAnswersRangeQuery = new int[Main.numQueries];
        SCap = new int[Main.numQueries];
        NMax = new int[Main.numQueries];
        jaccardEstimates2LHS = new double[Main.numQueries];
        unionEstimates2LHS = new double[Main.numQueries];
        witness2LHS = new int[Main.numQueries];
        queryExecutionTime = new long[Main.numQueries];
        intersectionOfR = new int[Main.numQueries];
        unionOfR = new int[Main.numQueries];

    }

    public void run() throws IOException {
        error = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        if (Main.rangeQueries) {
            for (int i = 0; i< Main.numQueries; i++) {
                computeErrorRangeQuery(i, d.rangeQueries[i], d.rangeQueryAnswers[i]);
                //h.addQueryResult(q);
            }
        } else {
            for (int i = 0; i< Main.numQueries; i++) {
                computeErrorPointQuery(i, d.pointQueries[i], d.pointQueriesNumAttrs[i], d.pointQueryAnswers[i], d.pointQueryUnion[i]);
                //h.addQueryResult(q);
            }
        }
        long endTime = System.currentTimeMillis();
        long totalQueryExecutionTime = endTime - startTime;

        long totalExecTime = 0;
        for (long l : queryExecutionTime) {
            totalExecTime = totalExecTime + l;
        }
        System.out.println("Total execution time: " + totalExecTime + " ms, average: "
                + (double) totalExecTime / Main.numQueries + " ms");

        System.out.println("Difference between total time and execution time per query: "
                + (totalQueryExecutionTime - totalExecTime));

        // Write results to file
        if (Main.rangeQueries) {
            h.writeResultsToFileRangeQuery(s, d, ingestionTime, estimatedAnswersRangeQuery,
                    queryExecutionTime);
            //TODO: write range query results to file here
        } else {
            h.setConditionInfo(intersectionOfR, unionOfR);
            h.writeResultsToFilePointQuery(s, d, ingestionTime, estimatedAnswersPointQuery,
                    SCap, NMax, jaccardEstimates2LHS, unionEstimates2LHS, witness2LHS, queryExecutionTime, totalQueryExecutionTime);
        }
        System.out.println("Total queries: " + Main.numQueries);
        System.out.println("Total queries with zero empty or singleton witnesses: " + s.countIsZero);
        System.out.println("Total queries with zero estimate: " + totalEstimatesZero);
    }

    public static class QueryInfo {
        public int CMRow;
        public int Scap;
        public int nmax;

        public double unionEstimate = 0;
        public ArrayList<Double> jaccardEstimates = new ArrayList<>();
        public ArrayList<Integer> witnessEstimates = new ArrayList<>();

        public double jaccardEstimate = 0;
        public int witness2LHS = 0;
        public QueryInfo() {
        }
        public QueryInfo(int CMRow) {
            this.CMRow = CMRow;
        }

        public void setCMRow(int CMRow) {
            this.CMRow = CMRow;
        }

        public void setScap(int Scap, int nmax) {
            this.Scap = Scap;
            this.nmax = nmax;
        }

        public void set2LHS(double unionEstimate, int witness2LHS, double jaccardEstimate) {
            this.unionEstimate = unionEstimate;
            this.witness2LHS = witness2LHS;
            this.jaccardEstimate = jaccardEstimate;
        }
        public void addJaccardEstimate(double jaccardEstimate, int witness2LHS) {
            this.jaccardEstimates.add(jaccardEstimate);
            this.witnessEstimates.add(witness2LHS);
        }
    }
    QueryInfo queryInfo = new QueryInfo(0);
    public void computeErrorPointQuery (int queryId, long[] q, int numPreds, int exactAnswer, int unionSize) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        queryInfo = new QueryInfo(0);
        int[] res = new int[2];
        long startTime = System.currentTimeMillis();
        if (Main.checkConditions ) { //&& s.ram == Main.ramVals[0]
            estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, unionSize, queryInfo);
            res = s.checkConditions(q, numPreds, unionSize, queryInfo);
        } else {
            estimatedAnswersPointQuery[queryId] = s.query(q, numPreds, queryInfo);
        }
        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        if (estimatedAnswersPointQuery[queryId] == 0) {
            totalEstimatesZero++;
        }
        unionOfR[queryId] = res[0];
        intersectionOfR[queryId] = res[1];
        totalTime = totalTime + queryExecutionTime[queryId];
        totalExecQueries++;
        SCap[queryId] = queryInfo.Scap;
        NMax[queryId] = queryInfo.nmax;
        jaccardEstimates2LHS[queryId] = queryInfo.jaccardEstimate;
        unionEstimates2LHS[queryId] = queryInfo.unionEstimate;
        witness2LHS[queryId] = queryInfo.witness2LHS;
        if (s.useTwoLHS) {
            if (estimatedAnswersPointQuery[queryId] > 0) {
                System.out.println("Estimated answer: " + estimatedAnswersPointQuery[queryId]);
                System.out.println("query info: " + queryInfo.CMRow + " union est: " + queryInfo.unionEstimate +
                        " witness 2lhs: " + queryInfo.witness2LHS + " Jaccard similarity" + queryInfo.jaccardEstimate);
            }
        }
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

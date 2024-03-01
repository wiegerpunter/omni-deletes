package omni;

import java.io.IOException;
import java.util.ArrayList;

public class HandlingDeletes {
    SynopsisRefactor posSyn;
    SynopsisRefactor negSyn;
    SynopsisRefactor resSyn;

    CleanDataset d;
    Helper h;
    //ExpWorkload ew;

    int[] estimatedAnswersPointQuery;

    int[] estimatedAnswersPointQueryResiduSketch;

    int[] estimatedAnswersRangeQuery;
    int[] SCap;
    long[] queryExecutionTime;

    long totalTime;
    int totalExecQueries;
    ArrayList<Double> error;

    int totalQueriesZero = 0;
    public HandlingDeletes(SynopsisRefactor posSyn, SynopsisRefactor negSyn, SynopsisRefactor resSyn, CleanDataset cd, Helper h, long timepassed) throws IOException {
        this.posSyn = posSyn;
        this.negSyn = negSyn;
        this.resSyn = resSyn;
        this.d = cd;
        this.h = h;
        //this.ew = ew;
        h.setRamSettingInfo(d, posSyn, timepassed);
        estimatedAnswersPointQuery = new int[Main.numQueries];
        estimatedAnswersRangeQuery = new int[Main.numQueries];
        SCap = new int[Main.numQueries];
        queryExecutionTime = new long[Main.numQueries];
        estimatedAnswersPointQueryResiduSketch = new int[Main.numQueries];

    }

    public void run() throws IOException {
        error = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        //Main.logger.info("Randomly chosen queries: " + ew.randChosenQueries.length);
        //System.out.println("Randomly chosen queries: " + ew.randChosenQueries.length);
        if (Main.rangeQueries) {
            for (int i = 0; i< d.pointQueries.length; i++) {
                computeErrorRangeQuery(i, d.rangeQueries[i], d.rangeQueryAnswers[i]);
                //h.addQueryResult(q);
            }
        } else {
            for (int i = 0; i< d.pointQueries.length; i++) {
                computeErrorPointQuery(i, d.pointQueries[i], d.pointQueriesNumAttrs[i], d.pointQueryAnswers[i]);
                //h.addQueryResult(q);
            }
        }
        long endTime = System.currentTimeMillis();
        long timePassed = endTime - startTime;
//        Main.logger.info("Time passed for queries: " + timePassed + " ms, average: "
//                + (double) timePassed / d.queries.size() + " ms");
//        System.out.println("Time passed for queries: " + timePassed + " ms, average: "
//                + (double) timePassed / d.queries.size() + " ms");
//
        long totalExecTime = 0;
        for (long l : queryExecutionTime) {
            totalExecTime = totalExecTime + l;
        }
        System.out.println("Total execution time: " + totalExecTime + " ms, average: "
                + (double) totalExecTime / Main.numQueries + " ms");

        System.out.println("Difference between total time and execution time per query: "
                + (timePassed - totalExecTime));

        // Write results to file
        if (Main.rangeQueries) {
            h.writeResultsToFileRangeQuery(posSyn, d, timePassed, estimatedAnswersRangeQuery, queryExecutionTime);
            //TODO: write range query results to file here
        } else {
            h.writeResultsToFilePointQuery(posSyn, d, timePassed, estimatedAnswersPointQuery, SCap, queryExecutionTime, estimatedAnswersPointQueryResiduSketch);
        }
    }

    public void computeErrorPointQuery (int queryId, long[] q, int numPreds, int exactAnswer) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        long startTime = System.currentTimeMillis();
        int posEst = posSyn.query(q, numPreds); //TODO: get SCAP as well.
        int negEst = negSyn.query(q, numPreds); //TODO: get SCAP as well.
//        System.out.println("------");
//        System.out.println("posEst: " + posEst + ", negEst: " + negEst);
//        System.out.println("exactAnswer: " + exactAnswer);
//        System.out.println("posEst - negEst: " + (posEst - negEst));
//        System.out.println("Error: " + (posEst - negEst - exactAnswer));
//        System.out.println("------");

        estimatedAnswersPointQuery[queryId] = posEst - negEst;
        long endTime = System.currentTimeMillis();
        queryExecutionTime[queryId] = endTime - startTime;
        totalTime = totalTime + queryExecutionTime[queryId];

        estimatedAnswersPointQueryResiduSketch[queryId] = resSyn.query(q, numPreds);

        totalExecQueries++;
//        if (q.thrm33Case2) {
//            queryWithinBound = Math.abs(q.estimate - q.exactAnswer) < Main.eps * d.size;
//        }  else {
//            queryWithinBound = q.exactAnswer <= q.estimate * 2;
//        }
//        q.setResult(queryWithinBound);
//        q.setBound(d.size * Main.eps);
//        q.setEpsError(d.dataset.size());
    }

    public void computeErrorRangeQuery (int queryId, long[][] q, int exactAnswer) {
        //q.exactAnswer = d.exactSolution(q);
        if (exactAnswer == 0) {
            totalQueriesZero++;
        }
        long startTime = System.currentTimeMillis();
        estimatedAnswersRangeQuery[queryId] = posSyn.rangeQuery(q[0], q[1]);

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

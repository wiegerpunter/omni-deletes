package omni.Old;

import omni.*;

import java.io.IOException;
import java.util.ArrayList;

public class AnalysisTwoEstimators {
    Synopsis s;
    Dataset d;
    Helper h;
    ExpWorkload ew;


    long totalTime;
    int totalExecQueries;
    ArrayList<Double> error;

    int totalQueriesZero = 0;
    public AnalysisTwoEstimators(Synopsis s, Dataset d, Helper h, ExpWorkload ew, long timepassed) throws IOException {
        this.s = s;
        this.d = d;
        this.h = h;
        this.ew = ew;
        h.setRamSettingInfo(d, s, (double) timepassed/d.size);
    }

    public void run() throws IOException {
        error = new ArrayList<>();

        for (Query q : ew.randChosenQueries) {
            //System.out.println("Query " + rec.predicates + " Rec Id: " + rec.id);
            if (q.idxHighestAttr >= Main.numAttributes) {
                continue;
            }
            computeError(q);
            h.addQueryResult(q);

        }
    }

    public void computeError (Query q) {
        //q.exactAnswer = d.exactSolution(q);
        if (q.exactAnswer == 0) {
            totalQueriesZero++;
        }
        long startTime = System.currentTimeMillis();
        q.estimate = s.query(q);
        q.execTime = System.currentTimeMillis() - startTime;
        totalTime = totalTime + q.execTime;
        totalExecQueries++;
        //error.add(Math.abs(q.estimate - q.exactAnswer));
        if (Main.epsDS <= Main.epsCM / (1 - Main.epsCM)) {
            boolean queryWithinTightBound = Math.abs(q.estimate - q.exactAnswer) < d.size * q.predAttrs.size() * Math.pow(Main.epsCM, q.predAttrs.size()) *(1 + Main.epsDS);
            boolean queryWithinBound = Math.abs(q.estimate - q.exactAnswer) < d.size * Main.epsCM*(1 + Main.epsDS);
            q.setResult(queryWithinBound);
            q.setBound(d.size * Main.epsCM*(1 + Main.epsDS));
        } else {
            boolean queryWithinTightBound = Math.abs(q.estimate - q.exactAnswer) < d.size * (Main.epsDS + (1 + Main.epsDS) * (q.predAttrs.size() * Math.pow(Main.epsCM, q.predAttrs.size()) - Main.epsCM));
            boolean queryWithinBound = Math.abs(q.estimate - q.exactAnswer) < d.size * Main.epsDS;
            q.setResult(queryWithinBound);
            q.setBound(d.size * Main.epsDS);
        }
        q.setEpsError(d.dataset.size());
    }

}

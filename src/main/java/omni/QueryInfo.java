package omni;

import java.util.ArrayList;

public class QueryInfo {
    public int CMRow = 0;
    public int Scap;
    public int nmax;
    public int maxSize;
    public boolean case1;

    public double unionEstimate;
    public ArrayList<Double> jaccardEstimates;
    public ArrayList<Integer> witnessEstimates;

    public double jaccardEstimate;
    public int witness2LHS;
    public int numberOfKmins;
    public int numberOfKminsExceedingBound;

    public int exactUnion;
    public int exactIntersection;
    public double bound;
    private int estimate;

    public QueryInfo() {
        CMRow = 0;
        case1= false;
        Scap = 0;
        nmax=0;
        maxSize=0;
        unionEstimate=0;
        jaccardEstimate=0;
        witness2LHS=0;
        jaccardEstimates = new ArrayList<>();
        witnessEstimates = new ArrayList<>();

        numberOfKmins=0;
        exactUnion=0;
        exactIntersection= 0;
        bound=0;

    }

    public QueryInfo(QueryInfo qi) {
        this.case1 = qi.case1;
        this.CMRow = qi.CMRow;
        // deepcopy
        this.jaccardEstimates = new ArrayList<>(qi.jaccardEstimates);
        this.bound = qi.bound;
        this.nmax = qi.nmax;
        this.Scap = qi.Scap;
        this.maxSize = qi.maxSize;
        this.unionEstimate = qi.unionEstimate;
        this.witnessEstimates = new ArrayList<>(qi.witnessEstimates);
        this.jaccardEstimate = qi.jaccardEstimate;
        this.numberOfKmins = qi.numberOfKmins;
        this.numberOfKminsExceedingBound = qi.numberOfKminsExceedingBound;
        this.exactIntersection = qi.exactIntersection;
        this.exactUnion = qi.exactUnion;
        this.witness2LHS = qi.witness2LHS;
        this.estimate = qi.estimate;
    }

    public void setCMRow(int CMRow) {
        this.CMRow = CMRow;
    }

    public void setScap(int Scap, int nmax, int maxSize, boolean case1) {
        this.Scap = Scap;
        this.nmax = nmax;
        this.maxSize = maxSize;
        this.case1 = case1;
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

    public void setEstimate(int estimate) {
        this.estimate = estimate;
    }

    public int getEstimate() {
        return estimate;
    }

    public int getScap() {
        return Scap;
    }

    public boolean isCase1() {
        return case1;
    }
}

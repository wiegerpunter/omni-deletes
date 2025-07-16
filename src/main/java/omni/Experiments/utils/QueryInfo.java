package omni.Experiments.utils;

import omni.Experiments.ExpSetting;

public class QueryInfo {
    public int CMRow = 0;
    public int Scap;
    public int nmax;
    public int maxSize;
    public boolean case1;

    public double unionEstimate;
    public double jaccardEstimate;
    public int witness2LHS;
    public int numberOfKmins;
    public int numberOfKminsExceedingBound;

    public int exactUnion;
    public int exactIntersection;
    public double bound;
    private int estimate;

    public ExpSetting expSetting;

    public QueryInfo() {
        CMRow = 0;
        case1= false;
        Scap = 0;
        nmax=0;
        maxSize=0;
        unionEstimate=0;
        jaccardEstimate=0;
        witness2LHS=0;
        numberOfKmins=0;
        exactUnion=0;
        exactIntersection= 0;
        bound=0;
        expSetting = new ExpSetting();

    }

    public QueryInfo(QueryInfo qi) {
        this.case1 = qi.case1;
        this.CMRow = qi.CMRow;
        // deepcopy
        this.bound = qi.bound;
        this.nmax = qi.nmax;
        this.Scap = qi.Scap;
        this.maxSize = qi.maxSize;
        this.unionEstimate = qi.unionEstimate;
        this.jaccardEstimate = qi.jaccardEstimate;
        this.numberOfKmins = qi.numberOfKmins;
        this.numberOfKminsExceedingBound = qi.numberOfKminsExceedingBound;
        this.exactIntersection = qi.exactIntersection;
        this.exactUnion = qi.exactUnion;
        this.witness2LHS = qi.witness2LHS;
        this.estimate = qi.estimate;
        this.expSetting = new ExpSetting(qi.expSetting.getIntersectionSize(), qi.expSetting.getB(), qi.expSetting.getSetSizes());
    }

    public void setScap(int Scap, int nmax, int maxSize, boolean case1) {
        this.Scap = Scap;
        this.nmax = nmax;
        this.maxSize = maxSize;
        this.case1 = case1;
    }

    public void setEstimate(double unionEstimate, int witness2LHS, double jaccardEstimate, int estimate) {
        this.unionEstimate = unionEstimate;
        this.witness2LHS = witness2LHS;
        this.jaccardEstimate = jaccardEstimate;
        this.estimate = estimate;
    }


    public void setEstimate(QueryInfo queryInfo, int estimate) {
        this.unionEstimate = queryInfo.unionEstimate;
        this.witness2LHS = queryInfo.witness2LHS;
        this.jaccardEstimate = queryInfo.jaccardEstimate;
        this.estimate = estimate;
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
    public int getUsedMaxSize() {
        return maxSize;
    }

    public boolean isCase1() {
        return case1;
    }

    public int getTWOLHSWitnesses() {
        return witness2LHS;
    }
    public double getUnionEstimate() {
        return unionEstimate;
    }
    public double getJaccardEstimate() {
        return jaccardEstimate;
    }
}

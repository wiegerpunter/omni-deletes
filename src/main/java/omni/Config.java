package omni;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Config {
    public String setting;
    public String datasetName;
    public int numRepetitions;
    public boolean runOnODC;
    public String readFolder;
    public boolean withDeletes;
    public boolean spreadOutDeletes;
    public boolean useExactUnionSize;
    public List<Long> ramVals;
    public List<Double> noiseUpdateFractions;
    public List<Double> bufferValuesOmni;
    public List<Double> ingestBuffers;
    public List<Integer> dGridSearch;
    public List<Integer> bGridSearch;
    public List<Double> parFactorGridSearch;
    public int numBins;
    public int numPredicates;
    public boolean expOmniSet;
    public boolean expOmniHouse;
    public boolean exp2LHS;
    public boolean expASH;
    public boolean expHydra;
    public boolean expResSample;
    public boolean expCM;
    public boolean expPerRow;
    public boolean expCase1ReturnSCap;
    public List<Double> epsValues;
    public String[] sizeFactorOptions;
    public List<Integer> sizeNoise;
    public String experimentName;
    public boolean useMultNumAttributes;
    public int numSynthAttributes;
    public int numZipfAttributes;
    public List<Double> zipfAlphas;
    public int numUniformAttributes;
    public boolean useS0;
    public int numQueries;
    public String fileStartCondition;
    public int warmupNumber;
    public boolean rangeQueries;
    public int numAttributes;
    public int numStoredAttributes;
    public boolean checkConditions;
    public String currentDate;
    public int d;
    public int b;
    public double parFactor;

    public String getInputFolder() {
        return readFolder + "/input/";
    }
    public String getOutputFolder() {
        return readFolder + "/output/";
    }

    public void ramMBToBits() {
        ramVals.replaceAll(aLong -> aLong * 1000000 * 8);
    }

    public void setCurrentDate() {
        currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
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
    public List<Long> ramVals;
    public List<Double> noiseUpdateFractions;
    public List<Double> ingestBuffers;
    public List<Integer> dGridSearch;
    public List<Integer> bGridSearch;
    public List<Double> parFactorGridSearch;
    public int numBins;
    public int numPredicates;
    public boolean expOmniSketchSampleFirstQLater;
    public boolean expOmniSketchSampleFirstQLaterPerRow;
    public boolean expOmniSketchVLDB;
    public boolean expOmniSketchVLDBSampleSize;
    public boolean expOmniSketchSFQLSampleSize;
    public boolean expOmniSketchSFQLSampleSizePerRow;
    public boolean expOmniSketchSFQLSampleSizeTestHashSet;
    public boolean expASH;
    public boolean expHydra;
    public boolean expResSample;
    public boolean expCM;
    public String[] sizeFactorOptions;
    public List<Integer> sizeNoise;
    public String experimentName;
    public boolean useMultNumAttributes;
    public int numSynthAttributes;
    public int numZipfAttributes;
    public List<Double> zipfAlphas;
    public int numUniformAttributes;
    public int numQueries;
    public String fileStartCondition;
    public boolean rangeQueries;
    public int numAttributes;
    public int numStoredAttributes;
    public boolean checkConditions;
    public String currentDate;
    public int d;
    public int b;
    public double parFactor;
    public double betaBuffer;
    public boolean readAllFiles;
    public int numFiles;

    // Old parameters, might use them in the future
//    public List<Double> bufferValuesOmni;
//    public boolean expOmniSet;
//    public boolean expOmniHouse;
//    public boolean exp2LHS;
//    public int warmupNumber;
//    public boolean useS0;
//    public boolean expPerRow;
//    public boolean expCase1ReturnSCap;
//    public List<Double> epsValues;


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
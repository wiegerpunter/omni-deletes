package omni;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Config {
    public String setting;
    public String datasetName;
    public int seed;
    public boolean runOnODC;
    public String readFolder;
    public boolean withDeletes;
    public boolean spreadOutDeletes;
    public List<Long> ramVals;
    public List<Double> noiseUpdateFractions;
    public List<Double> ingestBuffers;
    public List<Integer> dGridSearch;
    public List<Integer> bGridSearch;
    public List<Integer> BGridSearch;
    public List<Integer> wGridSearch;
    public List<Integer> ingestBufferSizeGridSearch;
    public List<Integer> deleteBufferSizeGridSearch;

    public List<Double> parFactorGridSearch;
    public int numBins;
    public int numPredicates;
    public boolean expOmniSketchSampleFirstQLater;
    public boolean expOmniSketchSampleFirstQLaterPerRow;
    public boolean expOmniSketchVLDB;
    public boolean expOmniSketchVLDBS0;
    public boolean expOmniSketchVLDBS0PerRow;

    public boolean expOmniSketchVLDBSampleSize;
    public boolean expOmniSketchSFQLSampleSize;
    public boolean expOmniSketchSFQLSampleSizePerRow;
    public boolean expOmniSketchSFQLSampleSizeTestHashSet;
    public boolean expOmniSketchVLDBArray;
    public boolean expOmniSketchVLDBArrayRegBuffer;
    public boolean expOmniSketchVLDBArrayWithBuffer;
    public boolean expOmniSketchVLDBArrayWithBufferPerRow;

    public boolean expOmniSketchVLDBArrayWithBufferPessDeleteCount;
    public boolean expOmniSketchVLDBArrayWithBufferOnlyValid;
    public boolean expOmniSketchVLDBArrayWithoutBuffer;
    public boolean expOmniSketchVLDBSimpleBuffer;
    public boolean expOmniSketchVLDBSimpleBufferNoDeleteBuffer;

    public boolean expASH;
    public boolean expHydra;
    public boolean expResSample;
    public boolean expCM;
    public boolean expSlowTWOLHS;
    public boolean expFastTWOLHS;
    public String[] sizeFactorOptions;
    public List<Integer> sizeNoise;
    public String experimentName;
    public boolean useMultNumAttributes;
    public int numSynthAttributes;
    public int numZipfAttributes;
    public List<Double> zipfAlphas;
    public int numUniformAttributes;
    public int numSelectedRecForQueries;
    public String fileStartCondition;
    public boolean rangeQueries;
    public int numAttributes;
    public int numStoredAttributes;
    public boolean checkConditions;
    public String currentDate;
    public int d;
    public int b;
    public double parFactor;
    public double bufferASH;
    public boolean readAllFiles;
    public int numFiles;
    public int w;
    public int B;
    public boolean expOmniSketchSFQLOptimized;
    public boolean expOmniSketchSFQLOptimizedOnlyNew;
    public String parameterSettingType;
    public double bufferDeletesMinwise;
    public boolean expOmniSketchVLDBTreeSet;
    public boolean expOmniSketchVLDBTreeSetWithoutBuffer;
    public boolean getUniqueRecords;
    public int domain;
    public int[] domain_sizes;
    public boolean readFromDisk;
    public double[] percs;
    public double[] percsForMultiplyers;
    public boolean expSlowTWOLHSPerRow;
    public boolean expFastTWOLHSPerRow;
    public boolean expFastTWOLHSOneBucket;
    public boolean expFastTWOLHSOneBucketPerRow;
    public boolean expNMaxTWOLHS;
    public boolean expNMaxTWOLHSPerRow;
    public boolean expTWOLHSPerRowExact;
    public boolean expTWOLHSExact;
    public boolean useNoBufferOmniVLDB; // if true, use no buffer in omniVLDB
    public boolean useNoCast; // if true, record is always long[]
    public boolean expOmniSketchVLDBArrayWithBufferOpt;
    public boolean expOmniSketchVLDBArrayWithBufferOptBatchDeletes;
    public int ingestBufferSize;
    public int deleteBufferSize;
    public boolean expReadFile;
    public String job_name;

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
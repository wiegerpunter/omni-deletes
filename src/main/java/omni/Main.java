package omni;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
//import omni.deprecated.*;
//import omni.deprecated.CompareBaselinesRefactor;
//import omni.deprecated.DeleteStream;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Integer.parseInt;

//import java.util.logging.*;

public class Main {
    public static int repetition;
    public static int numRepetitions;
    public static String experiment_name;


    // Parameters for sketches
    public static double eps = 0.1;//1;
    public static double delta = 0.1;
    public static double qTarget = 0.1; // Ratio |A_1 \cap A_2|/|A_1 \cup A_2|
    public static int depth;// = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
    public static int width;// = (int) Math.ceil(Math.exp(1)/epsCM); // Width of sketch (number of buckets)
    public static int numTwoLHSReps = 300;// = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Number of repetitions of TwoLHS

    // Parameters for dataset
    public static int numAttributes;
    public static int numStoredAttributes;


    public static int streamSize;
    public static int maxSize;// = (int) Math.ceil(Math.log(1/deltaDS)/Math.log(Math.exp(1))/(qTarget * Math.pow(epsDS, 2))* (1 + epsB));
    public static int b;
    public static String datasetName;
    public static int numQueries;//1000;
    public static boolean queriesFromDomain = true;

    public static String workloadFilename;
    public static String fileStartCondition = "03110";
    public static int warmupNumber;
    public static int dyadicRangeBits = 33;
    public static int numFiles = 1;
    public static boolean useExactUnionSize;
    public static boolean withDeletes;
    public static int kminDeletes = 0;
    public static boolean spreadOutDeletes;
    public static boolean onlyKmin;
    public static boolean expaSH;
    public static boolean expHydra;
    public static boolean exp2LHS;
    public static boolean expResSample;
    public static boolean expCM;
    public static boolean expOmniSenate;
    public static boolean expOmniHouse;
    public static boolean expPerRow;
    public static boolean expCase1ReturnScap;
    public static int[] bGridSearch;
    public static int[] dGridSearch;
    public static double[] epsValues;
    public static int[] wGridSearch;
    public static int[] BGridSearch;

    public static double[] parFactorGridSearch;
    public static double[] densityGridSearch;
    public static int numSynthAttrs;
    public static int numZipfianAttrs;
    public static double[] zipfAlphas;
    public static int numUniformAttrs;
    public static boolean useS0;

    static String setting;
    public static boolean readAllFiles = true; // Set true if all files should be read, false if only the first file should be read
    public static boolean rangeQueries = false;
    public static boolean checkConditions = false; // Set true if conditions should be checked

    public static boolean useMultNumAttributes; // Set true if want to test for multiple number of attributes instead of just all attributes.
    public static boolean runOnODC; // True if running on ODC, false if running on local machine.
    public static String outputFolder;
    public static String inputFolder;

    public static String readFolder;
    public static int filesToRead = 1;//5;//5;
    public static Helper h;
    public static boolean writeSNMP = false;
    public static int sensitivityNumberOfRecords = 5000000;
    public static long[] ramVals = {(long) (50*8E6), (long) (100*8E6), (long) (150*8E6), (long) (200*8E6)};

    public static String currentDate;
    public static boolean countUniqueSamples = false;
    public static HashMap<Long, Integer> uniqueSamples = new HashMap<>();
    public static HashSet<long[]> uniqueSamplesReservoir = new HashSet<>();
    //public static int[] widthOptionsGridSearch;
    //public static int[] depthOptionsGridSearch;
    private static int sizeFactor;
    public static int numBins;
    public static int numPredicates;
    static double[] noiseUpdateFractions;
    public static String sizeFactorOptions;
    static double[] bufferValuesOmni;
    static double[] ingestBuffers;
    static int[] sizeNoise;

    public static void main(String[] args) throws IOException, NotOpenException, PcapNativeException, CsvValidationException {

        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);

//        setting = args[0];
//        Main.datasetName = args[1];
//        Main.numRepetitions = parseInt(args[2]);
//        Main.runOnODC= Boolean.parseBoolean(args[3]);
//        Main.readFolder = args[4];
//        Main.withDeletes = Boolean.parseBoolean(args[5]);
//        Main.spreadOutDeletes = Boolean.parseBoolean(args[6]);
//        Main.useExactUnionSize = Boolean.parseBoolean(args[7]);
//        Main.ramVals = parseLongArray(args[8]);
//        for (int i = 0; i < Main.ramVals.length; i++) {
//            System.out.println("ramVals[" + i + "] = " + Main.ramVals[i]);
//        }
//        Main.noiseUpdateFractions = getDoubleArray(args[9]);
//        Main.bufferValuesOmni = getDoubleArray(args[10]);
//        Main.ingestBuffers = getDoubleArray(args[11]);
////        Main.densityGridSearch = getDoubleArray(args[12]);
//        Main.dGridSearch = parseIntArray(args[12]);
//        Main.bGridSearch = parseIntArray(args[13]);
//        Main.parFactorGridSearch = getDoubleArray(args[14]);
////        Main.wGridSearch = parseIntArray(args[15]);
////        Main.BGridSearch = parseIntArray(args[16]);
//        Main.numBins = parseInt(args[15]);
//        Main.numPredicates = parseInt(args[16]);
//        Main.expOmniSenate = Boolean.parseBoolean(args[17]);
//        Main.expOmniHouse = Boolean.parseBoolean(args[18]);
//        Main.exp2LHS = Boolean.parseBoolean(args[19]);
//        Main.expaSH = Boolean.parseBoolean(args[20]);
//        Main.expHydra = Boolean.parseBoolean(args[21]);
//        Main.expResSample = Boolean.parseBoolean(args[22]);
//        Main.expCM = Boolean.parseBoolean(args[23]);
//        Main.expPerRow = Boolean.parseBoolean(args[24]);
//        Main.expCase1ReturnScap = Boolean.parseBoolean(args[25]);
//        Main.epsValues = getDoubleArray(args[26]);
//        Main.sizeFactorOptions = args[27];
//        Main.sizeNoise = parseIntArray(args[28]);
//        Main.experiment_name = args[29];
//        Main.useMultNumAttributes = Boolean.parseBoolean(args[30]);
//        Main.numSynthAttrs = parseInt(args[31]);
//        Main.numZipfianAttrs = parseInt(args[32]);
//        Main.zipfAlphas = getDoubleArray(args[33]);
//        Main.numUniformAttrs = parseInt(args[34]);
//        Main.useS0 = Boolean.parseBoolean(args[35]);
//        //Main.useExactUnionSize = Boolean.parseBoolean(args[3]);
//
//        if (Main.datasetName.equals("CAIDA")) {
//            Main.warmupNumber = 1000000;
//        } else if (Main.datasetName.equals("SNMP")) {
//            Main.warmupNumber = (int) 3e5;
//        } else {
//            Main.warmupNumber = (int) 3e5;
//        }
        outputFolder = readFolder + "output/";
        inputFolder = readFolder + "input/";
        config.setCurrentDate();
        config.ramMBToBits();
        currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Main.workloadFilename = "workload_" + Main.datasetName + "_N" + Main.filesToRead + ".csv";
//        h = new Helper();

//        if (Objects.equals(setting,"Compare Baselines Refactor")) {
//            CompareBaselinesRefactor cb = new CompareBaselinesRefactor(h);
//            cb.run();
        if (Objects.equals(config.setting, "Compare Synopses")) {
            omni.Experiments.RunExperiments runExperiments = new omni.Experiments.RunExperiments(config);
            runExperiments.run();
        } else if (Objects.equals(setting,"Test_two_LHS")) {
            RunExperimentsOld tt = new RunExperimentsOld(config, sizeFactor);
            tt.run();
//        } else if (Objects.equals(setting,"Delete Stream")) {
//            DeleteStream ds = new DeleteStream(h);
//            ds.run();
//        else if (Objects.equals(setting,"Compare Estimators")) {
//            CompareEstimators ce = new CompareEstimators(h);
//            ce.run();
//        }  else if (Objects.equals(setting,"Compare Distributions")) {
//            CompareDistributions cd = new CompareDistributions(h);
//            cd.run();
//        } else if (Objects.equals(setting,"Scalability")) {
//            Scalability sc = new Scalability(h);
//            sc.run();
//        } else if (Objects.equals(setting, "Range Queries")) {
//            RangeQueries rq = new RangeQueries(h);
//            rq.run();
        } else {
            throw new IllegalArgumentException("Invalid argument");
//            logger.severe("Invalid argument");
//            logger.severe(setting + " & " + args[1]);
        }
    }

    private static double[] getDoubleArray(String arg) {
        // remove the brackets
        arg = arg.substring(1, arg.length() - 1);
        String[] vals = arg.split(",");
        double[] res = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            res[i] = Double.parseDouble(vals[i]);
        }
        return res;
    }

    private static long[] parseLongArray(String arg) {
        String[] vals = arg.split(",");
        long[] res = new long[vals.length];
        for (int i = 0; i < vals.length; i++) {
            System.out.println("vals[" + i + "] = " + vals[i]);
            res[i] = (long) (Double.parseDouble(vals[i]) * 8E6);
        }
        return res;
    }

    private static int[] parseIntArray(String arg) {
        String[] vals = arg.split(",");
        int[] res = new int[vals.length];
        for (int i = 0; i < vals.length; i++) {
            res[i] = parseInt(vals[i]);
        }
        return res;
    }

    static SynopsisRefactor rs;


}



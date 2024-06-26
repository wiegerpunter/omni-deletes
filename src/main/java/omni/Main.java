package omni;

import com.opencsv.exceptions.CsvValidationException;
//import omni.deprecated.*;
//import omni.deprecated.CompareBaselinesRefactor;
//import omni.deprecated.DeleteStream;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
//import java.util.logging.*;

public class Main {
    public static int repetition;


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
    public static int numQueries =1000;//1000;
    public static boolean queriesFromDomain = true;

    public static String workloadFilename;
    public static String fileStartCondition = "03110";
    public static int warmupNumber;
    public static int dyadicRangeBits = 33;
    public static int numFiles = 1;
    public static boolean estPerRow = true; // Estimate per row for 2lhs
    public static boolean minEstimate = true;
    public static boolean useExactUnionSize = false;
    public static boolean withDeletes = true;
    public static int kminDeletes = 0;
    public static boolean spreadOutDeletes = false;
    static String setting;
    public static boolean readAllFiles = true; // Set true if all files should be read, false if only the first file should be read
    public static boolean rangeQueries = false;
    public static boolean checkConditions = true; // Set true if conditions should be checked
    public static boolean createNewWorkload = true; // Set true if new workload should be created
    public static boolean createNewRangeWorkload = false;
    static boolean useCustomMaxSize = false; // Set true if custom maxsize should be used. Only relevant for sensitivity analysis, not for comparison.
    public static boolean useMultNumAttributes = false; // Set true if want to test for multiple number of attributes instead of just all attributes.
    static boolean useTighterBound = false; // Set true if want to check tighter bound in sensitivity analysis
    public static boolean useDS = false; // True if DS is used, false if Kminwise hashing is used. Should be false by default.
    public static boolean useTwoLHS = true; // True if TwoLHS is used, false if Kminwise hashing is used. Should be false by default.
    public static boolean sanityBound = false; // True if parameter setting is decided by sanity bound. False if decided by how much memory is left for B.
    public static boolean runOnODC = true; // True if running on ODC, false if running on local machine.
    public static String outputFolder;
    public static String inputFolder;

    public static String readFolder;
    public static int filesToRead = 5;//5;//5;
    public static Helper h;
    public static boolean useWarmup = false;
    public static boolean writeSNMP = false;
    public static boolean sensitivityAnalysis = false;
    public static int sensitivityNumberOfRecords = 5000000;
    public static boolean LOO = false;
    public static boolean LTO = false;
    public static long[] ramVals = {(long) (50*8E6), (long) (100*8E6), (long) (150*8E6), (long) (200*8E6)};//, (long) (400*8E6), (long) (600*8E6), (long) (800*8E6), (long) (1600*8E6)}; //(long) (10*8E6), (long) (20*8E6), (long) (50*8E6), (long) (100*8E6),
    //public static long[] ramVals = {(long) (1000*8E6), (long) (1500*8E6)};

    public static String currentDate;
    public static boolean countUniqueSamples = false;
    public static HashMap<Long, Integer> uniqueSamples = new HashMap<>();
    public static HashSet<long[]> uniqueSamplesReservoir = new HashSet<>();

    public static void main(String[] args) throws IOException, NotOpenException, PcapNativeException, CsvValidationException {
        // Args
        // Arg[0] = setting
        // arg[1] = dataset name
        // arg[2] = repetition
        // arg[3] = runOnODC
        // arg[4] = useWarmup
        // arg[5] = epsilon
        // arg[6] = delta
        // arg[7] = ram




        //setParameters(eps: 0.1, delta: 0.05, maxLevel: 32, seed: 1, numAttributes: 2, qTarget: 0.01);
        setting = args[0];
        Main.datasetName = args[1];
        Main.repetition = Integer.parseInt(args[2]);
        //Main.useExactUnionSize = Boolean.parseBoolean(args[3]);

        if (Main.datasetName.equals("CAIDA")) {
            Main.warmupNumber = 1000000;
        } else if (Main.datasetName.equals("SNMP")) {
            Main.warmupNumber = (int) 3e5;
        } else {
            Main.warmupNumber = (int) 3e5;
        }
        if (runOnODC) {
            readFolder = "/app/data/";
            outputFolder = "/app/data/output/";
            inputFolder = "/app/data/input/";
        } else {
            readFolder = "./";
            outputFolder = "./output/";
            inputFolder = "./input/";
        }

        currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Main.workloadFilename = "workload_" + Main.datasetName + "_N" + Integer.toString(Main.filesToRead) + ".csv";
        h = new Helper();

//        if (Objects.equals(setting,"Compare Baselines Refactor")) {
//            CompareBaselinesRefactor cb = new CompareBaselinesRefactor(h);
//            cb.run();
        if (Objects.equals(setting,"Test_two_LHS")) {
            TestTwoLHS tt = new TestTwoLHS(h);
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
    static SynopsisRefactor rs;


}



package omni;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static int repetition;
    // Parameters for sketches
    public static double eps = 0.1;//1;
    public static double delta = 0.1;
    public static int depth;
    public static int width;
    public static int numTwoLHSReps = 300;

    // Parameters for dataset
    public static int numAttributes;
    public static int numStoredAttributes;

    public static int streamSize;
    public static int b;
    public static String datasetName;
    public static int dyadicRangeBits = 33;
    public static int kminDeletes = 0;
    public static boolean rangeQueries = false;

    public static String outputFolder;
    public static String inputFolder;

    public static String readFolder;
    public static String currentDate;
    public static boolean countUniqueSamples = false;
    public static HashMap<Long, Integer> uniqueSamples = new HashMap<>();
    public static HashSet<long[]> uniqueSamplesReservoir = new HashSet<>();

    public static void main(String[] args) throws IOException, NotOpenException, PcapNativeException, CsvValidationException {

        String jsonFilePath = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(jsonFilePath), Config.class);
        outputFolder = readFolder + "output/";
        inputFolder = readFolder + "input/";
        config.setCurrentDate();
        config.ramMBToBits();
        currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        if (Objects.equals(config.setting, "Compare Synopses")) {
            omni.Experiments.RunExperiments runExperiments = new omni.Experiments.RunExperiments(config);
            runExperiments.run();
        } else {
            throw new IllegalArgumentException("Invalid argument");
        }
    }
}



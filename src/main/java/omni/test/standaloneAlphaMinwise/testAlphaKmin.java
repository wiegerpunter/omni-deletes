package omni.test.standaloneAlphaMinwise;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.SampleTypes.KminCustomArray;
import omni.synopses.omniFactory.SampleTypes.KminTreeSet;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.utils.KminUtils;
import omni.test.standaloneAlphaMinwise.Data;
import omni.test.standaloneAlphaMinwise.Event;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class testAlphaKmin {
    public static void main(String[] args) throws SQLException, IOException {
        create_table();



        int[] intersectionSizes = {1000, 10000};
        int[] numSets = {9};
        int[] noiseSizes = {1000};
        int[] Bs = {100};
        int[] domains = {10,100,100000};
        double[][] betaPairs = new double[][]{
                {1, 1},
                {1.33, 1.35},
                {2, 2.2},
                {4, 4.2},
                {10, 11}
        };

        int numSeeds = 2;

        for (int intersectionSize : intersectionSizes) {
            for (int numSet : numSets) {
                for (int noiseSize : noiseSizes) {
                    for (int domain : domains) {
                        for (int seed = 0; seed < numSeeds; seed++) {
                            for (int B : Bs) {
                                for (double[] betaPair : betaPairs) {
                                    double beta = betaPair[1];
                                    double alpha = betaPair[0];
                                    System.out.printf("\r Running test with intersectionSize: " + intersectionSize +
                                            ", numSet: " + numSet + ", noiseSize: " + noiseSize +
                                            ", seed: " + seed + ", B: " + B + ", beta: " + beta + ", alpha: " + alpha);

                                    // Run tests with different beta and alpha values
                                    runTest("KminCustomArray", intersectionSize, numSet, noiseSize, seed, B, beta, alpha, domain);
                                    runTest("KminCustomArray", intersectionSize, numSet, noiseSize, seed, B, 0, alpha, domain);
                                    runTest("KminTreeSet", intersectionSize, numSet, noiseSize, seed, B, beta, alpha, domain);
                                    runTest("KminTreeSet", intersectionSize, numSet, noiseSize, seed, B, 0, alpha, domain);

                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private static void create_table() {
        String datestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = "output/tests/testAlphaKmin_" + datestamp + ".csv";

        try (FileWriter csvWriter = new FileWriter(filePath)) {
            // Write CSV header
            csvWriter.append("setting,intersectionSize,numSets,noiseSize,domain,seed,B,beta,alpha,estimate,error,absoluteError\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void add_result(String setting, int intersectionSize, int numSets, int noiseSize, int domain,
                                   int seed,
                                   int B, double beta, double alpha, int estimate, int error, int absoluteError) throws SQLException, IOException {
        String datestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = "output/tests/testAlphaKmin_" + datestamp + ".csv";

        try (FileWriter csvWriter = new FileWriter(filePath, true)) { // true = append mode
            csvWriter.append(setting)
                    .append(",").append(String.valueOf(intersectionSize))
                    .append(",").append(String.valueOf(numSets))
                    .append(",").append(String.valueOf(noiseSize))
                    .append(",").append(String.valueOf(domain))
                    .append(",").append(String.valueOf(seed))
                    .append(",").append(String.valueOf(B))
                    .append(",").append(String.valueOf(beta))
                    .append(",").append(String.valueOf(alpha))
                    .append(",").append(String.valueOf(estimate))
                    .append(",").append(String.valueOf(error))
                    .append(",").append(String.valueOf(absoluteError)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTest(String setting, int intersectionSize, int numSets, int noiseSize,
                                int seed, int B, double beta, double alpha, int domain) throws SQLException, IOException {
        // Goal of test: use alpha kminwise standalone to see if accuracy of using beta is better than not using it.

        // Needed:
        // stream of residu inserts in intersection.
        // stream of noise in intersection.
        // stream of noise out of intersection.
        // stream of residu out of intersection.

        // three streams of data for the three sets. Intersecting three sets to find result which is intersection
        Data data = data_generation(intersectionSize, numSets, noiseSize, domain);

        int estimate = experiment(setting, data, B, beta, alpha, seed);
        int exact = (int) (data.intersection.length / alpha);
        int error = estimate - exact;
        int absoluteError = Math.abs(error);

        add_result(setting, intersectionSize, numSets, noiseSize, domain, seed, B, beta, alpha,
                estimate, error, absoluteError);

    }

    private static int experiment(String setting, Data data, int B, double beta, double alpha, int seed) {
        Sample[] samples;
        if (setting.equals("KminCustomArray")) {
           samples = new KminCustomArray[data.numberOfSets];

            for (int i = 0; i < data.numberOfSets; i++) {
                samples[i] = new KminCustomArray(B, 31, beta, "alpha", false);
            }
        } else if (setting.equals("KminTreeSet")) {
            samples = new KminTreeSet[data.numberOfSets];
            for (int i = 0; i < data.numberOfSets; i++) {
                samples[i] = new KminTreeSet(B, 31, beta, "alpha");
            }
        } else {
            throw new IllegalArgumentException("Unknown setting: " + setting);
        }


        HashFunction x = Hashing.murmur3_32_fixed(seed);

        ingest_data(samples, data, x, alpha);

        Sample[] results = new Sample[data.numberOfSets];
        System.arraycopy(samples, 0, results, 0, data.numberOfSets);
        QueryInfo queryInfo = new QueryInfo();
        // compute intersection of results
        if (setting.equals("KminCustomArray")) {
            return KminUtils.estimateArray(results, queryInfo, 0, data.numberOfSets);
        } else if (setting.equals("KminTreeSet")) {
            return KminUtils.estimateTreeSet(results, queryInfo, 0, data.numberOfSets);
        } else {
            throw new IllegalArgumentException("Unknown setting: " + setting);
        }
    }

    private static Data data_generation(int intersectionSize, int numberOfSets, int noiseSize, int domainSize) {
        int[] domainValues = new int[numberOfSets + 2];
        domainValues[0] = 1;
        for (int i = 1; i < domainValues.length; i++) {
            domainValues[i] = domainValues[i - 1] + domainSize; // Fill with values from 1 to numberOfSets
        }

        int[] intersection = new int[intersectionSize];
        Random random = new Random(0);
        for (int i = 0; i < intersectionSize; i++) {
            // Fill intersection with values from the domain, not necessarily unique
            intersection[i] = random.nextInt(domainValues[0], domainValues[1]);
        }

        int[][] noiseSets = new int[numberOfSets][noiseSize];
        for (int setIndex = 0; setIndex < numberOfSets; setIndex++) {
            Random setRandom = new Random(setIndex + 1); // Different seed for each set
            for (int i = 0; i < noiseSize; i++) {
                // Ensures values are outside the intersection and non-overlapping
                noiseSets[setIndex][i] = setRandom.nextInt(domainValues[setIndex + 1], domainValues[setIndex + 2]);
            }
        }
        return new Data(intersection, noiseSets);
        // Here you can return or store the generated data as needed
    }

    private static void ingest_data(Sample[] samples, Data data, HashFunction x, double alpha) {
        List<Event> events = new ArrayList<>();

        int[] intersection = data.intersection;
        int[][] noiseSets = data.noise;

        // Add insert/delete events for intersection (shared across all sets)
        for (int i = 0; i < intersection.length; i++) {
            int v = intersection[i];
            events.add(new Event(-1, v, 1)); // insert to all sets
        }
        for (int i = 0; i < intersection.length / alpha; i++) {
            int v = intersection[i];
            events.add(new Event(-1, v, -1)); // delete from all sets
        }

        // Add insert/delete events for noise (per set)
        for (int set = 0; set < noiseSets.length; set++) {
            int[] noise = noiseSets[set];
            for (int i = 0; i < noise.length; i++) {
                events.add(new Event(set, noise[i], 1)); // insert to specific set
            }
            for (int i = 0; i < noise.length / alpha; i++) {
                events.add(new Event(set, noise[i], -1)); // delete from specific set
            }
        }

        // Shuffle while maintaining insert-before-delete logic automatically
        Collections.shuffle(events, new Random());

        // Track what's been inserted to ensure valid deletes
        Set<Integer>[] inserted = new HashSet[samples.length];
        for (int i = 0; i < inserted.length; i++) {
            inserted[i] = new HashSet<>();
        }

        for (Event e : events) {
            int hx = x.hashInt(e.value).asInt();
            if (e.op == 1) {
                if (e.setIndex == -1) {
                    for (int i = 0; i < samples.length; i++) {
                        samples[i].ingest(hx, 1);
                        inserted[i].add(e.value);
                    }
                } else {
                    samples[e.setIndex].ingest(hx, 1);
                    inserted[e.setIndex].add(e.value);
                }
            } else { // deletion
                if (e.setIndex == -1) {
                    for (int i = 0; i < samples.length; i++) {
                        if (inserted[i].contains(e.value)) {
                            samples[i].ingest(hx, -1);
                        }
                    }
                } else {
                    if (inserted[e.setIndex].contains(e.value)) {
                        samples[e.setIndex].ingest(hx, -1);
                    }
                }
            }
        }
    }
//
//    private static void ingest_data(KminCustomArray[] samples, Data data, HashFunction x, double alpha) {
//
//        int[] intersection = data.intersection;
//        int[][] noiseSets = data.noise;
//
//
//        for (int k : intersection) {
//            int hx = x.hashInt(k).asInt();
//            for (KminCustomArray sample : samples) {
//                sample.ingest(hx, 1);
//            }
//        }
//
//        for (int i = 0; i < noiseSets.length; i++) {
//            int[] noise = noiseSets[i];
//            for (int j : noise) {
//                int hx = x.hashInt(j).asInt();
//                samples[i].ingest(hx, 1);
//            }
//        }
//
//        for (int j =0; j < intersection.length/alpha; j++) {
//            int hx = x.hashInt(j).asInt();
//            for (KminCustomArray sample : samples) {
//                sample.ingest(hx, -1);
//            }
//        }
//
//        for (int i = 0; i < noiseSets.length; i++) {
//            int[] noise = noiseSets[i];
//            for (int j = 0; j < noise.length/alpha; j++) {
//                int hx = x.hashInt(j).asInt();
//                samples[i].ingest(hx, -1);
//            }
//        }
//
//    }
}

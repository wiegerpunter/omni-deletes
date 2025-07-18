package omni.test.standaloneTWOLHS;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.SampleTypes.*;
import omni.synopses.omniFactory.utils.TWOLHSUtils;
import omni.test.standaloneAlphaMinwise.Data;
import omni.test.standaloneAlphaMinwise.Event;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class testAcc2LHS {
    public static void main(String[] args) throws SQLException, IOException {
        create_table();

        int[] intersectionSizes = {10000};
        int[] numSets = {1,2,3,4,5,6,7,8,9};
        int[] Bs = {3,6,33,66,333,666};
        int[] domains = {100000000};
        double[] alphas = {0.0,0.33,1.0,3.0,9.0};

        int numSeeds = 10;

        for (int intersectionSize : intersectionSizes) {
            for (int numSet : numSets) {
                    for (int domain : domains) {
                        for (int seed = 0; seed < numSeeds; seed++) {
                            for (int B : Bs) {
                                for (double alpha: alphas) {
                                    System.out.printf("\r Running test with intersectionSize: " + intersectionSize +
                                            ", numSet: " + numSet +
                                            ", seed: " + seed + ", B: " + B);
                                    int noiseSize = (int) (intersectionSize * (alpha));
                                    // Run tests with different beta and alpha values
                                    runTest("FastTWOLHS", intersectionSize, numSet, noiseSize, seed, B, domain);
//                                    runTest("ExactSolution", intersectionSize, numSet, noiseSize, seed, B, 0, alpha, domain);
//                                runTest("SlowTWOLHS", intersectionSize, numSet, noiseSize, seed, B, domain);
                                    runTest("ExactSolution", intersectionSize, numSet, noiseSize, seed, B, domain);
                                }
                            }
                        }
                    }
            }
        }
    }


    private static void create_table() {
        String datestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = "output/tests/test2LHS_" + datestamp + ".csv";

        try (FileWriter csvWriter = new FileWriter(filePath)) {
            // Write CSV header
            csvWriter.append("setting,intersectionSize,unionSize,numSets," +
                    "noiseSize,domain,seed,2LHSrepetitions,estimate,error," +
                    "absoluteError,witnesses,unionEstimate,JacEstimate\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void add_result(String setting, int intersectionSize, int unionSize, int numSets, int noiseSize, int domain,
                                   int seed,
                                   int B, int estimate, int error, int absoluteError,
                                   QueryInfo queryInfo) throws SQLException, IOException {
        String datestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = "output/tests/test2LHS_" + datestamp + ".csv";

        try (FileWriter csvWriter = new FileWriter(filePath, true)) { // true = append mode
            csvWriter.append(setting)
                    .append(",").append(String.valueOf(intersectionSize))
                    .append(",").append(String.valueOf(unionSize))
                    .append(",").append(String.valueOf(numSets))
                    .append(",").append(String.valueOf(noiseSize))
                    .append(",").append(String.valueOf(domain))
                    .append(",").append(String.valueOf(seed))
                    .append(",").append(String.valueOf(B))
                    .append(",").append(String.valueOf(estimate))
                    .append(",").append(String.valueOf(error))
                    .append(",").append(String.valueOf(absoluteError))
                    .append(",").append(String.valueOf(queryInfo.getTWOLHSWitnesses()))
                    .append(",").append(String.valueOf(queryInfo.getUnionEstimate()))
                    .append(",").append(String.valueOf(queryInfo.getJaccardEstimate()))
                    .append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTest(String setting, int intersectionSize, int numSets, int noiseSize,
                                int seed, int B, int domain) throws SQLException, IOException {
        // Goal of test: use alpha kminwise standalone to see if accuracy of using beta is better than not using it.

        // Needed:
        // stream of residu inserts in intersection.
        // stream of noise in intersection.
        // stream of noise out of intersection.
        // stream of residu out of intersection.

        // three streams of data for the three sets. Intersecting three sets to find result which is intersection
        Data data = data_generation(intersectionSize, numSets, noiseSize, domain);

        QueryInfo queryInfo = new QueryInfo();
        int estimate = experiment(setting, data, B, seed, queryInfo);
        int exact = (int) (data.intersection.length);
        if (numSets == 1) {
            exact += data.noise[0].length; // If only one set, the estimate is the intersection size plus noise size of that set.
        }

        int error = estimate - exact;
        int absoluteError = Math.abs(error);

        add_result(setting, data.getIntersectionSize(), data.getUnionSize(), numSets, data.getNoiseSize(), domain, seed, B,
                estimate, error, absoluteError, queryInfo);

    }

    private static int experiment(String setting, Data data, int B, int seed, QueryInfo queryInfo) {
        Sample[][] samples;

        switch(setting) {
            case "ExactSolution" -> {
                samples = new Sample[data.numberOfSets][1];
                for (int i = 0; i < data.numberOfSets; i++) {
                    samples[i][0] = new ExactSolution();
                }
            }
            case "FastTWOLHS", "SlowTWOLHS", "NMaxTWOLHS" -> {
                // Initialize samples for FastTWOLHS or SlowTWOLHS
                samples = new Sample[data.numberOfSets][B];
                for (int i = 0; i < data.numberOfSets; i++) {
                    for (int j = 0; j < B; j++) {
                        samples[i][j] = new TWOLHS(j, seed);
                    }
                }
            }
            default -> throw new IllegalArgumentException("Unknown setting: " + setting);
        }

        HashFunction x = Hashing.murmur3_32_fixed(seed);
        HashFunction x_g = Hashing.murmur3_32_fixed(seed + 23);
        ingest_data(setting, samples, data, x, x_g);

        // compute intersection of results
        switch (setting) {
            case "FastTWOLHS" -> {
                double u = TWOLHSUtils.setUnionEstimator(samples, 0.1, B, true);
                double estimate = TWOLHSUtils.setIntersectEstimatorAllBuckets(samples, u, true, queryInfo);
                queryInfo.setEstimate(queryInfo, (int) estimate);
                return (int) estimate;
            }
            case "NMaxTWOLHS" -> {
                double u = data.getIntersectionSize() + data.getNoiseSize();
                double estimate = TWOLHSUtils.setIntersectEstimatorAllBuckets(samples, u, true, queryInfo);
                queryInfo.setEstimate(queryInfo, (int) estimate);
                return (int) estimate;
            }
            case "SlowTWOLHS" -> {
                double u = TWOLHSUtils.setUnionEstimator(samples, 0.1, B, false);
                double estimate = TWOLHSUtils.setIntersectEstimatorAllBuckets(samples, u, false, queryInfo);
                queryInfo.setEstimate(queryInfo, (int) estimate);
                return (int) estimate;
            }
            default -> {
                // For ExactSolution, we can directly compute the intersection size
                return TWOLHSUtils.exactSolution(samples, queryInfo);
            }
        }

    }

    private static Data data_generation(int intersectionSize, int numberOfSets, int noiseSize, int domainSize) {
        int[] domainValues = new int[numberOfSets + 2];
        domainValues[0] = 1;
        for (int i = 1; i < domainValues.length; i++) {
            domainValues[i] = domainValues[i - 1] + domainSize; // Fill with values from 1 to numberOfSets
        }

        int[] intersection = new int[intersectionSize];
        int domainPerRecord = (domainValues[1] - domainValues[0]) / intersectionSize;
        Random random = new Random(0);
        for (int i = 0; i < intersectionSize; i++) {
            // Fill intersection with values from the domain, not necessarily unique
            intersection[i] = random.nextInt(domainValues[0] + i * domainPerRecord,
                    domainValues[0] + (i+1) * domainPerRecord);
        }

        int[][] noiseSets = new int[numberOfSets][noiseSize];
        for (int setIndex = 0; setIndex < numberOfSets; setIndex++) {
            if (noiseSize == 0) { break;};
            int domainPerSet = (domainValues[setIndex + 2] - domainValues[setIndex + 1]) / noiseSize;
            Random setRandom = new Random(setIndex + 1); // Different seed for each set
            for (int i = 0; i < noiseSize; i++) {
                // Ensures values are outside the intersection and non-overlapping
                noiseSets[setIndex][i] = setRandom.nextInt(domainValues[setIndex + 1] + i * domainPerSet,
                        domainValues[setIndex + 1] + (i+1) * domainPerSet);
            }
        }
        Data data = new Data(intersection, noiseSets);
        data.setIntersectionSize(intersectionSize);
        data.setNoiseSize(noiseSize);
        data.setUnionSize(intersectionSize + numberOfSets * noiseSize);
        return data;
        // Here you can return or store the generated data as needed
    }

    private static void ingest_data(String setting, Sample[][] samples, Data data, HashFunction x, HashFunction x_g) {
        List<Event> events = new ArrayList<>();

        int[] intersection = data.intersection;
        int[][] noiseSets = data.noise;

        // Add insert/delete events for intersection (shared across all sets)
        for (int v : intersection) {
            events.add(new Event(-1, v, 1)); // insert to all sets
        }
//        for (int i = 0; i < intersection.length / alpha; i++) {
//            int v = intersection[i];
//            events.add(new Event(-1, v, -1)); // delete from all sets
//        }

        // Add insert/delete events for noise (per set)
        for (int set = 0; set < noiseSets.length; set++) {
            int[] noise = noiseSets[set];
            for (int j : noise) {
                events.add(new Event(set, j, 1)); // insert to specific set
            }
//            for (int j : noise) {
//                events.add(new Event(set, j, -1)); // delete from specific set
//            }
        }

        // Shuffle while maintaining insert-before-delete logic automatically

        // Track what's been inserted to ensure valid deletes
        Set<Integer>[] inserted = new HashSet[samples.length];
        for (int i = 0; i < inserted.length; i++) {
            inserted[i] = new HashSet<>();
        }

        for (Event e : events) {
            int hx = x.hashInt(e.value).asInt() >>> 1; // hash the value
//            int hx = e.value;
            if (e.op == 1) {
                if (e.setIndex == -1) { // insertion to all sets
                    for (int i = 0; i < samples.length; i++) {
                        switch (setting) {
                            case "FastTWOLHS", "NMaxTWOLHS" -> {
                                int g = hashG(hx, samples[i].length, x_g);
                                samples[i][g].ingest(hx, e.op);
                            }
                            case "SlowTWOLHS" -> {
                                for (int k = 0; k < samples[0].length; k++) {
                                    samples[i][k].ingest(hx, e.op);
                                }
                            }
                            case "ExactSolution" -> samples[i][0].ingest(e.value, e.op);
                            default -> throw new IllegalArgumentException("Unknown setting: " + setting);
                        }
//                        samples[i].ingest(hx, 1);
                        inserted[i].add(e.value);
                    }
                } else {
                    switch (setting) {
                        case "FastTWOLHS", "NMaxTWOLHS" -> {
                            int g = hashG(hx, samples[e.setIndex].length, x_g);
                            samples[e.setIndex][g].ingest(hx, e.op);
                        }
                        case "SlowTWOLHS" -> {
                            for (int k = 0; k < samples[0].length; k++) {
                                samples[e.setIndex][k].ingest(hx, e.op);
                            }
                        }
                        case "ExactSolution" -> samples[e.setIndex][0].ingest(e.value, e.op);
                        default -> throw new IllegalArgumentException("Unknown setting: " + setting);
                    }
                    inserted[e.setIndex].add(e.value);
                    }
//            } else { // deletion
//                if (e.setIndex == -1) {
//                    throw new IllegalArgumentException("Cannot delete from all sets at once in this context.");
//                } else {
//                    if (inserted[e.setIndex].contains(e.value)) {
//                        samples[e.setIndex].ingest(hx, -1);
//                    } else {
//                        throw new IllegalArgumentException("Trying to delete value " + e.value + " that was never inserted in set " + e.setIndex);
//                    }
//                }
            }
        }
    }


    private static int hashG(long id, int B, HashFunction x_g) {
        // id hash modulo numTWOLHSRepetitions
        return (x_g.hashLong(id).asInt() >>> 1 )% B;
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

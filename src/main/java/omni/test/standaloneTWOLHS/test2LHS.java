package omni.test.standaloneTWOLHS;

import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.TWOLHS;
import omni.test.standaloneAlphaMinwise.Data;

import java.util.Random;

public class test2LHS {

    public static void main(String[] args) {
        // sample data
        //test2LHS();
        simpleTest();
    }

    public static void test2LHS() {
        int intersectionSize = 1000;
        int numReps = 30;

        TWOLHS[] sketch1 = new TWOLHS[numReps];
        TWOLHS[] sketch2 = new TWOLHS[numReps];
        for (int i = 0; i < numReps; i++) {
            sketch1[i] = new TWOLHS(i, 0);
            sketch2[i] = new TWOLHS(i, 0);
        }
        Data data = data_generation(intersectionSize, 2, 1000, 1000);

        for (int i = 0; i < data.intersection.length; i++) {
            for (int j = 0; j < numReps; j++) {
                sketch1[j].ingest(data.intersection[i], 1);
                sketch2[j].ingest(data.intersection[i], 1);
            }
        }
        for (int i = 0; i < data.noise[0].length; i++) {
            for (int j = 0; j < numReps; j++) {
                sketch1[j].ingest(data.noise[0][i], 1);
                sketch2[j].ingest(data.noise[1][i], 1);
            }
        }

        // Create samples from sketches
        Sample[][] samples = new Sample[2][numReps];
        for (int i = 0; i < numReps; i++) {
            samples[0][i] = sketch1[i];
            samples[1][i] = sketch2[i];
        }
        QueryInfo queryInfo = new QueryInfo();
        // Estimate union and intersection
        double unionEstimate = omni.synopses.omniFactory.utils.TWOLHSUtils.setUnionEstimator(samples, 0.1, numReps, false);
        double intersectionEstimate = omni.synopses.omniFactory.utils.TWOLHSUtils.setIntersectEstimator(samples, unionEstimate, false, queryInfo);
        System.out.println("Union Estimate: " + unionEstimate);
        System.out.println("Intersection Estimate: " + intersectionEstimate);
        System.out.println("Intersection Size: " + data.intersection.length);
        System.out.println("Noise Size: " + data.noise[0].length);
        System.out.println("Error in Intersection Estimate: " + Math.abs(intersectionEstimate - data.intersection.length));
    }


    public static void simpleTest(){
        int[] data = new int[]{1,2,3,4,5,6};
        int[] set2 = new int[]{1,2,3,4,7,8};
        int numReps = 30;
        TWOLHS[] sketch1 = new TWOLHS[numReps];
        TWOLHS[] sketch2 = new TWOLHS[numReps];
        for (int i = 0; i < numReps; i++) {
            sketch1[i] = new TWOLHS(i, 0);
            sketch2[i] = new TWOLHS(i, 0);
        }
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < numReps; j++) {
                sketch1[j].ingest(data[i], 1);
            }
        }
        for (int i = 0; i < set2.length; i++) {
            for (int j = 0; j < numReps; j++) {
                sketch2[j].ingest(set2[i], 1);
            }
        }
        // Create samples from sketches
        Sample[][] samples = new Sample[2][numReps];
        for (int i = 0; i < numReps; i++) {
            samples[0][i] = sketch1[i];
            samples[1][i] = sketch2[i];
        }

        QueryInfo queryInfo = new QueryInfo();

        // Estimate union and intersection
        double unionEstimate = omni.synopses.omniFactory.utils.TWOLHSUtils.setUnionEstimator(samples, 0.1, numReps, false);
        double intersectionEstimate = omni.synopses.omniFactory.utils.TWOLHSUtils.setIntersectEstimator(samples, unionEstimate, false, queryInfo);
        System.out.println("Union Estimate: " + unionEstimate);
        System.out.println("Intersection Estimate: " + intersectionEstimate);

    }
    public static Data data_generation(int intersectionSize, int numberOfSets, int noiseSize, int domainSize) {
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
}

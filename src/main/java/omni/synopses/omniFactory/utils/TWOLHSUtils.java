package omni.synopses.omniFactory.utils;

import omni.Experiments.utils.QueryInfo;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.TWOLHS;

import java.util.ArrayList;

import static java.lang.Math.*;

public class TWOLHSUtils {

    public static double setUnionEstimator(Sample[][] samples, double eps, int numTWOLHSRepetitions, boolean useFastTWOLHS) {
        // estimate union used in 2lhs estimator.

        double f = (1 + eps) * numTWOLHSRepetitions / 8; // from paper.

        int index = 0;
        int count = 0;
        while (true) {
            count = 0;
            boolean incrCount = false;
            for (int i = 0; i < numTWOLHSRepetitions; i++) {
                incrCount = false;
                for (Sample[] sample : samples) {
                    if (!((TWOLHS) sample[i]).emptyBucket(index)) {
                        incrCount = true;
                        break;
                    }
                }
                if (incrCount) {
                    count++;
                }
            }
            if (count < f) {
                break;
            } else {
                index++;
            }
        }
        double phat = (double) (count + 1) / (numTWOLHSRepetitions + 1);
        double R = pow(2, index + 1);
        double S = (log(1-phat)/log(2))/(log(1-1/R)/log(2));
        if (useFastTWOLHS) {
            return (int) ceil(S) * numTWOLHSRepetitions;
        } else{
            return (int) ceil(S);
        }
    }

    public static double setIntersectEstimator(Sample[][] samples, double unionEstimate, double eps, boolean useFastTWOLHS,
                                               QueryInfo queryInfo, OmniSketchConfig sketchConfig) {
        if (useFastTWOLHS) {
            return setIntersectEstimatorFastTWOLHS(samples, unionEstimate, queryInfo);
        } else {
            return setIntersectEstimatorTWOLHS(samples, unionEstimate, eps, queryInfo, sketchConfig);
        }
    }

    private static double setIntersectEstimatorFastTWOLHS(Sample[][] samples, double unionEstimate, QueryInfo queryInfo) {
        int sum = 0;
        int count = 0;
        for (int i = 0; i <samples[0].length; i++) {
            int[] bde = bucketDiffEstimatorFastTWOLHS(samples, i); // atomicDiffEstimator if section 3 of paper.
            sum += bde[0];
            count+=bde[1];
        }
        double result;
        result = ceil(((double) sum / count) * unionEstimate);
       
        queryInfo.addJaccardEstimate((double) sum / count, sum);
        int resultInt = (int) result;
        if (resultInt < 0) {
            System.out.println("Error: result > unionSize");
            System.exit(1);
        }

        return resultInt;
    }

    private static int[] bucketDiffEstimatorFastTWOLHS(Sample[][] samples, int repetition) {
        //int index;
        int sum=0;// witness count
        int count =0; // total count
        // Instead of doing it for one index, we want to check every index.
        int countSignaturesLength = ((TWOLHS) samples[0][repetition]).countSignatures.length;
//        if (repetition == 0) {
//            System.out.println("CountSignaturesLength: " + countSignaturesLength);
//        }
        for (int index=0; index < countSignaturesLength; index++) {
            if (singletonUnionBucket(samples, repetition, index)) {
                boolean witnessFound = true;
                for (Sample[] sample : samples) {
                    if (!((TWOLHS) sample[repetition]).singletonBucket(index)) {
                        witnessFound = false;
                    }
                }
                if (witnessFound) {
                    sum++;
                }
                count++;
            }
        }
        return new int[]{sum, count};
    }

    private static double setIntersectEstimatorTWOLHS(Sample[][] samples, double unionEstimate, double eps,
                                                      QueryInfo queryInfo, OmniSketchConfig sketchConfig) {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < samples[0].length; i++) {
            int diff = bucketDiffEstimator(samples, unionEstimate, eps, i, samples[0].length, sketchConfig); // atomicDiffEstimator if section 3 of paper.
            if (diff != -1) {
                sum += diff;
                count++;
            }
        }
        double result;
        result = ceil(((double) sum/ count)  * unionEstimate);
        queryInfo.addJaccardEstimate((double) sum /count, sum);
        return result;
        
    }

    private static int bucketDiffEstimator(Sample[][] samples, double unionEstimate,
                                           double eps, int repetition, int numTWOLHSRepetitions,
                                           OmniSketchConfig config) {
        int index;
        if (config.getUseFastTWOLHS()) {
            index = (int) ceil(log((2*unionEstimate)/(numTWOLHSRepetitions * pow((1 - eps), 2))) / log(2));
        } else {
            double Beta = 1.5;
            index = (int) ceil(log(log((Beta * unionEstimate)/(1 - eps)) / log(2)));
        }
        if (index < 0){
            throw new IllegalArgumentException("Index cannot be negative. Check your parameters.");
        }
        if (!singletonUnionBucket(samples, repetition, index)) {
            return - 1;
        }
        for (Sample[] sample: samples) {
            if (!((TWOLHS) sample[repetition]).singletonBucket(index)) {
                return 0;
            }
        }
        return 1; // witness found
    }

    private static boolean singletonUnionBucket(Sample[][] samples, int repetition, int lsb) {
        // check if union of buckets is singleton.
        // either one is empty and the other is singleton, or they are identical singleton buckets.
        // sample must be either empty or must be singleton.
        // samples is samples[predicates][2lhsreps]
        // repetiotn is the repetition of the 2lhs.
        // lsb is the least significant bit.
        ArrayList<TWOLHS> singletonElements = new ArrayList<>();
        for (Sample[] sample : samples) {
            if (((TWOLHS) sample[repetition]).singletonBucket(lsb)) {
                singletonElements.add(((TWOLHS) sample[repetition]));
            } else if (!((TWOLHS) sample[repetition]).emptyBucket(lsb)) {
                return false;
            }
        }
        // Now check if all singleton elements are identical.
        if (!singletonElements.isEmpty()) {
            TWOLHS[] singletons =  new TWOLHS[singletonElements.size()];
            for (int i=0;i<singletonElements.size();i++){
                singletons[i] = singletonElements.get(i);
            }
            return identicalSingletonBucket(singletons, lsb);
        } else {
            return false;
        }
    }

    private static boolean identicalSingletonBucket(TWOLHS[] samples, int lsb) {
        for (TWOLHS sample : samples) {
            if (!sample.singletonBucket(lsb)) {
                return false;
            }
        }
        int j = 1;
        while (j < samples[0].countSignatures[lsb].length) {
            // Check if no sample has a count of 0 and the other has a count > 0.
            for (int i = 0; i < samples.length - 1; i++) {
                for (int k = i + 1; k < samples.length; k++) {
                    if ((samples[i].countSignatures[lsb][j] > 0) != (samples[k].countSignatures[lsb][j] > 0)) { // true if they contain same sinleton element.
                        // if one is 0 and the other is not, they are not identical.
                        // They do not have to have the same count.
                        return false;
                    }
                }
            }
            j++;
        }
        return true;
    }

}

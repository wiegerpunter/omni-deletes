package omni.Experiments.parameterSetting;

import omni.Main;

import java.util.HashMap;
import java.util.List;

public class RamToPar {

    // 10 50 100 150 200 300 400 500 750 1000 Mb denoted in #bits
    //int[] ramVals = {(int) (80*1E6), (int) (80*2E6), (int) (80*3E6), (int) (80*4E6), (int) (80*5E6),
     //       (int) (80*10E6), (int) (80*15E6), (int) (80*20E6)};
    //long[] ramVals;// = {(long) (80*20E6)};


        //{5*8, 10*8, 50*8, 100*8, 150*8, 200*8, 300*8, 400*8, 500*8, 750*8, 800*8, 1000*8, 1600*8}; //2, 5, 10, 15, 20,
    // Make sure it is correct

    //@copilot double[] from 0.01 to 0.99 with step 0.01
    double[] epsVals = {1e-11, 1e-10, 1e-9, 0.00000001, 0.00000002, 0.00000003, 4e-8, 5e-8, 6e-8, 7e-8, 8e-8, 9e-8, 0.0000001, 0.000001, 0.00001,  0.0001,0.001,0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15,
    0.16, 0.17, 0.18, .19, .20, .21, .22, .23, .24, .25, .26, .27, .28, .29, .30, .31, .32, .33, .34, .35, .36, .37, .38, .39,
            .4, .41, .42, .43, .44, .45, .46, .47, .48, .49, .5, .51, .52, .53, .54, .55, .56, .57, .58, .59,
            .6, .61, .62, .63, .64, .65, .66, .67, .68, .69, .7, .71, .72, .73, .74, .75, .76, .77, .78, .79, .8,
            .81, .82, .83, .84, .85, .86, .87, .88, .89, .9, .91, .92, .93, .94, .95, .96, .97, .98, .99};
    // Baseline hashmaps
    HashMap<Long, double[]> ramToKminParams = new HashMap<>();

    HashMap<Long, double[]> ramToCMBaseline = new HashMap<>();

    HashMap<Long, double[]> ramToHydra = new HashMap<>();
    HashMap<Long, double[]> ramToReservoir = new HashMap<>();
    HashMap<Long, double[]> ramToAdap = new HashMap<>();
    HashMap<Long, double[]> ramToCM = new HashMap<>();

    // OmniSketch hashmaps ram to parameters

    HashMap<Long, double[]> ramToSketchTwoLHSParams = new HashMap<>();
    HashMap<Long, double[]> ramToSketchKminParams = new HashMap<>();
    HashMap<Long, double[]> ramToSketchRefParams = new HashMap<>();
    HashMap<Long, double[]> ramToSketchArrParams = new HashMap<>();
    HashMap<Long, double[]> ramToSketchKminParamsDynamic = new HashMap<>();

    HashMap<Long, HashMap<Integer, HashMap<Integer, double[]>>> ramToSketchKminParamsGridSearch = new HashMap<>();

    // OmniSketch hashmaps sampleSize to parameters
    HashMap<Integer, double[]> sampleSizeToSketchRefParams = new HashMap<>();
    HashMap<Integer, double[]> sampleSizeToSketchKminParams = new HashMap<>();


    int numAttrs;
    //int w;
    int d;
    int b;
    double parFactor;
    long[] ramVals;

    public RamToPar(int numAttrsToUse, List<Long> ramValsToUse) {
        this.numAttrs = numAttrsToUse;
        ramVals = new long[ramValsToUse.size()];
        for (int i = 0; i < ramValsToUse.size(); i++) {
            ramVals[i] = ramValsToUse.get(i);
        }
        runBaselines();
    }

    private void runBaselines() {
        for (long ram : ramVals) {
            double[] parsReservoir = getParamsReservoir(ram);
            ramToReservoir.put(ram, parsReservoir);

            double[] parsAdap = getParamsASH(ram);
            ramToAdap.put(ram, parsAdap);

            double[] parsCM = compParamsCMBaseline(ram);
            ramToCM.put(ram, parsCM);

            double[] parsHydra = compParamsHydra(ram);
            ramToHydra.put(ram, parsHydra);

            double[] parsKmin = compParamsSingleKmin(ram);
            ramToKminParams.put(ram , parsKmin);
        }
        System.out.println("Done with reservoir sampling");
    }

    public void computeOmniSketchParametersFromRAM(int d, int b, double parFactor) {
        this.d = d;
        this.b = b;
        this.parFactor = parFactor;
        // empty the hashmaps
        ramToSketchTwoLHSParams.clear();
        ramToSketchKminParams.clear();
        ramToSketchRefParams.clear();
        ramToSketchArrParams.clear();
        ramToSketchKminParamsDynamic.clear();

        for (long ram : ramVals) {

            double[] parsTwoLHS = compParamsOmniSketch(ram, true);
            ramToSketchTwoLHSParams.put(ram, parsTwoLHS);
            double[] parsOmniKmin = compParamsOmniSketch(ram, false);
            //HashMap<Integer, HashMap<Integer, double[]>> paramsOmniKminGridSearch = compParamsOmniSketchKmin(ram,
            //        Main.widthOptionsGridSearch, Main.depthOptionsGridSearch);
            ramToSketchKminParams.put(ram, parsOmniKmin);

            double[] parsOmniKminDynamic = compParamsOmniSketchDynamic(ram, false);
            ramToSketchKminParamsDynamic.put(ram, parsOmniKminDynamic);

            double[] parsOmniRef = compParamsRefParFactor(ram);
            ramToSketchRefParams.put(ram, parsOmniRef);

            double[] parsOmniArr = compParamsArrParFactor(ram);
            ramToSketchArrParams.put(ram, parsOmniArr);

        }
    }

    public void computeOmniSketchParametersFromSampleSize(int d, int b, double parFactor) {
        this.d = d;
        this.b = b;
        this.parFactor = parFactor;

        int[] sampleSizes = getSampleSizes();
        for (int sampleSize : sampleSizes) {
            double[] parsOmniRef = compParamsOmniSketchRefFromSampleSize(sampleSize);
            sampleSizeToSketchRefParams.put(sampleSize, parsOmniRef);
            double[] parsOmniKmin = compParamsOmniSketchKminFromSampleSize(sampleSize);
            sampleSizeToSketchKminParams.put(sampleSize, parsOmniKmin);
        }

    }


    private double[] getParamsReservoir(long ram) {
        // memory usage of reservoir sampling is sample size * 32 * numAttrs
        double sampleSize = (double) ram / (32 * numAttrs);
        return new double[]{sampleSize};
    }
    private double[] getParamsASH(long ram) {
        // memory usage of aSH sampling is sample size * 32 * numAttrs * 5
        //double sampleSize = (double) ram / (32 * numAttrs * 5);
        double sampleSize = (double) ram / (32 * (numAttrs + 5));
                // sampleSize * (32 * numAttrs + 32*4);
        return new double[]{sampleSize};
    }

    private double[] compParamsCMBaseline(long ram) {
        int d = 3;
        int w = (int) (ram / (d * 32));
        double info[] = new double[2];
        if (w > 0) {
            info = new double[]{d, w};
        }
        return info;
    }

    private double[] compParamsOmniSketch(long ram, boolean useTwoLHS) {
        double[] currentInfo;
       if (useTwoLHS) {
           currentInfo = compParams2LHS(ram,Main.eps);
       } else {
           //currentInfo = compParamsKmin(ram, Main.eps);
           //currentInfo = compParamsMinwise(ram, density, w, B);
           currentInfo = compParamsKminParFactor(ram, parFactor);
       }
       if (currentInfo[0] == 0) {
            System.out.println("No sketch found for ram " + ram + " and depth " + currentInfo[1] + " and width " + currentInfo[2]);
       }
       return currentInfo;
    }

    private double[] compParamsOmniSketchDynamic(long ram, boolean useTwoLHS) {
        double[] currentInfo;
        if (useTwoLHS) {
            currentInfo = compParams2LHS(ram,Main.eps);
        } else {
            //currentInfo = compParamsKmin(ram, Main.eps);
            //currentInfo = compParamsMinwise(ram, density, w, B);
            currentInfo = compParamsKminParFactorDynamic(ram, parFactor);
        }
        if (currentInfo[0] == 0) {
            System.out.println("No sketch found for ram " + ram + " and depth " + currentInfo[1] + " and width " + currentInfo[2]);
        }
        return currentInfo;
    }



    private double[] compParams2LHS(long ram, double eps) {
        int depth = (int) Math.ceil(Math.log(1/(Main.delta))/Math.log(Math.exp(1)));

        int width = (int) Math.ceil(Math.exp(1)/eps);
//        double factor = (double) ram / (depth* 31*(31 + 1) * numAttrs * 32);
//        int width = (int) Math.floor(Math.pow(factor, 0.5));
//        int numTwoLHSReps = (int) Math.floor(Math.pow(factor, 0.5));
        int numTwoLHSReps = (int) (Math.floor((double) (ram - (long) width * numAttrs * depth * 32) / ((long) width * numAttrs * depth * 32 * 31 *32)));

        double memUsage = Formulas.ramOmniTwoLHS(numAttrs, depth, width, numTwoLHSReps);//a(double) depth*width*Main.numTwoLHSReps * 31*(31 + 1) * numAttrs * 32;

        double[] info = new double[5];
        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, numTwoLHSReps, eps};
        }
        return info;
    }

    private double[] compParamsMinwise(long ram, double density, int w, int B) {
        int depth = this.d;
        //int B = (int) Math.ceil(density * Math.pow(2, b));
        int width = w;//(int) Math.floor((double) ram / (depth * numAttrs * (B * b + 32)));
        double memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);
        double[] info = new double[5];
        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, B, b, density};
        }
        return info;

    }

    private double[] compParamsOmniSketchKminFromSampleSize(int sampleSize){
        int depth = this.d;
        int width = (int) Math.sqrt((double) sampleSize / parFactor);
        int B = (int) Math.floor((double) sampleSize/width);
        double memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);
        return new double[]{memUsage, depth, width, B, b, parFactor};
    }

    private double[] compParamsKminParFactor(long ram, double parFactor) {
        int depth = this.d;
        double[] info = new double[8];
        // ParFactor is B / W, so B = parFactor * W
        double temp = parFactor * ram / (depth * numAttrs);
        int B = (int) (-32 + Math.sqrt(1024 + 4*b * parFactor * ram / (depth * numAttrs)) / (2 * b));
        //int B = (int) (b * Math.sqrt((b * temp + 256) / b * b) - 16) /b;

        int width = (int) Math.floor(B / parFactor);
        double memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);

        if (memUsage < ram) {
            //info = new double[]{memUsage, 3, 10, 10, 31, parFactor};
            info = new double[]{memUsage, depth, width, B, b, parFactor};
        }
        // throw exception if info is not set
        return info;
    }

    private int computeArrB(long ram, int depth, int numAttrs, double parFactor, int B, int prevB, int iter) {
        if (B <= 1) {
            return B;
        }
        if (B >= Integer.MAX_VALUE) {
            throw new RuntimeException("B is too large");
        }

        if (iter > 100) {
            System.out.println("Too many iterations, ram " + ram + " newRam " +
                    (numAttrs * depth * prevB * 32L + prevB * 32L) +
                    " prevB " + prevB);
            return prevB;
        }
        double v1 = 32L;
        double newRam = numAttrs * depth * B * v1 + B * 32L;
        // Return if distance between ram and newRam is less than 1 percent
        if (((double) ram * 0.99999) <= newRam && newRam <= ram) {
            System.out.println("ram: " + ram + " newRam: " + newRam + " B: " + B);
            return B;
        } else if (newRam < 0.99999 * ram) {
            return computeArrB(ram, depth, numAttrs, parFactor, B*2, B, iter+1);
        } else {
            return computeArrB(ram, depth, numAttrs, parFactor, (int) (B + prevB)/2, prevB, iter + 1);

        }
    }


    // Alternative to computeB:
    private int computealtB(long ram, int depth, int numAttrs, double parFactor, int B, int prevB, int iter) {
        if (B <= 1) {
            return B;
        }
        if (B >= Integer.MAX_VALUE) {
            throw new RuntimeException("B is too large");
        }

        if (iter > 100) {
            double v1 = Math.log(prevB) / Math.log(2) + Math.log(Math.floor(prevB / parFactor)) / Math.log(2);
            System.out.println("Too many iterations, ram " + ram + " newRam " +
                    (numAttrs * depth * prevB * v1) +
                    " prevB " + prevB);
            return prevB;
        }
        double v1 = Math.log(B) / Math.log(2) + Math.log(Math.floor(B / parFactor)) / Math.log(2);
        double newRam = numAttrs * depth * B * v1;
        // Return if distance between ram and newRam is less than 1 percent
        if (((double) ram * 0.99999) < newRam && newRam <= ram) {
            System.out.println("ram: " + ram + " newRam: " + newRam + " B: " + B);
            return B;
        } else if (newRam < 0.99999 * ram) {
            return computealtB(ram, depth, numAttrs, parFactor, B*2, B, iter+1);
        } else {
            return computealtB(ram, depth, numAttrs, parFactor, (int) (B + prevB)/2, prevB, iter + 1);

        }
    }

    private double[] compParamsOmniSketchRefFromSampleSize(int sampleSize){
        int depth = this.d;
        int width = (int) Math.max(1, Math.floor(sampleSize / (parFactor)));
        double memUsage = Formulas.ramOmniRef(numAttrs, depth, sampleSize, width);
        return new double[]{memUsage, depth, width, sampleSize, b, parFactor};
    }



    private double[] compParamsRefParFactor(long ram) {
        int depth = this.d;
        double[] info;
        int B = computealtB(ram, depth, numAttrs, parFactor, 2, 0, 0);
        int width = (int) Math.floor(B / parFactor);
        double memUsage = Formulas.ramOmniRef(numAttrs, depth, B, width);

        if (memUsage <= ram) {
            info = new double[]{memUsage, depth, width, B, b, parFactor};
            return info;
        }
        throw new RuntimeException("No sketch found for ram " + ram + " and depth " + depth + " and width " + width);

    }

    private double[] compParamsArrParFactor(long ram) {
        int depth = this.d;
        double[] info;
//        int B = (ram / (32L * ((long) numAttrs * depth + 1)) > 0) ? (int) (ram / (32L * numAttrs * depth)) : 1;
        int B = (int) ((double) ram / (depth *numAttrs + 1) * (1/32.0));
        int width = (int) Math.floor(B / parFactor);
        double memUsage = Formulas.ramOmniArr(numAttrs, depth,B);
        if (memUsage <= ram) {
            info = new double[]{memUsage, depth, width, B, b, parFactor};
            return info;
        }
        throw new RuntimeException("No sketch found for ram " + ram + " and depth " + depth + " and width " + width);

    }


    private double[] compParamsKminParFactorDynamic(long ram, double parFactor) {
        int depth = this.d;
        double[] info = new double[8];
        // ParFactor is B / W, so B = parFactor * W
        double temp = parFactor * ram / (depth * numAttrs);
        int B = (int) (-32 + Math.sqrt(1024 + 4*b * parFactor * ram / (depth * numAttrs)) / (2 * b));
        //int B = (int) (b * Math.sqrt((b * temp + 256) / b * b) - 16) /b;

        int width = (int) Math.floor(B / parFactor);
        double memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);

        if (memUsage < ram) {
            //info = new double[]{memUsage, 3, 10, 10, 31, parFactor};
            info = new double[]{memUsage, depth, width, B*width, b, parFactor};
        }
        // throw exception if info is not set
        return info;
    }


    private double[] compParamsKmin(long ram, double eps) {
        //double deltaCM =  Main.delta/2;
        int depth = this.d;//3;//(int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1)));
        double epsCMpowD = eps /(1 + eps);
        double epsCM = Math.pow(epsCMpowD, 1.0/depth);
        double epsDS = Math.pow(eps, depth);
        double[] info = new double[8];
        //int width = 12;// 1 + (int) Math.ceil(Math.exp(1)/epsCM);
        double factor = (double) ram / ramVals[0];
        int width = (int) 28;//(Math.pow(factor, 0.5) * 12);
        DetermineB determineB = new DetermineB(ram);
        int B;
        int b=0;
        int signatureFactor=1;
        B = determineB.determineB(depth, width, numAttrs, Main.delta, b, signatureFactor);
        b = Formulas.smallb(B, Main.delta, depth);
        //int b = Formulas.smallb(B, Main.delta);// ()int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/Main.delta));

        double memUsage;
        if (Main.rangeQueries) {
            memUsage = Main.dyadicRangeBits / Math.log(2) * Formulas.ramOmniKmin(numAttrs, depth, width, B, b);// * depth * width * numAttrs * (B * (b + 3 * 32 + 1) + 32);
        } else {
            memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);
        }

        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, B, b, signatureFactor, eps, epsCM, epsDS};
        }
        // throw exception if info is not set
        return info;
    }

    private HashMap<Integer, HashMap<Integer, double[]>> compParamsOmniSketchKmin(long ram,
                                                                                                    int[] w_values,
                                                                                                    int[] d_values) {
        HashMap<Integer, HashMap<Integer, double[]>> currentRamMap = new HashMap<>();
        double epsCM;
        double eps;
        double epsDS;
        for (int d_ : d_values) {
            DetermineB determineB = new DetermineB(ram);
            HashMap<Integer, double[]> currentDepthMap = new HashMap<>();
            for (int w_ : w_values) {
                epsCM = Math.exp(1)/(w_ - 1);
                eps = Math.pow(epsCM, d_) / (1 - Math.pow(epsCM, d_));
                epsDS = Math.pow(eps, d_);
                int B = determineB.determineB(d_, w_, numAttrs, Main.delta, 0, 1);
                int b = Formulas.smallb(B, Main.delta, d_);// ()int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/Main.delta));

                double memUsage;
                if (Main.rangeQueries) {
                    memUsage = Main.dyadicRangeBits / Math.log(2) * Formulas.ramOmniKmin(numAttrs, d_, w_, B, b);// * depth * width * numAttrs * (B * (b + 3 * 32 + 1) + 32);
                } else {
                    memUsage = Formulas.ramOmniKmin(numAttrs, d_, w_, B, b);
                    //(double) depth * width * numAttrs * (B * (b + 3 * 32 + 1) + 32);
                }

                if (memUsage < ram) {
                   currentDepthMap.put(w_, new double[]{memUsage, d_, w_, B, b, eps, epsCM, epsDS});
                }
            }
            currentRamMap.put(d_, currentDepthMap);
        }
        return currentRamMap;
    }

    private double[] compParamsHydra(long ram) {
        int maxSize = 0;
        double[] currentInfo = new double[8];
        for (double eps : epsVals) {
            double[] info = getSizeHydra(ram, eps);
            if (info[0] > maxSize) {
                maxSize = (int) info[0];
                currentInfo = info;
            }
        }
        return currentInfo;
    }

    private double[] compParamsSingleKmin(long ram) {
        int maxSize = 0;
        double[] currentInfo = new double[3];
        double[] info = getSizeKmin(ram);
        if (info[0] > maxSize) {
            maxSize = (int) info[0];
            currentInfo = info;
        }
        return currentInfo;
    }

    private double[] getSizeKmin(long ram) {
        double[] info = new double[3];
        //int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
        DetermineBKmin determineB = new DetermineBKmin(ram);
        int B = determineB.determineB(numAttrs, Main.delta);
        int b = (int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/Main.delta));
        //double memUsage = (double) (depth * width*(B *((double) Main.b/32 * 4) + 4) * numAttrs);//*Main.numAttributes);
        double memUsage = (double) (B * (b + 3 * 32 + 1 + 64)) * numAttrs + 32;
        // Memory = depth * width * ( sample size * small b size * bits needed for pointers + bits needed for counters ) * number of attributes
        if (memUsage < ram) {
            info = new double[]{memUsage, B, b};
        }
        return info;
    }

    private double[] getSizeCMBaseline(long ram, double eps) {
        double delta = Main.delta;

        double[] info = new double[4];
        int depth = (int) Math.ceil(Math.log(1/delta)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
        int width = (int) Math.ceil(Math.exp(1)/eps); // Width of sketch (number of buckets)

        int memUsage = depth*width*((int) Math.pow(2, numAttrs) - 1);
        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, eps};
        }
        return info;
    }

//    private double[] getSizeSketch(long ram, double eps) {
//        double deltaCM = 0.05;
//        double deltaDS = 0.05;
//        int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1)));
//        //double epsCM = Math.pow((Math.pow(eps, depth)/(1 + Math.pow(eps, depth))), (double) (1/depth));
//        double epsCMpowD = eps / (1 + eps);
//        double epsCM = Math.pow(epsCMpowD, 1.0/depth);
//        double epsDS = Math.pow(eps, depth);
//        double[] info = new double[7];
//        //int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
//        int width =  1 + (int) Math.ceil(Math.exp(1)/epsCM); // Width of sketch (number of buckets)
//        double numerator;
//        double denominator;
//        double ratio = 1/Main.qTarget;
//        int B;
//        int b;
//        long memUsage;
//        if (Main.useTwoLHS) {
//            width = (int) Math.ceil(Math.exp(1)/eps);
//            depth = (int) Math.ceil(Math.log(1/(deltaDS + deltaCM))/Math.log(Math.exp(1)));
//            B = Main.numTwoLHSReps;
//            memUsage = (long) depth*width*B*numAttrs*4;
//        } else {
//            numerator = Math.log(2* numAttrs *depth/deltaDS)/Math.log(Math.exp(1));
//            denominator = Math.pow(epsDS, 2);
//            B = (int) Math.ceil(2 * (numerator / denominator) * ratio);
//            b = (int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/deltaDS));
//            memUsage = (long) (depth * width*(((long) B *b + 3 * 32 + 1) + 32) * numAttrs);
//            // Memory = depth * width * ( sample size * small b size * bits needed for pointers + bits needed for counters ) * number of attributes
//        }
//        if (memUsage < ram) {
//            info = new double[]{memUsage, depth, width, B, eps, epsCM, eps};
//        }
//        return info;
//    }

    private double[] getSizeHydra(long ram, double eps) {
        double delta = Main.delta/2;
        double deltaCM = Main.delta/2;
        //double epsCM = eps/2;
        //double epsRoot = eps/2;
        double[] info = new double[8];
        int depthCM = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
        //int widthCM = (int) Math.ceil(Math.exp(1)/Math.pow(eps, 2)); // Width of sketch (number of buckets)
        int depthRoot = (int) Math.ceil(Math.log(1/delta)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
        //int widthRoot = (int) Math.ceil(Math.exp(1)/epsRoot); // Width of sketch (number of buckets)
        int memLeft = (int) Math.ceil((double) ram/(32*depthCM*depthRoot));
        int widthCM = (int) Math.pow((double) memLeft/2000, 0.66);
        int widthRoot = (int) Math.pow(Math.sqrt(memLeft) * 2000, 0.66);
        long memUsage = (long) depthCM*widthCM*depthRoot*widthRoot*32;
        if (memUsage < ram) {
            info = new double[]{memUsage, depthRoot, widthRoot, depthCM, widthCM, eps};
        } else {
            System.out.println("memUsage: " + memUsage + " ram: " + ram);
        }
        return info;
    }

    public int[] getParamsOmniSketch2LHS(long ram) {
        if (ramToSketchTwoLHSParams.containsKey(ram)) {
            double[] info = ramToSketchTwoLHSParams.get(ram);
            int d = (int) info[1];
            int w = (int) info[2];
            int numTwoLHSReps = (int) info[3];
             return new int[]{d, w, numTwoLHSReps};
        } else {
            throw new IllegalArgumentException("RAM not found in ramToSketchParams");
        }
    }
    public int[] getParamsOmniSketchKmin(long ram) {
        return getInts(ram, ramToSketchKminParams);
    }

    private int[] getInts(long ram, HashMap<Long, double[]> ramToSketchKminParams) {
        if (ramToSketchKminParams.containsKey(ram)) {
            double[] info = ramToSketchKminParams.get(ram);
            int d = (int) info[1];
            int w = (int) info[2];
            int B = (int) info[3];
            int b = (int) info[4];
            int density = (int) (100 *info[5]);
            return new int[]{d, w, B, b, density};
        } else {
            throw new IllegalArgumentException("RAM not found in ramToSketchParams");
        }
    }

    private int[] getIntsFromSample(int sampleSize, HashMap<Integer, double[]> ramToSketchKminParams) {
        if (ramToSketchKminParams.containsKey(sampleSize)) {
            double[] info = ramToSketchKminParams.get(sampleSize);
            int d = (int) info[1];
            int w = (int) info[2];
            int B = (int) info[3];
            int b = (int) info[4];
            return new int[]{d, w, B, b};
        } else {
            throw new IllegalArgumentException("Sample size not found in ramToSketchParams");
        }
    }

    public int[] getParamsOmniSketchRef(long ram) {
        return getInts(ram, ramToSketchRefParams);
    }

    public int[] getParamsOmniSketchRefFromSampleSize(int sampleSize) {
        return getIntsFromSample(sampleSize, sampleSizeToSketchRefParams);
    }

    public int[] getParamsOmniSketchKminFromSampleSize(int sampleSize) {
        return getIntsFromSample(sampleSize, sampleSizeToSketchKminParams);
    }

    public int[] getParamsOmniSketchArrayList(long ram) {
        return getInts(ram, ramToSketchArrParams);
    }

    public int[] getParamsOmniSketchDynamic(long ram) {
        return getInts(ram, ramToSketchKminParamsDynamic);
    }

    public int[] getParamsOmniSketchGridSearch(long ram, int width, int depth) {
        if (ramToSketchKminParamsGridSearch.containsKey(ram)) {
            HashMap<Integer, HashMap<Integer, double[]>> depthMap = ramToSketchKminParamsGridSearch.get(ram);
            if (depthMap.containsKey(depth)) {
                HashMap<Integer, double[]> widthMap = depthMap.get(depth);
                if (widthMap.containsKey(width)) {
                    double[] info = widthMap.get(width);
                    int d = (int) info[1];
                    int w = (int) info[2];
                    int B = (int) info[3];
                    int b = (int) info[4];
                    return new int[]{d, w, B, b};
                } else {
                    throw new IllegalArgumentException("Width not found in ramToSketchParamsGridSearch");
                }
            } else {
                throw new IllegalArgumentException("Depth not found in ramToSketchParamsGridSearch");
            }
        } else {
            throw new IllegalArgumentException("RAM not found in ramToSketchParams");
        }
    }

    public int[] getParamsReservoirSampling(long ram) {
        if (ramToReservoir.containsKey(ram)) {
            double[] info = ramToReservoir.get(ram);
            int sampleSize = (int) info[0];
            return new int[]{sampleSize};
        } else {
            throw new IllegalArgumentException("RAM not found in ramToReservoir");
        }
    }

    public int[] getParamsAdapSampling(long ram, double ingestBuffer) {
        if (ramToAdap.containsKey(ram)) {
            double[] info = ramToAdap.get(ram);
            int sampleSize = (int) info[0];
            int bufferSize = (int) Math.max((ingestBuffer * sampleSize), 1);
            return new int[]{sampleSize - bufferSize, bufferSize, (int) (10*ingestBuffer)};
        } else {
            throw new IllegalArgumentException("RAM not found in ramToAdap");
        }
    }
    public double[] getParamsSingleKmin(long ram) {
        //HashMap<Integer, double[]> ramToParams;
        if (ramToKminParams.containsKey(ram)) {
            double[] info = ramToKminParams.get(ram);
            double K = info[1];
            double b = info[2];
            System.out.println("K: " + K + " b: " + b);
            return new double[]{K, b};
        } else {
            long closestKey = getClosestKey(ram, ramToKminParams);
            double[] info = ramToKminParams.get(closestKey);
            double K = info[1];
            double b = info[2];
            System.out.println("K: " + K + " b: " + b);
            return new double[]{K, b};
        }
    }
    public double[] getParamsCMBaseline(long ram) {
        if (ramToCMBaseline.containsKey(ram)) {
            double[] info = ramToCMBaseline.get(ram);
            double d = info[1];
            double w = info[2];
            System.out.println("d: " + d + " w: " + w);
            return new double[]{d, w};
        } else {
            long closestKey = getClosestKey(ram, ramToCMBaseline);
            double[] info = ramToCMBaseline.get(closestKey);
            double d = info[1];
            double w = info[2];
            return new double[]{d, w};
        }
    }

    private long getClosestKey(long ram, HashMap<Long, double[]> ramToParams) {
        long closestKey = 0;
        long minDiff = Long.MAX_VALUE;
        for (long key : ramToParams.keySet()) {
            long diff = Math.abs(key - ram);
            if (diff < minDiff) {
                minDiff = diff;
                closestKey = key;
            }
        }
        return closestKey;

    }


    public int[] getParamsHydra(long ram) {
        if (ramToHydra.containsKey(ram)) {
            double[] info = ramToHydra.get(ram);
            int depthRoot = (int) info[1];
            int widthRoot = (int) info[2];
            int depthCS = (int) info[3];
            int widthCS = (int) info[4];
            return new int[]{depthRoot, widthRoot, depthCS, widthCS};
        } else {
            throw new RuntimeException("Parameters not found for Hydra");
        }
    }

    public int[] getParamsCountMin(long ram) {
        if (ramToCM.containsKey(ram)) {
            double[] info = ramToCM.get(ram);
            int d = (int) info[0];
            int w = (int) info[1];
            return new int[]{d, w};
        } else {
            throw new RuntimeException("Parameters not found for CountMin");
        }
    }

    public int[] getSampleSizes() {
        if (ramToReservoir.isEmpty()) {
            throw new IllegalArgumentException("Reservoir sample list is empty");
        } else {
            int[] sampleSizes = new int[ramToReservoir.size()];
            int i = 0;
            for (long ram : ramToReservoir.keySet()) {
                double[] info = ramToReservoir.get(ram);
                int sampleSize = (int) info[0];
                sampleSizes[i] = sampleSize;
                i++;
            }
            return sampleSizes;
        }
    }
//    public double[] getParamsHydra(long ram) {
//        if (ramToHydra.containsKey(ram)) {
//            double[] info = ramToHydra.get(ram);
//            double dRoot = info[1];
//            double wRoot = info[2];
//            double dCM = info[3];
//            double wCM = info[4];
//            System.out.println("d Root: " + dRoot + " w Root: " + wRoot + " d CM: " + dCM + " w CM: " + wCM);
//            return new double[]{dRoot, wRoot, dCM, wCM};
//        } else {
//            long closestKey = getClosestKey(ram, ramToHydra);
//            double[] info = ramToHydra.get(closestKey);
//            double dRoot = info[1];
//            double wRoot = info[2];
//            double dCM = info[3];
//            double wCM = info[4];
//            return new double[]{dRoot, wRoot, dCM, wCM};
//        }
//    }

}

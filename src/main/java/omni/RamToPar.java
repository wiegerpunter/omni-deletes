package omni;

import java.text.Normalizer;
import java.util.HashMap;

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
    HashMap<Long, double[]> ramToSketchTwoLHSParams = new HashMap<>();
    HashMap<Long, double[]> ramToSketchKminParams = new HashMap<>();
    HashMap<Long, double[]> ramToKminParams = new HashMap<>();

    HashMap<Long, double[]> ramToCMBaseline = new HashMap<>();

    HashMap<Long, double[]> ramToHydra = new HashMap<>();
    int numAttrs;
    public RamToPar(int numAttrsToUse) {
        this.numAttrs = numAttrsToUse;
        //this.ramVals = ramValsToUse;
        run();
    }

    public RamToPar(int numAttrsToUse, double eps, double delta) {
        this.numAttrs = numAttrsToUse;

        Main.eps = eps;
        Main.delta = delta;
        for (long r: Main.ramVals) {
            double[] pars = getInfoViaB(r, eps);
            //System.out.println("Info, Ram: " +  r + " usedRAM: "+ pars[0] + " B: " + pars[3] + " b: " + pars[4] + " width: " + pars[2] + " depth: " + pars[1]);
            ramToSketchKminParams.put(r, pars);
        }

        for (long r: Main.ramVals) {
            double[] pars = getInfoViaB(r/2, eps);
            //System.out.println("Info, Ram: " +  r + " usedRAM: "+ pars[0] + " B: " + pars[3] + " b: " + pars[4] + " width: " + pars[2] + " depth: " + pars[1]);
            ramToSketchKminParams.put(r/2, pars);
        }

        // Add params for hydra to ramToHydra based on main eps main delta
        for (long r: Main.ramVals) {
            double[] pars = gridSearchHydra(r);
            ramToHydra.put(r, pars);
        }
        for (long r: Main.ramVals) {
            double[] pars = gridSearchKmin(r);
            ramToKminParams.put(r, pars);
        }
    }

    public void run() {
        for (long ram : Main.ramVals) {

            double[] parsTwoLHS = gridSearchSketch(ram, true);
            ramToSketchTwoLHSParams.put(ram, parsTwoLHS);
            double[] parsOmniKmin = gridSearchSketch(ram, false);
            ramToSketchKminParams.put(ram, parsOmniKmin);
            double[] parsCM = gridSearchCMBaseline(ram);
            ramToCMBaseline.put(ram, parsCM);
            double[] parsHydra = gridSearchHydra(ram);
            ramToHydra.put(ram, parsHydra);

            double[] parsKmin = gridSearchKmin(ram);
            ramToKminParams.put(ram , parsKmin);

        }

//        for (long ram : Main.ramVals) {
//
//            double[] pars = gridSearchSketch(ram/2);
//            //double[] pars;
//            //pars = new double[]{5, 4, 30, 51200, 0.1, 0.0909, 0.1};
//
//            //Main.b = (int) Math.ceil(Math.log(4*Math.pow(51200, (double) 5/2)/Main.delta));
//            ramToSketchParams.put(ram/2, pars);
//            double[] parsCM = gridSearchCMBaseline(ram/2);
//            ramToCMBaseline.put(ram/2, parsCM);
//            double[] parsHydra = gridSearchHydra(ram/2);
//            ramToHydra.put(ram/2, parsHydra);
//
//            double[] parsKmin = gridSearchKmin(ram/2);
//            ramToKminParams.put(ram/2, parsKmin);
//
//        }
        System.out.println("Done with grid search");

    }


    private double[] gridSearchCMBaseline(long ram) {
        int maxSize = 0;
        double[] currentInfo = new double[4];
        for (double eps : epsVals) {
            double[] info = getSizeCMBaseline(ram, eps);
            if (info[0] > maxSize) {
                maxSize = (int) info[0];
                currentInfo = info;
            }
        }
        return currentInfo;
    }

    private double[] gridSearchSketch(long ram, boolean useTwoLHS) {
        double[] currentInfo;
       if (useTwoLHS) {
           currentInfo = getInfoTwoLHS(ram,Main.eps);
       } else {
           currentInfo = getInfoViaB(ram, Main.eps);
       }
       if (currentInfo[0] == 0) {
            System.out.println("No sketch found for ram " + ram + " and eps" + currentInfo[5] + " and depth " + currentInfo[1] + " and width " + currentInfo[2]);
       }
       return currentInfo;
    }

    private double[] getInfoTwoLHS(long ram, double eps) {
        int depth = (int) Math.ceil(Math.log(1/(Main.delta))/Math.log(Math.exp(1)));

        int width = (int) Math.ceil(Math.exp(1)/eps);
//        double factor = (double) ram / (depth* 31*(31 + 1) * numAttrs * 32);
//        int width = (int) Math.floor(Math.pow(factor, 0.5));
//        int numTwoLHSReps = (int) Math.floor(Math.pow(factor, 0.5));
        int numTwoLHSReps = (int) (Math.floor((double) (ram - (long) width * numAttrs * depth * 32) / ((long) width * numAttrs * depth * 32 * 31 *32)));

        double memUsage = Formulas.ramOmniTwoLHS(numAttrs, depth, width, numTwoLHSReps);//a(double) depth*width*Main.numTwoLHSReps * 31*(31 + 1) * numAttrs * 32;

        double[] info = new double[5];
        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, numTwoLHSReps, eps}; // Main.TODO: make sure its below ram
        }
        return info;
    }

    private double[] getInfoViaB(long ram, double eps) {

        double deltaCM =  Main.delta/2;
        Main.deltaDS = Main.delta/2;
        int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1)));
        double epsCMpowD = eps /(1 + eps);
        double epsCM = Math.pow(epsCMpowD, 1.0/depth);
        double epsDS = Math.pow(eps, depth);
        double[] info = new double[7];
        int width =  1 + (int) Math.ceil(Math.exp(1)/epsCM);
        double factor = (double) ram / Main.ramVals[0];
        //int width = (int) (Math.pow(factor, 0.75) * (1 + (int) Math.ceil(Math.exp(1)/epsCM)));
        DetermineB determineB = new DetermineB(ram);
        int B = determineB.determineB(depth, width, numAttrs, Main.delta);
        int b = Formulas.smallb(B, Main.delta);// ()int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/Main.delta));

        double memUsage;
        if (Main.rangeQueries) {
            memUsage = Main.dyadicRangeBits / Math.log(2) * Formulas.ramOmniKmin(numAttrs, depth, width, B, b);// * depth * width * numAttrs * (B * (b + 3 * 32 + 1) + 32);
        } else {
            memUsage = Formulas.ramOmniKmin(numAttrs, depth, width, B, b);
            //(double) depth * width * numAttrs * (B * (b + 3 * 32 + 1) + 32);
        }

        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, B, b, eps, epsCM, epsDS};
       }
       return info;
    }

    private double[] gridSearchHydra(long ram) {
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



    private double[] gridSearchKmin(long ram) {
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
        Main.deltaDS = Main.delta/2;
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

    private double[] getSizeSketch(long ram, double eps) {
        double deltaCM = 0.05;
        double deltaDS = 0.05;
        Main.deltaDS = deltaDS;
        int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1)));
        //double epsCM = Math.pow((Math.pow(eps, depth)/(1 + Math.pow(eps, depth))), (double) (1/depth));
        double epsCMpowD = eps / (1 + eps);
        double epsCM = Math.pow(epsCMpowD, 1.0/depth);
        double epsDS = Math.pow(eps, depth);
        double[] info = new double[7];
        //int depth = (int) Math.ceil(Math.log(1/deltaCM)/Math.log(Math.exp(1))); // Depth of sketch (number of hash functions)
        int width =  1 + (int) Math.ceil(Math.exp(1)/epsCM); // Width of sketch (number of buckets)
        double numerator;
        double denominator;
        double ratio = 1/Main.qTarget;
        int B;
        int b;
        long memUsage;
        if (Main.useTwoLHS) {
            width = (int) Math.ceil(Math.exp(1)/eps);
            depth = (int) Math.ceil(Math.log(1/(deltaDS + deltaCM))/Math.log(Math.exp(1)));
            B = Main.numTwoLHSReps;
            memUsage = (long) depth*width*B*numAttrs*4;
        } else {
            numerator = Math.log(2* numAttrs *depth/deltaDS)/Math.log(Math.exp(1));
            denominator = Math.pow(epsDS, 2);
            B = (int) Math.ceil(2 * (numerator / denominator) * ratio);
            b = (int) Math.ceil(Math.log(4*Math.pow(B, (double) 5/2)/deltaDS));
            memUsage = (long) (depth * width*(((long) B *b + 3 * 32 + 1) + 32) * numAttrs);
            // Memory = depth * width * ( sample size * small b size * bits needed for pointers + bits needed for counters ) * number of attributes
        }
        if (memUsage < ram) {
            info = new double[]{memUsage, depth, width, B, eps, epsCM, eps};
        }
        return info;
    }

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
            info = new double[]{memUsage, depthCM, widthCM, depthRoot, widthRoot, eps};
        } else {
            System.out.println("memUsage: " + memUsage + " ram: " + ram);
        }
        return info;
    }

    public int[] getParamsSketchTwoLHS(long ram) {
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
    public int[] getParamsSketchKmin(long ram) {
        if (ramToSketchKminParams.containsKey(ram)) {
            double[] info = ramToSketchKminParams.get(ram);
            int d = (int) info[1];
            int w = (int) info[2];
            int B = (int) info[3];
            int b = (int) info[4];
            return new int[]{d, w, B, b};
        } else {
            throw new IllegalArgumentException("RAM not found in ramToSketchParams");
        }
    }

    public int[] getParamsReservoirSampling(long ram) {
        //TODO: Implement
        return new int[]{0, 0, 0};
    }

    public double[] getParamsKmin(long ram) {
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

    public double[] getParamsHydra(long ram) {
        if (ramToHydra.containsKey(ram)) {
            double[] info = ramToHydra.get(ram);
            double dRoot = info[1];
            double wRoot = info[2];
            double dCM = info[3];
            double wCM = info[4];
            System.out.println("d Root: " + dRoot + " w Root: " + wRoot + " d CM: " + dCM + " w CM: " + wCM);
            return new double[]{dRoot, wRoot, dCM, wCM};
        } else {
            long closestKey = getClosestKey(ram, ramToHydra);
            double[] info = ramToHydra.get(closestKey);
            double dRoot = info[1];
            double wRoot = info[2];
            double dCM = info[3];
            double wCM = info[4];
            return new double[]{dRoot, wRoot, dCM, wCM};
        }
    }
}

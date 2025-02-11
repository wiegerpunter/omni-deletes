package omni;

import com.opencsv.exceptions.CsvValidationException;
import omni.CountMin.CountMin;
import omni.hydraRefactor.ImpHydraStruct;
import omni.resSample.ReservoirSample;
import omni.aSH.aSH;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class RunExperiments {
    Helper h;
    DatasetRefactor d;
    CleanDataset cd;
    //ExpWorkload ew;
    RamToPar rtp;
    String[] conditions;
    double sizeFactor;

    int numNoiseUpdates;
    public RunExperiments(Helper h, int sizeFactor) {
        this.h = h;
        this.sizeFactor = sizeFactor;
    }

    public void run() throws IOException, CsvValidationException {
        cd = new CleanDataset(h, Main.numBins,Main.numPredicates);
        readDatasetSettings();
        for (double zipfAlpha : Main.zipfAlphas) {
            for (int i = 0; i < conditions.length; i++) {
                for (int n : Main.sizeNoise) {
                    if (Main.datasetName.equals("SNMP") || Main.datasetName.equals("CAIDA")) {
                        readRealDataset(i);
                    } else {
                        sizeFactor = Double.parseDouble(conditions[i]);
                        cd.dataset = null;
                        cd.datasetResidu = null;
                        cd.datasetNegUpdates = null;
                        // garbage collect
                        System.gc();
                        System.out.println("Running with size factor " + sizeFactor);
                    }
                    int numRepetitions = Main.numRepetitions;
                    double[] percToDelete;
                    double maxPercent = 0;
                    if (Main.withDeletes) {
                        percToDelete = new double[]{0.901};//, 0.1, 0};
                        maxPercent = 0.901;
                    } else {
                        percToDelete = new double[]{0};
                    }
                    for (int repetition = 0; repetition < numRepetitions; repetition++) {
                        cd.setAttributes(repetition);
                        for (double perc : percToDelete) {
                            if (Main.useMultNumAttributes) {
                                if (Main.datasetName.contains("synth")) {
                                    readDataset(perc, maxPercent, sizeFactor, n, zipfAlpha);
                                }
                                for (int j = 3; j < cd.cleanIds.length + 1; j++) {
                                    Main.numStoredAttributes = j;
                                    if (Main.datasetName.equals("SNMP") || Main.datasetName.equals("CAIDA")) {
                                        readDataset(perc, maxPercent, sizeFactor, n, zipfAlpha);
                                    }
                                    runSyns(i, repetition);
                                }
                            } else {
                                Main.numStoredAttributes = cd.cleanIds.length;
                                readDataset(perc, maxPercent, sizeFactor, n, zipfAlpha);
                                runSyns(i, repetition);
                            }
                        }
                    }
                }
            }
        }
//        for (double perc : percToDelete) {
//            for (int i = 0; i < conditions.length; i++) {
//                if (Main.useMultNumAttributes) {
//                    for (int j = 2; j < cd.cleanIds.length; j++) {
//                            Main.numStoredAttributes = j;
//                            readDataset(i, perc, maxPercent, sizeFactor);
//                            runSyns(i, numRepetitions);
//                    }
//                } else {
//                    Main.numStoredAttributes = cd.cleanIds.length;
//                    readDataset(i, perc, maxPercent, sizeFactor);
//                    runSyns(i, numRepetitions);
//                }
//            }
//        }
    }

    private void readRealDataset(int i) throws CsvValidationException, IOException {
        if (Main.datasetName.equals("SNMP")) {
            Main.fileStartCondition = conditions[i];
        } else {
            // Convert conditions[i] to int
            Main.numFiles = Integer.parseInt(conditions[i]);
        }

        d = new DatasetRefactor(Main.datasetName, h);
    }

    private void runSyns(int i, int repetition) throws IOException {
        System.out.println("Running dataset " + (i + 1) + " of " + conditions.length);
        int numPotNeg = cd.datasetNegUpdates.length;

        for (double noiseUpdateFraction : Main.noiseUpdateFractions) {
            numNoiseUpdates = (int) (cd.datasetResidu.length * noiseUpdateFraction);
            System.out.println("Running with " + numNoiseUpdates + " noise updates");
            cd.setNoiseUpdates(numNoiseUpdates);

            ArrayList<Long> actRamVals = new ArrayList<>();
            for (long ram : Main.ramVals) {
                actRamVals.add(ram);
            }
//
            this.rtp = new RamToPar(Main.numStoredAttributes, actRamVals);
            for (long ram : actRamVals) {
                if (Main.expaSH) {
                    for (double ingestBuffer : Main.ingestBuffers) {
                        System.out.println("ADAP SAMPLING");
                        runAdapSampling(ram, rtp, false, ingestBuffer, repetition);
                        System.gc();
                        System.out.println("Num noise updates: " + numNoiseUpdates + " for factor " + noiseUpdateFraction);

                    }

                    //                            useBufferInQuery = false;
                    //                            runAdapSampling(ram, rtp, useBufferInQuery, repetition);
                }
                if (Main.expHydra) {
                    System.out.println("HYDRA Baseline");
                    runHydra(ram, rtp, repetition);
                }
                if (Main.expResSample) {
                    System.out.println("RESERVOIR SAMPLING");
                    runReservoirSampling(ram, rtp, repetition);
                    System.gc();
                }
                if (Main.expCM) {
                    System.out.println("COUNTMIN");
                    runCountMin(ram, rtp, repetition);
                    System.gc();
                }
            }
            ArrayList<Integer> resSampleSizes = new ArrayList<Integer>();

            for (int b: Main.bGridSearch) {
                for (int depth: Main.dGridSearch) {
                    for (double parFactor : Main.parFactorGridSearch) {
                        for (int c = 0; c < 1; c++) {
                            boolean dynamicSampleSizes = false;
                            this.rtp = new RamToPar(Main.numStoredAttributes, depth, b, parFactor);
                            for (long ram : Main.ramVals) {
                                System.out.println("Running sketch with ram " + ram);
                                System.out.println("OMNISKETCH");
                                if (Main.useS0){
                                    runOmniSketchPQPrimitive(ram, rtp, true,
                                            false, false,
                                            true, Main.rangeQueries, false,
                                            false, false,
                                            false, false, 1, false,
                                            dynamicSampleSizes, false, false, 0.1,
                                            repetition, actRamVals, resSampleSizes);
                                    System.gc();
                                }



                                boolean useS0 = false;

                                boolean useTwoLHS = true;
                                boolean useBetaKmin = false;
                                if (Main.countUniqueSamples) {
                                    Main.uniqueSamples = new HashMap<>();
                                    Main.uniqueSamplesReservoir = new HashSet<>();
                                }

                                boolean useTwoLHSAcrossRows = false;
                                boolean useFastTwoLHS = true;
                                boolean useInvDistPaper2LHS = true;
                                boolean useMinEstimate = true;
                                boolean useOnlyBestRow = false;

                                //UNCOMMENT:
                                if (Main.exp2LHS) {
                                    runOmniSketchPQPrimitive(ram, rtp, useS0,
                                            useTwoLHS, useOnlyBestRow,
                                            useTwoLHSAcrossRows, Main.rangeQueries, useBetaKmin,
                                            useFastTwoLHS, useInvDistPaper2LHS, useMinEstimate, Main.checkConditions,
                                            1, false,dynamicSampleSizes, false,true,
                                            0.1,repetition, actRamVals, resSampleSizes);

                                    System.gc();
                                }
                                Main.checkConditions = false;
                                useTwoLHS = false;
                                useFastTwoLHS = false;
                                useInvDistPaper2LHS = false;
                                useMinEstimate = false;
                                Double betaValue = getBeta(Main.inputFolder + "/paramTable/bufferMinwiseTable.csv", noiseUpdateFraction, ram, Main.numStoredAttributes);


                                int numberOfAggregations = 1;
                                if (Main.expPerRow) {
                                    numberOfAggregations = 2;
                                }
                                for (int a = 0; a < numberOfAggregations; a++) {
                                    boolean useAcrossRows;
                                    useAcrossRows = a == 0;
                                    if (Main.expOmniSenate) {
                                        for (int dynRes = 0; dynRes < 2; dynRes++) {
                                            boolean dynamicResizing = dynRes == 1;
                                            for (int nmaxUse = 0; nmaxUse <2; nmaxUse++) {
                                                boolean useNmax = nmaxUse == 1;
                                                for (int bestRow =0; bestRow<2; bestRow++) {
                                                    useOnlyBestRow = bestRow == 1;
                                                    runOmniSketchPQPrimitive(ram, rtp, useS0,
                                                            useTwoLHS,
                                                            useOnlyBestRow,
                                                            useAcrossRows, Main.rangeQueries,
                                                            useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
                                                            useMinEstimate, Main.checkConditions, betaValue, dynamicResizing,
                                                            dynamicSampleSizes, false, useNmax, 0.1,
                                                            repetition, actRamVals, resSampleSizes);
                                                    System.gc();
                                                    if (Main.expCase1ReturnScap) {
                                                        for (double eps : Main.epsValues) {
                                                            runOmniSketchPQPrimitive(ram, rtp, useS0,
                                                                    useTwoLHS, useOnlyBestRow,
                                                                    useAcrossRows, Main.rangeQueries,
                                                                    useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
                                                                    useMinEstimate, Main.checkConditions, betaValue, dynamicResizing,
                                                                    dynamicSampleSizes, true, useNmax, eps,
                                                                    repetition, actRamVals, resSampleSizes);
                                                            System.gc();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (Main.expOmniHouse) {
                                        runOmniSketchHouse(ram, rtp, useTwoLHS, useAcrossRows, Main.rangeQueries,
                                                useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
                                                useMinEstimate, Main.checkConditions, betaValue, dynamicSampleSizes, false, 0.1,
                                                repetition, actRamVals, resSampleSizes);
                                        System.gc();
                                        if (Main.expCase1ReturnScap) {
                                            for (double eps: Main.epsValues) {
                                                runOmniSketchHouse(ram, rtp, useTwoLHS, useAcrossRows, Main.rangeQueries,
                                                        useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
                                                        useMinEstimate, Main.checkConditions, betaValue, dynamicSampleSizes, true, eps,
                                                        repetition, actRamVals, resSampleSizes);
                                                System.gc();
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public static Double getBeta(String filePath, double factor, double RAM, int attrs) {
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] values = line.split(csvSplitBy);

                // Assuming columns: alpha, insert_stream_size, cells, Y, beta, iterations
                double factorValue = Double.parseDouble(values[1]);
                double ramValue = Double.parseDouble(values[7])*8*Math.pow(10,6);
                int cellsValue = Integer.parseInt(values[3]);
                double betaValue = Double.parseDouble(values[5]);

                // Check for matching values
                if (factorValue == factor && ramValue == RAM && cellsValue == attrs) {
                    System.out.println("Found beta value: " + betaValue);
                    return betaValue; // Return the beta value
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1.0;//null; // Return null if no match is found //TODO: update buffer values when new sample sizes come.
    }


    public void runOmniSketchPQPrimitive(long ram, RamToPar rtp, boolean useS0,
                                         boolean useTwoLHS,
                                boolean useOnlyBestRow,
                                boolean use2LHSAcrossRows, boolean rangeQueries,
                                boolean useBetaKmin, boolean useFastTwoLHS,
                                boolean useInvDistPaper2LHS,
                                boolean useMinEstimate, boolean checkExactUnion2LHS,
                                double BetaKmin, boolean dynamicResizing, boolean dynamicSampleSizes,
                                         boolean case1ReturnScap, boolean useNmax,
                                         double eps, int repetition,
                                ArrayList<Long> actRamVals, ArrayList<Integer> ramSizes) throws IOException {
        int[] params;
        if (useTwoLHS) {
            if (useBetaKmin) {
                throw new RuntimeException("Cannot use both TwoLHS and BetaKmin");
            }
            params = rtp.getParamsOmniSketch2LHS(ram);
            Main.numTwoLHSReps = params[2];
        } else {
            params = rtp.getParamsOmniSketchKmin(ram);
            Main.maxSize = params[2];
            Main.b =  params[3];
        }
        Main.depth =  params[0];
        Main.width = params[1];
        System.out.println("Using twolhs: " + useTwoLHS + " and twoKmin: " + useBetaKmin);
        for (int i = 0; i < params.length; i++) {
            System.out.println("params[" + i + "] = " + params[i]);
        }
        if (Main.depth == 0 || Main.width == 0) {
            return;
        }
        Main.rs = new omni.omniPQPrimitive.OmniSketch(ram, Main.numStoredAttributes, params, Main.dyadicRangeBits,
                useS0,
                useTwoLHS, useOnlyBestRow,
                use2LHSAcrossRows,
                rangeQueries, useBetaKmin,
                useFastTwoLHS, useInvDistPaper2LHS,
                useMinEstimate, checkExactUnion2LHS,
                BetaKmin, dynamicResizing,
                dynamicSampleSizes, case1ReturnScap, useNmax, eps, repetition);
        ((omni.omniPQPrimitive.OmniSketch) Main.rs).printParams();
        long synMem = runSynopsisRamBased(Main.rs, repetition);
        actRamVals.add(synMem);
        ramSizes.add(Main.maxSize * Main.depth * Main.width);
        Main.rs = null;
    }

    public void runOmniSketchHouse(long ram, RamToPar rtp, boolean useTwoLHS,
                                       boolean useAcrossRows, boolean rangeQueries,
                                       boolean useBetaKmin, boolean useFastTwoLHS,
                                       boolean useInvDistPaper2LHS,
                                       boolean useMinEstimate, boolean checkExactUnion2LHS,
                                       double BetaKmin, boolean dynamicSampleSizes, boolean case1ReturnScap, double eps,
                                   int repetition,
                                       ArrayList<Long> actRamVals, ArrayList<Integer> ramSizes) throws IOException {
        int[] params;
        if (useTwoLHS) {
            if (useBetaKmin) {
                throw new RuntimeException("Cannot use both TwoLHS and BetaKmin");
            }
            params = rtp.getParamsOmniSketch2LHS(ram);
            Main.numTwoLHSReps = params[2];
        } else {
            params = rtp.getParamsOmniSketchDynamic(ram);
            Main.maxSize = params[2];
            Main.b =  params[3];
        }
        Main.depth =  params[0];
        Main.width = params[1];
        System.out.println("Using twolhs: " + useTwoLHS + " and twoKmin: " + useBetaKmin);
        for (int i = 0; i < params.length; i++) {
            System.out.println("params[" + i + "] = " + params[i]);
        }
        if (Main.depth == 0 || Main.width == 0) {
            return;
        }
        Main.rs = new omni.omniReservoir.OmniSketch(ram, Main.numStoredAttributes, params, Main.dyadicRangeBits,
                useTwoLHS, useAcrossRows,
                rangeQueries, useBetaKmin,
                useFastTwoLHS, useInvDistPaper2LHS,
                useMinEstimate, checkExactUnion2LHS,
                BetaKmin, dynamicSampleSizes, case1ReturnScap, eps, repetition);
        ((omni.omniReservoir.OmniSketch) Main.rs).printParams();
        long synMem = runSynopsisRamBased(Main.rs, repetition);
        actRamVals.add(synMem);
        ramSizes.add(Main.maxSize * Main.depth * Main.width);
        Main.rs = null;
    }

    public void runReservoirSampling(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsReservoirSampling(ram);

        Main.rs = new ReservoirSample(ram, Main.numStoredAttributes, params, repetition);
        ((ReservoirSample) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        Main.rs = null;
    }


    public void runResSampleFixedSize(int size, int repetition) throws IOException {
        int[] params = new int[]{size};
        Main.rs = new ReservoirSample(0, Main.numStoredAttributes, params, repetition);
        ((ReservoirSample) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        Main.rs = null;
    }



    private void runCountMin(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsCountMin(ram);
        Main.rs = new CountMin(ram, Main.numStoredAttributes, params, repetition);
        ((CountMin) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        Main.rs = null;
    }

    public void runAdapSampling(long ram, RamToPar rtp, boolean useBufferInQuery, double ingestBuffer, int repetition) throws IOException {
        int[] params = rtp.getParamsAdapSampling(ram, ingestBuffer);
        //params = new int[]{1000, 1000};
        Main.rs = new aSH(ram, Main.numStoredAttributes, params, repetition, useBufferInQuery, ingestBuffer);
        ((aSH) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        System.out.println("Number of removals ash: " + ((aSH) Main.rs).numRemovals );
        System.out.println("Number of same records ash: " + ((aSH) Main.rs).sameRecCounter );
        Main.rs = null;
    }

    public void runHydra(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsHydra(ram);
        System.out.println("Initializing Hydra");
        System.out.println("Parameters: " + Arrays.toString(params));
        Main.rs = new ImpHydraStruct(ram, Main.numStoredAttributes, params[0], params[1], params[2], params[3], repetition);
        System.out.println("Done initializing.");
        ((ImpHydraStruct) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        Main.rs = null;
    }

    public long runSynopsisRamBased(SynopsisRefactor syn, int repetition) throws IOException {
        long time_passed;
        if (Main.withDeletes && Main.spreadOutDeletes) {
            time_passed= runSynWithSpreadDeletes(syn);
        } else {
            time_passed = runSynWithoutWarmup(syn);
        }
        if (syn.setting.equals("OmniSketch") && !syn.useTwoLHS) {
            System.out.println("Actual deletes from Kmin: " + Main.kminDeletes);
        }
        System.out.println("Time passed for updates synopsis " + syn.setting + " is: " + time_passed + " ms, average: " + (double) time_passed / (cd.dataset.length + 2 * cd.numDeletes) + " ms");
        System.out.println("Memory usage synopsis " + syn.setting + ": " + syn.getMemoryUsage());
        System.out.println("Memory usage dataset: " + cd.getMemoryUsage());
        System.out.println("\n");
        int collisions = 0;

        AnalysisBaselinesRefactor ab = new AnalysisBaselinesRefactor(syn, cd, h, time_passed, collisions, repetition);
        ab.run();
        long synMem = syn.getMemoryUsage();
        syn.reset();
        //ConditionChecks.run(d, s);
        return synMem;
    }

    private int countCollisions(SynopsisRefactor s) {
        int collisions = 0;
        for (int i = 0; i < ((omni.omniPQPrimitive.OmniSketch) s).numStoredAttributes; i++) {
            collisions += ((omni.omniPQPrimitive.OmniSketch) s).CMSketches[i].getCollisions();
        }
        // make sure we can write collisions to file

        return collisions;

    }

    private long runSynWithoutWarmup(SynopsisRefactor syn) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        long startTime = System.currentTimeMillis();
//
        if (Main.withDeletes) {
            for (int r=0; r < cd.noiseUpdates.length; r++) {
                if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
                    System.out.println("Number of updates: " + numUpdates);
                }
                syn.add(cd.noiseUpdates[r]);
                numUpdates++;
            }
//            for (long[] r : cd.noiseUpdates) {
//                if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
//                    System.out.println("Number of updates: " + numUpdates);
//                }
//                syn.add(r);
//                numUpdates++;
//            }
            for (long[] r : cd.datasetResidu) {
                if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
                    System.out.println("Number of updates: " + numUpdates);
                }
                syn.add(r);
                numUpdates++;
            }
            int numDeletes = 0;
            for (int r=0; r < cd.noiseUpdates.length; r++) {
                if (numDeletes % 1000000 == 0 && !Main.runOnODC) {
                    System.out.println("Number of deletes: " + numDeletes);
                }
                syn.delete(cd.noiseUpdates[r]);
                numDeletes++;
            }
//            for (long[] r : cd.noiseUpdates) {
//                if (numDeletes % 10000000 == 0 && !Main.runOnODC) {
//                    System.out.println("Number of deletes: " + numDeletes);
//                }
//                syn.delete(r);
//                numDeletes++;
//            }
        } else {
            for (long[] r : cd.datasetResidu) {
                if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
                    System.out.println("Number of updates: " + numUpdates);
                }
                syn.add(r);
                numUpdates++;
            }
        }
//        }
//        for (long[] r: cd.noiseUpdates) {
//            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
//                System.out.println("Number of updates: " + numUpdates);
//            }
//            syn.add(r);
//            numUpdates++;
//        }
//        for (long[] r: cd.datasetResidu) {
//            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
//                System.out.println("Number of updates: " + numUpdates);
//            }
//            syn.add(r);
//            numUpdates++;
//        }
//        int numDeletes = 0;
//        for (long[] r: cd.noiseUpdates) {
//            if (numDeletes % 10000000 == 0 && !Main.runOnODC) {
//                System.out.println("Number of deletes: " + numDeletes);
//            }
//            syn.delete(r);
//            numDeletes++;
//        }

        // Stop the timer
        long endTime = System.currentTimeMillis();
        //cd.numDeletes = cd.noiseUpdates.length;
        return endTime - startTime;
    }

    private long runSynWithSpreadDeletes(SynopsisRefactor syn) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < cd.datasetAllSpreadOut.length; i++) {
            // always add to synopsis
            if (cd.isDelete[i]) {
                syn.delete(cd.datasetAllSpreadOut[i]);
                numDeletes++;
            } else {
                syn.add(cd.datasetAllSpreadOut[i]);
            }
            if (numDeletes != 0 && numDeletes % 1000000 == 0) {
                System.out.println("Number of deletes: " + numDeletes);
            }
            if (numUpdates % 1000000 == 0) {
                System.out.println("Number of updates: " + numUpdates);
            }
            numUpdates++;
        }
        // Stop the timer
        long endTime = System.currentTimeMillis();

        cd.numDeletes = numDeletes;
        return endTime - startTime;
    }

    public void readDataset(double perc, double maxPerc, double sizeFactor, int noiseSize, double zipfAlpha) throws IOException, CsvValidationException {
        //cd.cleanDataset(d);
        if (Main.datasetName.equals("synthEquiDepthBins")) {
            cd.cleanDatasetEquiDepthBins(perc, sizeFactor, noiseSize);
        } else if (Main.datasetName.equals("synthZipf"))  {
            cd.synthZipf(perc, sizeFactor, noiseSize);
            cd.getDistributions();
        } else if (Main.datasetName.equals("synthDev")) {
            if (Main.numZipfianAttrs > Main.numSynthAttrs) {
                throw new RuntimeException("Number of zipfian attributes cannot be larger than number of attributes");
            }
            if (Main.numUniformAttrs > Main.numSynthAttrs) {
                throw new RuntimeException("Number of uniform attributes cannot be larger than number of attributes");
            }
            if (Main.numZipfianAttrs + Main.numUniformAttrs > Main.numSynthAttrs) {
                throw new RuntimeException("Number of zipfian and uniform attributes cannot be larger than number of attributes");
            }
            cd.synthDev(perc, sizeFactor, noiseSize, Main.numZipfianAttrs, zipfAlpha, Main.numUniformAttrs);
            //cd.getDistributions();
        } else if (Main.datasetName.equals("Test")) {
            cd.testDataset();
        } else {
            cd.cleanDataset(d, perc, maxPerc);
        }

        // print distributions
        //cd.getDistributions();
        //drop dataset
        cd.original = null;
        // dump memory
        System.gc();

    }


    public void readDatasetSettings() throws CsvValidationException, IOException {

        switch (Main.datasetName) {
            case "SNMP" -> {
                if (Main.useMultNumAttributes)
                    if (Main.runOnODC) {
                        this.conditions = new String[]{"0"};
                    } else {
                        this.conditions = new String[]{"03110_OR_031110_OR_031111_OR_031112_OR_031113"};//"03110_OR_03111"};//"03110_OR_031110_OR_031111_OR_031112_OR_031113"};//,
                    }

                else {
                    this.conditions = new String[]{"03"};//"03110_OR_03111"};
                   /*"03110"/*,
                        "03110_OR_031110_OR_031111",
                        "03110_OR_031110_OR_031111_OR_031112_OR_031113",
                        "03110_OR_031110_OR_031111_OR_031112_OR_031113_OR_031114_OR_031115_OR_031116",
                        "03110_OR_03111"*/
                }
                Main.numAttributes = 24;
            }
            case "CAIDA" -> {
                this.conditions = new String[]{Integer.toString(Main.filesToRead)};//, "4", "5", "6", "7", "8", "9", "10", "11", "12"};
                Main.numAttributes = 11;//10; //actually 7; can be 10;
            }
            case "Test" -> {
                this.conditions = new String[]{"0"};
                Main.numAttributes = 2;
            }

            default -> {
                if (!Main.datasetName.contains("synth")) {
                    throw new RuntimeException("Unknown dataset name");
                }
                Main.numAttributes =Main.numSynthAttrs;
                // make string array from Main.sizeFactorOptions
                this.conditions = Main.sizeFactorOptions.split(",");
            }
        }
    }
}

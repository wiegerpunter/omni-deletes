package omni;

import com.opencsv.exceptions.CsvValidationException;
import omni.hydraRefactor.ImpHydraStruct;
import omni.omniTwoLHS.OmniSketch;
import omni.omniTwoLHS.ReservoirSample;
import omni.aSH.aSH;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class TestTwoLHS {
    Helper h;
    DatasetRefactor d;
    CleanDataset cd;
    //ExpWorkload ew;
    RamToPar rtp;
    String[] conditions;
    double sizeFactor;

    int numNoiseUpdates;
    public TestTwoLHS(Helper h, int sizeFactor) {
        this.h = h;
        this.sizeFactor = sizeFactor;
    }

    public void run() throws IOException, CsvValidationException {
        for (int p: Main.numPredicates) {
            cd = new CleanDataset(h, Main.numBins,p);
            readDatasetSettings();
            for (int i = 0; i < conditions.length; i++) {
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
                                readDataset(perc, maxPercent, sizeFactor);
                            }
                            for (int j = 3; j < cd.cleanIds.length + 1; j++) {
                                Main.numStoredAttributes = j;
                                if (Main.datasetName.equals("SNMP") || Main.datasetName.equals("CAIDA")) {
                                    readDataset(perc, maxPercent, sizeFactor);
                                }
                                runSyns(i, repetition);
                            }
                        } else {
                            Main.numStoredAttributes = cd.cleanIds.length;
                            readDataset(perc, maxPercent, sizeFactor);
                            runSyns(i, repetition);
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
        this.rtp = new RamToPar(Main.numStoredAttributes);
        int numPotNeg = cd.datasetNegUpdates.length;
        //int[] noiseUpdates;
        /*if (Main.withDeletes) {
            // Noise updates should make (0, 25, 50, 75, 90, 99) % deletes.
            noiseUpdates = new int[]{0, (int) (cd.datasetResidu.length * 0.5), cd.datasetResidu.length, cd.datasetResidu.length * 3, numPotNeg};
        } else {
            noiseUpdates = new int[]{0};
        }*/
        for (double noiseUpdateFraction : Main.noiseUpdateFractions) {
            numNoiseUpdates = (int) (cd.datasetResidu.length * noiseUpdateFraction);
            System.out.println("Running with " + numNoiseUpdates + " noise updates");
            cd.setNoiseUpdates(numNoiseUpdates);
            for (long ram : Main.ramVals) {
                    if (Main.onlyKmin) {
                        for (int d_ : Main.depthOptionsGridSearch) {
                            for (int w_ : Main.widthOptionsGridSearch) {
                                runOmniSketchGridSearch(ram, rtp, w_, d_, repetition);
                            }
                        }
                    } else {
                        System.out.println("Running sketch with ram " + ram);
                        System.out.println("OMNISKETCH");
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

                        //UNCOMMENT:
                        if (Main.exp2LHS) {
                            runOmniSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, Main.rangeQueries, useBetaKmin,
                                useFastTwoLHS, useInvDistPaper2LHS, useMinEstimate, Main.checkConditions,
                                1, repetition);
                        }

//                        useMinEstimate = false;
//                        runOmniSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, Main.rangeQueries, useBetaKmin,
//                                useFastTwoLHS, useInvDistPaper2LHS, useMinEstimate, Main.checkConditions,
//                                1, repetition);
//                        runOmniSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, Main.rangeQueries, useBetaKmin,
//                                useFastTwoLHS, useInvDistPaper2LHS, useMinEstimate, Main.checkConditions,
//                                1, repetition);
//                        //                runSketch(ram, rtp, !useTwoLHS, false, repetition);
////                    int combinations = (int) Math.pow(2, 3);
////                    for (int comb = 0; comb < combinations; comb++) {
////                        Main.checkConditions = false;
////                        useTwoLHSAcrossRows = (comb & 1) == 1;
////                        useInvDistPaper2LHS = (comb & 2) == 2;
////                        useMinEstimate = (comb & 4) == 4;
////                        if (useInvDistPaper2LHS && useTwoLHSAcrossRows) {
////                            Main.checkConditions = false;
////                        }
////                        if (useTwoLHSAcrossRows && !useMinEstimate) {
////                            continue;
////                        }
////                        System.out.println("Running comp " + comb +
////                                " use2LHS: " + useTwoLHS +
////                                " useTwoLHSAcrossRows: " + useTwoLHSAcrossRows +
////                                " useInvDistPaper2LHS: " + useInvDistPaper2LHS +
////                                " useMinEstimate: " + useMinEstimate);
////                        runOmniSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, Main.rangeQueries,
////                                useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
////                                useMinEstimate, Main.checkConditions,
////                                1, repetition);
////                    }
//                        //Best settings of twoLHS: perRow, InvDist, Min, median
//
//
                        Main.checkConditions = false;
                        useTwoLHS = false;
                        useTwoLHSAcrossRows = false;
                        useFastTwoLHS = false;
                        useInvDistPaper2LHS = false;
                        useMinEstimate = false;
                        useBetaKmin = true;
                        // BetaKmin Vals for every # deletes

                        //UNCOMMENT:
                        //double[] BetaKminVals = new double[]{1, 1.23, 1.9, 4.2, 11.2};
                        Double betaValue = getBeta(Main.inputFolder + "/paramTable/bufferMinwiseTable.csv", noiseUpdateFraction, ram, Main.numStoredAttributes);
                        //for (double BetaKmin_ : Main.bufferValuesOmni) {
//                        System.out.println("Running betaLKmin_ " + betaValue +
//                                " use2LHS: " + useTwoLHS +
//                                " useTwoLHSAcrossRows: " + useTwoLHSAcrossRows +
//                                " useInvDistPaper2LHS: " + useInvDistPaper2LHS +
//                                " useMinEstimate: " + useMinEstimate);
                        runOmniSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, Main.rangeQueries,
                                useBetaKmin, useFastTwoLHS, useInvDistPaper2LHS,
                                useMinEstimate, Main.checkConditions, betaValue, repetition);
                        //}


                        //runSketch(ram, rtp, !useTwoLHS, true, repetition);
//                        //
//                        System.out.println("RESERVOIR SAMPLING");
//                        runReservoirSampling(ram, rtp, repetition);
//                        //////
                        if (Main.expaSH) {
                            double[] ingestBuffers = new double[]{0.1, 0.2, 0.3, 0.4, 0.5};
                            for (double ingestBuffer : ingestBuffers) {
                                System.out.println("ADAP SAMPLING");
                                runAdapSampling(ram, rtp, false,ingestBuffer, repetition);
                                System.out.println("Num noise updates: " + numNoiseUpdates + " for factor " + noiseUpdateFraction);
                            }
//                            useBufferInQuery = false;
//                            runAdapSampling(ram, rtp, useBufferInQuery, repetition);
                        }
                        if (Main.expHydra) {
                            System.out.println("HYDRA Baseline");
                            runHydra(ram, rtp, repetition);
                        }
////                    System.out.println("Running sketch with ram " + ram);
//                    System.out.println("ADAP SAMPLING");
//                    boolean useBufferInQuery = false;
//                    runAdapSampling(ram, rtp, useBufferInQuery, repetition);
//                    useBufferInQuery = true;
//                    runAdapSampling(ram, rtp, useBufferInQuery, repetition);

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

        return null; // Return null if no match is found
    }
    public void runOmniSketch(long ram, RamToPar rtp, boolean useTwoLHS,
                              boolean use2LHSAcrossRows, boolean rangeQueries,
                              boolean useBetaKmin, boolean useFastTwoLHS,
                              boolean useInvDistPaper2LHS,
                              boolean useMinEstimate, boolean checkExactUnion2LHS,
                              double BetaKmin, int repetition) throws IOException {
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
        Main.rs = new OmniSketch(ram, Main.numStoredAttributes, params, Main.dyadicRangeBits,
                useTwoLHS, use2LHSAcrossRows,
                rangeQueries, useBetaKmin,
                useFastTwoLHS, useInvDistPaper2LHS,
                useMinEstimate, checkExactUnion2LHS,
                BetaKmin, repetition);
        ((OmniSketch) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runOmniSketchGridSearch(long ram, RamToPar rtp, int width, int depth, int repetition) throws IOException {
        int[] params;
        params = rtp.getParamsOmniSketchGridSearch(ram, width, depth);
        Main.depth =  params[0];
        Main.width = params[1];
        Main.maxSize = params[2];
        Main.b =  params[3];
        for (int i = 0; i < params.length; i++) {
            System.out.println("params[" + i + "] = " + params[i]);
        }
        Main.rs = new OmniSketch(ram, Main.numStoredAttributes, params, Main.dyadicRangeBits,
                false, false,
                false, false,
                false, false,
                false, false,
                1, repetition);
        ((OmniSketch) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runReservoirSampling(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsReservoirSampling(ram);

        Main.rs = new ReservoirSample(ram, Main.numStoredAttributes, params, repetition);
        ((ReservoirSample) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runAdapSampling(long ram, RamToPar rtp, boolean useBufferInQuery, double ingestBuffer, int repetition) throws IOException {
        int[] params = rtp.getParamsAdapSampling(ram, ingestBuffer);
        //params = new int[]{1000, 1000};
        Main.rs = new aSH(ram, Main.numStoredAttributes, params, repetition, useBufferInQuery);
        ((aSH) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
        System.out.println("Number of removals ash: " + ((aSH) Main.rs).numRemovals );
    }

    public void runHydra(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsHydra(ram);
        System.out.println("Initializing Hydra");
        System.out.println("Parameters: " + Arrays.toString(params));
        Main.rs = new ImpHydraStruct(ram, Main.numStoredAttributes, params[0], params[1], params[2], params[3], repetition);
        System.out.println("Done initializing.");
        ((ImpHydraStruct) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runSynopsisRamBased(SynopsisRefactor syn, int repetition) throws IOException {
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
        AnalysisBaselinesRefactor ab = new AnalysisBaselinesRefactor(syn, cd, h, time_passed, repetition);
        ab.run();
        syn.reset();
        //ConditionChecks.run(d, s);
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

    public void readDataset(double perc, double maxPerc, double sizeFactor) throws IOException, CsvValidationException {
        //cd.cleanDataset(d);
        if (Main.datasetName.equals("synthEquiDepthBins")) {
            cd.cleanDatasetEquiDepthBins(perc, sizeFactor);
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

            default -> {
                if (!Main.datasetName.contains("synth")) {
                    throw new RuntimeException("Unknown dataset name");
                }
                Main.numAttributes = 9;
                // make string array from Main.sizeFactorOptions
                this.conditions = Main.sizeFactorOptions.split(",");
            }
        }
    }
}

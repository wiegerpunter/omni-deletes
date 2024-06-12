package omni;

import cern.jet.random.Beta;
import com.opencsv.exceptions.CsvValidationException;
import omni.omniTwoLHS.OmniSketch;
import omni.omniTwoLHS.ReservoirSample;
import omni.omniTwoLHS.aSH;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class TestTwoLHS {
    Helper h;
    DatasetRefactor d;
    CleanDataset cd;
    //ExpWorkload ew;
    RamToPar rtp;
    String[] conditions;

    int numNoiseUpdates;
    public TestTwoLHS(Helper h) {
        this.h = h;
    }

    public void run() throws IOException, CsvValidationException {
        //Main.useMultNumAttributes = true;
        cd = new CleanDataset(h);
        readDatasetSettings();
        int numRepetitions = 5;
        double[] percToDelete;
        double maxPercent = 0;
        if (Main.withDeletes) {
            percToDelete = new double[]{0.9, 0.1, 0};
            maxPercent = 0.9;
        } else {
            percToDelete = new double[]{0};
        }
        for (double perc : percToDelete) {
            for (int i = 0; i < conditions.length; i++) {
                if (Main.useMultNumAttributes) {
                    for (int j = 2; j < cd.cleanIds.length; j++) {
                            Main.numStoredAttributes = j;
                            readDataset(i, perc, maxPercent);
                            for (int repetition = 0; repetition < numRepetitions; repetition++) {
                                runSyns(i, repetition);
                            }
                    }
                } else {
                    Main.numStoredAttributes = cd.cleanIds.length;
                    readDataset(i, perc, maxPercent);
                    for (int repetition = 0; repetition < numRepetitions; repetition++) {
                        runSyns(i, repetition);
                    }
                }
            }
        }
    }

    private void runSyns(int i, int repetition) throws IOException {

        System.out.println("Running dataset " + (i + 1) + " of " + conditions.length);
        this.rtp = new RamToPar(Main.numStoredAttributes);
        int numPotNeg = cd.datasetNegUpdates.length;
        int[] noiseUpdates;
        if (Main.withDeletes && !Main.spreadOutDeletes) {
            noiseUpdates = new int[]{numPotNeg};//{numPotNeg / 100, numPotNeg / 50, numPotNeg / 10, numPotNeg / 5, numPotNeg / 2, numPotNeg};
        } else {
            noiseUpdates = new int[]{0};
        }
        for (int noiseUpdate : noiseUpdates) {
            numNoiseUpdates = noiseUpdate;
            System.out.println("Running with " + noiseUpdate + " noise updates");
            if (noiseUpdate> 0)
                cd.setNoiseUpdates(numNoiseUpdates);
            for (long ram : Main.ramVals) {
                System.out.println("Running sketch with ram " + ram);

                System.out.println("OMNISKETCH");
                boolean useTwoLHS = true;
                boolean useBetaKmin = false;
                if (Main.countUniqueSamples) {
                    Main.uniqueSamples = new HashMap<>();
                    Main.uniqueSamplesReservoir = new HashSet<>();
                }

                boolean useTwoLHSAcrossRows = true;
                boolean useFastTwoLHS = true;
                boolean useInvDistPaper2LHS = true;
                boolean useMinEstimate = true;
                boolean useExactUnion2LHS = false;
                double[] BetaKminVals = new double[]{1, 1.5, 2, 2.5, 3, 9};

                for (double BetaKmin_ : BetaKminVals) {
                    runSketch(ram, rtp, !useTwoLHS, false, false, false,
                            true, false, useExactUnion2LHS, BetaKmin_, repetition);
                }
//                runSketch(ram, rtp, !useTwoLHS, false, repetition);
                int combinations = (int) Math.pow(2, 3);
                for (int comb = 0; comb < combinations; comb++) {
                    useTwoLHSAcrossRows = (comb & 1) == 1;
                    useInvDistPaper2LHS = (comb & 2) == 2;
                    useMinEstimate = (comb & 4) == 4;
                    runSketch(ram, rtp, useTwoLHS, useTwoLHSAcrossRows, useFastTwoLHS, useInvDistPaper2LHS,
                            useBetaKmin, useMinEstimate,useExactUnion2LHS, 1, repetition);
                }


//                //runSketch(ram, rtp, !useTwoLHS, true, repetition);

//                System.out.println("RESERVOIR SAMPLING");
//                runReservoirSampling(ram, rtp, repetition);
////

            }

//            for (long ram : Main.ramVals) {
//                System.out.println("Running sketch with ram " + ram);
//                System.out.println("ADAP SAMPLING");
//                runAdapSampling(ram, rtp, repetition);
//            }
        }
    }

    public void runSketch(long ram, RamToPar rtp, boolean useTwoLHS, boolean use2LHSAcrossRows,
                          boolean useFastTwoLHS, boolean useInvDistPaper2LHS,
                          boolean useBetaKmin, boolean useMinEstimate, boolean useExactUnion2LHS, double BetaKmin, int repetition) throws IOException {
        int[] params;

        if (useTwoLHS) {
            if (useBetaKmin) {
                throw new RuntimeException("Cannot use both TwoLHS and BetaKmin");
            }
            params = rtp.getParamsSketchTwoLHS(ram);
            Main.numTwoLHSReps = params[2];
        } else {
            params = rtp.getParamsSketchKmin(ram);
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
                useTwoLHS, use2LHSAcrossRows, Main.rangeQueries, useBetaKmin, useFastTwoLHS,
                useInvDistPaper2LHS, useMinEstimate, useExactUnion2LHS, BetaKmin, repetition);
        ((OmniSketch) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runReservoirSampling(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsReservoirSampling(ram);

        Main.rs = new ReservoirSample(ram, Main.numStoredAttributes, params, repetition);
        ((ReservoirSample) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runAdapSampling(long ram, RamToPar rtp, int repetition) throws IOException {
        int[] params = rtp.getParamsAdapSampling(ram);
        params = new int[]{1000};
        Main.rs = new aSH(ram, Main.numStoredAttributes, params, repetition);
        ((aSH) Main.rs).printParams();
        runSynopsisRamBased(Main.rs, repetition);
    }

    public void runSynopsisRamBased(SynopsisRefactor syn, int repetition) throws IOException {
        long time_passed;
        if (Main.spreadOutDeletes) {
            time_passed= runSynWithSpreadDeletes(syn);
        } else {
            time_passed = runSynWithoutWarmup(syn);
        }
        System.out.println("Actual deletes from Kmin: " + Main.kminDeletes);
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
//        for (long[] r: cd.noiseUpdates) {
//            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
//                System.out.println("Number of updates: " + numUpdates);
//            }
//            syn.add(r);
//            numUpdates++;
//        }
        for (long[] r: cd.dataset) {
            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            syn.add(r);
            numUpdates++;
        }
//        int numDeletes = 0;
//        for (long[] r: cd.noiseUpdates) {
//            if (numDeletes % 10000000 == 0 && !Main.runOnODC) {
//                Main.logger.info("Number of deletes: " + numUpdates);
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
        for (int i = 0; i < cd.dataset.length; i++) {
            // always add to synopsis
            syn.add(cd.dataset[i]);

            // check if toDelete contains something at position i. If so, delete it.
            if (cd.toDelete[i] != null) {
                for (int j = 0; j < cd.toDelete[i].size(); j++) {
                    syn.delete(cd.dataset[cd.toDelete[i].get(j)]);
                    numDeletes++;
                    if (numDeletes != 0 && numDeletes % 10000000 == 0 && !Main.runOnODC) {
                        System.out.println("Number of deletes: " + numDeletes);
                    }
                }
            }

            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            numUpdates++;
        }
        // Stop the timer
        long endTime = System.currentTimeMillis();

        cd.numDeletes = numDeletes;
        return endTime - startTime;
    }

    public void readDataset(int i, double perc, double maxPerc) throws IOException, CsvValidationException {
        if (Main.datasetName.equals("SNMP")) {
            Main.fileStartCondition = conditions[i];
        } else {
            // Convert conditions[i] to int
            Main.numFiles = Integer.parseInt(conditions[i]);
        }

        //cd.cleanDataset(d);
        if (Main.datasetName.equals("synthEquiDepthBins")) {
            cd.cleanDatasetEquiDepthBins(perc, maxPerc);
        } else {
            d = new DatasetRefactor(Main.datasetName, h);
            cd.cleanDataset(d, perc, maxPerc);
        }

        // print distributions
        cd.getDistributions();
        //drop dataset
        d = null;
        cd.original = null;
        // dump memory
        System.gc();

    }


    public void readDatasetSettings() {

        switch (Main.datasetName) {
            case "SNMP" -> {
                if (Main.useMultNumAttributes)
                    if (Main.runOnODC) {
                        this.conditions = new String[]{"0"};
                    } else {
                        this.conditions = new String[]{"03110_OR_03111"};//"03110_OR_031110_OR_031111_OR_031112_OR_031113"};//,
                    }

                else {
                    this.conditions = new String[]{"03110_OR_03111"};
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
                Main.numAttributes = 5;
                this.conditions = new String[]{Integer.toString(Main.filesToRead)};
            }
        }
    }
}

package omni.Experiments;

import com.opencsv.exceptions.CsvValidationException;
import omni.*;
import omni.datasets.CleanDataset;
import omni.datasets.DatasetRefactor;
import omni.synopses.SynopsisRefactor;
import omni.synopses.omniFactory.OmniSketch;
import omni.synopses.omniFactory.OmniSketchBuilder;
import omni.Experiments.parameterSetting.RamToPar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunExperiments {
    DatasetRefactor d;
    CleanDataset cd;
    String[] conditions;
    static Config config;

    public RunExperiments(Config config) {
        RunExperiments.config = config;
    }

    public void run() throws IOException, CsvValidationException {
        cd = new CleanDataset(config);
        readDatasetSettings();
        for (int repetition = 0; repetition < config.numRepetitions; repetition++) {
            System.out.println("Running repetition " + repetition);
            cd.setAttributes(repetition);

            for (double zipfAlpha : config.zipfAlphas) {
                for (int i = 0; i < conditions.length; i++) {
                    for (int n : config.sizeNoise) {
                        if (config.useMultNumAttributes) {
                            for (int j = 2; j < cd.cleanIds.length; j++) {
                                config.numStoredAttributes = j + 1;
                                System.out.println("Running with " + config.numStoredAttributes + " attributes");
                                prepareDataset(i, zipfAlpha, n); // prepare dataset
                                runAllExperiments(repetition); // run all experiments in factory
                            }
                        } else {
                            config.numStoredAttributes = cd.cleanIds.length;
                            System.out.println("Running with " + config.numStoredAttributes + " attributes");
                            prepareDataset(i, zipfAlpha, n); // prepare dataset
                            runAllExperiments(repetition); // run all experiments in factory
                        }
                    }
                }
            }
        }
    }

    private void prepareDataset(int conditionIndex, double zipfAlpha, int noiseSize) throws IOException, CsvValidationException {
        double sizeFactor = 0;
        if (config.datasetName.equals("SNMP") || config.datasetName.equals("CAIDA")) {
            config.fileStartCondition = conditions[conditionIndex];
            readRealDataset(conditionIndex);
        } else {
            sizeFactor = Double.parseDouble(config.sizeFactorOptions[conditionIndex]);
            cd.initToNull();
            System.gc();
            System.out.println("Running with size factor " + sizeFactor);
        }
        double perc = 0;
        if (config.withDeletes) {
            perc = 0.901;
        }
        readDataset(perc, sizeFactor, noiseSize, zipfAlpha);
    }

    private void runAllExperiments(int repetition) throws IOException {

        RamToPar rtp = new RamToPar(config.numStoredAttributes, config.ramVals);
        for (double noiseUpdateFraction : config.noiseUpdateFractions) {
            int numNoiseUpdates = (int) (cd.getDatasetSize() * noiseUpdateFraction);
            System.out.println("Running with " + numNoiseUpdates + " deletes out of " + cd.getDatasetSize());
            cd.setNoiseUpdates(numNoiseUpdates);
            for (long ram : config.ramVals) {
                for (String experimentName : getEnabledExperiments()) {
                    Experiment experiment = ExperimentFactory.getExperiment(experimentName);
                    if (experiment != null) {
                        experiment.setRunExperiments(this);
                        if (experimentName.contains("OmniSketch")) {
                            for (int d : config.dGridSearch) {
                                config.d = d;
                                for (int b : config.bGridSearch) {
                                    config.b = b;
                                    if (config.parameterSettingType.equals("B/W")) {
                                        for (double parFactor : config.parFactorGridSearch) {
                                            config.parFactor = parFactor;
                                            experiment.run(ram, rtp, repetition, config);
                                        }
                                    } else {
                                        for (int w : config.wGridSearch) {
                                            config.w = w;
                                            if (config.parameterSettingType.equals("Custom")) {
                                                for (int B : config.BGridSearch) {
                                                    config.B = B;
                                                    experiment.run(ram, rtp, repetition, config);
                                                }
                                            } else if (config.parameterSettingType.equals("GridSearch")) {
                                                config.B = (int) ((ram / (config.d * config.w * config.numStoredAttributes) - 32)/config.b);
                                                if (config.B < 1) {
                                                    System.err.println("B is less than 1, skipping experiment");
                                                    continue;
                                                }
                                                System.out.println("d: " + config.d + ", b: " + config.b + ", w: " + config.w + ", B: " + config.B);
                                                experiment.run(ram, rtp, repetition, config);
                                            } else {
                                                throw new RuntimeException("Unknown parameter setting type: " + config.parameterSettingType);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (experimentName.contains("aSH")) {
                            for (double betaBuffer: config.ingestBuffers) {
                                config.betaBuffer = betaBuffer;
                                experiment.run(ram, rtp, repetition, config);
                            }
                        } else {
                            experiment.run(ram, rtp, repetition, config);
                        }
                    }
                }
            }
        }
    }

    private List<String> getEnabledExperiments() {
        List<String> enabledExperiments = new ArrayList<>();
        if (config.expHydra) enabledExperiments.add("Hydra");
        if (config.expCM) enabledExperiments.add("CountMin");
        if (config.expResSample) enabledExperiments.add("ReservoirSampling");
        if (config.expASH) enabledExperiments.add("aSH");
        if (config.expTWOLHS) enabledExperiments.add("OmniSketchTWOLHS");
        if (config.expFastTWOLHS) enabledExperiments.add("OmniSketchFastTWOLHS");

        if (config.parameterSettingType.equals("SampleSize")) {
            if (config.expOmniSketchVLDBSampleSize) enabledExperiments.add("OmniSketchVLDBSampleSize");
            if (config.expOmniSketchSFQLSampleSize) enabledExperiments.add("OmniSketchSFQLSampleSize");
            if (config.expOmniSketchSFQLSampleSizePerRow) enabledExperiments.add("OmniSketchSFQLSampleSizePerRow");
            if (config.expOmniSketchSFQLSampleSizeTestHashSet)
                enabledExperiments.add("OmniSketchSFQLSampleSizeTestHashSet");
        }
        if (config.parameterSettingType.equals("Custom") || config.parameterSettingType.equals("GridSearch")) {
            if (config.expOmniSketchVLDB) enabledExperiments.add("OmniSketchVLDBCustom");
            if (config.expOmniSketchVLDBArray) enabledExperiments.add("OmniSketchVLDBArrayCustom");
            if (config.expOmniSketchVLDBArrayWithBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithBufferCustom");

            if (config.expOmniSketchSFQLOptimized) enabledExperiments.add("OmniSketchSFQLOptimizedCustom");
            if (config.expOmniSketchSFQLOptimizedOnlyNew) enabledExperiments.add("OmniSketchSFQLOptimizedOnlyNewCustom");
        }

        if (config.parameterSettingType.equals("B/w")) {
            if (config.expOmniSketchVLDB) enabledExperiments.add("OmniSketchVLDB");
            if (config.expOmniSketchVLDBArray) enabledExperiments.add("OmniSketchVLDBArray");
            if (config.expOmniSketchVLDBArrayWithBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithBuffer");

            if (config.expOmniSketchSFQLOptimized) enabledExperiments.add("OmniSketchSFQLOptimized");
            if (config.expOmniSketchSFQLOptimizedOnlyNew) enabledExperiments.add("OmniSketchSFQLOptimizedOnlyNew");
            if (config.expOmniSketchSampleFirstQLater) enabledExperiments.add("OmniSketchSampleFirstQLater");
            if (config.expOmniSketchSampleFirstQLaterPerRow) enabledExperiments.add("OmniSketchSampleFirstQLaterPerRow");
        }
        return enabledExperiments;
    }


    public void readDataset(double perc, double sizeFactor, int noiseSize, double zipfAlpha) throws IOException, CsvValidationException {
        //cd.cleanDataset(d);
        switch (config.datasetName) {
            case "synthEquiDepthBins" -> cd.cleanDatasetEquiDepthBins(perc, sizeFactor, noiseSize);
            case "synthZipf" -> {
                cd.synthZipf(perc, sizeFactor, noiseSize);
                cd.getDistributions();
            }
            case "synthDev" -> {
                if (config.numZipfAttributes + config.numUniformAttributes > config.numSynthAttributes) {
                    throw new RuntimeException("Number of zipfian and uniform attributes cannot be larger than number of attributes");
                }
                cd.synthDev(perc, sizeFactor, config.numZipfAttributes , zipfAlpha, config.numUniformAttributes);
            }
            case "Test" -> cd.testDataset();
            default -> cd.cleanDataset(d, perc, noiseSize);
        }

        // get number of unique full records, ignoring the id
        cd.getUniqueRecords();

        // print distributions
        //cd.getDistributions();
        //drop dataset
        cd.original = null;
        // dump memory
        System.gc();

    }

    public long runSynopsisRamBased(SynopsisRefactor syn, int repetition) throws IOException {
        long time_passed;
        if (config.withDeletes && config.spreadOutDeletes) {
            time_passed= runSynWithSpreadDeletes(syn);
        } else {
            time_passed = runSynWithoutWarmup(syn);
        }

        System.out.println("Time passed for updates synopsis " + syn.getSetting() + " is: " + time_passed + " ms, average: " + (double) time_passed / (cd.getDatasetSize() + 2 * cd.numDeletes) + " ms");
        System.out.println("Memory usage synopsis " + syn.getSetting() + ": " + syn.getMemoryUsage());
        System.out.println("Memory usage dataset: " + cd.getMemoryUsage());
        System.out.println("Compression ratio: " + (double) syn.getMemoryUsage() / cd.getMemoryUsage());
        System.out.println("\n");
        int collisions = 0;

        runQueries ab = new runQueries(syn, cd, time_passed, collisions, repetition, config);
        ab.run();
        long synMem = syn.getMemoryUsage();
        syn.reset();
        //ConditionChecks.run(d, s);
        return synMem;
    }
    private long processDataset(SynopsisRefactor syn, long[][] dataset, boolean[] isDelete, boolean withDeletes, boolean spreadOutDeletes) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < dataset.length; i++) {
            if (spreadOutDeletes && isDelete != null && isDelete[i]) {
                syn.delete(dataset[i]);
                numDeletes++;
            } else {
                syn.add(dataset[i]);
                numUpdates++;
            }

            if (numDeletes > 0 && numDeletes % 1000000 == 0) {
                System.out.println("Number of deletes: " + numDeletes);
            }
            if (numUpdates % 1000000 == 0) {
                System.out.println("Number of updates: " + numUpdates);
            }
        }

        long endTime = System.currentTimeMillis();

        if (withDeletes) {
            cd.numDeletes = numDeletes;
        }

        return endTime - startTime;
    }

    private long runSynWithoutWarmup(SynopsisRefactor syn) {
        return processDataset(syn, cd.getDataset(), null, config.withDeletes, false);
    }

    private long runSynWithSpreadDeletes(SynopsisRefactor syn) {
        return processDataset(syn, cd.datasetAllSpreadOut, cd.isDelete, config.withDeletes, true);
    }

    private void readDatasetSettings() {

        switch (config.datasetName) {
            case "SNMP" -> {
                if (config.useMultNumAttributes)
                    if (config.runOnODC) {
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
                config.numAttributes = 24;
            }
            case "CAIDA" -> {
                this.conditions = new String[]{"1"};//, "4", "5", "6", "7", "8", "9", "10", "11", "12"};
                config.numAttributes = 11;//10; //actually 7; can be 10;
            }
            case "Test" -> {
                this.conditions = new String[]{"0"};
                config.numAttributes = 2;
            }

            default -> {
                if (!config.datasetName.contains("synth")) {
                    throw new RuntimeException("Unknown dataset name");
                }
                config.numAttributes =config.numSynthAttributes;
                // make string array from Main.sizeFactorOptions
                this.conditions = config.sizeFactorOptions;
            }
        }
    }

    private void readRealDataset(int i) throws CsvValidationException, IOException {
        if (config.datasetName.equals("SNMP")) {
            config.fileStartCondition = conditions[i];
        }
        d = new DatasetRefactor(config);
    }

    static void omniExperiment(long ram, int repetition, Config config, int[] params, OmniSketchBuilder omniSketchBuilder, RunExperiments runExperiments) throws IOException {
        System.out.println("Running OmniSketch with parameters: " + Arrays.toString(params));

        OmniSketch omniSketch = omniSketchBuilder
                .setRam(ram)
                .setNumStoredAttributes(config.numStoredAttributes)
                .setParams(params)
                .setSeed(repetition)
                .build();

        omniSketch.printParams();
        runExperiments.runSynopsisRamBased(omniSketch, repetition);
        omniSketch.reset();
        System.gc();
    }
}

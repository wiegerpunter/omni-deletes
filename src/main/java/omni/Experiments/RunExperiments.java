package omni.Experiments;

import com.opencsv.exceptions.CsvValidationException;
import omni.*;
import omni.Experiments.parameterSetting.BufferedParamMinwiseSettings;
import omni.datasets.CleanDataset;
import omni.datasets.DatasetRefactor;
import omni.datasets.Record.LongRecord;
import omni.datasets.Record.StringRecord;
import omni.synopses.SynopsisRefactor;
import omni.synopses.omniFactory.OmniSketch;
import omni.synopses.omniFactory.OmniSketchBuilder;
import omni.Experiments.parameterSetting.RamToPar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunExperiments {
    DatasetRefactor d;
    CleanDataset cd;
    String[] conditions;
    BufferedParamMinwiseSettings bp;
    static Config config;

    public RunExperiments(Config config) {
        RunExperiments.config = config;
    }

    public void run() throws IOException, CsvValidationException {
        bp = new BufferedParamMinwiseSettings(Main.inputFolder);
        cd = new CleanDataset(config);
        readDatasetSettings();
        int repetition= config.seed;
        System.out.println("Running repetition " + repetition);
        cd.setAttributes(repetition);

        for (double zipfAlpha : config.zipfAlphas) {
            for (int i = 0; i < conditions.length; i++) {
                for (int n : config.sizeNoise) {
                    for (int domain : config.domain_sizes) {
                        config.domain = domain;
                        if (config.useMultNumAttributes) {
                            for (int j = 2; j < cd.cleanIds.length; j++) { //cd.cleanIds.length
                                config.numStoredAttributes = j + 1;
                                if (config.numStoredAttributes % 3 != 0 && config.numStoredAttributes != 11) {
                                    continue; // skip attributes that are not multiples of 3, except for 11
                                }
                                System.out.println("Running with " + config.numStoredAttributes + " attributes");
                                if (config.readFromDisk) {
                                    config.noiseUpdateFractions = new ArrayList<>();
                                    config.noiseUpdateFractions.add(0.0);
                                    for (double perc : config.percs) {
                                        prepareDataset(i, zipfAlpha, n, perc); // prepare dataset
                                        runAllExperiments(repetition, perc); // run all experiments in factory
                                    }
                                } else {
                                    double perc = 0;
                                    if (config.withDeletes) {
                                        perc = 0.901;
                                    }
                                    prepareDataset(i, zipfAlpha, n, perc); // prepare dataset
                                    runAllExperiments(repetition, perc); // run all experiments in factory
                                }
                            }
                        } else {
                            config.numStoredAttributes = cd.cleanIds.length;
                            System.out.println("Running with " + config.numStoredAttributes + " attributes");
                            if (config.readFromDisk) {
                                config.noiseUpdateFractions = new ArrayList<>();
                                config.noiseUpdateFractions.add(0.0);
                                for (double perc : config.percs) {
                                    prepareDataset(i, zipfAlpha, n, perc); // prepare dataset
                                    runAllExperiments(repetition, perc); // run all experiments in factory
                                }
                            } else {
                                double perc = 0;
                                if (config.withDeletes) {
                                    perc = 0.901;
                                }
                                prepareDataset(i, zipfAlpha, n, perc); // prepare dataset
                                runAllExperiments(repetition, perc); // run all experiments in factory
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareDataset(int conditionIndex, double zipfAlpha, int noiseSize, double perc) throws IOException, CsvValidationException {
        double sizeFactor = 0;
        if ((config.datasetName.equals("SNMP") || config.datasetName.equals("CAIDA") && !config.readFromDisk)) {
            config.fileStartCondition = conditions[conditionIndex];
            if (!config.readFromDisk) {
                readRealDataset(conditionIndex);
            }
        } else {
            sizeFactor = Double.parseDouble(config.sizeFactorOptions[conditionIndex]);
            cd.initToNull();
            System.gc();
            System.out.println("Running with size factor " + sizeFactor);
        }
        readDataset(perc, sizeFactor, noiseSize, zipfAlpha);
    }
    //double[] betas = new double[]{1,1.35,2.1,4.2,11};
    private void runAllExperiments(int repetition, double noisePerc) throws IOException {
        double[] ramMultiplyers;

        int ramMultiplyers_count = 0;
        if (config.readFromDisk) {
            // first value is always 1.0, no buffer
            if (config.expOmniSketchVLDBArrayWithBufferOpt || config.expOmniSketchVLDBArrayWithBufferOptBatchDeletes) {
                ramMultiplyers = new double[config.percsForMultiplyers.length];
            } else {
                ramMultiplyers = new double[1];
                ramMultiplyers[0] = 1.0; // no buffer
            }
        } else {
            if (config.expOmniSketchVLDBArrayWithBufferOpt || config.expOmniSketchVLDBArrayWithBufferOptBatchDeletes) {
                ramMultiplyers = new double[config.noiseUpdateFractions.size()];
            } else {
                ramMultiplyers= new double[1];
                ramMultiplyers[0] = 1.0; // no buffer
            }
        }
        RamToPar rtp = new RamToPar(config.numStoredAttributes, config.ramVals);
        for (double noiseUpdateFraction : config.noiseUpdateFractions) {
            int numNoiseUpdates = (int) (cd.getDatasetSize());
            if (!config.readFromDisk && !cd.setNoiseUpdates((int) (noiseUpdateFraction * numNoiseUpdates))) {
                continue;
            };
            for (long ram : config.ramVals) {

                int s0 = 0;
                for (String experimentName : getEnabledExperiments()) {
                    Experiment experiment = ExperimentFactory.getExperiment(experimentName);
                    if (experiment != null) {
                        experiment.setRunExperiments(this);
                        if (experimentName.contains("OmniSketch")) {
                            for (int d : config.dGridSearch) {
                                config.d = d;
                                for (int b : config.bGridSearch) {
                                    config.b = b;
                                    for (int ingestBufferSize : config.ingestBufferSizeGridSearch) {
                                        config.ingestBufferSize = ingestBufferSize;
                                        for (int deleteBufferSize: config.deleteBufferSizeGridSearch) {
                                            if (deleteBufferSize > config.ingestBufferSize) {
                                                continue;
                                            }
                                            config.deleteBufferSize = deleteBufferSize;
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

                                                        if (experimentName.contains("VLDB")) {
                                                            config.B = (int) ((ram / (config.d * config.w * config.numStoredAttributes) - 32) / config.b);
                                                            if (config.B < 1) {
                                                                System.err.println("B is less than 1, skipping experiment");
                                                                continue;
                                                            }
                                                            bp.add(ram, config.B, config.d, config.w, config.numStoredAttributes, config.b);
                                                            if (config.readFromDisk) {
                                                                for (double perc : config.percsForMultiplyers) {
                                                                    config.bufferDeletesMinwise = getBeta(Main.inputFolder +
                                                                                    "/paramTable/bufferMinwiseTable.csv", perc,
                                                                            ram, config.numStoredAttributes, config.d, 1);
                                                                    if (config.useNoBufferOmniVLDB && config.bufferDeletesMinwise > 1) {
                                                                        continue;
                                                                    }
                                                                    if (s0 > 0 && experimentName.equals("OmniSketchVLDBS0Custom")) {
                                                                        continue;
                                                                    } else if (experimentName.equals("OmniSketchVLDBS0Custom")){
                                                                        s0++;
                                                                    }
                                                                    experiment.run(ram, rtp, repetition, config);
                                                                    if (ramMultiplyers_count < config.percsForMultiplyers.length) {
                                                                        ramMultiplyers[ramMultiplyers_count] = config.bufferDeletesMinwise;
                                                                        ramMultiplyers_count++;
                                                                    }
                                                                }
                                                            } else {
                                                                for (double noiseUpdateFraction1 : config.noiseUpdateFractions) {
                                                                    config.bufferDeletesMinwise = getBeta(Main.inputFolder +
                                                                                    "/paramTable/bufferMinwiseTable.csv",
                                                                            noiseUpdateFraction1, ram, config.numStoredAttributes,
                                                                            config.d, 1);
                                                                    experiment.run(ram, rtp, repetition, config);
                                                                    if (ramMultiplyers_count < config.noiseUpdateFractions.size()) {
                                                                        ramMultiplyers[ramMultiplyers_count] = config.bufferDeletesMinwise;
                                                                        ramMultiplyers_count++;
                                                                    }
                                                                }
                                                            }
                                                        } else if (experimentName.contains("TWOLHS")) {
                                                            if (noisePerc > 0) {
                                                                continue;
                                                            }
                                                            ; // TWOLHS is impervious to deletes.

                                                            config.B = (int) ((ram / (config.d * config.w * config.numStoredAttributes) - 32) / (31 * 32 * 32));
                                                            System.out.println("d: " + config.d + ", b: " + config.b + ", w: " + config.w + ", B: " + config.B);
                                                            if (config.B < 1) {
                                                                System.err.println("B is less than 1, skipping experiment");
                                                                continue;
                                                            }
                                                            experiment.run(ram, rtp, repetition, config);
                                                        } else {
                                                            throw new RuntimeException("OmniSketch experiment name not recognized " + experimentName);
                                                        }

                                                    } else {
                                                        throw new RuntimeException("Unknown parameter setting type: " + config.parameterSettingType);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (experimentName.contains("aSH")) {
                            ArrayList<Long> sizes = new ArrayList<Long>();
                            for (double ramMultiplyer: ramMultiplyers) {
                                sizes.add((long) (ram * ramMultiplyer));
                            }
                            RamToPar rtp_ash = new RamToPar(config.numStoredAttributes, sizes);
                            for (long size: sizes) {
                                for (double bufferASH : config.ingestBuffers) {
                                    config.bufferASH = bufferASH;

                                    experiment.run(size, rtp_ash, repetition, config);
                                }
                            }
                        } else {
                            experiment.run(ram, rtp, repetition, config);
                        }
                    }
                }
            }
        }
    }

    private static Double getBeta(String filePath, double factor, double RAM, int attrs, int d, int factorIndex) {
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] values = line.split(csvSplitBy);

                // Assuming columns: alpha, insert_stream_size, cells, Y, beta, iterations
                double factorValue = Double.parseDouble(values[factorIndex]);
                double ramValue = Double.parseDouble(values[7])*8*Math.pow(10,6);
                int cellsValue = (int) Double.parseDouble(values[3]);
                double betaValue = Double.parseDouble(values[5]);

                // Check for matching values
                if (factorValue == factor && ramValue == RAM && cellsValue == d * attrs) {
                    System.out.println("Found beta value: " + betaValue);
                    return betaValue; // Return the beta value
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1.0;
    }

    private List<String> getEnabledExperiments() {
        List<String> enabledExperiments = new ArrayList<>();
        if (config.expReadFile) enabledExperiments.add("readFile"); //always measure readFile time.
        if (config.expHydra) enabledExperiments.add("Hydra");
        if (config.expCM) enabledExperiments.add("CountMin");

        if (config.parameterSettingType.equals("SampleSize")) {
            if (config.expOmniSketchVLDBSampleSize) enabledExperiments.add("OmniSketchVLDBSampleSize");
            if (config.expOmniSketchSFQLSampleSize) enabledExperiments.add("OmniSketchSFQLSampleSize");
            if (config.expOmniSketchSFQLSampleSizePerRow) enabledExperiments.add("OmniSketchSFQLSampleSizePerRow");
            if (config.expOmniSketchSFQLSampleSizeTestHashSet)
                enabledExperiments.add("OmniSketchSFQLSampleSizeTestHashSet");
        }
        if (config.parameterSettingType.equals("Custom") || config.parameterSettingType.equals("GridSearch")) {
            if (config.expOmniSketchVLDB) enabledExperiments.add("OmniSketchVLDBCustom");
            if (config.expOmniSketchVLDBS0) enabledExperiments.add("OmniSketchVLDBS0Custom");
            if (config.expOmniSketchVLDBS0PerRow) enabledExperiments.add("OmniSketchVLDBS0PerRowCustom");

            if (config.expOmniSketchVLDBArray) enabledExperiments.add("OmniSketchVLDBArrayCustom");
            if (config.expOmniSketchVLDBArrayRegBuffer) enabledExperiments.add("OmniSketchVLDBArrayRegBufferCustom");

            if (config.expOmniSketchVLDBArrayWithBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithBufferCustom");
            if (config.expOmniSketchVLDBArrayWithBufferPerRow) enabledExperiments.add("OmniSketchVLDBArrayWithBufferPerRowCustom");
            if (config.expOmniSketchVLDBArrayWithBufferOpt) enabledExperiments.add("OmniSketchVLDBArrayWithBufferOptCustom");
            if (config.expOmniSketchVLDBArrayWithBufferOptBatchDeletes) enabledExperiments.add(
                    "OmniSketchVLDBArrayWithBufferOptBatchDeletesCustom");

            if (config.expOmniSketchVLDBArrayWithBufferPessDeleteCount) enabledExperiments.add("OmniSketchVLDBArrayWithBufferPessDeleteCountCustom");

            if (config.expOmniSketchVLDBArrayWithBufferOnlyValid) enabledExperiments.add("OmniSketchVLDBArrayWithBufferOnlyValidCustom");

            if (config.expOmniSketchVLDBArrayWithoutBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithoutBufferCustom");
            if (config.expOmniSketchVLDBTreeSet) enabledExperiments.add("OmniSketchVLDBTreeSetCustom");
            if (config.expOmniSketchVLDBTreeSetWithoutBuffer) enabledExperiments.add("OmniSketchVLDBTreeSetWithoutBufferCustom");
            if (config.expOmniSketchVLDBSimpleBuffer) enabledExperiments.add("OmniSketchVLDBSimpleBufferCustom");
            if (config.expOmniSketchVLDBSimpleBufferNoDeleteBuffer) enabledExperiments.add("OmniSketchVLDBSimpleBufferNoDeleteBufferCustom");

            if (config.expSlowTWOLHS) enabledExperiments.add("OmniSketchSlowTWOLHSCustom");
            if (config.expFastTWOLHS) enabledExperiments.add("OmniSketchFastTWOLHSCustom");

            if (config.expFastTWOLHSOneBucket) enabledExperiments.add("OmniSketchFastTWOLHSOneBucketCustom");
            if (config.expNMaxTWOLHS) enabledExperiments.add("OmniSketchNMaxTWOLHSCustom");


            if (config.expSlowTWOLHSPerRow) enabledExperiments.add("OmniSketchSlowTWOLHSPerRowCustom");
            if (config.expFastTWOLHSPerRow) enabledExperiments.add("OmniSketchFastTWOLHSPerRowCustom");
            if (config.expFastTWOLHSOneBucketPerRow) enabledExperiments.add("OmniSketchFastTWOLHSOneBucketPerRowCustom");
            if (config.expNMaxTWOLHSPerRow) enabledExperiments.add("OmniSketchNMaxTWOLHSPerRowCustom");

            if (config.expTWOLHSExact) enabledExperiments.add("OmniSketchTWOLHSExactCustom");
            if (config.expTWOLHSPerRowExact) enabledExperiments.add("OmniSketchTWOLHSPerRowExactCustom");


            if (config.expOmniSketchSFQLOptimized) enabledExperiments.add("OmniSketchSFQLOptimizedCustom");
            if (config.expOmniSketchSFQLOptimizedOnlyNew) enabledExperiments.add("OmniSketchSFQLOptimizedOnlyNewCustom");
        }

        if (config.parameterSettingType.equals("B/w")) {
            if (config.expOmniSketchVLDB) enabledExperiments.add("OmniSketchVLDB");
            if (config.expOmniSketchVLDBArray) enabledExperiments.add("OmniSketchVLDBArray");
            if (config.expOmniSketchVLDBArrayWithBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithBuffer");
            if (config.expOmniSketchVLDBArrayWithoutBuffer) enabledExperiments.add("OmniSketchVLDBArrayWithoutBuffer");
            if (config.expOmniSketchVLDBTreeSet) enabledExperiments.add("OmniSketchVLDBTreeSet");
            if (config.expOmniSketchVLDBTreeSetWithoutBuffer) enabledExperiments.add("OmniSketchVLDBTreeSetWithoutBuffer");
            if (config.expOmniSketchSFQLOptimized) enabledExperiments.add("OmniSketchSFQLOptimized");
            if (config.expOmniSketchSFQLOptimizedOnlyNew) enabledExperiments.add("OmniSketchSFQLOptimizedOnlyNew");
            if (config.expOmniSketchSampleFirstQLater) enabledExperiments.add("OmniSketchSampleFirstQLater");
            if (config.expOmniSketchSampleFirstQLaterPerRow) enabledExperiments.add("OmniSketchSampleFirstQLaterPerRow");

            if (config.expSlowTWOLHS) enabledExperiments.add("OmniSketchTWOLHS");
            if (config.expFastTWOLHS) enabledExperiments.add("OmniSketchFastTWOLHS");
        }


        if (config.expResSample) enabledExperiments.add("ReservoirSampling");
        if (config.expASH) enabledExperiments.add("aSH");
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
            case "synthFromDisk" -> {
                cd.synthetic(perc, sizeFactor, zipfAlpha);
            }
            case "StringTest" -> {
                cd.testStringData();
            }
            case "Test" -> cd.testDataset();
            case "tpc-ds" -> {
                cd.tpcDS(perc, (int) sizeFactor);
            }
            case "SNMP" -> {
                if (d.size == 0) {
                    // clean dataset
                    cd.datasetReaderName = d.datasetReaderName;
                    cd.readSNMP();
                } else {
                    cd.cleanDataset(d, perc, noiseSize);
                }
            }
            case "CAIDA" -> {
                if (config.readFromDisk) {
                    cd.caida(perc, (int) sizeFactor);
                } else {
                    cd.cleanDataset(d, perc, noiseSize);
                }
            }
            default -> cd.cleanDataset(d, perc, noiseSize);
        }

        // get number of unique full records, ignoring the id
        if (config.getUniqueRecords) {
            cd.getUniqueRecords();
        }

        // print distributions
        //cd.getDistributions();
        //drop dataset
        cd.original = null;
        // dump memory
        System.gc();

    }

    public long runSynopsisRamBased(SynopsisRefactor syn, int repetition) throws IOException {
        long time_passed;

        if (config.readFromDisk) {
            System.out.println("Reading dataset from disk");
            if (config.useNoCast) {
                time_passed = runDatasetFromDisk(syn);
            } else {
//            time_passed = runDatasetFromDiskLong(syn);
                time_passed = runDatasetFromDiskString(syn);
            }
        } else {

            if (config.withDeletes) {
                time_passed = runSynWithDeletes(syn);
            } else {
                time_passed = runSynWithoutWarmup(syn);
            }
        }

        System.out.println("Time passed for updates synopsis " + syn.getSetting() + " is: " + time_passed + " ms, average: " + (double) time_passed / (cd.getDatasetSize()) + " ms");
        System.out.println("Memory usage synopsis " + syn.getSetting() + ": " + syn.getMemoryUsage());
        System.out.println("Memory usage dataset: " + cd.getMemoryUsage());
        System.out.println("Compression ratio: " + (double) syn.getMemoryUsage() / cd.getMemoryUsage());
        System.out.println("\n");
        int collisions = 0;


        if (config.useNoCast) {
            runQueriesNoCast ab = new runQueriesNoCast(syn, cd, time_passed, collisions, repetition, config);
            ab.run();
        } else {
            runQueries ab = new runQueries(syn, cd, time_passed, collisions, repetition, config);
            ab.runObj();
        }
        long synMem = syn.getMemoryUsage();
        syn.reset();
        //ConditionChecks.run(d, s);
        return synMem;
    }

    private long runDatasetFromDisk(SynopsisRefactor syn) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();

        try (BufferedReader br = new BufferedReader(new FileReader(cd.datasetReaderName))) {
            br.readLine(); // Skip header line
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long[] record = new long[values.length - 1];
                for (int i = 0; i < values.length -1; i++) {
                    record[i] = Long.parseLong(values[i]);
                }
                int sign = Integer.parseInt(values[values.length - 1]);

                if (sign == 1) {
                    syn.add(record);
                    numUpdates++;
                } else if (sign == -1){
                    syn.delete(record);
                    numDeletes++;
                } else {
                    throw new RuntimeException("Invalid sign value: " + sign);
                }

                if (numDeletes > 0 && numDeletes % 1000000 == 0) {
                    System.out.printf("\rNumber of deletes: " + numDeletes + " / " + cd.getNoiseSize());
                }
                if (numUpdates % 1000000 == 0) {
                    double progress = (double) (numUpdates + numDeletes) / cd.getDatasetSize() * 100;
                    System.out.printf("\rProgress: " + progress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        cd.numDeletes = numDeletes;


        return endTime - startTime;

    }

    private long runDatasetFromDiskLong(SynopsisRefactor syn) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();

        try (BufferedReader br = new BufferedReader(new FileReader(cd.datasetReaderName))) {
            br.readLine(); // Skip header line
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long[] record = new long[values.length - 1];
                for (int i = 0; i < values.length -1; i++) {
                    record[i] = Long.parseLong(values[i]);
                }
                int sign = Integer.parseInt(values[values.length - 1]);
                LongRecord rec = new LongRecord(record);
                if (sign == 1) {
                    syn.add(rec);
                    numUpdates++;
                } else if (sign == -1){
                    syn.delete(rec);
                    numDeletes++;
                } else {
                    throw new RuntimeException("Invalid sign value: " + sign);
                }

                if (numDeletes > 0 && numDeletes % 1000000 == 0) {
                    System.out.printf("\rNumber of deletes: " + numDeletes + " / " + cd.getNoiseSize());
                }
                if (numUpdates % 1000000 == 0) {
                    double progress = (double) (numUpdates + numDeletes) / cd.getDatasetSize() * 100;
                    System.out.printf("\rProgress: " + progress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        cd.numDeletes = numDeletes;


        return endTime - startTime;

    }


    private long runDatasetFromDiskString(SynopsisRefactor syn) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();

        try (BufferedReader br = new BufferedReader(new FileReader(cd.datasetReaderName))) {
            br.readLine(); // Skip header line
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
//                String[] record = new String[values.length - 1];
                String[] record = new String[config.numStoredAttributes + 1];
                System.arraycopy(values, 0, record, 0, config.numStoredAttributes + 1);
//                System.arraycopy(values, 0, record, 0, values.length - 1);
                int sign = Integer.parseInt(values[values.length - 1]);
                StringRecord rec = new StringRecord(record);
                if (sign == 1) {
                    syn.add(rec);
                    numUpdates++;
                } else if (sign == -1){
                    syn.delete(rec);
                    numDeletes++;
                } else {
                    throw new RuntimeException("Invalid sign value: " + sign);
                }

                if (numDeletes > 0 && numDeletes % 1000000 == 0) {
                    System.out.printf("\rNumber of deletes: " + numDeletes + " / " + cd.getNoiseSize());
                }
                if (numUpdates % 1000000 == 0) {
                    double progress = (double) (numUpdates + numDeletes) / cd.getDatasetSize() * 100;
                    System.out.printf("\rProgress: " + progress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        cd.numDeletes = numDeletes;


        return endTime - startTime;

    }

    private long processDataset(SynopsisRefactor syn, long[][] dataset, boolean[] isDelete, boolean withDeletes) {
        System.out.println("Running synopsis");
        int numUpdates = 0;
        int numDeletes = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < dataset.length; i++) {
            if (isDelete != null && isDelete[i]) {
                syn.delete(dataset[i]);
                numDeletes++;
            } else {
                syn.add(dataset[i]);
                numUpdates++;
            }

            if (numDeletes > 0 && numDeletes % 1000000 == 0) {
                System.out.printf("\rNumber of deletes: " + numDeletes);
            }
            if (numUpdates % 1000000 == 0) {
                System.out.printf("\rNumber of updates: " + numUpdates + " / " + dataset.length);
            }
        }

        long endTime = System.currentTimeMillis();

        if (withDeletes) {
            cd.numDeletes = numDeletes;
        }

        return endTime - startTime;
    }

    private long runSynWithoutWarmup(SynopsisRefactor syn) {
        return processDataset(syn, cd.getDataset(), null, config.withDeletes);
    }

    private long runSynWithDeletes(SynopsisRefactor syn) {
        return processDataset(syn, cd.datasetAllUpdates, cd.isDelete, config.withDeletes);
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
                    this.conditions = new String[]{"031101"};//"03110_OR_03111"};
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
                config.numAttributes = 8;//10; //actually 7; can be 10;
            }
            case "Test" -> {
                this.conditions = new String[]{"0"};
                config.numAttributes = 2;
            }
            case "StringTest" -> {
                this.conditions = new String[]{"0"};
                config.numAttributes = 3;
            }
            case "tpc-ds"-> {
                this.conditions = new String[]{"0"};
                config.numAttributes = 5;
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

    public static void omniExperiment(long ram, int repetition, Config config, int[] params, OmniSketchBuilder omniSketchBuilder, RunExperiments runExperiments) throws IOException {
        System.out.println("Running OmniSketch with parameters: " + Arrays.toString(params));

        int numCells = config.d * config.w * config.numStoredAttributes;
        int bufferSize = 8000000 / numCells / 32; // 1MB buffer size
        OmniSketch omniSketch = omniSketchBuilder
                .setRam(ram)
                .setIngestBufferSize(bufferSize/2)
                .setDeleteBufferSize(bufferSize/2)
                .setNumStoredAttributes(config.numStoredAttributes)
                .setParams(params)
                .setSeed(repetition)
                .setBufferBeta(config.bufferDeletesMinwise)
                .build();

        omniSketch.printParams();
        runExperiments.runSynopsisRamBased(omniSketch, repetition);
        omniSketch.reset();
        System.gc();
    }
}

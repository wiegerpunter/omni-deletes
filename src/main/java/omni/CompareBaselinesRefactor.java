package omni;

import com.opencsv.exceptions.CsvValidationException;
import omni.baseline.SimpleSketch;
import omni.hydraRefactor.ImpHydraStruct;
import omni.omniRefactor.OmniSketch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CompareBaselinesRefactor {
    //TODO: before testing make sure to replace CompareBaselines in main with CompareBaselinesRefactor
    // Class that compares the baselines
    Helper h;
    DatasetRefactor d;
    CleanDataset cd;
    //ExpWorkload ew;
    RamToPar rtp;
    String[] conditions;

    public CompareBaselinesRefactor(Helper h) {
        this.h = h;
    }

    public void run() throws IOException, CsvValidationException {
        Main.useMultNumAttributes = true;
        cd = new CleanDataset(h);
        readDatasetSettings();

        for (int i = 0; i < conditions.length; i++) {
            if (Main.useMultNumAttributes) {
                for (int j = 2; j < cd.cleanIds.length; j++) {
                        Main.numStoredAttributes = j;
                        readDataset(i);
                        runSyns(i);
                }
            } else {
                Main.numStoredAttributes = cd.cleanIds.length;
                readDataset(i);
                runSyns(i);
            }
        }
    }

    private void runSyns(int i) throws IOException {

        System.out.println("Running dataset " + (i + 1) + " of " + conditions.length);
        this.rtp = new RamToPar(Main.numStoredAttributes);
        for (long ram : Main.ramVals) {
            System.out.println("Running sketch with ram " + ram);

            System.out.println("OMNISKETCH");
            runSketch(ram, rtp);
            //System.out.println("HYDRA");
            //runHydra(ram, rtp);
            /*if (!Main.datasetName.contains("synth")) {

            }*/
        }
    }

    public void runSketch(long ram, RamToPar rtp) throws IOException {
        int[] params = rtp.getParamsSketchKmin(ram);
        Main.depth = 5;// (int) params[0];
        Main.width = (int) params[1];
        Main.maxSize = (int) params[2];
        Main.b = (int) params[3];
        Main.rs = new OmniSketch(ram);
        runSynopsisRamBased(Main.rs);
    }

    public void runCMBaseline(int ram, RamToPar rtp) throws IOException {
        double[] params = rtp.getParamsCMBaseline(ram);
        int depth = (int) params[0];
        int width = (int) params[1];
        SimpleSketch cm = new SimpleSketch(ram, depth, width);
        //runSynopsisRamBased(cm); // TODO: refactor
    }

    public void runHydra(long ram, RamToPar rtp) throws IOException {
        double[] params = rtp.getParamsHydra(ram);
        int depthRoot = (int) params[0];
        int widthRoot = (int) params[1];
        int depthCM = (int) params[2];
        int widthCM = (int) params[3];
        //Hydra hydra = new Hydra(ram, depthRoot, widthRoot, depthCM, widthCM);

        ImpHydraStruct hydra = new ImpHydraStruct(ram, depthRoot, widthRoot, depthCM, widthCM);
        Main.rs = hydra;
        runSynopsisRamBased(hydra); // TODO: refactor
        System.out.println("Number of elements added to sketch: " + hydra.totalAdded);
        System.out.println("Number of elements in stream:" + d.dataset.size());
        System.out.println("Number of indexed attributes: " + Main.numStoredAttributes);
        System.out.println("Theoretical num elements added to sketch: " + d.dataset.size() * (Math.pow(2, Main.numStoredAttributes) - 1));

    }

    public void runSynopsisRamBased(SynopsisRefactor syn) throws IOException {
        int numUpdates = 0;
        long time_passed;
        if (Main.useWarmup) {
            time_passed = runSynWithWarmup(syn);
            numUpdates = d.dataset.size() - Main.warmupNumber;
        } else {
            runSynWithoutWarmup(syn);
            time_passed  = numUpdates = d.dataset.size();
        }
        Main.logger.info("Time passed for updates synopsis " + syn.setting + " is: " + time_passed + " ms, average: " + (double) time_passed / numUpdates + " ms");
        Main.logger.info("Memory usage synopsis " + syn.setting + ": " + syn.getMemoryUsage());
        Main.logger.info("Memory usage dataset: " + d.getMemoryUsage());

        System.out.println("Time passed for updates synopsis " + syn.setting + " is: " + time_passed + " ms, average: " + (double) time_passed / numUpdates + " ms");
        System.out.println("Memory usage synopsis " + syn.setting + ": " + syn.getMemoryUsage());
        System.out.println("Memory usage dataset: " + d.getMemoryUsage());
        System.out.println("\n");
        AnalysisBaselinesRefactor ab = new AnalysisBaselinesRefactor(syn, cd, h, time_passed);
        ab.run();
        syn.reset();
        //ConditionChecks.run(d, s);
    }

    private long runSynWithoutWarmup(SynopsisRefactor syn) {
        long time_passed = 0;
        System.out.println("Running synopsis");
        int numUpdates = 0;
        long startTime = System.currentTimeMillis();

        for (long[] r: cd.dataset) {
            if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
                Main.logger.info("Number of updates: " + numUpdates);
                System.out.println("Number of updates: " + numUpdates);
            }
            syn.add(r);
            numUpdates++;
        }

        // Stop the timer
        long endTime = System.currentTimeMillis();
        time_passed = endTime - startTime;
        return time_passed;
    }

    private long runSynWithWarmup(SynopsisRefactor syn) {
        int numUpdates = 0;
        long time_passed = 0;

        System.out.println("Running synopsis");
        for (long[] r: cd.warmupDataset) {
            if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            //System.out.println("Adding record " + i);
            //System.out.println(d.getOrderedDataset().get(i));
            syn.add(r);
            numUpdates++;
        }
        // Start the timer
        numUpdates = 0;
        long startTime = System.currentTimeMillis();
        for (long[] r: cd.ingestionDataset) {
            if (numUpdates % 1000000== 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            syn.add(r);
            numUpdates++;
        }

        // Stop the timer
        long endTime = System.currentTimeMillis();
        time_passed = endTime - startTime;

        return time_passed;
    }

    public void readDataset(int i) throws IOException, CsvValidationException {
        if (Main.datasetName.equals("SNMP")) {
            Main.fileStartCondition = conditions[i];
        } else {
            // Convert conditions[i] to int
            Main.numFiles = Integer.parseInt(conditions[i]);
        }

        d = new DatasetRefactor(Main.datasetName, h);
        cd.cleanDataset(d, 1, 1);

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
                    ;
                }
                Main.numAttributes = 24;
            }
            case "CAIDA" -> {
                this.conditions = new String[]{Integer.toString(Main.filesToRead)};//, "4", "5", "6", "7", "8", "9", "10", "11", "12"};
                Main.numAttributes = 11;//10; //actually 7; can be 10;
            }

            // Want to be able to change which attributes are read instead of only count


            //Main.numFiles = 3;//conditions.length;
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

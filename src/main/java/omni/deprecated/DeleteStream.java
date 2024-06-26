package omni.deprecated;

import com.opencsv.exceptions.CsvValidationException;
import omni.*;
import omni.baseline.SimpleSketch;
import omni.hydraRefactor.ImpHydraStruct;
import omni.omniTwoLHS.OmniSketch;

import java.io.IOException;

public class DeleteStream {
    Helper h;
    DatasetRefactor d;
    CleanDataset cd;
    //ExpWorkload ew;
    RamToPar rtp;
    String[] conditions;
    int numNoiseUpdates;

    public DeleteStream(Helper h) {
        this.h = h;
    }

    public void run() throws IOException, CsvValidationException {
        Main.useMultNumAttributes = false;// true;
        cd = new CleanDataset(h);
        readDatasetSettings();
        for (int i = 0; i < conditions.length; i++) {
            if (Main.useMultNumAttributes) {
                for (int j = 2; j < cd.cleanIds.length; j++) {
                    Main.numStoredAttributes = j;
                    readDataset(i);
                    runSyns(0);
                }
            } else {
                Main.numStoredAttributes = cd.cleanIds.length;
                readDataset(i);
                runSyns(0);
            }
        }
    }

    private void runSyns(int i) throws IOException {

        this.rtp = new RamToPar(Main.numStoredAttributes);
        int numPotNeg = cd.datasetNegUpdates.length;
        int[] noiseUpdates = new int[]{numPotNeg/100, numPotNeg/50, numPotNeg/10, numPotNeg/5, numPotNeg/2, numPotNeg};
        for (int j = 0; j < noiseUpdates.length; j++) {
            numNoiseUpdates = noiseUpdates[j];
            System.out.println("Running with " + noiseUpdates[j] + " noise updates");
            cd.setNoiseUpdates(numNoiseUpdates);
            for (long ram : Main.ramVals) {
                System.out.println("Running sketch with ram " + ram);
                runSketch(ram, rtp);

            }

        }

    }

    public void runSketch(long ram, RamToPar rtp) throws IOException {
        int[] paramsTwoSketches = rtp.getParamsSketchKmin(ram/2);
        int[] params = rtp.getParamsSketchKmin(ram);
        Main.depth = 5;// (int) params[0];
        Main.width = (int) params[1];
        Main.maxSize = (int) params[2];
        Main.b = (int) params[3];
        Main.rs = new OmniSketch(ram, paramsTwoSketches);
        SynopsisRefactor negSketch = new OmniSketch(ram,  paramsTwoSketches);
        SynopsisRefactor residuSketch = new OmniSketch(ram, params);

        runSynopsisRamBased(Main.rs, negSketch, residuSketch);
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
        runSynopsisRamBased(hydra, hydra, hydra); // TODO: refactor
        System.out.println("Number of elements added to sketch: " + hydra.totalAdded);
        System.out.println("Number of elements in stream:" + d.dataset.size());
        System.out.println("Number of indexed attributes: " + Main.numStoredAttributes);
        System.out.println("Theoretical num elements added to sketch: " + d.dataset.size() * (Math.pow(2, Main.numStoredAttributes) - 1));

    }

    public void runSynopsisRamBased(SynopsisRefactor posSyn, SynopsisRefactor negSyn, SynopsisRefactor residuSketch) throws IOException {

        long time_passed;
        time_passed  = runSynWithoutWarmup(posSyn, negSyn, residuSketch);

//        System.out.println("Time passed for updates synopsis " + posSyn.setting + " is: " + time_passed + " ms, average: " + (double) time_passed / numUpdates + " ms");
//        System.out.println("Memory usage synopsis " + posSyn.setting + ": " + posSyn.getMemoryUsage());
//        System.out.println("Memory usage dataset: " + d.getMemoryUsage());
//        System.out.println("\n");
        HandlingDeletes hd = new HandlingDeletes(posSyn, negSyn, residuSketch, cd, h, time_passed);
        hd.run();
        posSyn.reset();
        negSyn.reset();
        residuSketch.reset();

        //ConditionChecks.run(d, s);
    }

    private long runSynWithoutWarmup(SynopsisRefactor posSyn, SynopsisRefactor negSyn, SynopsisRefactor residuSketch) {
        long time_passed = 0;
        int numUpdates = 0;
        long startTime = System.currentTimeMillis();


        for (long[] r: cd.noiseUpdates) {
            if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
//                Main.logger.info("Number of updates: " + numUpdates);
                System.out.println("Number of updates: " + numUpdates);
            }
            posSyn.add(r);
            numUpdates++;
        }
        for (long[] r: cd.datasetResidu) {
            if (numUpdates % 1000000 == 0 && !Main.runOnODC) {
//                Main.logger.info("Number of updates: " + numUpdates);
                System.out.println("Number of updates: " + numUpdates);
            }
            posSyn.add(r);
            numUpdates++;
        }

        // Stop the timer
        long endTime = System.currentTimeMillis();
        time_passed = endTime - startTime;
        int numNegUpdates = 0;
        for (long[] r: cd.noiseUpdates) {
            if (numNegUpdates % 1000000 == 0 && !Main.runOnODC) {
                System.out.println("Number of updates to negative sketch: " + numNegUpdates);
            }
            negSyn.add(r);
            numNegUpdates++;
        }
        int numResUpdates = 0;
        for (long[] r: cd.datasetResidu) {
            if (numResUpdates % 1000000== 0 && !Main.runOnODC) {
                System.out.println("Number of updates to residu sketch: " + numResUpdates);
            }
            residuSketch.add(r);
            numResUpdates++;
        }

        return time_passed;
    }

    private long runSynWithWarmup(SynopsisRefactor posSyn, SynopsisRefactor negSyn, SynopsisRefactor residuSketch) {
        int numUpdates = 0;
        long time_passed = 0;

        System.out.println("Running synopsis");
        for (long[] r: cd.warmupDataset) {
            if (numUpdates % 10000000 == 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            //System.out.println("Adding record " + i);
            //System.out.println(d.getOrderedDataset().get(i));
            posSyn.add(r);
            numUpdates++;
        }
        // Start the timer
        numUpdates = 0;
        long startTime = System.currentTimeMillis();
        for (long[] r: cd.ingestionDataset) {
            if (numUpdates % 10000000== 0 && !Main.runOnODC) {
                System.out.println("Number of updates: " + numUpdates);
            }
            posSyn.add(r);
            numUpdates++;
        }

        // Stop the timer
        long endTime = System.currentTimeMillis();
        time_passed = endTime - startTime;
        int numNegUpdates = 0;
        for (long[] r: cd.datasetNegUpdates) {
            if (numNegUpdates % 10000000== 0 && !Main.runOnODC) {
                System.out.println("Number of updates to negative sketch: " + numNegUpdates);
            }
            negSyn.add(r);
            numNegUpdates++;
        }

        int numResUpdates = 0;
        for (long[] r: cd.datasetResidu) {
            if (numResUpdates % 1000000== 0 && !Main.runOnODC) {
                System.out.println("Number of updates to residu sketch: " + numResUpdates);
            }
            residuSketch.add(r);
            numResUpdates++;
        }

        return time_passed;
    }

    public void readDataset(int condition) throws IOException, CsvValidationException {
        if (Main.datasetName.equals("SNMP")) {
            Main.fileStartCondition = conditions[condition];
        } else {
            // Convert conditions[i] to int
            Main.numFiles = Integer.parseInt(conditions[condition]);
        }

        d = new DatasetRefactor(Main.datasetName, h);
        cd.cleanDataset(d, 0.95, 0.95);

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

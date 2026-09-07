package omni.Experiments.baselines;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.chowMin.ChowMinAllPairs;

import java.io.IOException;
import java.util.Arrays;

public class ChowMinExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        int[] params = rtp.getParamsChowMin(ram);
        System.out.println("Running chowMin with parameters: " + Arrays.toString(params));

        double[][] estimatedMIs = estimateMIs(runExperiments, config, ram, params);
        ChowMinAllPairs cm = new ChowMinAllPairs(ram, config.numStoredAttributes, params);
        cm.setMIs(estimatedMIs, true);

        cm.printParams();
//        aSH ash = new aSH(ram, config.useNoCast, config.numStoredAttributes, params, repetition, config.withDeletes, config.bufferASH);
        runExperiments.runSynopsisRamBased(cm, repetition);
    }


    public static double[][] estimateMIs(RunExperiments runExperiments, Config config, long ram, int[] params) {

        ChowMinAllPairs ChowLiuMinAllPairs = new ChowMinAllPairs(ram, config.numStoredAttributes, params);
        long time_passed = runExperiments.ingestData(ChowLiuMinAllPairs);
        return ChowLiuMinAllPairs.estimateMIs();
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

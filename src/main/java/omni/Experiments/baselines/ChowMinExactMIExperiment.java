package omni.Experiments.baselines;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.chowMin.ChowMinAllPairs;

import java.io.IOException;
import java.util.Arrays;

import static omni.synopses.chowMin.ChowLiu.computeExactMIsFromDisk;

public class ChowMinExactMIExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        int[] params = rtp.getParamsChowMin(ram);
        System.out.println("Running chowMin with parameters: " + Arrays.toString(params));
        double[][] MIs = computeExactMIsFromDisk(runExperiments, config,config.numStoredAttributes);
        ChowMinAllPairs cm = new ChowMinAllPairs(ram, config.numStoredAttributes, params);
        cm.setMIs(MIs, false);

        cm.printParams();
        runExperiments.runSynopsisRamBased(cm, repetition);
    }


    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

package omni.Experiments;

import omni.Config;
import omni.synopses.baselines.aSH.aSH;
import omni.Experiments.parameterSetting.RamToPar;

import java.io.IOException;
import java.util.Arrays;

public class aSHExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        int[] params = rtp.getParamsAdapSampling(ram, config.bufferASH);
        System.out.println("Running Adaptive S+H with parameters: " + Arrays.toString(params));
        aSH ash = new aSH(ram, config.numStoredAttributes, params, repetition, config.withDeletes, config.bufferASH);
        ash.printParams();
        runExperiments.runSynopsisRamBased(ash, repetition);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

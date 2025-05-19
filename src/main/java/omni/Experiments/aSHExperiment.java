package omni.Experiments;

import omni.Config;
import omni.aSH.aSH;
import omni.parameterSetting.RamToPar;

import java.io.IOException;
import java.util.Arrays;

public class aSHExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        int[] params = rtp.getParamsAdapSampling(ram, config.betaBuffer);
        System.out.println("Running Adaptive S+H with parameters: " + Arrays.toString(params));
        aSH ash = new aSH(ram, config.numStoredAttributes, params, repetition, config.withDeletes, config.betaBuffer);
        ash.printParams();
        runExperiments.runSynopsisRamBased(ash, repetition);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

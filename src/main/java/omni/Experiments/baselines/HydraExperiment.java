package omni.Experiments.baselines;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.baselines.aSH.aSH;
import omni.synopses.baselines.hydraRefactor.ImpHydraStruct;

import java.io.IOException;
import java.util.Arrays;

public class HydraExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {

        int[] params = rtp.getParamsHydra(ram);
        System.out.println("Running Hydra with parameters: " + Arrays.toString(params));
        ImpHydraStruct hydra = new ImpHydraStruct(ram, config.numStoredAttributes, params, repetition);
        hydra.printParams();
        runExperiments.runSynopsisRamBased(hydra, repetition);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

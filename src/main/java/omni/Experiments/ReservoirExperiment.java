package omni.Experiments;

import omni.Config;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.baselines.resSample.ReservoirSample;

import java.io.IOException;
import java.util.Arrays;

public class ReservoirExperiment implements Experiment {
    private RunExperiments runExperiments;
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        int[] params = rtp.getParamsReservoirSampling(ram);
        System.out.println("Running Reservoir with parameters: " + Arrays.toString(params));
        ReservoirSample rs = new ReservoirSample(ram, config.numStoredAttributes, params, repetition);
        rs.printParams();
        long synMem = runExperiments.runSynopsisRamBased(rs, repetition);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

package omni.Experiments;

import omni.Config;
import omni.omniFactory.OmniSketchBuilder;
import omni.parameterSetting.RamToPar;

import java.io.IOException;

public class OmniVLDBSampleSizeExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniVLDBSampleSizeExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.QFirstSampleLater();
    }
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        int sampleSize = rtp.getParamsReservoirSampling(ram)[0];
        rtp.computeOmniSketchParametersFromSampleSize(config.d, config.b, config.parFactor);
        int[] params = rtp.getParamsOmniSketchKminFromSampleSize(sampleSize);
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

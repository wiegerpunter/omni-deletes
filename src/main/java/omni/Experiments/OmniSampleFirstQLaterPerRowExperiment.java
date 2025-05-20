package omni.Experiments;

import omni.Config;
import omni.synopses.omniFactory.OmniSketchBuilder;
import omni.Experiments.parameterSetting.RamToPar;

import java.io.IOException;

public class OmniSampleFirstQLaterPerRowExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniSampleFirstQLaterPerRowExperiment() {
        omniSketchBuilder = OmniSketchBuilder.sampleFirstQLaterPerRow();
    }

    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        rtp.computeOmniSketchParametersFromRAM(config.d, config.b, config.parFactor);
        int[] params = rtp.getParamsOmniSketchRef(ram);
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

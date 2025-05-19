package omni.Experiments;

import omni.Config;
import omni.parameterSetting.RamToPar;
import omni.omniFactory.OmniSketchBuilder;
import java.io.IOException;

public class OmniVLDBExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniVLDBExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.QFirstSampleLater();
    }

    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        rtp.computeOmniSketchParametersFromRAM(config.d, config.b, config.parFactor);
        int[] params = rtp.getParamsOmniSketchKmin(ram);
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

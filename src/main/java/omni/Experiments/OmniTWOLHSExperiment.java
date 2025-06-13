package omni.Experiments;

import omni.Config;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.omniFactory.OmniSketchBuilder;
import java.io.IOException;

public class OmniTWOLHSExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniTWOLHSExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.TWOLHS();
    }

    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        rtp.computeOmniSketchParametersFromRAM(config.d, config.b, config.parFactor);
        int[] params = rtp.getParamsOmniSketch2LHS(ram);
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

package omni.Experiments.custom;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.Main;
import omni.synopses.omniFactory.OmniSketchBuilder;

import java.io.IOException;

public class OmniTWOLHSCustomExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniTWOLHSCustomExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.TWOLHS();
    }

    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        rtp.computeOmniSketchParametersFromRAM(config.d, config.b, config.parFactor);
        int[] params = new int[]{config.d, config.w, config.B};
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

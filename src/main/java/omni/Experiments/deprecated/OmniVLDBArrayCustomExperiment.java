package omni.Experiments.deprecated;

import omni.Config;
import omni.Experiments.Experiment;
import omni.Experiments.RunExperiments;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.omniFactory.OmniSketchBuilder;

import java.io.IOException;

public class OmniVLDBArrayCustomExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniVLDBArrayCustomExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.QFirstSampleLaterArray();
    }

    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        int[] params = new int[]{config.d, config.w, config.B, config.b};
        RunExperiments.omniExperiment(ram, repetition, config, params, omniSketchBuilder, runExperiments);
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

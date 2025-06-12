package omni.Experiments;

import omni.Config;
import omni.Experiments.parameterSetting.RamToPar;
import omni.synopses.omniFactory.OmniSketchBuilder;

import java.io.IOException;

public class OmniSFQLOptimizedOnlyNewCustomExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniSFQLOptimizedOnlyNewCustomExperiment() {
        this.omniSketchBuilder = OmniSketchBuilder.QFirstSampleLaterOptimizedOnlyNew();
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

package omni.Experiments;

import omni.Config;
//import omni.omniPQPrimitive.OmniSketch;
//import omni.omniPQPrimitive.OmniSketchBuilder;
import omni.omniFactory.OmniSketch;
import omni.omniFactory.OmniSketchBuilder;
import omni.parameterSetting.RamToPar;

import java.io.IOException;
import java.util.Arrays;

public class OmniSampleFirstQLaterExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniSampleFirstQLaterExperiment() {
        omniSketchBuilder = OmniSketchBuilder.sampleFirstQLater();
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

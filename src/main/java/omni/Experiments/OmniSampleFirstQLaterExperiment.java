package omni.Experiments;

import omni.Config;
import omni.omniPQPrimitive.OmniSketch;
import omni.omniPQPrimitive.OmniSketchBuilder;
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
        System.out.println("Running OmniSketch with parameters: " + Arrays.toString(params));

        OmniSketch omniSketch = omniSketchBuilder
                .setRam(ram)
                .setNumStoredAttributes(config.numStoredAttributes)
                .setParams(params)
                .setSeed(repetition)
                .build();

        omniSketch.printParams();
        long synMem = runExperiments.runSynopsisRamBased(omniSketch, repetition);
        omniSketch.reset();
        System.gc();
    }

    @Override
    public void setRunExperiments(RunExperiments runExperiments) {
        this.runExperiments = runExperiments;
    }
}

package omni.Experiments;

import omni.Config;
import omni.omniPQPrimitive.OmniSketch;
import omni.omniPQPrimitive.OmniSketchBuilder;
import omni.parameterSetting.RamToPar;

import java.io.IOException;
import java.util.Arrays;

public class OmniSFQLSampleSizeExperiment implements Experiment {
    private RunExperiments runExperiments;
    private final OmniSketchBuilder omniSketchBuilder;
    public OmniSFQLSampleSizeExperiment() {
        omniSketchBuilder = OmniSketchBuilder.sampleFirstQLater();
    }
    @Override
    public void run(long ram, RamToPar rtp, int repetition, Config config) throws IOException {
        int sampleSize = rtp.getParamsReservoirSampling(ram)[0];
        rtp.computeOmniSketchParametersFromSampleSize(config.d, config.b, config.parFactor);
        int[] params = rtp.getParamsOmniSketchRefFromSampleSize(sampleSize);
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

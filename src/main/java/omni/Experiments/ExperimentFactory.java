package omni.Experiments;

import omni.omniPQPrimitive.OmniSketchBuilder;

import java.util.HashMap;
import java.util.Map;

public class ExperimentFactory {
    private static final Map<String, Experiment> experiments = new HashMap<>();

    static {
        experiments.put("ReservoirSampling", new ReservoirExperiment());
        // Add other experiments here
        experiments.put("OmniSketchSampleFirstQLater", new OmniSampleFirstQLaterExperiment());
        experiments.put("OmniSketchVLDB", new OmniVLDBExperiment());
        experiments.put("OmniSketchSampleFirstQLaterWithSampleSize", new OmniSFQLSampleSizeExperiment());
    }

    public static Experiment getExperiment(String experimentName) {
        return experiments.getOrDefault(experimentName, null);
    }
}
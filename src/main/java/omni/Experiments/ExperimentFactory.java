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
        experiments.put("OmniSketchSampleFirstQLaterPerRow", new OmniSampleFirstQLaterPerRowExperiment());
        experiments.put("OmniSketchVLDB", new OmniVLDBExperiment());

        // Sample size as input
        experiments.put("OmniSketchSFQLSampleSize", new OmniSFQLSampleSizeExperiment());
        experiments.put("OmniSketchSFQLSampleSizePerRow", new OmniSFQLSampleSizePerRowExperiment());
        experiments.put("OmniSketchSFQLSampleSizeTestHashSet", new OmniSFQLSampleSizeTestHashSetExperiment());
        experiments.put("OmniSketchVLDBSampleSize", new OmniVLDBSampleSizeExperiment());

    }

    public static Experiment getExperiment(String experimentName) {
        return experiments.getOrDefault(experimentName, null);
    }
}
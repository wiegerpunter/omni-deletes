package omni.Experiments;
import java.util.HashMap;
import java.util.Map;

public class ExperimentFactory {
    private static final Map<String, Experiment> experiments = new HashMap<>();

    static {
        experiments.put("ReservoirSampling", new ReservoirExperiment());
        experiments.put("aSH", new aSHExperiment());

        // OmniSketch based on RAM values
        experiments.put("OmniSketchSampleFirstQLater", new OmniSampleFirstQLaterExperiment());
        experiments.put("OmniSketchSampleFirstQLaterPerRow", new OmniSampleFirstQLaterPerRowExperiment());
        experiments.put("OmniSketchVLDB", new OmniVLDBExperiment());
        experiments.put("OmniSketchSFQLOptimized", new OmniSFQLOptimizedExperiment());

        // Sample size as input
        experiments.put("OmniSketchSFQLSampleSize", new OmniSFQLSampleSizeExperiment());
        experiments.put("OmniSketchSFQLSampleSizePerRow", new OmniSFQLSampleSizePerRowExperiment());
        experiments.put("OmniSketchSFQLSampleSizeTestHashSet", new OmniSFQLSampleSizeTestHashSetExperiment());
        experiments.put("OmniSketchVLDBSampleSize", new OmniVLDBSampleSizeExperiment());

        // OmniSketch based on custom parameters;
        experiments.put("OmniSketchVLDBCustom", new OmniVLDBCustomExperiment());

    }

    public static Experiment getExperiment(String experimentName) {
        return experiments.getOrDefault(experimentName, null);
    }
}
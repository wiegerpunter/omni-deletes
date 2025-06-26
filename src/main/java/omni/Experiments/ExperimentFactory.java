package omni.Experiments;
import omni.Experiments.baselines.ReservoirExperiment;
import omni.Experiments.baselines.aSHExperiment;
import omni.Experiments.custom.*;
import omni.Experiments.deprecated.*;

import java.util.HashMap;
import java.util.Map;

public class ExperimentFactory {
    private static final Map<String, Experiment> experiments = new HashMap<>();

    static {

        // OmniSketch based on RAM values
        experiments.put("OmniSketchSampleFirstQLater", new OmniSampleFirstQLaterExperiment());
        experiments.put("OmniSketchSampleFirstQLaterPerRow", new OmniSampleFirstQLaterPerRowExperiment());
        experiments.put("OmniSketchVLDB", new OmniVLDBExperiment());
        experiments.put("OmniSketchVLDBArray", new OmniVLDBArrayExperiment());
        experiments.put("OmniSketchVLDBArrayWithBuffer", new OmniVLDBArrayWithBufferExperiment());
        experiments.put("OmniSketchVLDBArrayWithoutBuffer", new OmniVLDBArrayWithoutBufferExperiment());
        experiments.put("OmniSketchVLDBTreeSet", new OmniVLDBTreeSetExperiment());
        experiments.put("OmniSketchVLDBTreeSetWithoutBuffer", new OmniVLDBTreeSetWithoutBufferExperiment());



        experiments.put("OmniSketchSFQLOptimized", new OmniSFQLOptimizedExperiment());
        experiments.put("OmniSketchSFQLOptimizedOnlyNew", new OmniSFQLOptimizedOnlyNewExperiment());
        experiments.put("OmniSketchTWOLHS", new OmniTWOLHSExperiment());
        experiments.put("OmniSketchFastTWOLHS", new OmniFastTWOLHSExperiment());

        // Sample size as input
        experiments.put("OmniSketchSFQLSampleSize", new OmniSFQLSampleSizeExperiment());
        experiments.put("OmniSketchSFQLSampleSizePerRow", new OmniSFQLSampleSizePerRowExperiment());
        experiments.put("OmniSketchSFQLSampleSizeTestHashSet", new OmniSFQLSampleSizeTestHashSetExperiment());
        experiments.put("OmniSketchVLDBSampleSize", new OmniVLDBSampleSizeExperiment());

        // OmniSketch based on custom parameters;
        experiments.put("OmniSketchVLDBCustom", new OmniVLDBCustomExperiment());
        experiments.put("OmniSketchVLDBArrayCustom", new OmniVLDBArrayCustomExperiment());
        experiments.put("OmniSketchVLDBArrayRegBufferCustom", new OmniVLDBArrayRegBufferCustomExperiment());
        experiments.put("OmniSketchVLDBArrayWithBufferCustom", new OmniVLDBArrayWithBufferCustomExperiment());
        experiments.put("OmniSketchVLDBArrayWithoutBufferCustom", new OmniVLDBArrayWithoutBufferCustomExperiment());
        experiments.put("OmniSketchVLDBTreeSetCustom", new OmniVLDBTreeSetCustomExperiment());
        experiments.put("OmniSketchVLDBTreeSetWithoutBufferCustom", new OmniVLDBTreeSetWithoutBufferCustomExperiment());
        experiments.put("OmniSketchVLDBSimpleBufferCustom", new OmniVLDBSimpleBufferCustomExperiment());
        experiments.put("OmniSketchVLDBSimpleBufferNoDeleteBufferCustom", new OmniVLDBSimpleBufferNoDeleteBufferCustomExperiment());
        experiments.put("OmniSketchTWOLHSCustom", new OmniTWOLHSCustomExperiment());
        experiments.put("OmniSketchFastTWOLHSCustom", new OmniFastTWOLHSCustomExperiment());




        experiments.put("OmniSketchSFQLOptimizedCustom", new OmniSFQLOptimizedCustomExperiment());
        experiments.put("OmniSketchSFQLOptimizedOnlyNewCustom", new OmniSFQLOptimizedOnlyNewCustomExperiment());


        experiments.put("ReservoirSampling", new ReservoirExperiment());
        experiments.put("aSH", new aSHExperiment());


    }

    public static Experiment getExperiment(String experimentName) {
        return experiments.getOrDefault(experimentName, null);
    }
}
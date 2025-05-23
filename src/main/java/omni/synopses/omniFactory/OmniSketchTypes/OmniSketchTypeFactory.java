package omni.synopses.omniFactory.OmniSketchTypes;

import omni.synopses.omniFactory.OmniSketchConfig;

public class OmniSketchTypeFactory {
    public static OmniSketchType createOmniSketchType(String type, OmniSketchConfig sketchConfig) {
        return switch (type) {
            case "SampleFirst" -> new OmniSketchTypeSampleFirst(sketchConfig);
            case "SampleFirstPerRow" -> new OmniSketchTypeSampleFirstPerRow(sketchConfig);
            case "SampleFirstTest" -> new OmniSketchTypeSampleFirstTest(sketchConfig);
            case "SampleLater" -> new OmniSketchTypeSampleLater(sketchConfig, "KminPQ");
            case "SampleLaterOptimized" -> new OmniSketchTypeSampleLater(sketchConfig, "KminPQOptimized");
            // Add other sketch types as needed
            default -> throw new IllegalArgumentException("Unsupported attribute sketch type: " + type);
        };
    }
}

package omni.synopses.omniFactory.OmniSketchTypes;

import omni.synopses.omniFactory.OmniSketchConfig;

public class OmniSketchTypeFactory {
    public static OmniSketchType createOmniSketchType(String type, OmniSketchConfig sketchConfig) {
        return switch (type) {
            case "SampleFirst" -> new OmniSketchTypeSampleFirst(sketchConfig);
            case "SampleFirstPerRow" -> new OmniSketchTypeSampleFirstPerRow(sketchConfig);
            case "SampleFirstTest" -> new OmniSketchTypeSampleFirstTest(sketchConfig);
            case "SampleLater" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminPQ");
            case "SampleLaterExact" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminExact");
            case "SampleLaterArray" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArray");
            case "SampleLaterArrayRegBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayRegBuffer");

            case "SampleLaterArrayWithBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithBuffer");
            case "SampleLaterArrayWithBufferOpt" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithBufferOpt");
            case "SampleLaterArrayWithBufferOptBatchDeletes" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithBufferOptBatchDeletes");


            case "SampleLaterArrayWithBufferPessDeleteCount" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithBufferPessDeleteCount");

            case "SampleLaterArrayWithBufferOnlyValid" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithBufferOnlyValid");

            case "SampleLaterTreeSet" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminTreeSet");
            case "SampleLaterTreeSetWithoutBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminTreeSetWithoutBuffer");
            case "SampleLaterArrayWithoutBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminArrayWithoutBuffer");


            case "SampleLaterSimpleBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminSimpleBuffer");
            case "SampleLaterSimpleBufferNoDeleteBuffer" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminSimpleBufferNoDeleteBuffer");

            case "FastTWOLHS" -> new OmniSketchTypeSampleLaterTWOLHS(sketchConfig, "FastTWOLHS");
            case "FastTWOLHSOneBucket" -> new OmniSketchTypeSampleLaterTWOLHS(sketchConfig, "FastTWOLHSOneBucket");

            case "SlowTWOLHS" -> new OmniSketchTypeSampleLaterTWOLHS(sketchConfig, "SlowTWOLHS");
            case "NMaxTWOLHS" -> new OmniSketchTypeSampleLaterTWOLHS(sketchConfig, "NMaxTWOLHS");
            case "TWOLHSExact" -> new OmniSketchTypeSampleLaterTWOLHS(sketchConfig, "TWOLHSExact");



            case "SampleLaterOptimized" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminPQOptimized");
            case "SampleLaterOptimizedOnlyNew" -> new OmniSketchTypeSampleLaterKmin(sketchConfig, "KminPQOptimizedOnlyNew");

            // Add other sketch types as needed
            default -> throw new IllegalArgumentException("Unsupported attribute sketch type: " + type);
        };
    }
}

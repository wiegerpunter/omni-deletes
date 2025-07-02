package omni.synopses.omniFactory.SampleTypes;

import omni.Config;
import omni.synopses.omniFactory.OmniSketchConfig;

public class SampleFactory {
    public static Sample createSample(String kminType, int B, int b,OmniSketchConfig config) {
        return switch (kminType) {
            case "KminPQ" -> new KminPQ(B, b, "KminPQ");
            case "KminPQOptimized" -> new KminPQ(B, b, "KminPQOptimized");
            case "KminPQOptimizedOnlyNew" -> new KminPQ(B, b, "KminPQOptimizedOnlyNew");
            case "KminTreeSet" -> new KminTreeSet(B, b, config.getBufferBeta(), "KminTreeSet");
            case "KminTreeSetWithoutBuffer" -> new KminTreeSet(B, b, 0, "KminTreeSetWithoutBuffer");
            case "TWOLHS" -> new TWOLHS(B, config.getSeed());
            case "KminArray" -> new KminArray(B, b,0, "KminArray");
            case "KminArrayRegBuffer" -> new KminArray(B, b, config.getBufferBeta(), "KminArrayRegBuffer");

            case "KminArrayWithBuffer" -> new KminCustomArray(B, b, config.getBufferBeta(), "KminArrayWithBuffer", false, false);
            case "KminArrayWithBufferOnlyValid" -> new KminCustomArray(B, b, config.getBufferBeta(), "KminArrayWithBuffer", true, false);
            case "KminArrayWithoutBuffer" -> new KminCustomArray(B, b, b,"KminArrayWithoutBuffer", false, false);
            case "KminArrayWithBufferPessDeleteCount" -> new KminCustomArray(B, b, 0,"KminArrayWithBuffer", false, false);

            case "KminSimpleBuffer" -> new KminSimpleBuffer(B, b, config.getBufferBeta(), "KminSimpleBuffer");
            case "KminSimpleBufferNoDeleteBuffer" -> new KminSimpleBuffer(B, b, 0, "KminSimpleBufferNoDeleteBuffer");



            default -> throw new IllegalArgumentException("Unknown Kmin type: " + kminType);
        };
    }
}

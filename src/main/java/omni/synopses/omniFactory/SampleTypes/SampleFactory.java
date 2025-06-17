package omni.synopses.omniFactory.SampleTypes;

import omni.Config;
import omni.synopses.omniFactory.OmniSketchConfig;

public class SampleFactory {
    public static Sample createSample(String kminType, int B, int b,OmniSketchConfig config) {
        return switch (kminType) {
            case "KminPQ" -> new KminPQ(B, b, "KminPQ");
            case "KminPQOptimized" -> new KminPQ(B, b, "KminPQOptimized");
            case "KminPQOptimizedOnlyNew" -> new KminPQ(B, b, "KminPQOptimizedOnlyNew");
            case "KminTreeSet" -> new KminTreeSet(B, b);
            case "TWOLHS" -> new TWOLHS(B, config.getSeed());
            case "KminArray" -> new KminArray(B, b, "KminArray");
            case "KminArrayWithBuffer" -> new KminCustomArray(B, b, config.getBufferBeta(), "KminArrayWithBuffer");

            default -> throw new IllegalArgumentException("Unknown Kmin type: " + kminType);
        };
    }
}

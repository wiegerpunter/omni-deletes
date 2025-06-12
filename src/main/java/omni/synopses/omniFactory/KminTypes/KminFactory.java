package omni.synopses.omniFactory.KminTypes;

public class KminFactory {
    public static Kmin createKmin(String kminType, int width, int B, int b) {
        return switch (kminType) {
            case "KminPQ" -> new KminPQ(B, b, "KminPQ");
            case "KminPQOptimized" -> new KminPQ(B, b, "KminPQOptimized");
            case "KminPQOptimizedOnlyNew" -> new KminPQ(B, b, "KminPQOptimizedOnlyNew");
            case "KminTreeSet" -> new KminTreeSet(B, b);
            default -> throw new IllegalArgumentException("Unknown Kmin type: " + kminType);
        };
    }
}

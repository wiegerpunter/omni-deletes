package omni.synopses.omniFactory.KminTypes;

public class KminFactory {
    public static Kmin createKmin(String kminType, int width, int B, int b) {
        return switch (kminType) {
            case "KminPQ" -> new KminPQ(B, b);
            case "KminTreeSet" -> new KminTreeSet(B, b);
            default -> throw new IllegalArgumentException("Unknown Kmin type: " + kminType);
        };
    }
}

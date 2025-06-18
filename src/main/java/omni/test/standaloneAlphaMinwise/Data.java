package omni.test.standaloneAlphaMinwise;

public class Data {
    public int[] intersection;
    public int[][] noise;
    public int numberOfSets;
    public Data(int[] intersection, int[][] noise) {
        this.intersection = intersection;
        this.noise = noise;
        this.numberOfSets = noise.length;
    }
}

package omni.test.standaloneAlphaMinwise;

public class Data {
    public int[] intersection;
    public int[][] noise;
    public int numberOfSets;
    private int intersectionSize;
    private int noiseSize;
    private int unionSize;
    public Data(int[] intersection, int[][] noise) {
        this.intersection = intersection;
        this.noise = noise;
        this.numberOfSets = noise.length;
    }

    public int getIntersectionSize() {
        return intersectionSize;
    }
    public void setIntersectionSize(int intersectionSize) {
        this.intersectionSize = intersectionSize;
    }
    public int getNoiseSize() {
        return noiseSize;
    }
    public void setNoiseSize(int noiseSize) {
        this.noiseSize = noiseSize;
    }
    public int getUnionSize() {
        return unionSize;
    }
    public void setUnionSize(int unionSize) {
        this.unionSize = unionSize;
    }
}

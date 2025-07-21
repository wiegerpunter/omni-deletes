package omni.datasets.stringData;

import omni.Config;

public class StringDataset {
    private Config config;

    public StringDataset(Config config) {
        this.config = config;
        int numQueries = config.numQueries;
    }

    public void loader(double perc, double sizeFactor, int noiseSize) {
    }

}

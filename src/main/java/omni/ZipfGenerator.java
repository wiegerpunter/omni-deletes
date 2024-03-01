package omni;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.Well19937c;

public class ZipfGenerator {
    public static long[][] zipfData(int numberOfRecords,int numberOfAttributes, int maxValue, double alpha) {
        long[][] data = new long[numberOfRecords][numberOfAttributes];
        Well19937c random = new Well19937c(1);
        ZipfDistribution zipf = new ZipfDistribution(random, maxValue, alpha);
        for (int i=0;i<numberOfRecords;i++) for (int j=0;j<numberOfAttributes;j++) data[i][j] = zipf.sample();
        return data;
    }
}

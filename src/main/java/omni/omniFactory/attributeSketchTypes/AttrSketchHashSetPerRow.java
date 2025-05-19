package omni.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.omniFactory.OmniSketchConfig;
import omni.omniFactory.utils.HashUtils;

import java.util.HashSet;

public class AttrSketchHashSetPerRow {
    HashSet<Integer>[] sketch;
    int[] sketchIndex;

    HashFunction xx;

    OmniSketchConfig sketchConfig;
    final int width;
    final int sampleSize;
    int reusableHashes;

    public AttrSketchHashSetPerRow(OmniSketchConfig sketchConfig, int row, int width, int sampleSize) {
        this.sketchConfig = sketchConfig;
        this.width = width;
        this.sampleSize = sampleSize;
        this.sketchIndex = new int[sampleSize];
        this.sketch = new HashSet[width];
        for (int j = 0; j < width; j++) {
            sketch[j] = new HashSet<>(2* sampleSize/width);
        }

        xx = Hashing.murmur3_32_fixed(row + sketchConfig.getSeed());
    }

    private int hash(long attrValue) {
        return HashUtils.computeHashes(xx, attrValue, width);
    }

    public void ingest(long attrValue, int sampleToReplace, int sign) {
        reusableHashes = hash(attrValue);
        if (sign == 1) {
            int oldHash = sketchIndex[sampleToReplace];
            if (oldHash != reusableHashes) {
                sketch[oldHash].remove(sampleToReplace);
                sketch[reusableHashes].add(sampleToReplace);
                sketchIndex[sampleToReplace] = reusableHashes;
            }
        } else {
            sketch[reusableHashes].remove(sampleToReplace);
        }
    }

    public HashSet<Integer> query(long attrValue) {
        return sketch[hash(attrValue)];
    }

    public long getMemoryFootprint() {
        long memoryUsage = 0;
        double sizeSigSample = Math.log(sampleSize) / Math.log(2);
        for (int i = 0; i < width; i++) {
            //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
            memoryUsage += (long) (sketch[i].size() * sizeSigSample);
        }
        memoryUsage += (long) (sketchIndex.length * Math.log(width)/Math.log(2));
        return memoryUsage;
    }

    public void reset() {
        for (int i = 0; i < width; i++) {
            sketch[i].clear();
        }
        sketchIndex = new int[sampleSize];
    }


}

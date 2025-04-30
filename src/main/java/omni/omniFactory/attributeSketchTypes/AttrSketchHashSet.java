package omni.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.omniFactory.OmniSketchConfig;
import omni.omniFactory.utils.HashUtils;

import java.util.HashSet;

public class AttrSketchHashSet {
    HashSet<Integer>[][] sketch;
    int[][] sketchIndex;

    HashFunction[] xx;

    OmniSketchConfig sketchConfig;
    final int depth;
    final int width;
    final int sampleSize;
    int[] reusableHashes;

    public AttrSketchHashSet(OmniSketchConfig sketchConfig, int depth, int width, int sampleSize) {
        this.sketchConfig = sketchConfig;
        this.depth = depth;
        this.reusableHashes = new int[depth];
        this.width = width;
        this.sampleSize = sampleSize;
        this.sketchIndex = new int[sampleSize][depth];
        this.sketch = new HashSet[depth][width];
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                sketch[i][j] = new HashSet<>(sampleSize/width);
            }
        }

        xx = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            xx[i] = Hashing.murmur3_32_fixed(i + sketchConfig.getSeed());
        }
    }

    private void hash(long attrValue) {
        HashUtils.computeHashes(xx, attrValue, depth, width, reusableHashes);
    }

    public void ingest(long attrValue, int sampleToReplace, int sign) {
        hash(attrValue);
        for (int j = 0; j < depth; j++) {
            int w = reusableHashes[j];
            if (sign == 1) {
                int oldHash = sketchIndex[sampleToReplace][j];
                if (oldHash != w) {
                    sketch[j][oldHash].remove(sampleToReplace);
                    sketch[j][w].add(sampleToReplace);
                    sketchIndex[sampleToReplace][j] = w;
                }
            } else {
                sketch[j][w].remove(sampleToReplace);
            }
        }
    }

    public HashSet<Integer>[] query(long attrValue) {
        hash(attrValue);
        HashSet<Integer>[] result = new HashSet[depth];
        for (int j = 0; j < depth; j++) {
            int w = reusableHashes[j];
            result[j] = sketch[j][w];
        }
        return result;
    }

    public long getMemoryFootprint() {
        long memoryUsage = 0;
        double sizeSigSample = Math.log(sampleSize) / Math.log(2);
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                //System.out.println("CM[" + j + "][" + i + "].curSampleSize = " + CM.get(j).get(i).curSampleSize);
                memoryUsage += (long) (sketch[j][i].size() * sizeSigSample);
            }
        }
        memoryUsage += (long) (sketchIndex.length * depth * Math.log(width)/Math.log(2));
        return memoryUsage;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                sketch[j][i].clear();
            }
        }
        sketchIndex = new int[sampleSize][depth];
    }


}

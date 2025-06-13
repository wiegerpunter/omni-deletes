package omni.synopses.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.SampleFactory;
import omni.synopses.omniFactory.utils.HashUtils;

public class AttrSketchTWOLHS {
    HashFunction[] attrHashFunctions;

    Sample[][][] sketch;
    final int depth;
    final int width;
    final int numTWOLHSRepetitions;
    int[] reusableHashes;

    public AttrSketchTWOLHS(OmniSketchConfig config, int depth, int width, int numTWOLHSRepetitions, String sampleType) {
        this.depth = depth;
        this.width = width;
        this.numTWOLHSRepetitions = numTWOLHSRepetitions;

        this.sketch = new Sample[depth][width][numTWOLHSRepetitions];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < numTWOLHSRepetitions; k++) {
                    sketch[j][i][k] = SampleFactory.createSample(sampleType, k, 0, config);
                }
            }
        }
    }

    private void attrHash(long attrValue) {
        HashUtils.computeHashes(attrHashFunctions, attrValue, depth, width, reusableHashes);
    }

    public void ingest(long attrValue, int id, int sign) {
        attrHash(attrValue);
        for (int j = 0; j < depth; j++) {
            int i = reusableHashes[j];
            for (int k = 0; k < numTWOLHSRepetitions; k++) {
                sketch[j][i][k].ingest(id, sign);
            }
        }
    }

    public Sample[][] query(long attrValue) {
        attrHash(attrValue);
        Sample[][] result = new Sample[depth][numTWOLHSRepetitions];
        for (int j = 0; j < depth; j++) {
            int i = reusableHashes[j];
            result[j] = sketch[j][i];//.query();
        }
        return result;

    }

    public long getMemoryFootprint() {
        long memoryFootprint = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                memoryFootprint += sketch[j][i].getMemoryFootprint();
            }
        }
        return memoryFootprint;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                sketch[j][i].reset();
            }
        }
    }



}

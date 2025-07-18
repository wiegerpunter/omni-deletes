package omni.synopses.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.SampleFactory;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.utils.HashUtils;

public class AttrSketchKmin {
    HashFunction[] attrHashFunctions;

    Sample[][] sketch;
    final int depth;
    final int width;
    final double beta;
    int[] reusableHashes;

    public AttrSketchKmin(OmniSketchConfig config, int depth, int width, int B, int b, String SampleType) {
        this.depth = depth;
        this.width = width;
        this.beta = config.getBufferBeta();

        this.reusableHashes = new int[depth];
        this.attrHashFunctions = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            attrHashFunctions[i] = Hashing.murmur3_32_fixed(i + config.getSeed());
        }
        this.sketch = new Sample[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                sketch[j][i] = SampleFactory.createSample(SampleType, B, b, config);
            }
        }
    }

    private void attrHash(long attrValue) {
        HashUtils.computeHashes(attrHashFunctions, attrValue, depth, width, reusableHashes);
    }

    public void ingest(long attrValue, int[] hx, int sign) {
        attrHash(attrValue);
        for (int j = 0; j < depth; j++) {
            int i = reusableHashes[j];
            sketch[j][i].ingest(hx[j], sign);
        }
    }

    public Sample[] query(long attrValue) {
        attrHash(attrValue);
        Sample[] result = new Sample[depth];
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

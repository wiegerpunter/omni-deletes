package omni.synopses.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.synopses.omniFactory.KminTypes.Kmin;
import omni.synopses.omniFactory.KminTypes.KminFactory;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.utils.HashUtils;

public class AttrSketchKmin {
    HashFunction[] attrHashFunctions;

    Kmin[][] sketch;
    final int depth;
    final int width;
    final int B;
    final int b;
    int[] reusableHashes;

    public AttrSketchKmin(OmniSketchConfig config, int depth, int width, int B, int b, String KminType) {
        this.depth = depth;
        this.width = width;
        this.B = B;
        this.b = b;


        this.reusableHashes = new int[depth];
        this.attrHashFunctions = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            attrHashFunctions[i] = Hashing.murmur3_32_fixed(i + config.getSeed());
        }
        this.sketch = new Kmin[depth][width];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                sketch[j][i] = KminFactory.createKmin(KminType, width, B, b);
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

    public Kmin[] query(long attrValue) {
        attrHash(attrValue);
        Kmin[] result = new Kmin[depth];
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

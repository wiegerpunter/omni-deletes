package omni.synopses.omniFactory.attributeSketchTypes;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import omni.synopses.omniFactory.OmniSketchBuilder;
import omni.synopses.omniFactory.OmniSketchConfig;
import omni.synopses.omniFactory.SampleTypes.ExactSolution;
import omni.synopses.omniFactory.SampleTypes.Sample;
import omni.synopses.omniFactory.SampleTypes.SampleFactory;
import omni.synopses.omniFactory.utils.HashUtils;

public class AttrSketchTWOLHS {
    HashFunction[] attrHashFunctions;

    Sample[][][] sketch;
    final int depth;
    final int width;
    final int numTWOLHSRepetitions;
    final String sampleType;
    int[] reusableHashes;
    OmniSketchConfig omniSketchConfig;
    HashFunction hashFunctionG;


    private int hashG(long id) {
        // id hash modulo numTWOLHSRepetitions
        return (((hashFunctionG.hashLong(id)).asInt() >>>1) % numTWOLHSRepetitions);
    }

    public AttrSketchTWOLHS(OmniSketchConfig config, int depth, int width, int numTWOLHSRepetitions, String sampleType) {
        this.depth = depth;
        this.width = width;
        this.numTWOLHSRepetitions = numTWOLHSRepetitions;
        this.omniSketchConfig = config;
        this.sampleType = sampleType;
        checkSampleType(sampleType, omniSketchConfig);
        hashFunctionG = Hashing.murmur3_32_fixed(omniSketchConfig.getSeed() + OmniSketchBuilder.G_HASH_OFFSET);

        reusableHashes = new int[depth];
        this.attrHashFunctions = new HashFunction[depth];
        for (int i = 0; i < depth; i++) {
            attrHashFunctions[i] = Hashing.murmur3_32_fixed(i + config.getSeed());
        }

        this.sketch = new Sample[depth][width][numTWOLHSRepetitions];
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                if (sampleType.equals("TWOLHSExact")) { // Only one exact solution needed.
                    sketch[j][i][0] = new ExactSolution();
                    continue;
                }
                for (int k = 0; k < numTWOLHSRepetitions; k++) {
                    sketch[j][i][k] = SampleFactory.createSample(sampleType, k, 0, config);
                }
            }
        }
    }

    private void checkSampleType(String sampleType, OmniSketchConfig omniSketchConfig) {
        if (sampleType.equals("SlowTWOLHS") && omniSketchConfig.getUseFastTWOLHS()) {
            throw new IllegalArgumentException("Cannot use SlowTWOLHS with FastTWOLHS configuration");
        } else if ((sampleType.equals("FastTWOLHS") ||
                sampleType.equals("NMaxTWOLHS") ||
                sampleType.equals("FastTWOLHSOneBucket"))
                && !omniSketchConfig.getUseFastTWOLHS()) {
            throw new IllegalArgumentException("Cannot use FastTWOLHS with SlowTWOLHS configuration");
        }
    }

    private void attrHash(long attrValue) {
        HashUtils.computeHashes(attrHashFunctions, attrValue, depth, width, reusableHashes);
    }

    private void attrHash(Object attrValue) {
        HashUtils.computeHashes(attrHashFunctions, attrValue, depth, width, reusableHashes);
    }

    public void ingest(long attrValue, int id, int sign) {
        attrHash(attrValue);
        for (int j = 0; j < depth; j++) {
            int i = reusableHashes[j];
            if (omniSketchConfig.getUseFastTWOLHS()) {
                int g = hashG(id);
                sketch[j][i][g].ingest(id, sign);
            } else {
                if (sampleType.equals("TWOLHSExact")) { // insert only once.
                    sketch[j][i][0].ingest(id, sign);
                    continue;
                }
                for (int k = 0; k < numTWOLHSRepetitions; k++) {
                    sketch[j][i][k].ingest(id, sign);
                }
            }
        }
    }


    public void ingest(Object attrValue, int id, int sign) {
        attrHash(attrValue);
        for (int j = 0; j < depth; j++) {
            int i = reusableHashes[j];
            if (omniSketchConfig.getUseFastTWOLHS()) {
                int g = hashG(id);
                sketch[j][i][g].ingest(id, sign);
            } else {
                if (sampleType.equals("TWOLHSExact")) { // insert only once.
                    sketch[j][i][0].ingest(id, sign);
                    continue;
                }
                for (int k = 0; k < numTWOLHSRepetitions; k++) {
                    sketch[j][i][k].ingest(id, sign);
                }
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

    public Sample[][] query(Object attrValue) {
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
            for (int k = 0; k < numTWOLHSRepetitions; k++) {
                if (sketch[j][i][k] == null) {
                    continue;
                }
                memoryFootprint += sketch[j][i][k].getMemoryFootprint();
            }
            }
        }
        return memoryFootprint;
    }

    public void reset() {
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < numTWOLHSRepetitions; k++) {
                    if (sketch[j][i][k] == null) {
                        continue;
                    }
                    sketch[j][i][k].reset();
                }
            }
        }
    }



}

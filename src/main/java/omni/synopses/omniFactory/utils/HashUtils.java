package omni.synopses.omniFactory.utils;

import com.google.common.hash.HashFunction;

public class HashUtils {
    public static void computeHashes(HashFunction[] hashFunctions, long attrValue, int depth, int width, int[] hashes) {
        for (int i = 0; i < depth; i++) {
            int hash = (hashFunctions[i].hashLong(attrValue).asInt() % width);
            if (hash < 0) {
                hash += width;
            }
            hashes[i] = hash;
        }
    }

    public static int computeHashes(HashFunction hashFunction, long attrValue, int width) {
        int hash = (hashFunction.hashLong(attrValue).asInt() % width);
        if (hash < 0) {
            hash += width;
        }
        return hash;
    }
}

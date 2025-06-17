package omni.synopses.omniFactory.utils;

import com.google.common.hash.HashFunction;

public class HashUtils {
    public static void computeHashes(HashFunction[] hashFunctions, long attrValue, int depth, int width, int[] hashes) {
        for (int i = 0; i < depth; i++) {
            int hash = hashFunctions[i].hashLong(attrValue).asInt();
            hashes[i] = fastPositiveModulo(hash, width);
//
//            int hash = (hashFunctions[i].hashLong(attrValue).asInt() % width);
//            if (hash < 0) {
//                hash += width;
//            }
//            hashes[i] = hash;
        }
    }


    // Fast modulo for positive results
    private static int fastPositiveModulo(int x, int mod) {
        int r = x & (mod - 1); // works if mod is a power of two
        return mod == (mod & -mod) ? r : (x % mod + mod) % mod;
    }

    public static int computeHashes(HashFunction hashFunction, long attrValue, int width) {
        int hash = (hashFunction.hashLong(attrValue).asInt() % width);
        if (hash < 0) {
            hash += width;
        }
        return hash;
    }
}

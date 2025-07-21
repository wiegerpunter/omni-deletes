package omni.synopses.omniFactory.utils;

import com.google.common.hash.HashFunction;

import java.nio.charset.StandardCharsets;

public class HashUtils {
    public static void computeHashes(HashFunction[] hashFunctions, long attrValue, int depth, int width, int[] hashes) {
        for (int i = 0; i < depth; i++) {
            int hash = hashFunctions[i].hashLong(attrValue).asInt();
            hashes[i] = fastPositiveModulo(hash, width);
        }
    }

    public static void computeHashes(HashFunction[] hashFunctions, Object attrValue, int depth, int width, int[] hashes) {

        if (attrValue instanceof String) {
            for (int i = 0; i < depth; i++) {
                int hash = hashFunctions[i].hashString((String) attrValue, StandardCharsets.UTF_8).asInt();
                hashes[i] = fastPositiveModulo(hash, width);
            }
            return;
        }else if (attrValue instanceof Long) {
            long longValue = (Long) attrValue;
            for (int i = 0; i < depth; i++) {
                int hash = hashFunctions[i].hashLong((longValue)).asInt();
                hashes[i] = fastPositiveModulo(hash, width);
            }
            return;
        }
        throw new IllegalArgumentException("Unsupported attribute type: " + attrValue.getClass().getName());
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

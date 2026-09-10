package com.github.wohaopa.MyCTMLib;

/**
 * High-performance position-based random number generator.
 * Uses a pure hash function optimized for texture selection.
 */
public class FastRandom {

    /**
     * Generates a random index from a world seed and block position.
     * Uses Wang Hash without caching or object allocation.
     */
    public static int getRandomIndex(long worldSeed, int x, int y, int z, int bound) {
        if (bound <= 1) return 0;

        // Mix the world seed into an integer.

        // Hash the block coordinates.
        int hash = Long.hashCode(worldSeed);
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;

        // Apply Wang Hash finalization.
        hash = wangHash(hash);

        // Use rejection sampling to avoid modulo bias.
        int max = (0x7fffffff / bound) * bound;
        while ((hash & 0x7fffffff) >= max) {
            hash = wangHash(hash); // Rehash the rejected value.
        }
        return (hash & 0x7fffffff) % bound;
    }

    /**
     * Wang Hash implementation for high-quality integer hashing.
     */
    private static int wangHash(int key) {
        key = (~key) + (key << 21); // key = (key << 21) - key - 1;
        key = key ^ (key >>> 24);
        key = (key + (key << 3)) + (key << 8); // key * 265
        key = key ^ (key >>> 14);
        key = (key + (key << 2)) + (key << 4); // key * 21
        key = key ^ (key >>> 28);
        key = key + (key << 31);
        return key;
    }
}

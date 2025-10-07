package com.github.wohaopa.MyCTMLib;

/**
 * 基于位置的高性能随机数生成器
 * 使用纯哈希函数，专为纹理显示优化
 */
public class FastRandom {

    /**
     * 根据世界种子和位置生成随机索引
     * 使用Wang Hash算法，无缓存、无对象分配、线程安全
     */
    public static int getRandomIndex(long worldSeed, int x, int y, int z, int bound) {
        if (bound <= 1) return 0;
        
        // 将世界种子混合为int
        int seed = (int)(worldSeed ^ (worldSeed >>> 32));
        
        // 哈希位置坐标
        int hash = seed;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        
        // 应用Wang Hash最终化
        hash = wangHash(hash);
        
        // 拒绝采样避免偏差
        int max = (0x7fffffff / bound) * bound;
        while ((hash & 0x7fffffff) >= max) {
            hash = wangHash(hash); // 重新哈希
        }
        return (hash & 0x7fffffff) % bound;
    }

    /**
     * Wang Hash算法 - 高质量整数哈希
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


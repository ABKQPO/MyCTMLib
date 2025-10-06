package com.github.wohaopa.MyCTMLib;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能随机数生成器，使用XorShift算法
 */
public class FastRandom {
    private long seed;
    
    public FastRandom(long seed) {
        this.seed = seed;
    }
    
    /**
     * 生成下一个随机数
     */
    public long nextLong() {
        seed ^= seed << 21;
        seed ^= seed >>> 35;
        seed ^= seed << 4;
        return seed;
    }
    
    /**
     * 生成指定范围内的随机整数
     */
    public int nextInt(int bound) {
        if (bound <= 0) return 0;
        return (int) (Math.abs(nextLong()) % bound);
    }
    
    /**
     * 生成随机浮点数 [0.0, 1.0)
     */
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }
}

/**
 * 基于位置的随机数缓存管理器
 * 避免重复计算相同位置的随机数
 */
class PositionRandomCache {
    private static final ConcurrentHashMap<Long, Integer> cache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10000; // 最大缓存大小
    
    /**
     * 根据世界种子和位置生成缓存键
     */
    private static long generateCacheKey(long worldSeed, int x, int y, int z) {
        return worldSeed + x * 435L + y * 357L + z * 299L;
    }
    
    /**
     * 获取指定位置的随机索引，带缓存
     */
    public static int getRandomIndex(long worldSeed, int x, int y, int z, int maxIndex) {
        if (maxIndex <= 0) return 0;
        
        long cacheKey = generateCacheKey(worldSeed, x, y, z);
        
        // 尝试从缓存获取
        Integer cachedIndex = cache.get(cacheKey);
        if (cachedIndex != null) {
            return cachedIndex % maxIndex;
        }
        
        // 缓存未命中，计算新的随机索引
        FastRandom random = new FastRandom(cacheKey);
        int randomIndex = random.nextInt(maxIndex);
        
        // 缓存管理：如果缓存过大，清理一部分
        if (cache.size() >= MAX_CACHE_SIZE) {
            clearOldCache();
        }
        
        // 存储到缓存
        cache.put(cacheKey, randomIndex);
        
        return randomIndex;
    }
    
    /**
     * 清理旧缓存，保留一半
     */
    private static void clearOldCache() {
        int targetSize = MAX_CACHE_SIZE / 2;
        cache.entrySet().removeIf(entry -> Math.random() < 0.5);
        
        // 如果还是太大，强制清理到目标大小
        if (cache.size() > targetSize) {
            cache.clear();
        }
    }
    
    /**
     * 清空所有缓存
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * 获取当前缓存大小
     */
    public static int getCacheSize() {
        return cache.size();
    }
}

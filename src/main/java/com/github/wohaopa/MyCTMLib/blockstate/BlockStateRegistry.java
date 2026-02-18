package com.github.wohaopa.MyCTMLib.blockstate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

/**
 * 方块状态 → 模型 ID 的注册表。
 * 键：(blockId, metadata)，如 ("modid:blockname", 0)。
 * 值：modelId 字符串，如 "modid:block/stone"。
 * 由 BlockStateParser 在资源加载时填充。
 */
public class BlockStateRegistry {

    private static final BlockStateRegistry INSTANCE = new BlockStateRegistry();

    /** blockId (e.g. "modid:blockname") -> (variantKey -> modelId). variantKey 如 "" 或 "0".."15" 表示 metadata */
    private final Map<String, Map<String, String>> blockToVariants = new ConcurrentHashMap<>();

    public static BlockStateRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册某方块的所有 variant 映射。variantKey 通常为 "" 或 "0".."15"。
     */
    public void put(String blockId, Map<String, String> variantToModel) {
        if (variantToModel == null || variantToModel.isEmpty()) return;
        blockToVariants.put(TextureKeyNormalizer.normalizeDomain(blockId),
            Collections.unmodifiableMap(variantToModel));
    }

    /**
     * 根据方块 ID 和 metadata 解析得到模型 ID。
     * 先查 variantKey = ""，再查 variantKey = String.valueOf(meta)。
     */
    public String getModelId(String blockId, int meta) {
        Map<String, String> variants = blockToVariants.get(TextureKeyNormalizer.normalizeDomain(blockId));
        if (variants == null) return null;
        String key = String.valueOf(meta);
        if (variants.containsKey(key)) return variants.get(key);
        return variants.get("");
    }

    /** 返回所有已注册的 modelId，供资源加载时扫描并加载模型。 */
    public Set<String> getAllModelIds() {
        Set<String> out = new HashSet<>();
        for (Map<String, String> variants : blockToVariants.values()) {
            out.addAll(variants.values());
        }
        return out;
    }

    public void clear() {
        blockToVariants.clear();
    }

    /** 供 BlockTextureDumpUtil 导出 blockId → (variantKey → modelId) 到 JSON。 */
    public Map<String, Map<String, String>> getBlockToVariantsForDump() {
        return blockToVariants;
    }

    /** debug 模式下仅打出 size 摘要，避免刷屏。 */
    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG.info("[CTMLibFusion] BlockStateRegistry size={}", blockToVariants.size());
    }
}

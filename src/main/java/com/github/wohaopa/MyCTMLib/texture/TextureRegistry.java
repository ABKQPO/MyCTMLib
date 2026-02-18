package com.github.wohaopa.MyCTMLib.texture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

/**
 * 纹理路径（如 "modid:blocks/stone"）→ 解析后的 TextureTypeData。
 * 在 MixinTextureMap.registerIcon 或资源加载时，读取 .mcmeta 并调用 TextureTypeRegistry.deserialize 后 put 到此表。
 * 使用 TextureKeyNormalizer 做规范化与多键回退，put 时写入所有 lookup 候选键，get 时按候选顺序查找。
 */
public class TextureRegistry {

    private static final TextureRegistry INSTANCE = new TextureRegistry();
    private final Map<String, TextureTypeData> pathToData = new ConcurrentHashMap<>();

    public static TextureRegistry getInstance() {
        return INSTANCE;
    }

    public void put(String texturePath, TextureTypeData data) {
        if (texturePath == null || data == null) return;
        String normalized = normalizePath(texturePath);
        pathToData.put(normalized, data);
        for (String candidate : TextureKeyNormalizer.getLookupCandidates(normalized)) {
            if (!candidate.equals(normalized)) {
                pathToData.put(candidate, data);
            }
        }
        if (normalized.indexOf(':') < 0 && normalized.indexOf('/') < 0) {
            pathToData.put(TextureKeyNormalizer.toCanonicalTextureKey("minecraft", normalized), data);
        }
    }

    public TextureTypeData get(String texturePath) {
        if (texturePath == null) return null;
        for (String candidate : TextureKeyNormalizer.getLookupCandidates(normalizePath(texturePath))) {
            TextureTypeData data = pathToData.get(candidate);
            if (data != null) return data;
        }
        return null;
    }

    public void clear() {
        pathToData.clear();
    }

    /** debug 模式下仅打出 size 摘要，避免刷屏。 */
    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG.info("[CTMLibFusion] TextureRegistry size={}", pathToData.size());
    }

    private static String normalizePath(String path) {
        if (path == null) return "";
        return path.replace('\\', '/')
            .trim();
    }
}

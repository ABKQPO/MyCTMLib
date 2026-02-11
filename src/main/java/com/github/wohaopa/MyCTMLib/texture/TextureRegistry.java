package com.github.wohaopa.MyCTMLib.texture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

/**
 * 纹理路径（如 "modid:blocks/stone"）→ 解析后的 TextureTypeData。
 * 在 MixinTextureMap.registerIcon 或资源加载时，读取 .mcmeta 并调用 TextureTypeRegistry.deserialize 后 put 到此表。
 */
public class TextureRegistry {

    private static final TextureRegistry INSTANCE = new TextureRegistry();
    private final Map<String, TextureTypeData> pathToData = new ConcurrentHashMap<>();

    public static TextureRegistry getInstance() {
        return INSTANCE;
    }

    public void put(String texturePath, TextureTypeData data) {
        if (texturePath != null && data != null) {
            pathToData.put(normalizePath(texturePath), data);
        }
    }

    public TextureTypeData get(String texturePath) {
        return texturePath == null ? null : pathToData.get(normalizePath(texturePath));
    }

    public void clear() {
        pathToData.clear();
    }

    /** debug 模式下打出 texturePath → 数据类型 便于查看数据。 */
    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG.info("[CTMLibFusion] --- TextureRegistry (size={}) ---", pathToData.size());
        for (Map.Entry<String, TextureTypeData> e : pathToData.entrySet()) {
            String type = e.getValue() != null ? e.getValue().getClass().getSimpleName() : "null";
            MyCTMLib.LOG.info("[CTMLibFusion] TextureRegistry | path={} type={}", e.getKey(), type);
        }
    }

    private static String normalizePath(String path) {
        if (path == null) return "";
        return path.replace('\\', '/')
            .trim();
    }
}

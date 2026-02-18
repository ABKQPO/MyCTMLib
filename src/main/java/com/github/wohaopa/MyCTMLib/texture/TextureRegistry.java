package com.github.wohaopa.MyCTMLib.texture;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.mixins.AccessorTextureMap;

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

    /**
     * 从 blocks TextureMap 的 mapRegisteredSprites 中按 texturePath 查找 IIcon。
     * 若 TexReg 无该路径数据则返回 null；否则用 getLookupCandidates 依次尝试，找到即返回。
     */
    public IIcon getIcon(String texturePath) {
        if (get(texturePath) == null) return null;
        Object texObj = Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(TextureMap.locationBlocksTexture);
        if (!(texObj instanceof TextureMap textureMap)) return null;
        Map<String, TextureAtlasSprite> map = ((AccessorTextureMap) textureMap).getMapRegisteredSprites();
        List<String> candidates = TextureKeyNormalizer.getLookupCandidates(normalizePath(texturePath));
        for (String candidate : candidates) {
            TextureAtlasSprite sprite = map.get(candidate);
            if (sprite != null) return sprite;
        }
        return null;
    }

    public void clear() {
        pathToData.clear();
    }

    /** 供 RegistryDumpUtil 导出，按 TextureTypeData 去重后每个 value 保留一个 representative key。 */
    public Map<String, TextureTypeData> getPathToDataForDump() {
        Map<TextureTypeData, String> firstKeyPerValue = new LinkedHashMap<>();
        for (Map.Entry<String, TextureTypeData> e : pathToData.entrySet()) {
            firstKeyPerValue.putIfAbsent(e.getValue(), e.getKey());
        }
        Map<String, TextureTypeData> result = new LinkedHashMap<>();
        for (Map.Entry<TextureTypeData, String> e : firstKeyPerValue.entrySet()) {
            result.put(e.getValue(), e.getKey());
        }
        return result;
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

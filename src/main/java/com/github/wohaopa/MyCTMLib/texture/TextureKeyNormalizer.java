package com.github.wohaopa.MyCTMLib.texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 纹理键规范化工具。统一模型纹理路径与 TextureRegistry 查找键的格式。
 * 规范格式：domain:blocks/name（如 minecraft:blocks/cobblestone），与 textures/blocks/xxx.png 对应。
 */
public final class TextureKeyNormalizer {

    private TextureKeyNormalizer() {}

    /**
     * 将模型纹理路径转为规范键。
     * - minecraft:block/cobblestone → minecraft:blocks/cobblestone
     * - block/cobblestone + domain → domain:blocks/cobblestone
     * - stone + domain → domain:blocks/stone
     */
    public static String toCanonicalTextureKey(String domain, String path) {
        if (path == null) return null;
        String d = domain != null ? domain : "minecraft";
        String p = path.replace('\\', '/')
            .trim();
        if (p.isEmpty()) return null;
        String pathPart;
        if (p.indexOf(':') >= 0) {
            int colon = p.indexOf(':');
            String pathDomain = p.substring(0, colon);
            pathPart = p.substring(colon + 1);
            d = pathDomain;
        } else {
            pathPart = p;
        }
        pathPart = pathPart.replace("block/", "blocks/");
        if (!pathPart.startsWith("blocks/")) {
            pathPart = "blocks/" + pathPart;
        }
        return d + ":" + pathPart;
    }

    /**
     * 返回查找顺序列表，用于 get 时的多键回退。
     * 输入任意格式（minecraft:block/cobblestone、minecraft:blocks/cobblestone、cobblestone），
     * 输出 [原始key, pathPart, simpleName] 等，simpleName 为路径最后一段。
     */
    public static List<String> getLookupCandidates(String key) {
        if (key == null || key.isEmpty()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        String k = key.replace('\\', '/')
            .trim();
        out.add(k);
        int colon = k.indexOf(':');
        if (colon >= 0) {
            String pathPart = k.substring(colon + 1);
            if (!pathPart.isEmpty()) {
                out.add(pathPart);
            }
        }
        int lastSlash = k.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash + 1 < k.length()) {
            String simpleName = k.substring(lastSlash + 1);
            if (!out.contains(simpleName)) {
                out.add(simpleName);
            }
        } else if (colon < 0 && !out.contains(k)) {
            out.add(k);
        }
        return out;
    }
}

package com.github.wohaopa.MyCTMLib.texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 纹理键规范化工具。统一模型纹理路径与 TextureRegistry 查找键的格式。
 * 规范格式：domain:blocks/name（如 minecraft:blocks/cobblestone），与 textures/blocks/xxx.png 对应。
 */
public final class TextureKeyNormalizer {

    private TextureKeyNormalizer() {}

    /**
     * 规范化 icon 名用于 Registry 查找。与 IIcon.getIconName() 配合：
     * 第二个冒号及之后替换为 &，与 MixinRenderBlocks / CTMRenderEntry / Textures 一致。
     */
    public static String normalizeIconName(String name) {
        if (name == null) return "";
        int first = name.indexOf(':');
        int second = name.indexOf(':', first + 1);
        if (second != -1) {
            return name.substring(0, second) + "&"
                + name.substring(second + 1)
                    .replace(":", "&");
        }
        return name;
    }

    /**
     * 将 domain:path 的 domain 转为小写，用于注册表存储与查找，避免大小写敏感导致失败。
     */
    public static String normalizeDomain(String key) {
        if (key == null) return "";
        int colon = key.indexOf(':');
        if (colon >= 0) {
            return key.substring(0, colon)
                .toLowerCase(Locale.ROOT) + ":" + key.substring(colon + 1);
        }
        return key;
    }

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
     * 递归解析 #key 引用，支持引用链（如 "#particle" -> "#all" -> "stone"）及循环检测。
     * 兼容两种 Map 存储：key 带 #（如 "#all"）或不带 #（如 "all"）。
     *
     * @param key      纹理引用，如 "#all" 或 "all"
     * @param textures 模型 textures Map
     * @return 解析后的纹理路径，或 null
     */
    public static String resolveTexturePath(String key, Map<String, String> textures) {
        return resolveTexturePath(key, textures, new HashSet<String>());
    }

    private static String resolveTexturePath(String key, Map<String, String> textures, Set<String> visiting) {
        if (key == null || textures == null) return null;
        String lookupKey = key.startsWith("#") ? key.substring(1)
            .trim() : key;
        if (visiting.contains(lookupKey)) return null;
        visiting.add(lookupKey);
        try {
            String v = textures.get(key);
            if (v == null && key.startsWith("#")) v = textures.get(lookupKey);
            if (v != null && v.startsWith("#")) {
                return resolveTexturePath(v, textures, visiting);
            }
            return v;
        } finally {
            visiting.remove(lookupKey);
        }
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
        if (colon >= 0) {
            String domainLower = normalizeDomain(k);
            if (!out.contains(domainLower)) {
                out.add(domainLower);
            }
        }
        return out;
    }
}

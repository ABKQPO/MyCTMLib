package com.github.wohaopa.MyCTMLib.predicate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;

import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 从 ModelData.connections 的 JsonObject 或 "#key" 引用反序列化得到 ConnectionPredicate。
 * JSON 使用 "condition" 字段：is_same_block、match_block（需 "block": "modid:block_id"）、is_same_texture。
 */
public final class PredicateRegistry {

    /**
     * 根据 key 从 connections 映射中解析出谓词；支持 "#ref" 引用。
     *
     * @param key         谓词键（如 "default"、"blue"）
     * @param connections ModelData.getConnections()
     * @return 解析后的谓词，若不存在或解析失败返回 null
     */
    public static ConnectionPredicate getPredicate(String key, Map<String, Object> connections) {
        if (key == null || connections == null) return null;
        return getPredicate(key, connections, new HashSet<String>());
    }

    private static ConnectionPredicate getPredicate(String key, Map<String, Object> connections,
        Set<String> resolving) {
        if (resolving.contains(key)) return null;
        Object v = connections.get(key);
        if (v == null) return null;
        if (v instanceof String) {
            String s = ((String) v).trim();
            if (s.startsWith("#")) {
                String refKey = s.substring(1)
                    .trim();
                resolving.add(key);
                try {
                    return getPredicate(refKey, connections, resolving);
                } finally {
                    resolving.remove(key);
                }
            }
            return null;
        }
        if (v instanceof JsonObject) {
            return deserialize((JsonObject) v);
        }
        return null;
    }

    /**
     * 从单个 JsonObject 反序列化谓词（需含 "condition"）。
     */
    public static ConnectionPredicate deserialize(JsonObject json) throws JsonParseException {
        if (json == null || !json.has("condition")
            || !json.get("condition")
                .isJsonPrimitive()) {
            throw new JsonParseException("Predicate must have string property 'condition'");
        }
        String condition = json.get("condition")
            .getAsString();
        if ("is_same_block".equals(condition)) {
            return IsSameBlockPredicate.INSTANCE;
        }
        if ("is_same_texture".equals(condition)) {
            return IsSameTexturePredicate.INSTANCE;
        }
        if ("match_block".equals(condition)) {
            if (!json.has("block") || !json.get("block")
                .isJsonPrimitive()) {
                throw new JsonParseException("match_block predicate must have string property 'block'");
            }
            String blockId = json.get("block")
                .getAsString();
            Block block = (Block) Block.blockRegistry.getObject(blockId);
            if (block == null) {
                String norm = TextureKeyNormalizer.normalizeDomain(blockId);
                for (Object k : Block.blockRegistry.getKeys()) {
                    if (k instanceof String
                        && TextureKeyNormalizer.normalizeDomain((String) k).equals(norm)) {
                        block = (Block) Block.blockRegistry.getObject((String) k);
                        break;
                    }
                }
            }
            if (block == null) {
                throw new JsonParseException("Unknown block: " + blockId);
            }
            return new MatchBlockPredicate(block);
        }
        throw new JsonParseException("Unknown predicate condition: " + condition);
    }

    /**
     * 默认谓词（纯 TextureRegistry 路径、无 Model 时使用）：is_same_block。
     */
    public static ConnectionPredicate defaultPredicate() {
        return IsSameTexturePredicate.INSTANCE;
    }

    /**
     * 获取谓词的可读调试名。用于 Debug HUD。
     *
     * @param predicate     谓词实例
     * @param connectionKey  Model 中的 connectionKey，可为 null 表示 default
     * @return 如 "default (is_same_texture)" 或 "blue (is_same_block)"
     */
    public static String getPredicateDebugName(ConnectionPredicate predicate, String connectionKey) {
        if (predicate == null) return "null";
        String type = predicate.getDebugName();
        if (connectionKey != null && !connectionKey.isEmpty()) {
            return connectionKey + " (" + type + ")";
        }
        return "default (" + type + ")";
    }
}

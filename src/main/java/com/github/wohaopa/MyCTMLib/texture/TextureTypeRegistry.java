package com.github.wohaopa.MyCTMLib.texture;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 按 mcmeta 中的 "type" 分发到对应反序列化逻辑，产出 TextureTypeData。
 * 在首次反序列化前需完成注册；可调用 finalizeRegistration() 冻结。
 */
public class TextureTypeRegistry {

    private static final Map<String, TextureTypeDeserializer<?>> TYPE_TO_DESERIALIZER = new HashMap<>();
    private static boolean finalized = false;

    static {
        register("connecting", ConnectingTextureDataDeserializer.INSTANCE);
        register("base", BaseTextureDataDeserializer.INSTANCE);
    }

    public static synchronized void register(String typeId, TextureTypeDeserializer<?> deserializer) {
        if (finalized) {
            throw new IllegalStateException("Texture types must be registered before textures get loaded!");
        }
        if (TYPE_TO_DESERIALIZER.containsKey(typeId)) {
            throw new IllegalArgumentException("Duplicate texture type: " + typeId);
        }
        TYPE_TO_DESERIALIZER.put(typeId, deserializer);
    }

    public static void finalizeRegistration() {
        if (!finalized) {
            synchronized (TextureTypeRegistry.class) {
                finalized = true;
            }
        }
    }

    /**
     * 从 mcmeta 的 JSON 根节点反序列化，根据 "type" 选择反序列化器。
     */
    @SuppressWarnings("unchecked")
    public static TextureTypeData deserialize(JsonObject json) throws JsonParseException {
        if (!finalized) {
            finalizeRegistration();
        }
        String type = "base";
        if (json.has("type") && json.get("type")
            .isJsonPrimitive()) {
            type = json.get("type")
                .getAsString();
        }
        TextureTypeDeserializer<?> d = TYPE_TO_DESERIALIZER.get(type);
        if (d == null) {
            throw new JsonParseException("Unknown texture type: " + type);
        }
        return ((TextureTypeDeserializer<TextureTypeData>) d).deserialize(json);
    }

    /** 单类型反序列化器 */
    public interface TextureTypeDeserializer<T extends TextureTypeData> {

        T deserialize(JsonObject json) throws JsonParseException;
    }
}

package com.github.wohaopa.MyCTMLib.texture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 反序列化 type=random 的 mcmeta：rows、columns、count、seed。
 */
public enum RandomTextureDataDeserializer
    implements TextureTypeRegistry.TextureTypeDeserializer<RandomTextureData> {

    INSTANCE;

    @Override
    public RandomTextureData deserialize(JsonObject json) throws JsonParseException {
        int rows = 1;
        int columns = 1;
        int count = -1;
        Long seed = null;

        if (json.has("rows") && json.get("rows").isJsonPrimitive()) {
            rows = json.get("rows").getAsInt();
        }
        if (json.has("columns") && json.get("columns").isJsonPrimitive()) {
            columns = json.get("columns").getAsInt();
        }
        if (json.has("count") && json.get("count").isJsonPrimitive()) {
            count = json.get("count").getAsInt();
        }
        if (json.has("seed") && json.get("seed").isJsonPrimitive()) {
            seed = json.get("seed").getAsLong();
        }

        return new RandomTextureData(rows, columns, count, seed);
    }
}

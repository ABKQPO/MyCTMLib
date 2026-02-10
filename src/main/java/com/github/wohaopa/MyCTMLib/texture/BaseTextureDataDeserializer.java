package com.github.wohaopa.MyCTMLib.texture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 反序列化 type=base 的 mcmeta。
 */
public enum BaseTextureDataDeserializer implements TextureTypeRegistry.TextureTypeDeserializer<BaseTextureData> {

    INSTANCE;

    @Override
    public BaseTextureData deserialize(JsonObject json) throws JsonParseException {
        return new BaseTextureData();
    }
}

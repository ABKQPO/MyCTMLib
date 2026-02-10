package com.github.wohaopa.MyCTMLib.texture;

import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 反序列化 type=connecting 的 mcmeta：layout、random。
 */
public enum ConnectingTextureDataDeserializer
    implements TextureTypeRegistry.TextureTypeDeserializer<ConnectingTextureData> {

    INSTANCE;

    @Override
    public ConnectingTextureData deserialize(JsonObject json) throws JsonParseException {
        ConnectingLayout layout = ConnectingLayout.FULL;
        if (json.has("layout") && json.get("layout")
            .isJsonPrimitive()) {
            layout = ConnectingLayout.fromString(
                json.get("layout")
                    .getAsString());
        }
        boolean random = false;
        if (json.has("random") && json.get("random")
            .isJsonPrimitive()) {
            random = json.get("random")
                .getAsBoolean();
        }
        return new ConnectingTextureData(layout, random);
    }
}

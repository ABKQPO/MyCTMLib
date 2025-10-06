package com.github.wohaopa.MyCTMLib;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * CTM配置解析器
 * 负责解析myctmlib.json配置文件中的各种配置项
 */
public class CTMConfig {

    // 解析结果
    public final String connectionTexture;
    public final String altTexture;
    public final List<String> randomTextures;
    public final List<String> equivalents;

    public CTMConfig(JsonObject ctmObj) {
        // 解析所有配置
        this.connectionTexture = parseStringField(ctmObj, "connection");
        this.altTexture = parseStringField(ctmObj, "alt");
        this.randomTextures = parseStringArray(ctmObj, "random");
        this.equivalents = parseStringArray(ctmObj, "equivalents");
    }

    // 解析方法
    private String parseStringField(JsonObject ctmObj, String fieldName) {
        JsonPrimitive primitive = ctmObj.getAsJsonPrimitive(fieldName);
        if (primitive == null) {
            return null;
        }
        String value = primitive.getAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return processTextureName(value);
    }

    private List<String> parseStringArray(JsonObject ctmObj, String fieldName) {
        if (!ctmObj.has(fieldName)) {
            return new ArrayList<>();
        }

        JsonElement element = ctmObj.get(fieldName);
        if (!element.isJsonArray()) {
            return new ArrayList<>();
        }

        JsonArray array = element.getAsJsonArray();
        List<String> result = new ArrayList<>();

        for (JsonElement el : array) {
            String originalValue = el.getAsString();
            String processedValue = processTextureName(originalValue);
            result.add(processedValue);
        }

        return result;
    }

    /**
     * 处理纹理名称，移除前缀和后缀
     */
    private static String processTextureName(String texture) {
        return texture.replace("minecraft:", "")
            .replace("textures/blocks/", "")
            .replace(".png", "");
    }
}

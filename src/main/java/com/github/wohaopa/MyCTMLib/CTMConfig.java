package com.github.wohaopa.MyCTMLib;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Parses CTM metadata configuration.
 */
public class CTMConfig {

    // Parsed configuration values.
    public final String connectionTexture;
    public final String altTexture;
    public final List<String> randomTextures;
    public final List<String> equivalents;

    public CTMConfig(JsonObject ctmObj) {
        this.connectionTexture = parseStringField(ctmObj, "connection");
        this.altTexture = parseStringField(ctmObj, "alt");
        this.randomTextures = parseStringArray(ctmObj, "random");
        this.equivalents = parseStringArray(ctmObj, "equivalents");
    }

    // Configuration parsing helpers.
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
     * Normalizes a texture name by removing known prefixes and suffixes.
     */
    private static String processTextureName(String texture) {
        return texture.replace("minecraft:", "")
            .replace("textures/blocks/", "")
            .replace(".png", "");
    }
}

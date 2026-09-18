package com.github.wohaopa.MyCTMLib.client.ctm;

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
    public final String method;
    public final String symmetry;
    public final int[] weights;
    public final int columns;
    public final int width;
    public final int height;

    public CTMConfig(JsonObject ctmObj) {
        this.connectionTexture = parseStringField(ctmObj, "connection");
        this.altTexture = parseStringField(ctmObj, "alt");
        this.randomTextures = parseStringArray(ctmObj, "random");
        this.equivalents = parseStringArray(ctmObj, "equivalents");
        this.method = parseRawStringField(ctmObj, "method");
        this.symmetry = parseRawStringField(ctmObj, "symmetry");
        this.weights = parseRawIntArray(ctmObj, "weights");
        this.columns = parseRawIntField(ctmObj, "columns", 0);
        this.width = parseRawIntField(ctmObj, "width", 0);
        this.height = parseRawIntField(ctmObj, "height", 0);
    }

    // Reads a whole number field, falling back when it is absent or not a number.
    private int parseRawIntField(JsonObject ctmObj, String fieldName, int fallback) {
        JsonPrimitive primitive = ctmObj.getAsJsonPrimitive(fieldName);
        if (primitive == null) {
            return fallback;
        }
        try {
            return primitive.getAsInt();
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // Reads a whole number array, returning an empty array when it is absent or not an array.
    private int[] parseRawIntArray(JsonObject ctmObj, String fieldName) {
        if (!ctmObj.has(fieldName)) {
            return new int[0];
        }

        JsonElement element = ctmObj.get(fieldName);
        if (!element.isJsonArray()) {
            return new int[0];
        }

        JsonArray array = element.getAsJsonArray();
        int[] result = new int[array.size()];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = array.get(i)
                    .getAsInt();
            } catch (NumberFormatException e) {
                result[i] = 1;
            }
        }
        return result;
    }

    // Reads a field whose value is not a texture name, so it is kept verbatim.
    private String parseRawStringField(JsonObject ctmObj, String fieldName) {
        JsonPrimitive primitive = ctmObj.getAsJsonPrimitive(fieldName);
        if (primitive == null) {
            return null;
        }
        String value = primitive.getAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
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

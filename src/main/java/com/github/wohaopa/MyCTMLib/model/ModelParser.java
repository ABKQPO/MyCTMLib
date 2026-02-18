package com.github.wohaopa.MyCTMLib.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * 解析 models/block/*.json（标准路径 assets/&lt;modid&gt;/models/block/）。
 * 仅当 loader 为 "myctmlib:model" 或 "ctmlib:model" 且 type 为 "connection" 或 "base" 时解析。
 */
public class ModelParser {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final String[] LOADER_IDS = { "myctmlib:model", "ctmlib:model" };
    private static final ForgeDirection[] SIDES = { ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH,
        ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

    /**
     * 判断 JSON 是否为本解析器支持的模型（含 loader + type）。
     */
    public boolean isSupported(JsonObject root) {
        if (!root.has("loader") || !root.get("loader")
            .isJsonPrimitive()) return false;
        String loader = root.get("loader")
            .getAsString();
        boolean loaderOk = false;
        for (String id : LOADER_IDS) {
            if (id.equals(loader)) {
                loaderOk = true;
                break;
            }
        }
        if (!loaderOk) return false;
        if (!root.has("type") || !root.get("type")
            .isJsonPrimitive()) return false;
        String type = root.get("type")
            .getAsString();
        return "connection".equals(type) || "base".equals(type);
    }

    /**
     * 解析 JSON 并返回 ModelData；不注册，由调用方放入 ModelRegistry。
     */
    public ModelData parse(JsonObject root) throws JsonParseException {
        String type = root.get("type")
            .getAsString();
        List<ModelElement> elements = parseElements(root);
        Map<String, String> textures = parseTextures(root);
        Map<String, Object> connections = "connection".equals(type) ? parseConnections(root) : new HashMap<>();
        return new ModelData(type, elements, textures, connections);
    }

    /**
     * 从输入流解析并返回 ModelData。
     */
    public ModelData parse(InputStream in) throws JsonParseException {
        JsonObject root = JSON_PARSER.parse(new InputStreamReader(in, StandardCharsets.UTF_8))
            .getAsJsonObject();
        if (!isSupported(root)) throw new JsonParseException("Unsupported model: missing or invalid loader/type");
        return parse(root);
    }

    private static List<ModelElement> parseElements(JsonObject root) throws JsonParseException {
        if (!root.has("elements") || !root.get("elements")
            .isJsonArray()) {
            return new ArrayList<>();
        }
        JsonArray arr = root.getAsJsonArray("elements");
        List<ModelElement> list = new ArrayList<>(arr.size());
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) throw new JsonParseException("Element must be an object");
            list.add(parseElement(el.getAsJsonObject()));
        }
        return list;
    }

    private static ModelElement parseElement(JsonObject obj) throws JsonParseException {
        float[] from = parseFloat3(obj, "from", 0, 0, 0);
        float[] to = parseFloat3(obj, "to", 16, 16, 16);
        Map<ForgeDirection, ModelFace> faces = new EnumMap<>(ForgeDirection.class);
        if (obj.has("faces") && obj.get("faces")
            .isJsonObject()) {
            JsonObject facesObj = obj.getAsJsonObject("faces");
            for (ForgeDirection dir : SIDES) {
                String name = dir.name()
                    .toLowerCase();
                if (!facesObj.has(name)) continue;
                JsonElement faceEl = facesObj.get(name);
                if (!faceEl.isJsonObject()) continue;
                JsonObject faceObj = faceEl.getAsJsonObject();
                String textureKey = faceObj.has("texture") && faceObj.get("texture")
                    .isJsonPrimitive() ? faceObj.get("texture")
                        .getAsString() : null;
                String connectionKey = null;
                if (faceObj.has("connections") && faceObj.get("connections")
                    .isJsonPrimitive()) {
                    connectionKey = faceObj.get("connections")
                        .getAsString();
                }
                if (textureKey != null) {
                    faces.put(dir, new ModelFace(textureKey, connectionKey));
                }
            }
        }
        return new ModelElement(from, to, faces);
    }

    private static float[] parseFloat3(JsonObject obj, String key, float defX, float defY, float defZ)
        throws JsonParseException {
        if (!obj.has(key) || !obj.get(key)
            .isJsonArray()) {
            return new float[] { defX, defY, defZ };
        }
        JsonArray a = obj.getAsJsonArray(key);
        if (a.size() != 3) throw new JsonParseException("'" + key + "' must have 3 numbers");
        return new float[] { a.get(0)
            .getAsFloat(),
            a.get(1)
                .getAsFloat(),
            a.get(2)
                .getAsFloat() };
    }

    private static Map<String, String> parseTextures(JsonObject root) {
        Map<String, String> out = new HashMap<>();
        if (!root.has("textures") || !root.get("textures")
            .isJsonObject()) return out;
        JsonObject tex = root.getAsJsonObject("textures");
        for (Map.Entry<String, JsonElement> e : tex.entrySet()) {
            if (e.getValue()
                .isJsonPrimitive()) {
                out.put(
                    e.getKey(),
                    e.getValue()
                        .getAsString());
            }
        }
        return out;
    }

    private static Map<String, Object> parseConnections(JsonObject root) throws JsonParseException {
        Map<String, Object> out = new HashMap<>();
        out.put("default", new JsonObject()); // placeholder; real default is is_same_block
        if (!root.has("connections") || !root.get("connections")
            .isJsonObject()) return out;
        JsonObject conn = root.getAsJsonObject("connections");
        for (Map.Entry<String, JsonElement> e : conn.entrySet()) {
            JsonElement v = e.getValue();
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive()
                .isString()) {
                String ref = v.getAsString();
                if (ref.startsWith("#")) out.put(e.getKey(), ref);
                else throw new JsonParseException("Connection reference must start with #");
            } else if (v.isJsonObject()) {
                out.put(e.getKey(), v.getAsJsonObject());
            }
        }
        return out;
    }
}

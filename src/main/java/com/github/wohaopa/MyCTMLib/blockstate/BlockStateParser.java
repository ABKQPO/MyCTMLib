package com.github.wohaopa.MyCTMLib.blockstate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * 解析 blockstates/&lt;namespace&gt;/&lt;block_id&gt;.json，
 * 产出 variantKey → modelId 的映射并写入 BlockStateRegistry。
 * 格式参考现代 MC：{ "variants": { "": { "model": "modid:block/stone" }, "1": { "model": "..." } } }
 */
public class BlockStateParser {

    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * 解析输入流中的 blockstate JSON，将结果注册到 BlockStateRegistry。
     *
     * @param blockId 方块 ID，如 "modid:blockname"（由调用方从路径 blockstates/namespace/blockname.json 得到）
     * @param in      JSON 输入流
     * @throws JsonParseException 解析失败时
     */
    public void parseAndRegister(String blockId, InputStream in) throws JsonParseException {
        JsonObject root = JSON_PARSER.parse(new InputStreamReader(in, StandardCharsets.UTF_8))
            .getAsJsonObject();
        if (!root.has("variants") || !root.get("variants")
            .isJsonObject()) {
            throw new JsonParseException("BlockState must have an object 'variants'");
        }
        JsonObject variants = root.getAsJsonObject("variants");
        Map<String, String> variantToModel = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : variants.entrySet()) {
            String variantKey = e.getKey();
            JsonElement val = e.getValue();
            if (val.isJsonArray()) {
                JsonElement first = val.getAsJsonArray()
                    .size() > 0 ? val.getAsJsonArray()
                        .get(0) : null;
                if (first != null) {
                    String model = getModelFromVariantValue(first);
                    if (model != null) variantToModel.put(variantKey, model);
                }
            } else {
                String model = getModelFromVariantValue(val);
                if (model != null) variantToModel.put(variantKey, model);
            }
        }
        if (!variantToModel.isEmpty()) {
            BlockStateRegistry.getInstance()
                .put(blockId, variantToModel);
        }
    }

    private static String getModelFromVariantValue(JsonElement val) {
        if (!val.isJsonObject()) return null;
        JsonObject obj = val.getAsJsonObject();
        if (!obj.has("model") || !obj.get("model")
            .isJsonPrimitive()) return null;
        return obj.get("model")
            .getAsString();
    }
}

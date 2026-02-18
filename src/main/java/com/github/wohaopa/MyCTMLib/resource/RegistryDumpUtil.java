package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 导出三大 Registry（BlockStateRegistry、ModelRegistry、TextureRegistry）的详细数据至 JSON 文件。
 * 由 /ctmlib dump_registry 命令调用。
 */
public class RegistryDumpUtil {

    /**
     * 导出完整 Registry dump 到指定文件。
     * 格式见计划文档。
     */
    public static void dumpToFile(File outputFile) {
        try {
            JsonObject root = new JsonObject();

            // BlockStateRegistry
            JsonObject bsRoot = new JsonObject();
            JsonArray bsEntries = new JsonArray();
            Map<String, Map<String, String>> blockToVariants = BlockStateRegistry.getInstance()
                .getBlockToVariantsForDump();
            for (Map.Entry<String, Map<String, String>> e : blockToVariants.entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("blockId", e.getKey());
                JsonObject variants = new JsonObject();
                for (Map.Entry<String, String> v : e.getValue()
                    .entrySet()) {
                    variants.addProperty(v.getKey(), v.getValue());
                }
                entry.add("variants", variants);
                bsEntries.add(entry);
            }
            bsRoot.add("entries", bsEntries);
            root.add("blockStateRegistry", bsRoot);

            // ModelRegistry
            JsonObject modelRoot = new JsonObject();
            JsonArray modelEntries = new JsonArray();
            Map<String, ModelData> modelById = ModelRegistry.getInstance()
                .getModelByIdForDump();
            for (Map.Entry<String, ModelData> e : modelById.entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("modelId", e.getKey());
                ModelData data = e.getValue();
                if (data != null) {
                    entry.addProperty("type", data.getType());
                    entry.addProperty("elementsCount", data.getElements()
                        .size());
                    JsonObject textures = new JsonObject();
                    for (Map.Entry<String, String> t : data.getTextures()
                        .entrySet()) {
                        textures.addProperty(t.getKey(), t.getValue());
                    }
                    entry.add("textures", textures);
                    entry.add("connections", new GsonBuilder().create()
                        .toJsonTree(data.getConnections()));
                }
                modelEntries.add(entry);
            }
            modelRoot.add("entries", modelEntries);
            root.add("modelRegistry", modelRoot);

            // TextureRegistry
            JsonObject texRoot = new JsonObject();
            JsonArray texEntries = new JsonArray();
            Map<String, TextureTypeData> pathToData = TextureRegistry.getInstance()
                .getPathToDataForDump();
            for (Map.Entry<String, TextureTypeData> e : pathToData.entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("path", e.getKey());
                TextureTypeData data = e.getValue();
                if (data != null) {
                    entry.addProperty("type", data.getType());
                    if (data instanceof ConnectingTextureData ctd) {
                        entry.addProperty("layout", ctd.getLayout()
                            .name());
                        entry.addProperty("random", ctd.isRandom());
                    }
                }
                texEntries.add(entry);
            }
            texRoot.add("entries", texEntries);
            root.add("textureRegistry", texRoot);

            outputFile.getParentFile()
                .mkdirs();
            try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(outputFile),
                StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, w);
            }
            MyCTMLib.LOG.info("[CTMLibFusion] RegistryDump written to {} (blockState={} model={} texture={})",
                outputFile,
                bsEntries.size(),
                modelEntries.size(),
                texEntries.size());
        } catch (Exception e) {
            MyCTMLib.LOG.warn("[CTMLibFusion] RegistryDump failed", e);
        }
    }
}

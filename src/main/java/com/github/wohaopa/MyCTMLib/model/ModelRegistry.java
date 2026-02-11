package com.github.wohaopa.MyCTMLib.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

/**
 * modelId → ModelData 的注册表。
 * 可选：纹理路径 → (modelId, faceInfo) 的回退索引，供无 BlockState 时按纹理名查模型。
 */
public class ModelRegistry {

    private static final ModelRegistry INSTANCE = new ModelRegistry();

    private final Map<String, ModelData> modelById = new ConcurrentHashMap<>();
    /** texturePath (normalized) -> list of (modelId, faceDirection) for fallback lookup */
    private final Map<String, List<TextureModelEntry>> textureToModel = new ConcurrentHashMap<>();

    public static ModelRegistry getInstance() {
        return INSTANCE;
    }

    public void put(String modelId, ModelData data) {
        modelById.put(modelId, data);
    }

    public ModelData get(String modelId) {
        return modelById.get(modelId);
    }

    public void putTextureFallback(String texturePath, String modelId, int faceOrdinal) {
        TextureModelEntry e = new TextureModelEntry(modelId, faceOrdinal);
        textureToModel.computeIfAbsent(texturePath, k -> new java.util.ArrayList<>())
            .add(e);
    }

    public List<TextureModelEntry> getModelsForTexture(String texturePath) {
        List<TextureModelEntry> list = textureToModel.get(texturePath);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    public void clear() {
        modelById.clear();
        textureToModel.clear();
    }

    /** debug 模式下打出 modelId 列表便于查看数据。 */
    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG.info("[CTMLibFusion] --- ModelRegistry (size={}) ---", modelById.size());
        for (String modelId : modelById.keySet()) {
            MyCTMLib.LOG.info("[CTMLibFusion] ModelRegistry | modelId={}", modelId);
        }
    }

    public static class TextureModelEntry {

        public final String modelId;
        public final int faceOrdinal;

        public TextureModelEntry(String modelId, int faceOrdinal) {
            this.modelId = modelId;
            this.faceOrdinal = faceOrdinal;
        }
    }
}

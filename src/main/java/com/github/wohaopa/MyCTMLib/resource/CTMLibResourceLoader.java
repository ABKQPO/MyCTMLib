package com.github.wohaopa.MyCTMLib.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateParser;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelParser;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 资源重载时清空 BlockState/Model 注册表并尝试加载 blockstates/*.json 与 models/block/*.json。
 * 在 Mod 入口注册为 IResourceManagerReloadListener。
 */
@SideOnly(Side.CLIENT)
public class CTMLibResourceLoader implements net.minecraft.client.resources.IResourceManagerReloadListener {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private final BlockStateParser blockStateParser = new BlockStateParser();
    private final ModelParser modelParser = new ModelParser();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        BlockStateRegistry.getInstance()
            .clear();
        ModelRegistry.getInstance()
            .clear();

        if (!(resourceManager instanceof IReloadableResourceManager)) {
            return;
        }

        loadBlockStates(resourceManager);
        loadModels(resourceManager);
        if (MyCTMLib.debugMode) {
            MyCTMLib.LOG.info("[CTMLibFusion] --- dump after onResourceManagerReload ---");
            BlockStateRegistry.getInstance()
                .dumpForDebug();
            ModelRegistry.getInstance()
                .dumpForDebug();
            TextureRegistry.getInstance()
                .dumpForDebug();
        }
    }

    /**
     * 遍历 Block 注册表，对每个 blockId 尝试加载 blockstates/&lt;path&gt;.json。
     */
    private void loadBlockStates(IResourceManager resourceManager) {
        try {
            Iterator<?> it = Block.blockRegistry.getKeys()
                .iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (!(key instanceof String)) continue;
                String blockId = (String) key;
                int colon = blockId.indexOf(':');
                String domain = colon >= 0 ? blockId.substring(0, colon) : "minecraft";
                String path = colon >= 0 ? blockId.substring(colon + 1) : blockId;
                String blockstatePath = "blockstates/" + path + ".json";
                try {
                    IResource res = resourceManager.getResource(new ResourceLocation(domain, blockstatePath));
                    try (InputStream in = res.getInputStream()) {
                        blockStateParser.parseAndRegister(blockId, in);
                        if (MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(blockId)) {
                            MyCTMLib.LOG.info(
                                "[CTMLibFusion] BlockState loaded | blockId={} path={}",
                                blockId,
                                domain + ":" + blockstatePath);
                        }
                    }
                } catch (IOException ignored) {} catch (Exception e) {
                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG.warn("BlockState parse failed: " + domain + ":" + blockstatePath, e);
                    }
                }
            }
        } catch (Throwable t) {
            if (MyCTMLib.debugMode) {
                MyCTMLib.LOG.warn("BlockState scan failed", t);
            }
        }
    }

    /**
     * 从 BlockStateRegistry 已注册的 modelId 中收集并加载对应模型。
     */
    private void loadModels(IResourceManager resourceManager) {
        for (String modelId : BlockStateRegistry.getInstance()
            .getAllModelIds()) {
            try {
                loadModel(resourceManager, modelId);
            } catch (Throwable t) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("Model load failed: " + modelId, t);
                }
            }
        }
    }

    /**
     * 按 modelId（如 "modid:block/stone"）尝试加载并解析模型，写入 ModelRegistry。
     * 先尝试 models/block/stone.json，再尝试 blockmodel/stone.json。
     */
    public void loadModel(IResourceManager resourceManager, String modelId) {
        int colon = modelId.indexOf(':');
        String domain = colon >= 0 ? modelId.substring(0, colon) : "minecraft";
        String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
        String resourcePath = path.startsWith("block/") ? "models/" + path + ".json" : "models/block/" + path + ".json";
        try {
            tryLoadOneModel(resourceManager, domain, resourcePath, modelId);
            return;
        } catch (Exception ignored) {}
        String simple = path.replace("block/", "");
        try {
            tryLoadOneModel(resourceManager, domain, "blockmodel/" + simple + ".json", modelId);
        } catch (Exception e) {
            if (MyCTMLib.debugMode) {
                MyCTMLib.LOG.warn("Model load failed: " + modelId, e);
            }
        }
    }

    private void tryLoadOneModel(IResourceManager resourceManager, String domain, String path, String modelId)
        throws IOException {
        IResource res = resourceManager.getResource(new ResourceLocation(domain, path));
        try (InputStream in = res.getInputStream()) {
            JsonObject root = JSON_PARSER
                .parse(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            if (!modelParser.isSupported(root)) return;
            ModelData data = modelParser.parse(root);
            ModelRegistry.getInstance()
                .put(modelId, data);
            if (MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(modelId)) {
                MyCTMLib.LOG.info(
                    "[CTMLibFusion] Model loaded | modelId={} path={} elements={} textures={}",
                    modelId,
                    domain + ":" + path,
                    data.getElements()
                        .size(),
                    data.getTextures()
                        .keySet());
            }
        }
    }
}

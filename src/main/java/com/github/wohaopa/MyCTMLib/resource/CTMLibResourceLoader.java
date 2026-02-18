package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.IMetadataSection;
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
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureMetadataSection;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 资源重载时清空 BlockState/Model 注册表并尝试加载 blockstates/*.json 与 models/block/*.json（标准路径 assets/&lt;modid&gt;/models/block/）。
 * 在 Mod 入口注册为 IResourceManagerReloadListener。
 */
@SideOnly(Side.CLIENT)
public class CTMLibResourceLoader implements net.minecraft.client.resources.IResourceManagerReloadListener {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static CTMLibResourceLoader instance;
    private final BlockStateParser blockStateParser = new BlockStateParser();
    private final ModelParser modelParser = new ModelParser();
    private boolean loaded;

    public CTMLibResourceLoader() {
        instance = this;
    }

    /**
     * 若尚未加载，则执行 loadBlockStates + loadModels。供 loadTextureAtlas HEAD 注入在 listener 顺序不确定时调用。
     */
    public static void ensureLoaded(IResourceManager resourceManager) {
        if (instance != null) instance.doEnsureLoaded(resourceManager);
    }

    private void doEnsureLoaded(IResourceManager resourceManager) {
        if (loaded) return;
        if (!(resourceManager instanceof IReloadableResourceManager)) return;
        doLoad(resourceManager);
        loaded = true;
    }


    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        loaded = false;
        BlockStateRegistry.getInstance()
            .clear();
        ModelRegistry.getInstance()
            .clear();
        DebugErrorCollector.getInstance()
            .clear();

        if (!(resourceManager instanceof IReloadableResourceManager)) {
            return;
        }

        doLoad(resourceManager);
        loaded = true;

        if (MyCTMLib.debugMode) {
            BlockStateRegistry.getInstance()
                .dumpForDebug();
            ModelRegistry.getInstance()
                .dumpForDebug();
            TextureRegistry.getInstance()
                .dumpForDebug();
            DebugErrorCollector.getInstance()
                .flushToFile(new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_debug_errors.json"));
        }
    }

    private void doLoad(IResourceManager resourceManager) {
        loadBlockStates(resourceManager);
        loadModels(resourceManager);
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
                String domain = colon >= 0 ? blockId.substring(0, colon).toLowerCase(Locale.ROOT) : "minecraft";
                String path = colon >= 0 ? blockId.substring(colon + 1) : blockId;
                String blockstatePath = "blockstates/" + path + ".json";
                ResourceLocation blockstateLoc = new ResourceLocation(domain, blockstatePath);
                try {
                    IResource res = resourceManager.getResource(blockstateLoc);
                    try (InputStream in = res.getInputStream()) {
                        blockStateParser.parseAndRegister(TextureKeyNormalizer.normalizeDomain(blockId), in);
                    }
                } catch (IOException e) {
                    if (MyCTMLib.debugMode) {
                        String attemptedPath = "assets/" + blockstateLoc.getResourceDomain() + "/" + blockstateLoc.getResourcePath();
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                } catch (Exception e) {
                    if (MyCTMLib.debugMode) {
                        String attemptedPath = "assets/" + blockstateLoc.getResourceDomain() + "/" + blockstateLoc.getResourcePath();
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                }
            }
        } catch (Throwable t) {
            if (MyCTMLib.debugMode) {
                DebugErrorCollector.getInstance()
                    .add("blockstate", "blockstate_scan", t);
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
                    DebugErrorCollector.getInstance()
                        .add("model", modelId, t);
                    MyCTMLib.LOG.warn("Model load failed: " + modelId, t);
                }
            }
        }
    }

    /**
     * 按 modelId（如 "modid:block/stone"）加载并解析模型，写入 ModelRegistry。
     * 标准路径：assets/&lt;domain&gt;/models/block/&lt;name&gt;.json。
     */
    public void loadModel(IResourceManager resourceManager, String modelId) {
        int colon = modelId.indexOf(':');
        String domain = colon >= 0 ? modelId.substring(0, colon).toLowerCase(Locale.ROOT) : "minecraft";
        String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
        String resourcePath = path.startsWith("block/") ? "models/" + path + ".json" : "models/block/" + path + ".json";
        ResourceLocation modelLoc = new ResourceLocation(domain, resourcePath);
        try {
            tryLoadOneModel(resourceManager, domain, resourcePath, modelId);
        } catch (Exception e) {
            if (MyCTMLib.debugMode) {
                String attemptedPath = "assets/" + modelLoc.getResourceDomain() + "/" + modelLoc.getResourcePath();
                DebugErrorCollector.getInstance()
                    .add("model", modelId, attemptedPath, e);
                MyCTMLib.LOG.warn("Model load failed: " + attemptedPath, e);
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
                .put(TextureKeyNormalizer.normalizeDomain(modelId), data);
            prefillTextureRegistryForModel(resourceManager, domain, data);
        }
    }

    /**
     * 根据模型引用的纹理路径预填充 TextureRegistry。
     * 若已存在则跳过；若不存在则尝试加载纹理资源，有 ctmlib mcmeta 则注册；纹理不存在则静默跳过。
     */
    private void prefillTextureRegistryForModel(IResourceManager resourceManager, String modelDomain,
        ModelData data) {
        Map<String, String> textures = data.getTextures();
        if (textures == null || textures.isEmpty()) return;
        Set<String> resolvedPaths = new HashSet<>();
        for (String value : textures.values()) {
            if (value == null) continue;
            String resolved = value.startsWith("#") ? TextureKeyNormalizer.resolveTexturePath(value, textures) : value;
            if (resolved != null && !resolved.startsWith("#")) {
                resolvedPaths.add(resolved);
            }
        }
        TextureRegistry texReg = TextureRegistry.getInstance();
        for (String texturePath : resolvedPaths) {
            String lookupKey = TextureKeyNormalizer.toCanonicalTextureKey(modelDomain, texturePath);
            if (lookupKey == null) continue;
            if (texReg.get(lookupKey) != null) continue;
            try {
                ResourceLocation texRes = toTextureResourceLocation(modelDomain, texturePath);
                if (texRes == null) continue;
                IResource resource = resourceManager.getResource(texRes);
                IMetadataSection sec = resource.getMetadata("ctmlib");
                if (sec instanceof TextureMetadataSection tms) {
                    texReg.put(lookupKey, tms.getData());
                }
            } catch (IOException e) {
                if (MyCTMLib.debugMode) {
                    ResourceLocation texRes = toTextureResourceLocation(modelDomain, texturePath);
                    String attemptedPath = texRes != null
                        ? "assets/" + texRes.getResourceDomain() + "/" + texRes.getResourcePath()
                        : null;
                    DebugErrorCollector.getInstance()
                        .add("texture_prefill", lookupKey, attemptedPath, e);
                }
            } catch (Exception e) {
                if (MyCTMLib.debugMode) {
                    ResourceLocation texRes = toTextureResourceLocation(modelDomain, texturePath);
                    String attemptedPath = texRes != null
                        ? "assets/" + texRes.getResourceDomain() + "/" + texRes.getResourcePath()
                        : null;
                    DebugErrorCollector.getInstance()
                        .add("texture_prefill", lookupKey, attemptedPath, e);
                }
            }
        }
    }

    /**
     * 将模型纹理路径转为 ResourceLocation。
     * 如 ic2:block/blockAlloyGlass&0 → (ic2, textures/blocks/blockAlloyGlass&0)。
     */
    private static ResourceLocation toTextureResourceLocation(String modelDomain, String texturePath) {
        String canonical = TextureKeyNormalizer.toCanonicalTextureKey(modelDomain, texturePath);
        if (canonical == null) return null;
        int colon = canonical.indexOf(':');
        if (colon < 0) return null;
        String domain = canonical.substring(0, colon);
        String pathPart = canonical.substring(colon + 1);
        if (pathPart.isEmpty()) return null;
        String resourcePath = "textures/" + pathPart + ".png";
        return new ResourceLocation(domain, resourcePath);
    }
}

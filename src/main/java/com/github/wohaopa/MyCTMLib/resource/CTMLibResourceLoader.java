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
        ResourceLoadTrace.getInstance()
            .clear();
        ResourceLoadTrace.getInstance()
            .add("reload_start", "", null, true);

        if (!(resourceManager instanceof IReloadableResourceManager)) {
            return;
        }

        doLoad(resourceManager);
        loaded = true;
        ResourceLoadTrace.getInstance()
            .add("reload_end", "", null, true);

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
                String attemptedPath = "assets/" + blockstateLoc.getResourceDomain() + "/" + blockstateLoc.getResourcePath();
                try {
                    IResource res = resourceManager.getResource(blockstateLoc);
                    try (InputStream in = res.getInputStream()) {
                        blockStateParser.parseAndRegister(TextureKeyNormalizer.normalizeDomain(blockId), in);
                    }
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, true);
                } catch (IOException e) {
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, false, null, e);
                    if (MyCTMLib.debugMode) {
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                } catch (Exception e) {
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, false, null, e);
                    if (MyCTMLib.debugMode) {
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                }
            }
        } catch (Throwable t) {
            ResourceLoadTrace.getInstance()
                .add("blockstate_scan", "blockstate_scan", null, false, null, t);
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
                ResourceLoadTrace.getInstance()
                    .add("model_file", modelId, null, false, null, t);
                if (MyCTMLib.debugMode) {
                    DebugErrorCollector.getInstance()
                        .add("model", modelId, t);
                    MyCTMLib.LOG.warn("Model load failed: " + modelId, t);
                }
            }
        }
    }

    /**
     * 按 modelId（如 "modid:block/stone" 或 "modid:item/diamond"）加载并解析模型，写入 ModelRegistry。
     * 标准路径：assets/&lt;domain&gt;/models/block/&lt;name&gt;.json 或 models/item/&lt;name&gt;.json。
     */
    public void loadModel(IResourceManager resourceManager, String modelId) {
        int colon = modelId.indexOf(':');
        String domain = colon >= 0 ? modelId.substring(0, colon).toLowerCase(Locale.ROOT) : "minecraft";
        String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
        String resourcePath = (path.startsWith("block/") || path.startsWith("item/"))
            ? "models/" + path + ".json"
            : "models/block/" + path + ".json";
        ResourceLocation modelLoc = new ResourceLocation(domain, resourcePath);
        String attemptedPath = "assets/" + modelLoc.getResourceDomain() + "/" + modelLoc.getResourcePath();
        try {
            tryLoadOneModel(resourceManager, domain, resourcePath, modelId);
            ResourceLoadTrace.getInstance()
                .add("model_file", modelId, attemptedPath, true);
        } catch (Exception e) {
            ResourceLoadTrace.getInstance()
                .add("model_file", modelId, attemptedPath, false, null, e);
            if (MyCTMLib.debugMode) {
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
            if (MyCTMLib.debugMode && data.getTextures() != null && !data.getTextures()
                .isEmpty()) {
                MyCTMLib.LOG.info("[CTMLibFusion] prefillTextureRegistry ENTRY modelId={} textures={}", modelId,
                    data.getTextures());
            }
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
        if (MyCTMLib.debugMode && !resolvedPaths.isEmpty()) {
            MyCTMLib.LOG.info("[CTMLibFusion] prefillTextureRegistry modelDomain={} resolvedPaths={}", modelDomain,
                resolvedPaths);
        }
        TextureRegistry texReg = TextureRegistry.getInstance();
        for (String texturePath : resolvedPaths) {
            String lookupKey = TextureKeyNormalizer.toCanonicalTextureKey(modelDomain, texturePath);
            if (lookupKey == null) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("[CTMLibFusion] prefillTextureRegistry lookupKey=null modelDomain={} texturePath={}",
                        modelDomain, texturePath);
                }
                continue;
            }
            if (texReg.get(lookupKey) != null) {
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill_skip", lookupKey, null, true);
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.debug("[CTMLibFusion] prefillTextureRegistry skip existing lookupKey={}", lookupKey);
                }
                continue;
            }
            ResourceLocation texRes = toTextureResourceLocation(modelDomain, texturePath);
            if (texRes == null) {
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill", lookupKey, null, false);
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("[CTMLibFusion] prefillTextureRegistry texRes=null modelDomain={} texturePath={} lookupKey={}",
                        modelDomain, texturePath, lookupKey);
                }
                continue;
            }
            String fullPath = "assets/" + texRes.getResourceDomain() + "/" + texRes.getResourcePath();
            if (MyCTMLib.debugMode) {
                MyCTMLib.LOG.info("[CTMLibFusion] prefillTextureRegistry try resource domain={} path={} full={}",
                    texRes.getResourceDomain(), texRes.getResourcePath(),
                    "assets/" + texRes.getResourceDomain() + "/" + texRes.getResourcePath());
            }
            try {
                IResource resource = resourceManager.getResource(texRes);
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.info("[CTMLibFusion] prefillTextureRegistry resource found for lookupKey={}", lookupKey);
                }
                IMetadataSection sec;
                try {
                    sec = resource.getMetadata("ctmlib");
                } catch (Exception deserEx) {
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill", lookupKey, fullPath, false, null, deserEx);
                    MyCTMLib.LOG.warn("[CTMLibFusion] prefillTextureRegistry ctmlib deserialize failed lookupKey={} path={}",
                        lookupKey, fullPath, deserEx);
                    DebugErrorCollector.getInstance()
                        .add("texture_prefill_deserialize", lookupKey, fullPath, deserEx);
                    continue;
                }
                if (sec instanceof TextureMetadataSection tms) {
                    texReg.put(lookupKey, tms.getData());
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill_ok", lookupKey, fullPath, true);
                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG.info("[CTMLibFusion] prefillTextureRegistry REGISTERED lookupKey={}", lookupKey);
                    }
                } else {
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill_skip", lookupKey, fullPath, true);
                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG.debug("[CTMLibFusion] prefillTextureRegistry no ctmlib metadata lookupKey={} sec={}",
                            lookupKey, sec != null ? sec.getClass()
                                .getName() : "null");
                    }
                }
            } catch (IOException e) {
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill", lookupKey, fullPath, false, null, e);
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("[CTMLibFusion] prefillTextureRegistry resource not found lookupKey={} path={}",
                        lookupKey, fullPath);
                }
                DebugErrorCollector.getInstance()
                    .add("texture_prefill", lookupKey, fullPath, e);
            } catch (Exception e) {
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill", lookupKey, fullPath, false, null, e);
                MyCTMLib.LOG.warn("[CTMLibFusion] prefillTextureRegistry unexpected lookupKey={} path={}", lookupKey,
                    fullPath, e);
                DebugErrorCollector.getInstance()
                    .add("texture_prefill", lookupKey, fullPath, e);
            }
        }
    }

    /**
     * 将模型纹理路径转为 ResourceLocation。
     *
     * <h2>Minecraft 模型纹理路径规范</h2>
     * <p>
     * 根据 Minecraft 官方模型系统规范，模型 JSON 中的纹理引用格式为：
     * </p>
     * <pre>{@code
     * {
     *   "textures": {
     *     "all": "block/stone",           // Minecraft 原生：block/ 前缀
     *     "layer0": "item/diamond",       // Minecraft 原生：item/ 前缀
     *     "default": "ic2:block/xxx"      // Mod 纹理：modid:block/ 前缀
     *   }
     * }
     * }</pre>
     * <p>
     * 纹理实际文件路径为：
     * </p>
     * <pre>
     * assets/&lt;namespace&gt;/textures/&lt;path&gt;.png
     *                             ^^^^^^
     *                    必须包含 block/ 或 item/ 前缀
     * </pre>
     * <p>
     * 例如：
     * </p>
     * <ul>
     *   <li>{@code "block/stone"} → {@code assets/minecraft/textures/block/stone.png}</li>
     *   <li>{@code "ic2:block/xxx"} → {@code assets/ic2/textures/block/xxx.png}</li>
     * </ul>
     *
     * <h2>CTMLib 的规范化处理</h2>
     * <p>
     * {@link TextureKeyNormalizer#toCanonicalTextureKey(String, String)} 会将路径规范化为：
     * </p>
     * <ul>
     *   <li>{@code "block/xxx"} → {@code "blocks/xxx"} (单数 → 复数)</li>
     *   <li>{@code "item/xxx"} → {@code "items/xxx"} (单数 → 复数)</li>
     * </ul>
     *
     * <h2>转换示例</h2>
     * <pre>
     * 输入：modelDomain = "ic2", texturePath = "ic2:block/blockAlloyGlass"
     * ↓
     * canonical = "ic2:blocks/blockAlloyGlass"  (block/ → blocks/)
     * ↓
     * pathPart = "blocks/blockAlloyGlass"
     * ↓
     * resourcePath = "textures/" + pathPart + ".png"
     *              = "textures/blocks/blockAlloyGlass.png"
     * ↓
     * 返回：ResourceLocation("ic2", "textures/blocks/blockAlloyGlass.png")
     * </pre>
     *
     * @param modelDomain   模型所在的 domain（如 "minecraft", "ic2", "gregtech"）
     * @param texturePath   模型 textures 对象中的值（如 "block/stone", "ic2:block/xxx"）
     * @return 用于 ResourceManager 查找纹理的 ResourceLocation
     * @see TextureKeyNormalizer#toCanonicalTextureKey(String, String)
     */
    private static ResourceLocation toTextureResourceLocation(String modelDomain, String texturePath) {
        String canonical = TextureKeyNormalizer.toCanonicalTextureKey(modelDomain, texturePath);
        if (canonical == null) return null;
        int colon = canonical.indexOf(':');
        if (colon < 0) return null;
        String domain = canonical.substring(0, colon);
        String pathPart = canonical.substring(colon + 1);
        if (pathPart.isEmpty()) return null;
        // 此时 pathPart 格式为 "blocks/xxx"、"items/xxx" 或 "iconsets/xxx" 等
        // 构建完整资源路径：textures/blocks/xxx.png 或 textures/items/xxx.png
        String resourcePath = "textures/" + pathPart + ".png";
        return new ResourceLocation(domain, resourcePath);
    }
}

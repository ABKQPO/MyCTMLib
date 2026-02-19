package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmReplaceMap;
import static com.github.wohaopa.MyCTMLib.Textures.gtBWBlocksGlassCTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtBlockCasings4CTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtGregtechMetaCasingBlocks3CTM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.CTMConfig;
import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.InterpolatedIcon;
import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.NewTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.resource.BlockTextureDumpUtil;
import com.github.wohaopa.MyCTMLib.resource.DebugErrorCollector;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureMetadataSection;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.google.gson.JsonObject;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap extends AbstractTexture implements ITickableTextureObject, IIconRegister {

    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow
    protected abstract ResourceLocation completeResourceLocation(ResourceLocation location, int type);

    @Shadow
    @Final
    private String basePath;

    private TextureKeyNormalizer.TextureCategory getAtlasCategory() {
        return (basePath != null && (basePath.contains("items")))
            ? TextureKeyNormalizer.TextureCategory.ITEMS
            : TextureKeyNormalizer.TextureCategory.BLOCKS;
    }

    private void registerCanonicalToMapKey(String mapKey) {
        if (mapKey == null) return;
        TextureRegistry.getInstance()
            .putCanonicalToMapKey(TextureKeyNormalizer.toCanonicalTextureKey(mapKey), mapKey, getAtlasCategory());
    }

    @Inject(
        method = "registerIcon",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;<init>(Ljava/lang/String;)V"),
        cancellable = true)
    private void onRegisterIcon(String textureName, CallbackInfoReturnable<IIcon> cir) {
        try {
            TextureAtlasSprite currentBase = null;
            TextureAtlasSprite currentCTM = null;
            TextureAtlasSprite currentAlt = null;
            IResource resource = getResourceFromTextureName(textureName);

            if (basePath.contains("textures\\items") || basePath.contains("textures/items")) {
                return;
            }

            // 新管线：若存在 ctmlib section 则写入 TextureRegistry（用 canonicalKey），并替换 sprite 为整张连接图（否则 block 拿到的是 16x16）
            boolean hadCtmlib = false;
            try {
                IMetadataSection ctmlibSec = resource.getMetadata("ctmlib");
                if (ctmlibSec != null && ctmlibSec instanceof TextureMetadataSection) {
                    String canonicalKey = TextureKeyNormalizer.toCanonicalTextureKey(textureName);
                    if (canonicalKey != null) {
                        TextureRegistry.getInstance()
                            .put(canonicalKey, ((TextureMetadataSection) ctmlibSec).getData());
                    }
                    hadCtmlib = true;
                }
            } catch (Exception e) {
                if (MyCTMLib.debugMode) {
                    DebugErrorCollector.getInstance()
                        .add("texture_mixin", textureName, e);
                }
            }

            if (!(resource instanceof SimpleResource simple)) {
                if (hadCtmlib) {
                    TextureAtlasSprite sprite = new NewTextureAtlasSprite(textureName);
                    mapRegisteredSprites.put(textureName, sprite);
                    registerCanonicalToMapKey(textureName);
                    cir.setReturnValue(sprite);
                    cir.cancel();
                }
                return;
            }

            IMetadataSection myctmlibSec = resource.getMetadata("myctmlib");
            JsonObject ctmObj = (myctmlibSec != null && myctmlibSec instanceof MyCTMLibMetadataSection)
                ? ((MyCTMLibMetadataSection) myctmlibSec).getJson()
                : null;

            if (ctmObj == null) {
                if (hadCtmlib) {
                    TextureAtlasSprite sprite = new NewTextureAtlasSprite(textureName);
                    mapRegisteredSprites.put(textureName, sprite);
                    registerCanonicalToMapKey(textureName);
                    cir.setReturnValue(sprite);
                    cir.cancel();
                }
                return;
            }

            CTMIconManager.Builder builder = CTMIconManager.builder();
            CTMConfig config = new CTMConfig(ctmObj);

            currentBase = useInterpolation(simple) ? new InterpolatedIcon(textureName)
                : new NewTextureAtlasSprite(textureName);
            builder.setIconSmall(currentBase);
            mapRegisteredSprites.put(textureName, currentBase);
            registerCanonicalToMapKey(textureName);

            if (config.connectionTexture != null) {

                // 修复代码
                updateGTNHFlags(config.connectionTexture);

                try {
                    IResource resourceCTM = getResourceFromJson(ctmObj, "connection");

                    if (resourceCTM instanceof SimpleResource simpleCTM) {
                        currentCTM = useInterpolation(simpleCTM) ? new InterpolatedIcon(config.connectionTexture)
                            : new NewTextureAtlasSprite(config.connectionTexture);
                        mapRegisteredSprites.put(config.connectionTexture, currentCTM);
                        registerCanonicalToMapKey(config.connectionTexture);
                        builder.setIconCTM(currentCTM);
                    }

                } catch (IOException ignored) {}
            }

            if (!config.randomTextures.isEmpty()) {

                List<String> processedTextures = config.randomTextures;
                List<CTMIconManager> randomManagers = new ArrayList<>();

                // 对random和connection同时存在的情况处理
                if (config.connectionTexture != null) {
                    for (String processedTexture : processedTextures) {
                        if (!processedTexture.contains("_ctm")) {
                            continue;
                        }

                        String baseTextureName = processedTexture.replace("_ctm", "");

                        // 配对随机纹理（IconCTM和IconSmall）
                        if (!processedTextures.contains(baseTextureName)) {
                            continue;
                        }

                        TextureAtlasSprite baseSprite = new NewTextureAtlasSprite(baseTextureName);
                        TextureAtlasSprite randomSprite = new NewTextureAtlasSprite(processedTexture);
                        mapRegisteredSprites.put(baseTextureName, baseSprite);
                        registerCanonicalToMapKey(baseTextureName);
                        mapRegisteredSprites.put(processedTexture, randomSprite);
                        registerCanonicalToMapKey(processedTexture);

                        randomManagers.add(
                            CTMIconManager.builder()
                                .setIconSmall(baseSprite)
                                .setIconCTM(randomSprite)
                                .buildAndInit());
                    }

                }

                // 单独的random字段处理
                if (config.connectionTexture == null) {

                    for (String processedTexture : processedTextures) {
                        TextureAtlasSprite randomSprite = new NewTextureAtlasSprite(processedTexture);
                        mapRegisteredSprites.put(processedTexture, randomSprite);
                        registerCanonicalToMapKey(processedTexture);

                        randomManagers.add(
                            CTMIconManager.builder()
                                .setIconSmall(randomSprite)
                                .buildAndInit());
                    }
                }

                if (!randomManagers.isEmpty()) {
                    ctmRandomMap.put(textureName, randomManagers);
                }

            }

            if (config.altTexture != null) {
                try {
                    IResource resourceAlt = getResourceFromJson(ctmObj, "alt");

                    if (resourceAlt instanceof SimpleResource simpleAlt) {
                        currentAlt = useInterpolation(simpleAlt) ? new InterpolatedIcon(config.altTexture)
                            : new NewTextureAtlasSprite(config.altTexture);

                        mapRegisteredSprites.put(config.altTexture, currentAlt);
                        registerCanonicalToMapKey(config.altTexture);
                        builder.setIconAlt(currentAlt);
                        ctmAltMap.put(textureName, currentAlt.getIconName());
                    }

                } catch (IOException ignored) {}

            }

            if (!config.equivalents.isEmpty()) {
                ctmReplaceMap.put(textureName, config.equivalents.toArray(new String[0]));
            }

            CTMIconManager ctmManager = builder.buildAndInit();
            ctmIconMap.put(textureName, ctmManager);

            cir.setReturnValue(currentBase);
        } catch (Exception e) {
            // System.out.println("[CTMLibFusion] Error: " + e.getMessage());
        }
    }

    /**
     * 修复代码所用的方法，移动到这里
     */
    public void updateGTNHFlags(String connectionTexture) {
        if (connectionTexture.startsWith("gregtech:iconsets/MACHINE_CASING_FUSION_")
            && connectionTexture.endsWith("_ctm")
            && Loader.isModLoaded("gregtech")) {
            gtBlockCasings4CTM = true;
        }

        if (connectionTexture.startsWith("miscutils:iconsets/MACHINE_CASING_FUSION_")
            && connectionTexture.endsWith("_ctm")
            && Loader.isModLoaded("gregtech")) {
            gtGregtechMetaCasingBlocks3CTM = true;
        }

        if (connectionTexture.contains("BoronSilicateGlass") && connectionTexture.endsWith("_ctm")
            && Loader.isModLoaded("gregtech")) {
            gtBWBlocksGlassCTM = true;
        }
    }

    /**
     * 判断是否应该使用插值纹理
     */
    public boolean useInterpolation(SimpleResource simple) {
        if (simple.getMetadata("animation") == null) {
            return false;
        }

        JsonObject animationObj = ((AccessorSimpleResource) simple).getMcMetaJson()
            .getAsJsonObject("animation");
        return animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
            .getAsBoolean();
    }

    /**
     * 从JSON对象中获取资源
     */
    public IResource getResourceFromJson(JsonObject ctmObj, String fieldName) throws IOException {
        ResourceLocation res = completeResourceLocation(
            new ResourceLocation(
                ctmObj.getAsJsonPrimitive(fieldName)
                    .getAsString()),
            0);
        return Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(res);
    }

    /**
     * 图集加载完成后打出三个 Registry 的 size 摘要（仅 debug 模式，避免刷屏）。
     */
    @Inject(method = "loadTextureAtlas", at = @At("RETURN"))
    private void afterLoadTextureAtlas(net.minecraft.client.resources.IResourceManager p_110571_1_, CallbackInfo ci) {
        if (MyCTMLib.debugMode) {
            BlockStateRegistry.getInstance()
                .dumpForDebug();
            ModelRegistry.getInstance()
                .dumpForDebug();
            TextureRegistry.getInstance()
                .dumpForDebug();
        }
        if (MyCTMLib.dumpBlockTextureMapping && basePath != null
            && (basePath.contains("blocks") && !basePath.contains("items"))) {
            File dumpFile = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_block_texture_dump.json");
            BlockTextureDumpUtil.dumpToFile(dumpFile);
        }
    }

    /**
     * 纹理构建阶段：RuntimeException 时 FML 会调用 trackBrokenTexture。
     * 对 stone/cobblestone 打点确认是否在 loadTextureAtlas 的 catch 中失败。
     */
    @Redirect(
        method = "loadTextureAtlas",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/client/FMLClientHandler;trackBrokenTexture(Lnet/minecraft/util/ResourceLocation;Ljava/lang/String;)V"))
    public void onTrackBrokenTexture(FMLClientHandler handler, ResourceLocation location, String message) {
        String path = location != null ? location.getResourcePath() : "";
        if ("stone".equals(path) || "cobblestone".equals(path)) {
            com.github.wohaopa.MyCTMLib.MyCTMLib.LOG
                .warn("[CTMLibFusion] loadTextureAtlas RuntimeException path={} message={}", location, message);
        }
        handler.trackBrokenTexture(location, message);
    }

    /**
     * 纹理构建阶段：IOException 时 FML 会调用 trackMissingTexture。
     */
    @Redirect(
        method = "loadTextureAtlas",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/client/FMLClientHandler;trackMissingTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    public void onTrackMissingTexture(FMLClientHandler handler, ResourceLocation location) {
        String path = location != null ? location.getResourcePath() : "";
        if ("stone".equals(path) || "cobblestone".equals(path)) {
            com.github.wohaopa.MyCTMLib.MyCTMLib.LOG
                .warn("[CTMLibFusion] loadTextureAtlas IOException trackMissingTexture path={}", location);
        }
        handler.trackMissingTexture(location);
    }

    /**
     * 从纹理名称获取资源
     */
    public IResource getResourceFromTextureName(String textureName) throws IOException {
        ResourceLocation res = completeResourceLocation(new ResourceLocation(textureName), 0);
        return Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(res);
    }
}

package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmReplaceMap;
import static com.github.wohaopa.MyCTMLib.Textures.gtBWBlocksGlassCTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtBlockCasings4CTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtGregtechMetaCasingBlocks3CTM;

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
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.CTMConfig;
import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.InterpolatedIcon;
import com.github.wohaopa.MyCTMLib.NewTextureAtlasSprite;
import com.google.gson.JsonObject;

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

            if (!(resource instanceof SimpleResource simple)) {
                return;
            }

            if (simple.getMetadata("myctmlib") == null) {
                return;
            }

            CTMIconManager.Builder builder = CTMIconManager.builder();
            JsonObject ctmObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                .getAsJsonObject("myctmlib");
            CTMConfig config = new CTMConfig(ctmObj);

            currentBase = useInterpolation(simple) ? new InterpolatedIcon(textureName)
                : new NewTextureAtlasSprite(textureName);
            builder.setIconSmall(currentBase);
            mapRegisteredSprites.put(textureName, currentBase);

            if (config.connectionTexture != null) {

                // 修复代码
                updateGTNHFlags(config.connectionTexture);

                try {
                    IResource resourceCTM = getResourceFromJson(ctmObj, "connection");

                    if (resourceCTM instanceof SimpleResource simpleCTM) {
                        currentCTM = useInterpolation(simpleCTM) ? new InterpolatedIcon(config.connectionTexture)
                            : new NewTextureAtlasSprite(config.connectionTexture);
                        mapRegisteredSprites.put(config.connectionTexture, currentCTM);
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
                        mapRegisteredSprites.put(processedTexture, randomSprite);

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

                        randomManagers.add(
                            CTMIconManager.builder()
                                .setIconSmall(currentBase)
                                .setIconCTM(randomSprite)
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
        } catch (Exception ignored) {}
    }

    /**
     * 修复代码所用的方法，移动到这里
     */
    private void updateGTNHFlags(String connectionTexture) {
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
    private boolean useInterpolation(SimpleResource simple) {
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
    private IResource getResourceFromJson(JsonObject ctmObj, String fieldName) throws IOException {
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
     * 从纹理名称获取资源
     */
    private IResource getResourceFromTextureName(String textureName) throws IOException {
        ResourceLocation res = completeResourceLocation(new ResourceLocation(textureName), 0);
        return Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(res);
    }
}

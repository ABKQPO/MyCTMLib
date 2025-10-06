package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.*;

import com.github.wohaopa.MyCTMLib.CTMConfig;
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
            TextureAtlasSprite currentCTM = null;
            TextureAtlasSprite currentAlt = null;
            ResourceLocation res = completeResourceLocation(new ResourceLocation(textureName), 0);
            IResource resource = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(res);

            if (basePath.contains("textures\\items") || basePath.contains("textures/items")) {
                return;
            }

            if (!(resource instanceof SimpleResource simple)) {
                return;
            }

            if (simple.getMetadata("myctmlib") == null) {
                return;
            }

            TextureAtlasSprite currentBase = new NewTextureAtlasSprite(textureName);
            mapRegisteredSprites.put(textureName, currentBase);

            if (simple.getMetadata("animation") != null) {
                JsonObject animationObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                    .getAsJsonObject("animation");
                if (animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
                    .getAsBoolean()) {
                    InterpolatedIcon interpolatedIcon = new InterpolatedIcon(textureName);
                    mapRegisteredSprites.put(textureName, interpolatedIcon);
                    currentBase = interpolatedIcon;
                }
            }

            JsonObject ctmObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                .getAsJsonObject("myctmlib");

            CTMConfig config = new CTMConfig(ctmObj);

            if (config.connectionTexture != null) {
                
                // 修复代码
                updateGTNHFlags(config.connectionTexture);

                currentCTM = new NewTextureAtlasSprite(config.connectionTexture);
                mapRegisteredSprites.put(config.connectionTexture, currentCTM);
                try {
                    ResourceLocation resCTM = completeResourceLocation(new ResourceLocation(ctmObj.getAsJsonPrimitive("connection").getAsString()), 0);
                    IResource resourceCTM = Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(resCTM);

                    if (resourceCTM instanceof SimpleResource simpleCTM) {
                        if (simpleCTM.getMetadata("animation") != null) {
                            JsonObject animationObjCTM = ((AccessorSimpleResource) simpleCTM).getMcMetaJson()
                                .getAsJsonObject("animation");
                            if (animationObjCTM.has("interpolate") && animationObjCTM.getAsJsonPrimitive("interpolate")
                                .getAsBoolean()) {
                                InterpolatedIcon interpolatedIconCTM = new InterpolatedIcon(config.connectionTexture);
                                mapRegisteredSprites.put(config.connectionTexture, interpolatedIconCTM);

                                currentCTM = interpolatedIconCTM;
                            }
                        }
                    }
                } catch (IOException ignored) {}

                // 处理随机纹理，仅针对CTM纹理
                if (!config.randomTextures.isEmpty()) {
                    System.out.println("[CTM_Random_0] Found " + config.randomTextures.size() + " random textures");
                    List<String> processedTextures = config.randomTextures;

                    // 生成randomManager数组
                    List<CTMIconManager> randomManagers = new ArrayList<>();

                    for (String processedTexture : processedTextures) {
                        // 如果包含"ctm"，查找对应的基础纹理
                        if (processedTexture.contains("_ctm")) {
                            // 生成对应的基础纹理名称（去掉_ctm）
                            String baseTextureName = processedTexture.replace("_ctm", "");

                            // 在已处理的纹理中查找对应的基础纹理
                            if (processedTextures.contains(baseTextureName)) {
                                System.out
                                    .println("[CTM_Random_Pair] " + baseTextureName + " <-> " + processedTexture);

                                // 创建并注册纹理
                                TextureAtlasSprite baseSprite = new NewTextureAtlasSprite(baseTextureName);
                                TextureAtlasSprite ctmSprite = new NewTextureAtlasSprite(processedTexture);
                                mapRegisteredSprites.put(baseTextureName, baseSprite);
                                mapRegisteredSprites.put(processedTexture, ctmSprite);

                                // 配对成功，创建CTMIconManager
                                CTMIconManager.Builder builder = CTMIconManager.builder()
                                    .setIconSmall(baseSprite)
                                    .setIconCTM(ctmSprite);
                                if (currentAlt != null) {
                                    builder.setIconAlt(currentAlt);
                                }
                                CTMIconManager randomManager = builder.buildAndInit();
                                randomManagers.add(randomManager);

                                System.out.println(
                                    "[CTM_Random_Manager] Created manager for: " + baseTextureName
                                        + " <-> "
                                        + processedTexture);
                            }
                        }
                    }

                    // 循环结束后，将randomManagers注册到ctmRandomMap
                    if (!randomManagers.isEmpty()) {
                        ctmRandomMap.put(textureName, randomManagers);
                        System.out.println(
                            "[CTM_Random_Register] Registered " + randomManagers.size()
                                + " random managers for: "
                                + textureName);
                    }
                }
            }

            if (config.altTexture != null) {
                currentAlt = new NewTextureAtlasSprite(config.altTexture);
                mapRegisteredSprites.put(config.altTexture, currentAlt);
                try {
                    ResourceLocation resAlt = completeResourceLocation(new ResourceLocation(ctmObj.getAsJsonPrimitive("alt").getAsString()), 0);
                    IResource resourceAlt = Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(resAlt);

                    if (resourceAlt instanceof SimpleResource simpleAlt) {
                        if (simpleAlt.getMetadata("animation") != null) {
                            JsonObject animationObjAlt = ((AccessorSimpleResource) simpleAlt).getMcMetaJson()
                                .getAsJsonObject("animation");
                            if (animationObjAlt.has("interpolate") && animationObjAlt.getAsJsonPrimitive("interpolate")
                                .getAsBoolean()) {
                                InterpolatedIcon interpolatedIconAlt = new InterpolatedIcon(config.altTexture);
                                mapRegisteredSprites.put(config.altTexture, interpolatedIconAlt);

                                currentAlt = interpolatedIconAlt;
                            }
                        }
                    }
                } catch (IOException ignored) {}
            }

            if (!config.equivalents.isEmpty()) {
                ctmReplaceMap.put(textureName, config.equivalents.toArray(new String[0]));
            }

            // 创建基础CTMIconManager
            CTMIconManager.Builder builder = CTMIconManager.builder()
                .setIconSmall(currentBase);
            if (currentCTM != null) {
                builder.setIconCTM(currentCTM);
            }
            if (currentAlt != null) {
                builder.setIconAlt(currentAlt);
                ctmAltMap.put(textureName, currentAlt.getIconName());
            }
            CTMIconManager ctmManager = builder.build();

            // 添加到映射表
            if (currentCTM != null) {
                ctmIconMap.put(textureName, ctmManager);
            }
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
}

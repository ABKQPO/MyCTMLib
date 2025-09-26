package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.*;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

            if (resource instanceof SimpleResource simple) {

                if (simple.getMetadata("myctmlib") != null) {
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

                    JsonPrimitive connectionPrimitive = ctmObj.getAsJsonPrimitive("connection");
                    String connectTexture = null;
                    if (connectionPrimitive != null) {
                        connectTexture = connectionPrimitive.getAsString();
                    }

                    if (connectTexture != null && !connectTexture.isEmpty()) {
                        String connectTextureName = connectTexture.replace("minecraft:", "")
                            .replace("textures/blocks/", "")
                            .replace(".png", "");

                        // 修复代码
                        if (connectTextureName.startsWith("gregtech:iconsets/MACHINE_CASING_FUSION_")
                            && connectTexture.endsWith("_ctm")
                            && Loader.isModLoaded("gregtech")) {
                            gtBlockCasings4CTM = true;
                        }

                        if (connectTextureName.startsWith("miscutils:iconsets/MACHINE_CASING_FUSION_")
                            && connectTexture.endsWith("_ctm")
                            && Loader.isModLoaded("gregtech")) {
                            gtGregtechMetaCasingBlocks3CTM = true;
                        }

                        if (connectTexture.contains("BoronSilicateGlass") && connectTextureName.endsWith("_ctm")
                            && Loader.isModLoaded("gregtech")) {
                            gtBWBlocksGlassCTM = true;
                        }

                        currentCTM = new NewTextureAtlasSprite(connectTextureName);
                        mapRegisteredSprites.put(connectTextureName, currentCTM);
                        try {
                            ResourceLocation resCTM = completeResourceLocation(new ResourceLocation(connectTexture), 0);
                            IResource resourceCTM = Minecraft.getMinecraft()
                                .getResourceManager()
                                .getResource(resCTM);

                            if (resourceCTM instanceof SimpleResource simpleCTM) {
                                if (simpleCTM.getMetadata("animation") != null) {
                                    JsonObject animationObjCTM = ((AccessorSimpleResource) simpleCTM).getMcMetaJson()
                                        .getAsJsonObject("animation");
                                    if (animationObjCTM.has("interpolate")
                                        && animationObjCTM.getAsJsonPrimitive("interpolate")
                                            .getAsBoolean()) {
                                        InterpolatedIcon interpolatedIconCTM = new InterpolatedIcon(connectTextureName);
                                        mapRegisteredSprites.put(connectTextureName, interpolatedIconCTM);

                                        currentCTM = interpolatedIconCTM;
                                    }
                                }
                            }
                        } catch (IOException ignored) {}
                    }

                    JsonPrimitive altPrimitive = ctmObj.getAsJsonPrimitive("alt");
                    String altTexture = null;
                    if (altPrimitive != null) {
                        altTexture = altPrimitive.getAsString();
                    }

                    if (altTexture != null && !altTexture.isEmpty()) {
                        String altTextureName = altTexture.replace("minecraft:", "")
                            .replace("textures/blocks/", "")
                            .replace(".png", "");
                        currentAlt = new NewTextureAtlasSprite(altTextureName);
                        mapRegisteredSprites.put(altTextureName, currentAlt);
                        try {
                            ResourceLocation resAlt = completeResourceLocation(new ResourceLocation(altTexture), 0);
                            IResource resourceAlt = Minecraft.getMinecraft()
                                .getResourceManager()
                                .getResource(resAlt);

                            if (resourceAlt instanceof SimpleResource simpleAlt) {
                                if (simpleAlt.getMetadata("animation") != null) {
                                    JsonObject animationObjAlt = ((AccessorSimpleResource) simpleAlt).getMcMetaJson()
                                        .getAsJsonObject("animation");
                                    if (animationObjAlt.has("interpolate")
                                        && animationObjAlt.getAsJsonPrimitive("interpolate")
                                            .getAsBoolean()) {
                                        InterpolatedIcon interpolatedIconAlt = new InterpolatedIcon(altTextureName);
                                        mapRegisteredSprites.put(altTextureName, interpolatedIconAlt);

                                        currentAlt = interpolatedIconAlt;
                                    }
                                }
                            }
                        } catch (IOException ignored) {}
                    }

                    List<String> equivalents = new ArrayList<>();
                    if (ctmObj.has("equivalents")) {
                        JsonArray arr = ctmObj.getAsJsonArray("equivalents");
                        for (JsonElement el : arr) {
                            String eq = el.getAsString()
                                .replace("minecraft:", "")
                                .replace("textures/blocks/", "")
                                .replace(".png", "");
                            equivalents.add(eq);
                        }
                    }
                    if (!equivalents.isEmpty()) {
                        ctmReplaceMap.put(textureName, equivalents.toArray(new String[0]));
                    }
                    if (currentAlt != null) {
                        ctmIconMap.put(textureName, new CTMIconManager(currentBase, currentCTM, currentAlt));
                        ctmAltMap.put(textureName, currentAlt.getIconName());
                    } else if (currentCTM != null) {
                        ctmIconMap.put(textureName, new CTMIconManager(currentBase, currentCTM));
                    }
                    cir.setReturnValue(currentBase);
                }
            }
        } catch (Exception ignored) {}
    }
}

package com.github.wohaopa.MyCTMLib;

import static com.github.wohaopa.MyCTMLib.Textures.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.mixins.AccessorSimpleResource;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.common.Loader;

public class IC2TextureLoader {

    public static void handleTexture(IIconRegister iconRegister, String originName) {
        for (int side = 0; side < 6; ++side) {
            try {
                String textureName = originName + "&" + side + ".png";

                TextureAtlasSprite currentCTM = null;
                TextureAtlasSprite currentAlt = null;
                ResourceLocation res = new ResourceLocation(textureName);
                IResource resource = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(res);

                if (resource instanceof SimpleResource simple) {

                    if (simple.getMetadata("myctmlib") != null) {
                        TextureAtlasSprite currentBase = new NewTextureAtlasSprite(textureName);
                        ((TextureMap) iconRegister).setTextureEntry(textureName, currentBase);

                        if (simple.getMetadata("animation") != null) {
                            JsonObject animationObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                                .getAsJsonObject("animation");
                            if (animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
                                .getAsBoolean()) {
                                InterpolatedIcon interpolatedIcon = new InterpolatedIcon(textureName);
                                ((TextureMap) iconRegister).setTextureEntry(textureName, interpolatedIcon);
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
                            ((TextureMap) iconRegister).setTextureEntry(connectTextureName, currentCTM);
                            try {
                                ResourceLocation resCTM = new ResourceLocation(connectTexture);
                                IResource resourceCTM = Minecraft.getMinecraft()
                                    .getResourceManager()
                                    .getResource(resCTM);

                                if (resourceCTM instanceof SimpleResource simpleCTM) {
                                    if (simpleCTM.getMetadata("animation") != null) {
                                        JsonObject animationObjCTM = ((AccessorSimpleResource) simpleCTM)
                                            .getMcMetaJson()
                                            .getAsJsonObject("animation");
                                        if (animationObjCTM.has("interpolate")
                                            && animationObjCTM.getAsJsonPrimitive("interpolate")
                                                .getAsBoolean()) {
                                            InterpolatedIcon interpolatedIconCTM = new InterpolatedIcon(
                                                connectTextureName);
                                            ((TextureMap) iconRegister)
                                                .setTextureEntry(connectTextureName, interpolatedIconCTM);

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
                            ((TextureMap) iconRegister).setTextureEntry(altTextureName, currentAlt);
                            try {
                                ResourceLocation resAlt = new ResourceLocation(altTexture);
                                IResource resourceAlt = Minecraft.getMinecraft()
                                    .getResourceManager()
                                    .getResource(resAlt);

                                if (resourceAlt instanceof SimpleResource simpleAlt) {
                                    if (simpleAlt.getMetadata("animation") != null) {
                                        JsonObject animationObjAlt = ((AccessorSimpleResource) simpleAlt)
                                            .getMcMetaJson()
                                            .getAsJsonObject("animation");
                                        if (animationObjAlt.has("interpolate")
                                            && animationObjAlt.getAsJsonPrimitive("interpolate")
                                                .getAsBoolean()) {
                                            InterpolatedIcon interpolatedIconAlt = new InterpolatedIcon(altTextureName);
                                            ((TextureMap) iconRegister)
                                                .setTextureEntry(altTextureName, interpolatedIconAlt);

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
                            CTMIconManager manager = CTMIconManager.builder()
                                .setIconSmall(currentBase)
                                .setIconCTM(currentCTM)
                                .setIconAlt(currentAlt)
                                .buildAndInit();
                            ctmIconMap.put(textureName, manager);
                            ctmAltMap.put(textureName, currentAlt.getIconName());
                        } else if (currentCTM != null) {
                            CTMIconManager manager = CTMIconManager.builder()
                                .setIconSmall(currentBase)
                                .setIconCTM(currentCTM)
                                .buildAndInit();
                            ctmIconMap.put(textureName, manager);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}

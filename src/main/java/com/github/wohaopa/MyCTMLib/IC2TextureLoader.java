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

public class IC2TextureLoader {

    public static void handleTexture(IIconRegister iconRegister, String textureName) {
        for (int side = 0; side < 6; ++side) {
            try {
                String subName = textureName + "&" + side + ".png";

                ResourceLocation res = new ResourceLocation(subName);
                IResource resource = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(res);

                if (resource instanceof SimpleResource simple) {
                    if (simple.getMetadata("myctmlib") != null) {
                        subName = subName.replace("minecraft:", "")
                            .replace("textures/blocks/", "")
                            .replace(".png", "");
                        TextureAtlasSprite currentBase = new NewTextureAtlasSprite(subName);
                        ((TextureMap) iconRegister).setTextureEntry(subName, currentBase);

                        if (simple.getMetadata("animation") != null) {
                            JsonObject animationObj = ((AccessorSimpleResource) simple).getMcmetaJson()
                                .getAsJsonObject("animation");
                            if (animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
                                .getAsBoolean()) {
                                InterpolatedIcon interpolatedIcon = new InterpolatedIcon(subName);
                                ((TextureMap) iconRegister).setTextureEntry(subName, interpolatedIcon);
                                currentBase = interpolatedIcon;
                            }
                        }

                        JsonObject ctmObj = ((AccessorSimpleResource) simple).getMcmetaJson()
                            .getAsJsonObject("myctmlib");
                        String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                            .getAsString();
                        if (connectTexture.isEmpty()) return;

                        String connectTextureName = connectTexture.replace("minecraft:", "")
                            .replace("textures/blocks/", "")
                            .replace(".png", "");

                        TextureAtlasSprite currentCTM = new NewTextureAtlasSprite(connectTextureName);
                        ((TextureMap) iconRegister).setTextureEntry(connectTextureName, currentCTM);

                        try {
                            ResourceLocation resCTM = new ResourceLocation(connectTexture);
                            IResource resourceCTM = Minecraft.getMinecraft()
                                .getResourceManager()
                                .getResource(resCTM);

                            if (resourceCTM instanceof SimpleResource simpleCTM) {
                                if (simpleCTM.getMetadata("animation") != null) {
                                    JsonObject animationObjCTM = ((AccessorSimpleResource) simpleCTM).getMcmetaJson()
                                        .getAsJsonObject("animation");
                                    if (animationObjCTM.has("interpolate")
                                        && animationObjCTM.getAsJsonPrimitive("interpolate")
                                            .getAsBoolean()) {
                                        InterpolatedIcon interpolatedIconCTM = new InterpolatedIcon(connectTextureName);
                                        ((TextureMap) iconRegister)
                                            .setTextureEntry(connectTextureName, interpolatedIconCTM);
                                        currentCTM = interpolatedIconCTM;
                                    }
                                }
                            }
                        } catch (IOException ignored) {}

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
                            ctmReplaceMap.put(subName, equivalents.toArray(new String[0]));
                        }

                        ctmIconMap.put(subName, new CTMIconManager(currentBase, currentCTM));
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}

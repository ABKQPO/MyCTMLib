package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

import java.io.IOException;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.InterpolatedIcon;
import com.google.gson.JsonObject;

import ic2.core.IC2;
import ic2.core.block.BlockBase;
import ic2.core.block.BlockMetaData;
import ic2.core.init.InternalName;

@Mixin(value = BlockMetaData.class, remap = false)
public abstract class MixinBlockMetaData extends BlockBase {

    public MixinBlockMetaData(InternalName internalName1, Material material) {
        super(internalName1, material);
    }

    @Inject(
        method = "registerBlockIcons",
        at = @At(
            value = "INVOKE",
            target = "Lic2/core/block/BlockMetaData;getTextureFolder(I)Ljava/lang/String;",
            shift = At.Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beforeGetTextureFolder(IIconRegister iconRegister, CallbackInfo ci, int metaCount, int index) {
        String name = IC2.textureDomain + ":textures/blocks/" + this.getTextureName(index);
        for (int side = 0; side < 6; ++side) {
            try {
                String subName = name + "&" + side + ".png";

                ResourceLocation res = new ResourceLocation(subName);
                IResource resource = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(res);

                if (resource instanceof SimpleResource simple) {

                    if (simple.getMetadata("myctmlib") != null) {
                        TextureAtlasSprite currentBase = AccessorTextureAtlasSprite.newInstance(subName);
                        ((TextureMap) iconRegister).setTextureEntry(subName, currentBase);

                        if (simple.getMetadata("animation") != null) {
                            JsonObject animationObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                                .getAsJsonObject("animation");
                            if (animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
                                .getAsBoolean()) {
                                InterpolatedIcon interpolatedIcon = new InterpolatedIcon(subName);
                                ((TextureMap) iconRegister).setTextureEntry(subName, interpolatedIcon);
                                currentBase = interpolatedIcon;
                            }
                        }

                        JsonObject ctmObj = ((AccessorSimpleResource) simple).getMcMetaJson()
                            .getAsJsonObject("myctmlib");
                        String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                            .getAsString();

                        String connectTextureName = connectTexture.replace("minecraft:", "")
                            .replace("textures/blocks/", "")
                            .replace(".png", "");

                        TextureAtlasSprite currentCTM = AccessorTextureAtlasSprite.newInstance(connectTextureName);
                        ((TextureMap) iconRegister).setTextureEntry(connectTextureName, currentCTM);

                        try {
                            ResourceLocation resCTM = new ResourceLocation(connectTexture);
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
                                        ((TextureMap) iconRegister)
                                            .setTextureEntry(connectTextureName, interpolatedIconCTM);

                                        currentCTM = interpolatedIconCTM;
                                    }
                                }
                            }
                        } catch (IOException ignored) {}

                        ctmIconMap.put(subName, new CTMIconManager(currentBase, currentCTM));
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}

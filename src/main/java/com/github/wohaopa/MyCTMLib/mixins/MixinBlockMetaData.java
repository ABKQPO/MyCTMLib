package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

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

                if (resource instanceof SimpleResource simple && resource.getMetadata("myctmlib") != null) {
                    JsonObject mcmetaJson = ((SimpleResourceAccessor) simple).getMcmetaJson();

                    JsonObject ctmObj = mcmetaJson.getAsJsonObject("myctmlib");
                    String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                        .getAsString();

                    connectTexture = connectTexture.replace("minecraft:", "")
                        .replace("textures/blocks/", "")
                        .replace(".png", "");

                    subName = subName.replace("minecraft:", "")
                        .replace("textures/blocks/", "")
                        .replace(".png", "");

                    TextureAtlasSprite base = new TextureAtlasSprite(subName);
                    ((TextureMap) iconRegister).setTextureEntry(subName, base);

                    TextureAtlasSprite ctm = new TextureAtlasSprite(connectTexture);
                    ((TextureMap) iconRegister).setTextureEntry(connectTexture, ctm);

                    ctmIconMap.put(subName, new CTMIconManager(base, ctm));
                }
            } catch (Exception ignored) {}
        }
    }
}

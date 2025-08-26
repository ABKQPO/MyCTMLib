package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.IC2TextureLoader;

import ic2.core.IC2;
import ic2.core.block.BlockBase;
import ic2.core.block.BlockMetaData;
import ic2.core.block.BlockTextureStitched;
import ic2.core.init.InternalName;

@Mixin(value = BlockMetaData.class)
public abstract class MixinBlockMetaData extends BlockBase {

    public MixinBlockMetaData(InternalName internalName1, Material material) {
        super(internalName1, material);
    }

    @Inject(method = "registerBlockIcons", at = @At("HEAD"), cancellable = true)
    private void injectRegisterBlockIcons(IIconRegister iconRegister, CallbackInfo ci) {
        int metaCount = this.getMetaCount();
        this.textures = new IIcon[metaCount][6];

        for (int index = 0; index < metaCount; ++index) {
            String name = IC2.textureDomain + ":textures/blocks/" + this.getTextureName(index);
            IC2TextureLoader.handleTexture(iconRegister, name);

            String textureFolder = this.getTextureFolder(index);
            textureFolder = textureFolder == null ? "" : textureFolder + "/";
            String baseName = IC2.textureDomain + ":" + textureFolder + this.getTextureName(index);

            for (int side = 0; side < 6; ++side) {
                String subName = baseName + ":" + side;
                TextureAtlasSprite texture = new BlockTextureStitched(subName, side);
                this.textures[index][side] = texture;
                ((TextureMap) iconRegister).setTextureEntry(subName, texture);
            }
        }
        ci.cancel();
    }
}

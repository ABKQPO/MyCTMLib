package com.github.wohaopa.MyCTMLib.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.interfaces.ITexture;
import gregtech.common.render.GTMultiTextureRender;

@Mixin(value = GTMultiTextureRender.class, remap = false)
public interface AccessorGTMultiTextureRender {

    @Accessor("mTextures")
    ITexture[] getTextures();
}

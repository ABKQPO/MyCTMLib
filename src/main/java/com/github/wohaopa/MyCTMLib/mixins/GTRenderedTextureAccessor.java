package com.github.wohaopa.MyCTMLib.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.interfaces.IIconContainer;
import gregtech.common.render.GTRenderedTexture;

@Mixin(value = GTRenderedTexture.class, remap = false)
public interface GTRenderedTextureAccessor {

    @Accessor("mIconContainer")
    IIconContainer getIconContainer();
}

package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.renderer.texture.TextureMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureMap.class)
public interface AccessorTextureMap {

    @Accessor(value = "skipFirst", remap = false)
    boolean myctmlib$isSkippingFirstLoad();
}

package com.github.wohaopa.MyCTMLib.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import bartworks.common.blocks.BWBlocksGlass;

@Mixin(value = BWBlocksGlass.class, remap = false)
public interface BWBlocksGlassAccessor {

    @Accessor("connectedTex")
    boolean getConnectedTex();

    @Accessor("connectedTex")
    void setConnectedTex(boolean value);
}

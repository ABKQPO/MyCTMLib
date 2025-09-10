package com.github.wohaopa.MyCTMLib.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import bartworks.common.blocks.BWBlocksGlass2;

@Mixin(value = BWBlocksGlass2.class, remap = false)
public interface BWBlocksGlass2Accessor {

    @Accessor("connectedTex")
    boolean getConnectedTex();

    @Accessor("connectedTex")
    void setConnectedTex(boolean value);
}

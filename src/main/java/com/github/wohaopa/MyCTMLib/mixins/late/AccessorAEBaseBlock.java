package com.github.wohaopa.MyCTMLib.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.block.AEBaseBlock;

@Mixin(value = AEBaseBlock.class, remap = false)
public interface AccessorAEBaseBlock {

    @Accessor("isOpaque")
    void myctmlib$setOpaque(boolean opaque);
}

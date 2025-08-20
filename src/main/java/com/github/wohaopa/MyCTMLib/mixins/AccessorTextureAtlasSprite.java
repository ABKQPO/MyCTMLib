package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlasSprite.class)
public interface AccessorTextureAtlasSprite {

    @Invoker("<init>")
    static TextureAtlasSprite newInstance(String name) {
        throw new AssertionError();
    }
}

package com.github.wohaopa.MyCTMLib.mixins;

import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureMap.class)
public interface AccessorTextureMap {

    @Accessor("mapRegisteredSprites")
    Map<String, TextureAtlasSprite> getMapRegisteredSprites();
}

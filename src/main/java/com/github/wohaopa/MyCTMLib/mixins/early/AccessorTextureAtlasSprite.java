package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlasSprite.class)
public interface AccessorTextureAtlasSprite {

    @Accessor("animationMetadata")
    AnimationMetadataSection getAnimationMetadata();

    @Accessor("animationMetadata")
    void setAnimationMetadata(AnimationMetadataSection metadata);
}

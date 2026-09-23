package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TextureAtlasSprite.class, remap = true)
public interface AccessorTextureAtlasSprite {

    @Accessor(value = "animationMetadata", remap = true)
    AnimationMetadataSection getAnimationMetadata();
}

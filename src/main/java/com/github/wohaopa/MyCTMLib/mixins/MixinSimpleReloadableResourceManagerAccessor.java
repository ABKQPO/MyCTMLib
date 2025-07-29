package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.IMetadataSerializer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MixinSimpleReloadableResourceManagerAccessor {

    @Accessor("metadataSerializer_")
    @Mutable
    void setMetadataSerializer_(IMetadataSerializer serializer);

    @Accessor("metadataSerializer_")
    IMetadataSerializer getMetadataSerializer();
}

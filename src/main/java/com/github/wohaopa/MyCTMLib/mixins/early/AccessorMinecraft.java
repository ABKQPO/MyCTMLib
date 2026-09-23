package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.IMetadataSerializer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Minecraft.class, remap = true)
public interface AccessorMinecraft {

    @Accessor(value = "metadataSerializer_", remap = true)
    IMetadataSerializer getMetadataSerializer();
}

package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.IMetadataSerializer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Minecraft.class)
public interface AccessorMinecraft {

    @Accessor("metadataSerializer_")
    IMetadataSerializer getMetadataSerializer();
}

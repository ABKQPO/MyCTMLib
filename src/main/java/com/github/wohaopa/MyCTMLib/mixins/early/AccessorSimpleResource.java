package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.resources.SimpleResource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.gson.JsonObject;

@Mixin(value = SimpleResource.class, remap = true)
public interface AccessorSimpleResource {

    @Accessor(value = "mcmetaJson", remap = true)
    JsonObject getMcMetaJson();
}

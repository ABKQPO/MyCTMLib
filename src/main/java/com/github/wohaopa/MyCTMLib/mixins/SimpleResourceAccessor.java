package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.resources.SimpleResource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.gson.JsonObject;

@Mixin(SimpleResource.class)
public interface SimpleResourceAccessor {

    @Accessor("mcmetaJson")
    JsonObject getMcMetaJson();
}

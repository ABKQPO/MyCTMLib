package com.github.wohaopa.MyCTMLib.mixins;

import java.io.InputStream;
import java.util.Map;

import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.gson.JsonObject;

@Mixin(SimpleResource.class)
public interface SimpleResourceAccessor {

    @Accessor("mapMetadataSections")
    Map getMapMetadataSections();

    @Accessor("srResourceLocation")
    ResourceLocation getSrResourceLocation();

    @Accessor("resourceInputStream")
    InputStream getResourceInputStream();

    @Accessor("mcmetaInputStream")
    InputStream getMcmetaInputStream();

    @Accessor("srMetadataSerializer")
    IMetadataSerializer getSrMetadataSerializer();

    @Accessor("mcmetaJsonChecked")
    boolean isMcmetaJsonChecked();

    @Accessor("mcmetaJson")
    JsonObject getMcmetaJson();

    @Accessor("mcmetaJsonChecked")
    void setMcmetaJsonChecked(boolean checked);

    @Accessor("mcmetaJson")
    void setMcmetaJson(JsonObject json);
}

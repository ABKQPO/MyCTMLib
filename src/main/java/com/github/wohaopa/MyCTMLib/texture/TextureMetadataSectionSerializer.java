package com.github.wohaopa.MyCTMLib.texture;

import java.lang.reflect.Type;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 新 .mcmeta section 的序列化器。section 名为 "ctmlib"。
 * 反序列化时先 finalize 注册表，再按 type 分发并返回 TextureMetadataSection。
 */
@SideOnly(Side.CLIENT)
public class TextureMetadataSectionSerializer implements IMetadataSectionSerializer {

    @Override
    public String getSectionName() {
        return "ctmlib";
    }

    @Override
    public IMetadataSection deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        TextureTypeRegistry.finalizeRegistration();
        TextureTypeData data = TextureTypeRegistry.deserialize(json.getAsJsonObject());
        return new TextureMetadataSection(data);
    }
}

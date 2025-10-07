package com.github.wohaopa.MyCTMLib;

import java.lang.reflect.Type;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MyCTMLibMetadataSectionSerializer implements IMetadataSectionSerializer {

    @Override
    public MyCTMLibMetadataSection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        
        // 直接返回整个 myctmlib JSON 对象
        return new MyCTMLibMetadataSection(json);
    }

    @Override
    public String getSectionName() {
        return "myctmlib";
    }

    @SideOnly(Side.CLIENT)
    public static class MyCTMLibMetadataSection implements IMetadataSection {
        private final JsonObject json;
        
        public MyCTMLibMetadataSection(JsonObject json) {
            this.json = json;
        }
        
        public JsonObject getJson() {
            return json;
        }
    }
}

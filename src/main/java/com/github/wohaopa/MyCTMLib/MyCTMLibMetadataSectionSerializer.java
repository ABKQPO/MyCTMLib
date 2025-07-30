package com.github.wohaopa.MyCTMLib;

import java.lang.reflect.Type;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class MyCTMLibMetadataSectionSerializer implements IMetadataSectionSerializer {

    @Override
    public MyCTMLibMetadataSection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        if (!json.has("connection")) {
            throw new JsonParseException("Missing 'connection' field in myctmlib metadata");
        }

        String connection = json.get("connection")
            .getAsString();
        return new MyCTMLibMetadataSection(connection);
    }

    @Override
    public String getSectionName() {
        return "myctmlib";
    }

    @Desugar
    public record MyCTMLibMetadataSection(String connection) implements IMetadataSection {}
}

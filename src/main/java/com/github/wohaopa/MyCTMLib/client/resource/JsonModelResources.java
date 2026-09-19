package com.github.wohaopa.MyCTMLib.client.resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc.ModelLoc;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;

/** A reload-scoped, UTF-8 resource reader using GTNHLib's JSON model deserializer. */
public class JsonModelResources {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(JSONModel.class, new ModelDeserializer())
        .create();
    private final IResourceManager manager;
    private final Map<ModelLoc, JSONModel> models = new HashMap<>();
    private final Set<ModelLoc> resolving = new HashSet<>();

    public JsonModelResources(IResourceManager manager) {
        this.manager = manager;
    }

    public JsonObject readObject(ResourceLocation location) {
        return read(location, JsonObject.class);
    }

    public JSONModel load(ModelLoc location) {
        JSONModel cached = models.get(location);
        if (cached != null) return cached;
        if (!resolving.add(location)) throw new JsonParseException("Cyclic model parent: " + location.path());
        try {
            JSONModel model = read(
                new ResourceLocation(location.owner(), "models/" + location.path() + ".json"),
                JSONModel.class);
            model.resolveParents(this::load);
            models.put(location, model);
            return model;
        } finally {
            resolving.remove(location);
        }
    }

    private <T> T read(ResourceLocation location, Class<T> type) {
        try (Reader reader = new InputStreamReader(
            manager.getResource(location)
                .getInputStream(),
            StandardCharsets.UTF_8)) {
            T value = GSON.fromJson(reader, type);
            if (value == null) throw new JsonParseException("Empty JSON resource: " + location);
            return value;
        } catch (IOException exception) {
            throw new JsonParseException("Cannot read JSON resource: " + location, exception);
        }
    }
}

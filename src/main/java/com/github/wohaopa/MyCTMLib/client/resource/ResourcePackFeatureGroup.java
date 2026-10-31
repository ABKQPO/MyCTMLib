package com.github.wohaopa.MyCTMLib.client.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.mixins.early.AccessorMinecraft;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResourcePackFeatureGroup implements IResourceManagerReloadListener {

    private final String group;
    private final Set<String> keys;
    private volatile Set<String> enabled = Set.of();

    public ResourcePackFeatureGroup(String group, Collection<String> keys) {
        this.group = group;
        this.keys = Set.copyOf(keys);
    }

    public boolean isEnabled(String key) {
        return enabled.contains(key);
    }

    public Set<String> getEnabled() {
        return enabled;
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        // The server pack wins over the selected packs, and later selected packs win over earlier ones.
        List<IResourcePack> packs = new ArrayList<>();
        packs.add(repository.func_148530_e());
        List<ResourcePackRepository.Entry> entries = repository.getRepositoryEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            packs.add(
                entries.get(i)
                    .getResourcePack());
        }
        Map<String, Boolean> settings = new HashMap<>();
        for (IResourcePack pack : packs) {
            JsonObject switches = read(pack);
            if (switches == null) continue;
            for (Map.Entry<String, JsonElement> entry : switches.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (!keys.contains(key)) {
                    MyCTMLib.LOG.warn("Unknown {} switch {} in resource pack {}", group, key, pack.getPackName());
                    continue;
                }
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive()
                    .isBoolean()) {
                    MyCTMLib.LOG.warn("{}.{} in resource pack {} is not a boolean", group, key, pack.getPackName());
                    continue;
                }
                settings.putIfAbsent(key, value.getAsBoolean());
            }
        }
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, Boolean> entry : settings.entrySet()) {
            if (entry.getValue()) result.add(entry.getKey());
        }
        enabled = Set.copyOf(result);
        MyCTMLib.LOG.info("Resource-pack feature group {}: {}", group, result);
    }

    private JsonObject read(IResourcePack pack) {
        if (pack == null) return null;
        try {
            MyCTMLibMetadataSection section = (MyCTMLibMetadataSection) pack
                .getPackMetadata(((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer(), "myctmlib");
            if (section == null || !section.getJson()
                .has(group)) return null;
            JsonObject switches = section.getObjectSetting(group);
            if (switches == null) {
                MyCTMLib.LOG.warn(
                    "{} in resource pack {} must be an object of per-model switches; ignoring it",
                    group,
                    pack.getPackName());
            }
            return switches;
        } catch (IOException | RuntimeException exception) {
            MyCTMLib.LOG.warn("Cannot read {} from resource pack {}", group, pack.getPackName(), exception);
            return null;
        }
    }
}

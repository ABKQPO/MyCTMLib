package com.github.wohaopa.MyCTMLib.client.resource;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;

public class ResourcePackFeature implements IResourceManagerReloadListener {

    private final String key;
    private volatile boolean enabled;

    public ResourcePackFeature(String key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        Boolean setting = read(repository.func_148530_e());
        List<ResourcePackRepository.Entry> packs = repository.getRepositoryEntries();
        for (int i = packs.size() - 1; setting == null && i >= 0; i--) {
            setting = read(
                packs.get(i)
                    .getResourcePack());
        }
        enabled = Boolean.TRUE.equals(setting);
        MyCTMLib.LOG.info("Resource-pack feature {}: {}", key, enabled);
    }

    private Boolean read(IResourcePack pack) {
        if (pack == null) return null;
        try {
            MyCTMLibMetadataSection section = (MyCTMLibMetadataSection) pack
                .getPackMetadata(Minecraft.getMinecraft().metadataSerializer_, "myctmlib");
            return section == null ? null : section.getBooleanSetting(key);
        } catch (IOException | RuntimeException exception) {
            MyCTMLib.LOG.warn("Cannot read {} from resource pack {}", key, pack.getPackName(), exception);
            return null;
        }
    }
}

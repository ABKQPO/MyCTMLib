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
import com.github.wohaopa.MyCTMLib.mixins.early.AccessorMinecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;

@SideOnly(Side.CLIENT)
public class BeeJsonModelPackState implements IResourceManagerReloadListener {

    public static final BeeJsonModelPackState INSTANCE = new BeeJsonModelPackState();

    @Getter
    private volatile boolean enabled;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        IResourcePack pack = findHighestPriorityPack();
        enabled = pack != null && isEnabledForPack(pack);
        if (pack != null) {
            MyCTMLib.LOG.info(
                "JSON bee item models {} by highest-priority resource pack '{}'.",
                enabled ? "enabled" : "disabled",
                pack.getPackName());
        } else {
            MyCTMLib.LOG.info("JSON bee item models disabled: no active resource pack.");
        }
    }

    private IResourcePack findHighestPriorityPack() {
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        IResourcePack serverPack = repository.func_148530_e();
        if (serverPack != null) {
            return serverPack;
        }
        // Minecraft loads selected packs in this order; the final pack overrides earlier ones.
        List<ResourcePackRepository.Entry> entries = repository.getRepositoryEntries();
        return entries.isEmpty() ? null
            : entries.get(entries.size() - 1)
                .getResourcePack();
    }

    private boolean isEnabledForPack(IResourcePack pack) {
        try {
            MyCTMLibMetadataSection metadata = (MyCTMLibMetadataSection) pack
                .getPackMetadata(((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer(), "myctmlib");
            return metadata != null && metadata.isBeeJsonModelsEnabled();
        } catch (IOException | RuntimeException exception) {
            MyCTMLib.LOG
                .warn("Cannot read JSON bee model settings from resource pack '{}'.", pack.getPackName(), exception);
            return false;
        }
    }
}

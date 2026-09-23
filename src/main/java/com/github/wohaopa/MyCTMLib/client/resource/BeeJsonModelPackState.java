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
import com.github.wohaopa.MyCTMLib.config.ModConfig;

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
        IResourcePack pack = findHighestPriorityPackWithSetting();
        if (!ModConfig.debug) return;
        if (pack != null) {
            MyCTMLib.LOG.info(
                "JSON bee item models {} by highest-priority resource pack declaring the setting '{}'.",
                enabled ? "enabled" : "disabled",
                pack.getPackName());
        } else {
            MyCTMLib.LOG.info("JSON bee item models disabled: no active resource pack.");
        }
    }

    private IResourcePack findHighestPriorityPackWithSetting() {
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        IResourcePack serverPack = repository.func_148530_e();
        if (serverPack != null && applySetting(serverPack)) {
            return serverPack;
        }
        // Minecraft loads selected packs in this order; the final pack overrides earlier ones.
        List<ResourcePackRepository.Entry> entries = repository.getRepositoryEntries();
        for (int index = entries.size() - 1; index >= 0; index--) {
            IResourcePack pack = entries.get(index)
                .getResourcePack();
            if (applySetting(pack)) {
                return pack;
            }
        }
        enabled = false;
        return null;
    }

    private boolean applySetting(IResourcePack pack) {
        try {
            MyCTMLibMetadataSection metadata = (MyCTMLibMetadataSection) pack
                .getPackMetadata(Minecraft.getMinecraft().metadataSerializer_, "myctmlib");
            if (metadata == null) {
                return false;
            }
            Boolean setting = metadata.getBeeJsonModelsEnabledSetting();
            if (setting == null) {
                return false;
            }
            enabled = setting;
            return true;
        } catch (IOException | RuntimeException exception) {
            if (ModConfig.debug) {
                MyCTMLib.LOG.warn(
                    "Cannot read JSON bee model settings from resource pack '{}'.",
                    pack.getPackName(),
                    exception);
            }
            return false;
        }
    }
}

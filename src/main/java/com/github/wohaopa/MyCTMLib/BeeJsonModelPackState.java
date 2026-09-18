package com.github.wohaopa.MyCTMLib;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import com.github.wohaopa.MyCTMLib.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.mixins.AccessorMinecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;

@SideOnly(Side.CLIENT)
public class BeeJsonModelPackState implements IResourceManagerReloadListener {

    @Getter
    private volatile boolean enabled;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        BeeJsonModelItemRenderer.INSTANCE.clearModels();
        boolean hasEnabledPack = false;
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        for (ResourcePackRepository.Entry entry : repository.getRepositoryEntries()) {
            IResourcePack pack = entry.getResourcePack();
            if (pack != null && isEnabledForPack(pack)) {
                hasEnabledPack = true;
                break;
            }
        }
        enabled = hasEnabledPack;
    }

    private boolean isEnabledForPack(IResourcePack pack) {
        try {
            MyCTMLibMetadataSection metadata = (MyCTMLibMetadataSection) pack
                .getPackMetadata(((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer(), "myctmlib");
            return metadata != null && metadata.isBeeJsonModelsEnabled();
        } catch (IOException ignored) {
            return false;
        }
    }
}

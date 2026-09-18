package com.github.wohaopa.MyCTMLib;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import com.github.wohaopa.MyCTMLib.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.mixins.early.AccessorMinecraft;
import com.gtnewhorizon.gtnhlib.client.model.loading.BackingResourceManager;
import com.gtnewhorizon.gtnhlib.client.model.loading.GlobalResourceManager;

import cpw.mods.fml.common.FMLContainerHolder;
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
        IResourcePack enabledPack = findEnabledPack(resourceManager);
        enabled = enabledPack != null;
        if (enabledPack != null) {
            MyCTMLib.LOG.info("JSON bee item models enabled by resource pack '{}'.", enabledPack.getPackName());
        }
    }

    private IResourcePack findEnabledPack(IResourceManager resourceManager) {
        for (IResourcePack pack : getActiveResourcePacks(resourceManager)) {
            if (isEnabledForPack(pack)) {
                return pack;
            }
        }
        return null;
    }

    private Set<IResourcePack> getActiveResourcePacks(IResourceManager resourceManager) {
        Set<IResourcePack> packs = Collections.newSetFromMap(new IdentityHashMap<IResourcePack, Boolean>());
        ResourcePackRepository repository = Minecraft.getMinecraft()
            .getResourcePackRepository();
        for (ResourcePackRepository.Entry entry : repository.getRepositoryEntries()) {
            IResourcePack pack = entry.getResourcePack();
            if (pack != null) {
                packs.add(pack);
            }
        }

        if (resourceManager instanceof GlobalResourceManager manager) {
            for (Map.Entry<String, IResourceManager> entry : manager.nhlib$getDomainResourceManagers()
                .entrySet()) {
                IResourceManager domainResourceManager = entry.getValue();
                if (domainResourceManager instanceof BackingResourceManager backing) {
                    for (IResourcePack pack : backing.nhlib$getResourcePacks()) {
                        if (!(pack instanceof FMLContainerHolder)) {
                            packs.add(pack);
                        }
                    }
                }
            }
        }
        return packs;
    }

    private boolean isEnabledForPack(IResourcePack pack) {
        try {
            MyCTMLibMetadataSection metadata = (MyCTMLibMetadataSection) pack
                .getPackMetadata(((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer(), "myctmlib");
            return metadata != null && metadata.isBeeJsonModelsEnabled();
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }
}

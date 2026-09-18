package com.github.wohaopa.MyCTMLib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

import com.github.wohaopa.MyCTMLib.client.resource.BeeJsonModelPackState;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.compat.forestry.ForestryIntegration;
import com.github.wohaopa.MyCTMLib.core.Mods;
import com.github.wohaopa.MyCTMLib.mixins.early.AccessorMinecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientLifecycle {

    public static void preInit() {
        Minecraft minecraft = Minecraft.getMinecraft();
        ((AccessorMinecraft) minecraft).getMetadataSerializer()
            .registerMetadataSectionType(new MyCTMLibMetadataSectionSerializer(), MyCTMLibMetadataSection.class);
        if (minecraft.getResourceManager() instanceof IReloadableResourceManager manager) {
            manager.registerReloadListener(BeeJsonModelPackState.INSTANCE);
            if (Mods.FORESTRY.isModLoaded()) {
                ForestryIntegration.registerResources(manager);
            }
        }
    }

    public static void loadComplete() {
        if (Mods.FORESTRY.isModLoaded()) {
            ForestryIntegration.registerItemRenderers();
        }
    }
}

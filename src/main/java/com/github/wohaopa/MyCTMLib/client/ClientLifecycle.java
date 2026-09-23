package com.github.wohaopa.MyCTMLib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

import com.github.wohaopa.MyCTMLib.client.resource.BeeJsonModelPackState;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.compat.ae2.Ae2Integration;
import com.github.wohaopa.MyCTMLib.compat.forestry.ForestryIntegration;
import com.github.wohaopa.MyCTMLib.compat.opencomputers.OpenComputersIntegration;
import com.github.wohaopa.MyCTMLib.core.Mods;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientLifecycle {

    public static void preInit() {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.metadataSerializer_
            .registerMetadataSectionType(new MyCTMLibMetadataSectionSerializer(), MyCTMLibMetadataSection.class);
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            Ae2Integration.registerResources();
        }
        if (minecraft.getResourceManager() instanceof IReloadableResourceManager manager) {
            manager.registerReloadListener(BeeJsonModelPackState.INSTANCE);
            if (Mods.Forestry.isModLoaded()) {
                ForestryIntegration.registerResources(manager);
            }
        }
    }

    public static void loadComplete() {
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            Ae2Integration.registerItemRenderers();
        }
        if (Mods.Forestry.isModLoaded()) {
            ForestryIntegration.registerItemRenderers();
            if (Mods.OpenComputers.isModLoaded()) {
                OpenComputersIntegration.registerItemRenderer();
            }
        }
    }
}

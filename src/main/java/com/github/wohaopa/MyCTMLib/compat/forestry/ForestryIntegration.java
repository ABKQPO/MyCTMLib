package com.github.wohaopa.MyCTMLib.compat.forestry;

import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.plugins.PluginApiculture;

@SideOnly(Side.CLIENT)
public class ForestryIntegration {

    public static void registerResources(IReloadableResourceManager manager) {
        ModelRegistry.registerModid(MyCTMLib.MODID);
        manager.registerReloadListener(BeeJsonModelItemRenderer.INSTANCE);
    }

    public static void registerItemRenderers() {
        if (PluginApiculture.items == null) {
            return;
        }
        BeeJsonModelItemRenderer renderer = BeeJsonModelItemRenderer.INSTANCE;
        renderer.register(new ItemStack(PluginApiculture.items.beeDroneGE));
        renderer.register(new ItemStack(PluginApiculture.items.beePrincessGE));
        renderer.register(new ItemStack(PluginApiculture.items.beeQueenGE));
        MyCTMLib.LOG.info("Registered JSON bee item renderer.");
    }
}

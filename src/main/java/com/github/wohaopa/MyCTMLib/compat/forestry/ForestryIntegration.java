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

    private static ItemStack droneGuiStack;
    private static ItemStack queenGuiStack;
    private static ItemStack princessGuiStack;
    private static ItemStack larvaeGuiStack;

    public static ItemStack getGuiIconStack(String iconName) {
        if (iconName == null || PluginApiculture.items == null) {
            return null;
        }
        String normalized = iconName.startsWith("forestry:") ? iconName.substring("forestry:".length()) : iconName;
        if (normalized.equals("errors/noDrone") || normalized.equals("errors/noSpecimen")
            || normalized.equals("analyzer/bee")
            || normalized.equals("analyzer/drone")
            || normalized.equals("analyzer/item")
            || normalized.equals("analyzer/pure_breed")) {
            return droneGuiStack;
        }
        if (normalized.equals("errors/noQueen") || normalized.equals("analyzer/queen")) {
            return queenGuiStack;
        }
        if (normalized.equals("analyzer/princess")) {
            return princessGuiStack;
        }
        if (normalized.equals("analyzer/larvae")) {
            return larvaeGuiStack;
        }
        return null;
    }

    public static void registerResources(IReloadableResourceManager manager) {
        ModelRegistry.registerModid(MyCTMLib.MODID);
        manager.registerReloadListener(BeeJsonModelItemRenderer.INSTANCE);
    }

    public static void registerItemRenderers() {
        if (PluginApiculture.items == null) {
            return;
        }
        BeeJsonModelItemRenderer renderer = BeeJsonModelItemRenderer.INSTANCE;
        droneGuiStack = new ItemStack(PluginApiculture.items.beeDroneGE);
        princessGuiStack = new ItemStack(PluginApiculture.items.beePrincessGE);
        queenGuiStack = new ItemStack(PluginApiculture.items.beeQueenGE);
        larvaeGuiStack = new ItemStack(PluginApiculture.items.beeLarvaeGE);
        renderer.register(droneGuiStack);
        renderer.register(princessGuiStack);
        renderer.register(queenGuiStack);
        renderer.register(larvaeGuiStack);
        MyCTMLib.LOG.info("Registered JSON bee item renderer.");
    }
}

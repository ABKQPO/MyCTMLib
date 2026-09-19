package com.github.wohaopa.MyCTMLib.compat.opencomputers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.compat.forestry.ForestryIntegration;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;

@SideOnly(Side.CLIENT)
public class OpenComputersIntegration {

    @Optional.Method(modid = "OpenComputers")
    public static void registerItemRenderer() {
        ItemInfo descriptor = Items.get("beekeeperUpgrade");
        ItemStack bee = ForestryIntegration.getGuiIconStack("analyzer/drone");
        if (descriptor == null || bee == null) {
            return;
        }
        ItemStack upgrade = descriptor.createItemStack(1);
        if (upgrade == null) {
            return;
        }

        IItemRenderer fallback = null;
        for (ItemRenderType type : ItemRenderType.values()) {
            fallback = MinecraftForgeClient.getItemRenderer(upgrade, type);
            if (fallback != null) {
                break;
            }
        }
        if (fallback instanceof BeekeeperUpgradeItemRenderer) {
            return;
        }
        MinecraftForgeClient
            .registerItemRenderer(upgrade.getItem(), new BeekeeperUpgradeItemRenderer(upgrade, bee, fallback));
        MyCTMLib.LOG.info("Registered OpenComputers beekeeper upgrade bee item layer.");
    }
}

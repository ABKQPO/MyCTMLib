package com.github.wohaopa.MyCTMLib.compat.forestry;

import net.minecraft.item.ItemStack;

import com.github.wohaopa.MyCTMLib.client.resource.BeeJsonModelPackState;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BeeGuiItemLayerRenderer {

    public static boolean render(ItemStack stack, int x, int y, float zLevel) {
        return BeeJsonModelItemRenderer.INSTANCE.renderGui(stack, x, y, zLevel);
    }

    public static boolean render(String iconName, int x, int y, float zLevel) {
        if (!BeeJsonModelPackState.INSTANCE.isEnabled()) {
            return false;
        }
        ItemStack stack = ForestryIntegration.getGuiIconStack(iconName);
        return render(stack, x, y, zLevel);
    }
}

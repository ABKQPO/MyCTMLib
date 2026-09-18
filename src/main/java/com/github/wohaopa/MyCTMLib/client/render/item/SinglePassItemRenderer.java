package com.github.wohaopa.MyCTMLib.client.render.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

/**
 * Identifies renders that draw all item color layers together in the first render pass.
 */
public interface SinglePassItemRenderer extends IItemRenderer {

    boolean rendersAllPasses(ItemStack stack, ItemRenderType type);
}

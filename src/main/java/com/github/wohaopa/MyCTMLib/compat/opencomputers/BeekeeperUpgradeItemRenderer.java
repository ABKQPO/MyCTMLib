package com.github.wohaopa.MyCTMLib.compat.opencomputers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import com.github.wohaopa.MyCTMLib.client.render.item.SinglePassItemRenderer;
import com.github.wohaopa.MyCTMLib.compat.forestry.BeeJsonModelItemRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BeekeeperUpgradeItemRenderer implements SinglePassItemRenderer {

    private final Item item;
    private final int metadata;
    private final ItemStack beeStack;
    private final IItemRenderer fallbackRenderer;

    public BeekeeperUpgradeItemRenderer(ItemStack upgrade, ItemStack bee, IItemRenderer fallbackRenderer) {
        this.item = upgrade.getItem();
        this.metadata = upgrade.getItemDamage();
        this.beeStack = bee.copy();
        this.beeStack.stackSize = 1;
        this.fallbackRenderer = fallbackRenderer;
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return rendersAllPasses(stack, type) || canRenderFallback(stack, type);
    }

    @Override
    public boolean rendersAllPasses(ItemStack stack, ItemRenderType type) {
        return stack != null && stack.getItem() == item
            && stack.getItemDamage() == metadata
            && BeeJsonModelItemRenderer.INSTANCE.rendersAllPasses(beeStack, type);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        if (rendersAllPasses(stack, type)) {
            return BeeJsonModelItemRenderer.INSTANCE.shouldUseRenderHelper(type, beeStack, helper);
        }
        return canRenderFallback(stack, type) && fallbackRenderer.shouldUseRenderHelper(type, stack, helper);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if (rendersAllPasses(stack, type)) {
            BeeJsonModelItemRenderer.INSTANCE.renderWithOverlay(
                type,
                beeStack,
                stack.getItem()
                    .getIcon(stack, 0));
        } else if (canRenderFallback(stack, type)) {
            fallbackRenderer.renderItem(type, stack, data);
        }
    }

    private boolean canRenderFallback(ItemStack stack, ItemRenderType type) {
        return stack != null && fallbackRenderer != null && fallbackRenderer.handleRenderType(stack, type);
    }
}

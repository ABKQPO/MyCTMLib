package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.BeeJsonModelItemRenderer;
import com.github.wohaopa.MyCTMLib.MyCTMLib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.apiculture.render.RenderBeeItem;

@SideOnly(Side.CLIENT)
@Mixin(RenderBeeItem.class)
public class MixinRenderBeeItem {

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void renderJsonBee(ItemRenderType type, ItemStack item, Object[] data, CallbackInfo callbackInfo) {
        if (!MyCTMLib.BEE_JSON_MODEL_PACK_STATE.isEnabled()) {
            return;
        }
        if (BeeJsonModelItemRenderer.INSTANCE.render(type, item)) {
            callbackInfo.cancel();
        }
    }
}

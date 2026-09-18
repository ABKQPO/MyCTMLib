package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;
import com.github.wohaopa.MyCTMLib.compat.forestry.ForestryIntegration;

import forestry.api.apiculture.EnumBeeType;

@Pseudo
@Mixin(targets = "logisticspipes.gui.modules.GuiApiaristSink", remap = false)
public abstract class MixinGuiApiaristSink {

    @Inject(method = "renderForestryBeeAt", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderBeeItemLayer(Minecraft minecraft, int x, int y, float zLevel, String alleleId,
        CallbackInfo callbackInfo) {
        ItemStack stack = ForestryIntegration.getAlleleStack(alleleId, EnumBeeType.DRONE);
        if (BeeGuiItemLayerRenderer.render(stack, x, y, zLevel)) {
            callbackInfo.cancel();
        }
    }
}

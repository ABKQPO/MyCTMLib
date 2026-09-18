package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;

import forestry.core.gui.ledgers.Ledger;

@Mixin(value = Ledger.class, remap = false)
public abstract class MixinForestryLedger {

    @Inject(method = "drawIcon", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderBeeItemLayer(IIcon icon, int x, int y, CallbackInfo callbackInfo) {
        if (icon != null && BeeGuiItemLayerRenderer.render(icon.getIconName(), x, y, 0.0F)) {
            callbackInfo.cancel();
        }
    }
}

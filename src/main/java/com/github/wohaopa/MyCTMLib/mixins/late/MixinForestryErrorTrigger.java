package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;

@Pseudo
@Mixin(targets = "net.bdew.gendustry.compat.triggers.ForestryErrorTrigger", remap = false)
public abstract class MixinForestryErrorTrigger {

    @Shadow
    public abstract IIcon getIcon();

    @Inject(method = "drawGuiIcon", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderBeeItemLayer(int x, int y, float zLevel, CallbackInfoReturnable<Boolean> callbackInfo) {
        IIcon icon = getIcon();
        if (icon != null) {
            boolean rendered = BeeGuiItemLayerRenderer.render(icon.getIconName(), x, y, zLevel);
            if (rendered) {
                callbackInfo.setReturnValue(true);
            }
        }
    }
}

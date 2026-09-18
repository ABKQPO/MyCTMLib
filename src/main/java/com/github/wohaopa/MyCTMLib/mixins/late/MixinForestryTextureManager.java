package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;

@Pseudo
@Mixin(targets = "forestry.core.render.TextureManager", remap = false)
public class MixinForestryTextureManager {

    @Inject(method = "drawGuiIcon", at = @At("HEAD"), require = 0)
    private void renderBeeItemLayer(IIcon icon, int x, int y, float zLevel, CallbackInfo callbackInfo) {
        if (icon != null) {
            BeeGuiItemLayerRenderer.render(icon.getIconName(), x, y, zLevel);
        }
    }
}

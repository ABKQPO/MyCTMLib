package com.github.wohaopa.MyCTMLib.mixins.late;

import net.bdew.lib.gui.BaseRect;
import net.bdew.lib.gui.Color;
import net.bdew.lib.gui.IconWrapper;
import net.bdew.lib.gui.Texture;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;

@Pseudo
@Mixin(targets = "net.bdew.lib.gui.WidgetContainerWindow", remap = false)
public abstract class MixinGendustryWidgetContainerWindow {

    @Inject(method = "drawTexture", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderBeeItemLayer(BaseRect<Object> rect, Texture texture, Color color, CallbackInfo callbackInfo) {
        if (!(texture instanceof IconWrapper)) {
            return;
        }
        IIcon icon = ((IconWrapper) texture).icon();
        if (icon == null) {
            return;
        }
        int x = Math.round(((Number) rect.x1()).floatValue());
        int y = Math.round(((Number) rect.y1()).floatValue());
        if (BeeGuiItemLayerRenderer.render(icon.getIconName(), x, y, getZLevel())) {
            callbackInfo.cancel();
        }
    }

    @Shadow
    public abstract float getZLevel();
}

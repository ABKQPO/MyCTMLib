package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.forestry.BeeGuiItemLayerRenderer;

@Mixin(targets = "logisticspipes.gui.modules.GuiApiaristSink$TypeSlot", remap = false)
@Pseudo
public abstract class MixinGuiApiaristSinkTypeSlot {

    @Inject(method = "customRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderBeeItemLayer(Minecraft minecraft, float zLevel, CallbackInfoReturnable<Boolean> callbackInfo) {
        IIcon icon = getTextureIcon();
        if (icon != null) {
            boolean rendered = BeeGuiItemLayerRenderer.render(icon.getIconName(), getXPos() + 1, getYPos() + 1, zLevel);
            if (rendered) {
                callbackInfo.setReturnValue(true);
            }
        }
    }

    @Shadow
    public abstract IIcon getTextureIcon();

    @Shadow
    public abstract int getXPos();

    @Shadow
    public abstract int getYPos();
}

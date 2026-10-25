package com.github.wohaopa.MyCTMLib.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2EntityModelRenderer;

import appeng.client.render.model.ModelCompass;

@Mixin(value = ModelCompass.class, remap = false)
public abstract class MixinModelCompass {

    @Inject(method = "renderAll(F)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void renderJsonModel(float pointerRadians, CallbackInfo callback) {
        if (Ae2EntityModelRenderer.renderCompass(pointerRadians)) callback.cancel();
    }
}

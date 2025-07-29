package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

import net.minecraft.client.resources.SimpleReloadableResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager {

    @Inject(method = "clearResources", at = @At("HEAD"))
    private void onClearResources(CallbackInfo ci) {
        ctmIconMap.clear();
    }
}

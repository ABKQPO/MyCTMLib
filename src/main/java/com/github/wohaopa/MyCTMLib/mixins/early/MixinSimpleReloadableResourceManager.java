package com.github.wohaopa.MyCTMLib.mixins.early;

import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmReplaceMap;

import net.minecraft.client.resources.SimpleReloadableResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleReloadableResourceManager.class, remap = true)
public class MixinSimpleReloadableResourceManager {

    @Inject(method = "clearResources", at = @At("HEAD"))
    private void onClearResources(CallbackInfo ci) {
        ctmIconMap.clear();
        ctmAltMap.clear();
        ctmReplaceMap.clear();
        ctmRandomMap.clear();
    }
}

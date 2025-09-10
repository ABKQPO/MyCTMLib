package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.*;

import net.minecraft.client.resources.SimpleReloadableResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;

import cpw.mods.fml.common.Loader;

@Mixin(value = SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager {

    @Unique
    private static boolean isInit = false;

    @Inject(method = "clearResources", at = @At("HEAD"))
    private void onClearResources(CallbackInfo ci) {
        ctmIconMap.clear();
        if (Loader.isModLoaded("gregtech")) {
            if (!isInit) {
                isInit = true;
                return;
            }
            GTNHIntegrationHelper.setBlockCasings4CTM(true);
            GTNHIntegrationHelper.setGregtechMetaCasingBlocks3CTM(true);
            GTNHIntegrationHelper.setBWBlocksGlassCTM(true);
            gtBlockCasings4CTM = false;
            gtGregtechMetaCasingBlocks3CTM = false;
            gtBWBlocksGlassCTM = false;
        }
    }
}

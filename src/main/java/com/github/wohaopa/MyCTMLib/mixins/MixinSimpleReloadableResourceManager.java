package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.MyCTMLib.isInit;
import static com.github.wohaopa.MyCTMLib.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmReplaceMap;
import static com.github.wohaopa.MyCTMLib.Textures.gtBWBlocksGlassCTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtBlockCasings4CTM;
import static com.github.wohaopa.MyCTMLib.Textures.gtGregtechMetaCasingBlocks3CTM;

import net.minecraft.client.resources.SimpleReloadableResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;

import cpw.mods.fml.common.Loader;

@Mixin(value = SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager {

    @Inject(method = "clearResources", at = @At("HEAD"))
    private void onClearResources(CallbackInfo ci) {
        ctmIconMap.clear();
        ctmAltMap.clear();
        ctmReplaceMap.clear();
        ctmRandomMap.clear();
        if (isInit && Loader.isModLoaded("gregtech")) {
            GTNHIntegrationHelper.setBlockCasings4CTM(true);
            GTNHIntegrationHelper.setGregtechMetaCasingBlocks3CTM(true);
            GTNHIntegrationHelper.setBWBlocksGlassCTM(true);
            gtBlockCasings4CTM = false;
            gtGregtechMetaCasingBlocks3CTM = false;
            gtBWBlocksGlassCTM = false;
        }
    }
}

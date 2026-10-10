package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.client.ctm.Textures;
import com.github.wohaopa.MyCTMLib.compat.angelica.AngelicaCtmBridge;
import com.prupe.mcpatcher.ctm.CTMUtils;

@Mixin(value = CTMUtils.class, remap = false)
public abstract class MixinCTMUtils {

    @Inject(method = "hasCandidates", at = @At("HEAD"), cancellable = true, require = 0)
    private static void myctmlib$yieldToOwnConnectionTexture(Block block, IIcon icon,
        CallbackInfoReturnable<Boolean> cir) {
        if (!AngelicaCtmBridge.isYielding() && Textures.findConnectionManager(icon) != null) {
            cir.setReturnValue(false);
        }
    }
}

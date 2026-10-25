package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockAdvancedInscriber;
import appeng.client.render.BaseBlockRender;

@Mixin(value = BaseBlockRender.class, remap = false)
public abstract class MixinBaseBlockRender {

    @Inject(
        method = "renderInWorld(Lappeng/block/AEBaseBlock;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (block instanceof BlockAdvancedInscriber
            && Ae2WorldRenderer.renderFacing("advanced_inscriber", block, world, x, y, z, renderer)) {
            callback.setReturnValue(true);
        }
    }
}

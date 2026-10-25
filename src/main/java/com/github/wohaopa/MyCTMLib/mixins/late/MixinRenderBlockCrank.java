package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.grindstone.BlockCrank;
import appeng.client.render.blocks.RenderBlockCrank;
import appeng.tile.grindstone.TileCrank;

@Mixin(value = RenderBlockCrank.class, remap = false)
public abstract class MixinRenderBlockCrank {

    @Inject(
        method = "renderInWorld(Lappeng/block/grindstone/BlockCrank;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonBase(BlockCrank block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (Ae2WorldRenderer.hasDevice("crank_handle")
            && Ae2WorldRenderer.renderCrankBase(block, world, x, y, z, renderer)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(
        method = "renderTile(Lappeng/block/grindstone/BlockCrank;Lappeng/tile/grindstone/TileCrank;Lnet/minecraft/client/renderer/Tessellator;DDDFLnet/minecraft/client/renderer/RenderBlocks;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonHandle(BlockCrank block, TileCrank tile, Tessellator tessellator, double x, double y,
        double z, float partialTicks, RenderBlocks renderer, CallbackInfo callback) {
        if (Ae2WorldRenderer.hasDevice("crank_base") && Ae2WorldRenderer.renderCrankHandle(tile, x, y, z, renderer)) {
            callback.cancel();
        }
    }
}

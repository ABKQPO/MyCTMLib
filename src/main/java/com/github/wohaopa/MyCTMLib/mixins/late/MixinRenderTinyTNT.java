package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.misc.BlockTinyTNT;
import appeng.client.render.blocks.RenderTinyTNT;

@Mixin(value = RenderTinyTNT.class, remap = false)
public abstract class MixinRenderTinyTNT {

    @Inject(
        method = "renderInWorld(Lappeng/block/misc/BlockTinyTNT;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(BlockTinyTNT block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (Ae2WorldRenderer.renderStatic("tiny_tnt", block, world, x, y, z, renderer)) callback.setReturnValue(true);
    }
}

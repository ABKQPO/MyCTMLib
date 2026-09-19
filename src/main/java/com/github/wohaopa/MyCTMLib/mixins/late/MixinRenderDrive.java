package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.storage.BlockDrive;
import appeng.client.render.blocks.RenderDrive;

@Mixin(value = RenderDrive.class, remap = false)
public abstract class MixinRenderDrive {

    @Inject(
        method = "renderInWorld(Lappeng/block/storage/BlockDrive;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(BlockDrive block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (Ae2WorldRenderer.render(block, world, x, y, z, renderer)) callback.setReturnValue(true);
    }
}

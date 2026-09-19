package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.storage.BlockChest;
import appeng.client.render.blocks.RenderMEChest;

@Mixin(value = RenderMEChest.class, remap = false)
public abstract class MixinRenderMEChest {

    @Inject(
        method = "renderInWorld(Lappeng/block/storage/BlockChest;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(BlockChest block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (Ae2WorldRenderer.render(block, world, x, y, z, renderer)) callback.setReturnValue(true);
    }
}

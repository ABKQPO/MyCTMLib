package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.crafting.BlockMolecularAssembler;
import appeng.client.render.blocks.RenderBlockAssembler;

@Mixin(value = RenderBlockAssembler.class, remap = false)
public abstract class MixinRenderBlockAssembler {

    @Shadow
    private void renderCableAt(double thickness, IBlockAccess world, int x, int y, int z, BlockMolecularAssembler block,
        RenderBlocks renderer, double pull, boolean covered) {}

    @Inject(
        method = "renderInWorld(Lappeng/block/crafting/BlockMolecularAssembler;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(BlockMolecularAssembler block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderer, CallbackInfoReturnable<Boolean> callback) {
        if (!Ae2WorldRenderer.hasDevice("molecular_assembler")) return;
        if (ForgeHooksClient.getWorldRenderPass() == 1) {
            callback.setReturnValue(Ae2WorldRenderer.renderMolecularAssembler(block, world, x, y, z, renderer, true));
            return;
        }
        // Keep the cable stubs that show through the glass; the JSON model replaces only the housing.
        renderer.renderAllFaces = true;
        try {
            renderCableAt(0.11D, world, x, y, z, block, renderer, 0.141D, false);
            renderCableAt(0.188D, world, x, y, z, block, renderer, 0.1875D, true);
        } finally {
            block.getRendererInstance()
                .setTemporaryRenderIcon(null);
            renderer.renderAllFaces = false;
        }
        callback.setReturnValue(Ae2WorldRenderer.renderMolecularAssembler(block, world, x, y, z, renderer, false));
    }
}

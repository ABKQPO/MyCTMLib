package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2WorldRenderer;

import appeng.block.misc.BlockInscriber;
import appeng.client.render.blocks.RenderBlockInscriber;
import appeng.client.texture.ExtraBlockTextures;

@Mixin(value = RenderBlockInscriber.class, remap = false)
public abstract class MixinRenderBlockInscriber {

    @Inject(
        method = "renderInWorld(Lappeng/block/misc/BlockInscriber;Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/client/renderer/RenderBlocks;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1)
    private void renderJsonModel(BlockInscriber block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer,
        CallbackInfoReturnable<Boolean> callback) {
        if (Ae2WorldRenderer.render(block, world, x, y, z, renderer)) callback.setReturnValue(true);
    }

    @Redirect(
        method = "renderTile(Lappeng/block/misc/BlockInscriber;Lappeng/tile/misc/TileInscriber;Lnet/minecraft/client/renderer/Tessellator;DDDFLnet/minecraft/client/renderer/RenderBlocks;)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/texture/ExtraBlockTextures;getIcon()Lnet/minecraft/util/IIcon;"),
        require = 1)
    private IIcon getPressIcon(ExtraBlockTextures texture) {
        return Ae2WorldRenderer.sprite("inscriber_inside", texture.getIcon());
    }
}

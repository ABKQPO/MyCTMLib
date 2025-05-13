package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.Textures;

@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {

    @Shadow
    public IBlockAccess blockAccess;

    @Inject(method = "renderFaceYNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.DOWN))

            ci.cancel();

    }

    @Inject(method = "renderFaceYPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.UP))
            ci.cancel();
    }

    @Inject(method = "renderFaceZNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.NORTH))
            ci.cancel();
    }

    @Inject(method = "renderFaceZPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.SOUTH))
            ci.cancel();
    }

    @Inject(method = "renderFaceXNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.WEST))
            ci.cancel();
    }

    @Inject(method = "renderFaceXPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null || !Textures.contain(iIcon.getIconName())) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.EAST))
            ci.cancel();
    }
}

package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.PaneCtmRenderer;
import com.github.wohaopa.MyCTMLib.Textures;

@Mixin(value = RenderBlocks.class, priority = 900, remap = true)
public abstract class MixinRenderBlocks {

    @Shadow(remap = true)
    public IBlockAccess blockAccess;

    @Shadow(remap = true)
    public abstract boolean hasOverrideBlockTexture();

    @Inject(method = "renderFaceYNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.DOWN, ci);
    }

    @Inject(method = "renderFaceYPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceYPos(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.UP, ci);
    }

    @Inject(method = "renderFaceZNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.NORTH, ci);
    }

    @Inject(method = "renderFaceZPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceZPos(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.SOUTH, ci);
    }

    @Inject(method = "renderFaceXNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.WEST, ci);
    }

    @Inject(method = "renderFaceXPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceXPos(Block block, double x, double y, double z, IIcon icon, CallbackInfo ci) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.EAST, ci);
    }

    @Inject(method = "renderBlockPane", at = @At("HEAD"), cancellable = true)
    private void renderMyCtmPane(BlockPane pane, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (blockAccess == null || hasOverrideBlockTexture()) {
            return;
        }

        IIcon icon = pane.getIcon(ForgeDirection.DOWN.ordinal(), blockAccess.getBlockMetadata(x, y, z));
        CTMIconManager manager = Textures.findConnectionManager(icon);
        if (manager == null) {
            return;
        }
        boolean handled = pane.getClass() == BlockPane.class
            ? PaneCtmRenderer.renderThin((RenderBlocks) (Object) this, blockAccess, pane, x, y, z, icon, manager)
            : PaneCtmRenderer.renderThick((RenderBlocks) (Object) this, blockAccess, pane, x, y, z, icon, manager);
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderBlockStainedGlassPane", at = @At("HEAD"), cancellable = true)
    private void renderMyCtmStainedPane(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!(block instanceof BlockPane pane) || blockAccess == null || hasOverrideBlockTexture()) {
            return;
        }

        IIcon icon = pane.getIcon(ForgeDirection.DOWN.ordinal(), blockAccess.getBlockMetadata(x, y, z));
        CTMIconManager manager = Textures.findConnectionManager(icon);
        if (manager != null
            && PaneCtmRenderer.renderThick((RenderBlocks) (Object) this, blockAccess, pane, x, y, z, icon, manager)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void renderCtmFace(Block block, double x, double y, double z, IIcon icon, ForgeDirection direction,
        CallbackInfo ci) {
        if (blockAccess == null || icon == null || hasOverrideBlockTexture()) {
            return;
        }

        CTMIconManager manager = Textures.findConnectionManager(icon);
        if (manager != null && Textures
            .renderWorldBlock((RenderBlocks) (Object) this, blockAccess, block, x, y, z, icon, manager, direction)) {
            ci.cancel();
        }
    }
}

package com.github.wohaopa.MyCTMLib.mixins.early;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.client.ctm.CTMIconManager;
import com.github.wohaopa.MyCTMLib.client.ctm.CtmFaceRenderer;
import com.github.wohaopa.MyCTMLib.client.ctm.LayeredFaceRender;
import com.github.wohaopa.MyCTMLib.client.ctm.PaneCtmRenderer;
import com.github.wohaopa.MyCTMLib.client.ctm.Textures;
import com.github.wohaopa.MyCTMLib.compat.angelica.AngelicaCtmBridge;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(value = RenderBlocks.class, priority = 1100, remap = true)
public abstract class MixinRenderBlocks {

    @Shadow(remap = true)
    public IBlockAccess blockAccess;

    @Shadow(remap = true)
    public abstract boolean hasOverrideBlockTexture();

    @WrapMethod(method = "renderFaceYNeg")
    private void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.DOWN, original);
    }

    @WrapMethod(method = "renderFaceYPos")
    private void renderFaceYPos(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.UP, original);
    }

    @WrapMethod(method = "renderFaceZNeg")
    private void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.NORTH, original);
    }

    @WrapMethod(method = "renderFaceZPos")
    private void renderFaceZPos(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.SOUTH, original);
    }

    @WrapMethod(method = "renderFaceXNeg")
    private void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.WEST, original);
    }

    @WrapMethod(method = "renderFaceXPos")
    private void renderFaceXPos(Block block, double x, double y, double z, IIcon icon, Operation<Void> original) {
        renderCtmFace(block, x, y, z, icon, ForgeDirection.EAST, original);
    }

    @WrapMethod(method = "getBlockIconFromSideAndMetadata")
    private IIcon getBlockIconFromSideAndMetadata(Block block, int side, int metadata, Operation<IIcon> original) {
        IIcon icon = block.getIcon(side, metadata);
        return Textures.findConnectionManager(icon) == null ? original.call(block, side, metadata) : icon;
    }

    @WrapMethod(method = "getBlockIconFromSide")
    private IIcon getBlockIconFromSide(Block block, int side, Operation<IIcon> original) {
        IIcon icon = block.getBlockTextureFromSide(side);
        return Textures.findConnectionManager(icon) == null ? original.call(block, side) : icon;
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
        Operation<Void> original) {
        if (icon == null || hasOverrideBlockTexture()) {
            original.call(block, x, y, z, icon);
            return;
        }

        RenderBlocks renderer = (RenderBlocks) (Object) this;
        CTMIconManager manager = Textures.findConnectionManager(icon);
        if (blockAccess == null) {
            if (manager == null || !Textures.renderInventoryBlock(renderer, block, x, y, z, icon, manager, direction)) {
                original.call(block, x, y, z, icon);
            }
            return;
        }

        IIcon faceIcon = icon;
        if (manager != null) {
            if (Textures.renderWorldBlock(renderer, blockAccess, block, x, y, z, icon, manager, direction)) {
                AngelicaCtmBridge.dropQueuedFace();
                return;
            }
            faceIcon = AngelicaCtmBridge
                .resolveIcon(icon, block, blockAccess, (int) x, (int) y, (int) z, direction.ordinal());
        }

        if (LayeredFaceRender.needsQuadrantSplit(renderer, direction, x, y, z)) {
            AngelicaCtmBridge.dropQueuedFace();
            CtmFaceRenderer.renderMatchingSplitLayer(renderer, block, x, y, z, faceIcon, direction, original::call);
            return;
        }
        original.call(block, x, y, z, faceIcon);
    }
}

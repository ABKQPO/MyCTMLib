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
import com.github.wohaopa.MyCTMLib.client.ctm.LayeredFaceRender;
import com.github.wohaopa.MyCTMLib.client.ctm.PaneCtmRenderer;
import com.github.wohaopa.MyCTMLib.client.ctm.Textures;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(value = RenderBlocks.class, priority = 900, remap = true)
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
        if (blockAccess == null || icon == null || hasOverrideBlockTexture()) {
            original.call(block, x, y, z, icon);
            return;
        }

        RenderBlocks renderer = (RenderBlocks) (Object) this;
        CTMIconManager manager = Textures.findConnectionManager(icon);
        double offset = LayeredFaceRender.nextOffset(renderer, direction, x, y, z, manager != null);
        double bound = myctmlib$getFaceBound(renderer, direction);
        if (offset != 0.0D) {
            int normal = direction.offsetX + direction.offsetY + direction.offsetZ;
            myctmlib$setFaceBound(renderer, direction, bound + normal * offset);
        }
        try {
            if (manager == null
                || !Textures.renderWorldBlock(renderer, blockAccess, block, x, y, z, icon, manager, direction)) {
                original.call(block, x, y, z, icon);
            }
        } finally {
            if (offset != 0.0D) {
                myctmlib$setFaceBound(renderer, direction, bound);
            }
        }
    }

    @Unique
    private static double myctmlib$getFaceBound(RenderBlocks renderer, ForgeDirection direction) {
        return switch (direction) {
            case DOWN -> renderer.renderMinY;
            case UP -> renderer.renderMaxY;
            case NORTH -> renderer.renderMinZ;
            case SOUTH -> renderer.renderMaxZ;
            case WEST -> renderer.renderMinX;
            case EAST -> renderer.renderMaxX;
            default -> throw new IllegalArgumentException("Unsupported layered face: " + direction);
        };
    }

    @Unique
    private static void myctmlib$setFaceBound(RenderBlocks renderer, ForgeDirection direction, double bound) {
        switch (direction) {
            case DOWN -> renderer.renderMinY = bound;
            case UP -> renderer.renderMaxY = bound;
            case NORTH -> renderer.renderMinZ = bound;
            case SOUTH -> renderer.renderMaxZ = bound;
            case WEST -> renderer.renderMinX = bound;
            case EAST -> renderer.renderMaxX = bound;
            default -> throw new IllegalArgumentException("Unsupported layered face: " + direction);
        }
    }
}

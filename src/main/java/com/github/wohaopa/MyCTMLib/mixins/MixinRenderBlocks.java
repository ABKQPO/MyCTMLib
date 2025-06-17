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

/**
 * 一个用于注入 Minecraft 渲染流程的 Mixin 类，用于拦截和替代 {@link RenderBlocks} 中的方块面渲染逻辑，
 * 从而实现连接纹理（CTM）或其他定制渲染方式。
 *
 * <p>
 * 本类通过注入六个面（上下南北东西）的 `renderFace*` 方法，在符合条件时调用自定义的
 * {@link Textures#renderWorldBlock} 方法进行渲染，并通过取消默认逻辑实现“完全替代”。
 */
@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {

    /**
     * 渲染器使用的世界访问接口，用于获取周围方块信息。
     * 由 Minecraft 渲染系统初始化赋值。
     */
    @Shadow
    public IBlockAccess blockAccess;

    /**
     * 注入 renderFaceYNeg（底面）渲染方法，在匹配指定纹理时调用自定义渲染逻辑。
     */
    @Inject(method = "renderFaceYNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.DOWN)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceYPos（顶面）渲染方法。
     */
    @Inject(method = "renderFaceYPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.UP)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceZNeg（北面）渲染方法。
     */
    @Inject(method = "renderFaceZNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.NORTH)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceZPos（南面）渲染方法。
     */
    @Inject(method = "renderFaceZPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.SOUTH)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceXNeg（西面）渲染方法。
     */
    @Inject(method = "renderFaceXNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.WEST)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceXPos（东面）渲染方法。
     */
    @Inject(method = "renderFaceXPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (blockAccess == null) return;

        String iconName = iIcon.getIconName();
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            iconName = iconName.substring(0, secondColon) + "&"
                + iconName.substring(secondColon + 1)
                    .replace(":", "&");
        }

        if (!Textures.contain(iconName)) return;

        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.EAST)) {
            ci.cancel();
        }
    }
}

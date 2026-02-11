package com.github.wohaopa.MyCTMLib.mixins;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.renderer.texture.TextureUtil;

/**
 * 修复原版 TextureUtil.generateMipmapData 的 ArrayIndexOutOfBoundsException。
 * 原版假定输入 int[][] 至少有 (mipmapLevels+1) 个元素，但 loadSprite 与自定义 loader 均只提供 length=1。
 * 在方法入口对 p_147949_2_ 进行填充，使原版逻辑能正常工作。
 *
 * @see docs/generateMipmapData-越界崩溃与修复.md
 */
@Mixin(TextureUtil.class)
public class MixinTextureUtil {

    @ModifyVariable(
        method = "generateMipmapData",
        at = @At("HEAD"),
        index = 2,
        argsOnly = true)
    @SuppressWarnings("unused")
    private static int[][] padFrameData(int[][] frameData, int mipmapLevels, int width) {
        int required = mipmapLevels + 1;
        if (frameData != null && frameData.length < required) {
            return Arrays.copyOf(frameData, required);
        }
        return frameData;
    }
}

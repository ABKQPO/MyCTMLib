package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.github.wohaopa.MyCTMLib.client.ctm.LayeredFaceRender;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import gregtech.api.interfaces.ITexture;
import gregtech.api.render.ISBRContext;
import gregtech.common.render.SBRWorldContext;

@Mixin(value = SBRWorldContext.class, remap = false)
public abstract class MixinSBRWorldContext {

    @WrapMethod(method = "renderNegativeYFacing")
    private void myctmlib$renderDown(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.DOWN);
    }

    @WrapMethod(method = "renderPositiveYFacing")
    private void myctmlib$renderUp(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.UP);
    }

    @WrapMethod(method = "renderNegativeZFacing")
    private void myctmlib$renderNorth(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.NORTH);
    }

    @WrapMethod(method = "renderPositiveZFacing")
    private void myctmlib$renderSouth(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.SOUTH);
    }

    @WrapMethod(method = "renderNegativeXFacing")
    private void myctmlib$renderWest(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.WEST);
    }

    @WrapMethod(method = "renderPositiveXFacing")
    private void myctmlib$renderEast(ITexture[] textures, Operation<Void> original) {
        myctmlib$renderLayers(textures, original, ForgeDirection.EAST);
    }

    @Unique
    private void myctmlib$renderLayers(ITexture[] textures, Operation<Void> original, ForgeDirection direction) {
        ISBRContext context = (ISBRContext) this;
        try (LayeredFaceRender ignored = LayeredFaceRender
            .begin(context.getRenderBlocks(), direction, context.getX(), context.getY(), context.getZ())) {
            original.call((Object) textures);
        }
    }
}

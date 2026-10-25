package com.github.wohaopa.MyCTMLib.mixins.late;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2EntityModelRenderer;

import appeng.block.storage.BlockSkyChest;
import appeng.client.render.blocks.RenderBlockSkyChest;
import appeng.tile.storage.TileSkyChest;

@Mixin(value = RenderBlockSkyChest.class, remap = false)
public abstract class MixinRenderBlockSkyChest {

    @Unique
    private boolean myctmlib$smooth;

    @Inject(
        method = "renderInventory(Lappeng/block/storage/BlockSkyChest;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraftforge/client/IItemRenderer$ItemRenderType;[Ljava/lang/Object;)V",
        at = @At("HEAD"),
        require = 1)
    private void captureItemVariant(BlockSkyChest block, ItemStack stack, RenderBlocks renderer, ItemRenderType type,
        Object[] data, CallbackInfo callback) {
        myctmlib$smooth = stack.getItemDamage() == 1;
    }

    @Inject(
        method = "renderTile(Lappeng/block/storage/BlockSkyChest;Lappeng/tile/storage/TileSkyChest;Lnet/minecraft/client/renderer/Tessellator;DDDFLnet/minecraft/client/renderer/RenderBlocks;)V",
        at = @At("HEAD"),
        require = 1)
    private void captureBlockVariant(BlockSkyChest block, TileSkyChest chest, Tessellator tessellator, double x,
        double y, double z, float partialTicks, RenderBlocks renderer, CallbackInfo callback) {
        myctmlib$smooth = chest != null && chest.hasWorldObj()
            && chest.getWorldObj()
                .getBlockMetadata(chest.xCoord, chest.yCoord, chest.zCoord) == 1;
    }

    @Redirect(
        method = {
            "renderInventory(Lappeng/block/storage/BlockSkyChest;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraftforge/client/IItemRenderer$ItemRenderType;[Ljava/lang/Object;)V",
            "renderTile(Lappeng/block/storage/BlockSkyChest;Lappeng/tile/storage/TileSkyChest;Lnet/minecraft/client/renderer/Tessellator;DDDFLnet/minecraft/client/renderer/RenderBlocks;)V" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelChest;renderAll()V", remap = true),
        require = 2)
    private void renderJsonModel(ModelChest model) {
        if (!Ae2EntityModelRenderer.renderSkyChest(myctmlib$smooth, model.chestLid.rotateAngleX)) model.renderAll();
    }
}

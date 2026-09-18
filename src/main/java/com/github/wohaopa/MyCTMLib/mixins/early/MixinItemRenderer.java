package com.github.wohaopa.MyCTMLib.mixins.early;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.client.render.item.SinglePassItemRenderer;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(
        method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false)
    private void skipCompletedItemPass(EntityLivingBase entity, ItemStack stack, int pass, ItemRenderType type,
        CallbackInfo ci) {
        if (pass > 0 && MinecraftForgeClient.getItemRenderer(stack, type) instanceof SinglePassItemRenderer renderer
            && renderer.rendersAllPasses(stack, type)) {
            ci.cancel();
        }
    }
}

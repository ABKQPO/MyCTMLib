package com.github.wohaopa.MyCTMLib.mixins;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap extends AbstractTexture implements ITickableTextureObject, IIconRegister {

    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow
    protected abstract ResourceLocation completeResourceLocation(ResourceLocation location, int type);

    @Inject(
        method = "registerIcon",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;<init>(Ljava/lang/String;)V"),
        cancellable = true)
    private void onRegisterIcon(String textureName, CallbackInfoReturnable<IIcon> cir) {
        try {
            ResourceLocation res = completeResourceLocation(new ResourceLocation(textureName), 0);
            IResource resource = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(res);

            if (resource instanceof SimpleResource simple) {
                JsonObject mcmetaJson = simple.mcmetaJson;

                if (mcmetaJson.has("myctmlib")) {
                    JsonObject ctmObj = mcmetaJson.getAsJsonObject("myctmlib");
                    String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                        .getAsString();

                    TextureAtlasSprite base = new TextureAtlasSprite(textureName);
                    mapRegisteredSprites.put(textureName, base);

                    String ctmTextureName = connectTexture.endsWith("_ctm") ? connectTexture : connectTexture + "_ctm";
                    TextureAtlasSprite ctm = new TextureAtlasSprite(ctmTextureName);
                    mapRegisteredSprites.put(ctmTextureName, ctm);

                    cir.setReturnValue(base);
                }
            }
        } catch (Exception e) {}
    }
}

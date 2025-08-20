package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

import java.io.IOException;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
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

import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.InterpolatedIcon;
import com.github.wohaopa.MyCTMLib.NewTextureAtlasSprite;
import com.google.gson.JsonObject;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap extends AbstractTexture implements ITickableTextureObject, IIconRegister {

    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow
    protected abstract ResourceLocation completeResourceLocation(ResourceLocation location, int type);

    @Shadow
    @Final
    private String basePath;

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

            if (basePath.contains("textures\\items") || basePath.contains("textures/items")) {
                return;
            }

            if (resource instanceof SimpleResource simple) {

                if (simple.getMetadata("myctmlib") != null) {
                    TextureAtlasSprite currentBase = new NewTextureAtlasSprite(textureName);
                    mapRegisteredSprites.put(textureName, currentBase);

                    if (simple.getMetadata("animation") != null) {
                        JsonObject animationObj = ((AccessorSimpleResource) simple).getMcmetaJson()
                            .getAsJsonObject("animation");
                        if (animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
                            .getAsBoolean()) {
                            InterpolatedIcon interpolatedIcon = new InterpolatedIcon(textureName);
                            mapRegisteredSprites.put(textureName, interpolatedIcon);
                            currentBase = interpolatedIcon;
                        }
                    }

                    JsonObject ctmObj = ((AccessorSimpleResource) simple).getMcmetaJson()
                        .getAsJsonObject("myctmlib");
                    String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                        .getAsString();
                    if (connectTexture.isEmpty()) return;

                    String connectTextureName = connectTexture.replace("minecraft:", "")
                        .replace("textures/blocks/", "")
                        .replace(".png", "");

                    TextureAtlasSprite currentCTM = new NewTextureAtlasSprite(connectTextureName);
                    mapRegisteredSprites.put(connectTextureName, currentCTM);

                    try {
                        ResourceLocation resCTM = completeResourceLocation(new ResourceLocation(connectTexture), 0);
                        IResource resourceCTM = Minecraft.getMinecraft()
                            .getResourceManager()
                            .getResource(resCTM);

                        if (resourceCTM instanceof SimpleResource simpleCTM) {
                            if (simpleCTM.getMetadata("animation") != null) {
                                JsonObject animationObjCTM = ((AccessorSimpleResource) simpleCTM).getMcmetaJson()
                                    .getAsJsonObject("animation");
                                if (animationObjCTM.has("interpolate")
                                    && animationObjCTM.getAsJsonPrimitive("interpolate")
                                        .getAsBoolean()) {
                                    InterpolatedIcon interpolatedIconCTM = new InterpolatedIcon(connectTextureName);
                                    mapRegisteredSprites.put(connectTextureName, interpolatedIconCTM);

                                    currentCTM = interpolatedIconCTM;
                                }
                            }
                        }
                    } catch (IOException ignored) {}

                    ctmIconMap.put(textureName, new CTMIconManager(currentBase, currentCTM));
                    cir.setReturnValue(currentBase);
                }
            }
        } catch (Exception ignored) {}
    }
}

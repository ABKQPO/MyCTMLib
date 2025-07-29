package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;

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

            if (resource instanceof SimpleResource simple && resource.getMetadata("myctmlib") != null) {
                JsonObject mcmetaJson = simple.mcmetaJson;

                JsonObject ctmObj = mcmetaJson.getAsJsonObject("myctmlib");
                String connectTexture = ctmObj.getAsJsonPrimitive("connection")
                    .getAsString();

                connectTexture = connectTexture.replace("minecraft:", "")
                    .replace("textures/blocks/", "")
                    .replace(".png", "");

                TextureAtlasSprite base = new TextureAtlasSprite(textureName);
                mapRegisteredSprites.put(textureName, base);

                TextureAtlasSprite ctm = new TextureAtlasSprite(connectTexture);
                mapRegisteredSprites.put(connectTexture, ctm);

                ctmIconMap.put(textureName, new CTMIconManager(base, ctm));

                cir.setReturnValue(base);
            }
        } catch (Exception ignored) {}
    }
}

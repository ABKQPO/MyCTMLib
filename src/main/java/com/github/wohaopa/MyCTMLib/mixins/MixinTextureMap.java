package com.github.wohaopa.MyCTMLib.mixins;

import static com.github.wohaopa.MyCTMLib.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.Textures.ctmReplaceMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.CTMConfig;
import com.github.wohaopa.MyCTMLib.CTMIconManager;
import com.github.wohaopa.MyCTMLib.CtmSheetSprite;
import com.github.wohaopa.MyCTMLib.InterpolatedIcon;
import com.github.wohaopa.MyCTMLib.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.NewTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.Textures;
import com.google.gson.JsonObject;

@Mixin(value = TextureMap.class, remap = true)
public abstract class MixinTextureMap extends AbstractTexture implements ITickableTextureObject, IIconRegister {

    @Shadow(remap = true)
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow(remap = true)
    protected abstract ResourceLocation completeResourceLocation(ResourceLocation location, int type);

    @Shadow(remap = true)
    @Final
    private String basePath;

    @Inject(method = "loadTextureAtlas", at = @At("HEAD"))
    private void clearMyCtmRegistrations(IResourceManager resourceManager, CallbackInfo ci) {
        if (!isItemAtlas()) {
            Textures.clearTextureRegistrations();
        }
    }

    @Inject(
        method = "registerIcon",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;<init>(Ljava/lang/String;)V"),
        cancellable = true)
    private void onRegisterIcon(String textureName, CallbackInfoReturnable<IIcon> cir) {
        try {
            TextureAtlasSprite currentBase;
            TextureAtlasSprite currentCTM;
            TextureAtlasSprite currentAlt = null;
            List<CTMIconManager> randomManagers = new ArrayList<>();
            IResource resource = getResourceFromTextureName(textureName);

            if (isItemAtlas()) {
                return;
            }

            if (!(resource instanceof SimpleResource simple)) {
                return;
            }

            JsonObject ctmObj = ((MyCTMLibMetadataSection) resource.getMetadata("myctmlib")).getJson();

            if (ctmObj == null) {
                return;
            }

            CTMIconManager.Builder builder = CTMIconManager.builder();
            CTMConfig config = new CTMConfig(ctmObj);

            currentBase = useInterpolation(simple) ? new InterpolatedIcon(textureName, 2, 2)
                : new NewTextureAtlasSprite(textureName, 2, 2);
            builder.setIconSmall(currentBase);
            mapRegisteredSprites.put(textureName, currentBase);

            if (config.connectionTexture != null) {

                try {
                    IResource resourceCTM = getResourceFromJson(ctmObj, "connection");

                    if (resourceCTM instanceof SimpleResource simpleCTM) {
                        BufferedImage sheet = ImageIO.read(resourceCTM.getInputStream());
                        if (sheet != null && sheet.getWidth() == sheet.getHeight()
                            && sheet.getWidth() >= 4
                            && (sheet.getWidth() & 1) == 0) {
                            CtmSheetSprite sheetSprite = new CtmSheetSprite(
                                config.connectionTexture,
                                completeResourceLocation(new ResourceLocation(config.connectionTexture), 0),
                                sheet,
                                CtmSheetSprite.DEFAULT_PADDING,
                                useInterpolation(simpleCTM));
                            mapRegisteredSprites.put(config.connectionTexture, sheetSprite);
                            builder.setIconVariants(sheetSprite.getVariantIcons());
                        } else {
                            currentCTM = useInterpolation(simpleCTM)
                                ? new InterpolatedIcon(config.connectionTexture, 4, 4)
                                : new NewTextureAtlasSprite(config.connectionTexture, 4, 4);
                            mapRegisteredSprites.put(config.connectionTexture, currentCTM);
                            builder.setIconCTM(currentCTM);
                        }
                    }

                } catch (IOException ignored) {}
            }

            if (!config.randomTextures.isEmpty()) {

                List<String> processedTextures = config.randomTextures;

                // Pair random connection sheets with their fallback sheets.
                if (config.connectionTexture != null) {
                    for (String processedTexture : processedTextures) {
                        if (!processedTexture.contains("_ctm")) {
                            continue;
                        }

                        String baseTextureName = processedTexture.replace("_ctm", "");

                        if (!processedTextures.contains(baseTextureName)) {
                            continue;
                        }

                        TextureAtlasSprite baseSprite = new NewTextureAtlasSprite(baseTextureName, 2, 2);
                        TextureAtlasSprite randomSprite = new NewTextureAtlasSprite(processedTexture, 4, 4);
                        mapRegisteredSprites.put(baseTextureName, baseSprite);
                        mapRegisteredSprites.put(processedTexture, randomSprite);

                        randomManagers.add(
                            CTMIconManager.builder()
                                .setIconSmall(baseSprite)
                                .setIconCTM(randomSprite)
                                .buildAndInit());
                    }

                }

                // Build fallback-only random variants when no connection sheet exists.
                if (config.connectionTexture == null) {

                    for (String processedTexture : processedTextures) {
                        TextureAtlasSprite randomSprite = new NewTextureAtlasSprite(processedTexture, 2, 2);
                        mapRegisteredSprites.put(processedTexture, randomSprite);

                        randomManagers.add(
                            CTMIconManager.builder()
                                .setIconSmall(randomSprite)
                                .buildAndInit());
                    }
                }

            }

            if (config.altTexture != null) {
                try {
                    IResource resourceAlt = getResourceFromJson(ctmObj, "alt");

                    if (resourceAlt instanceof SimpleResource simpleAlt) {
                        currentAlt = useInterpolation(simpleAlt) ? new InterpolatedIcon(config.altTexture, 2, 2)
                            : new NewTextureAtlasSprite(config.altTexture, 2, 2);

                        mapRegisteredSprites.put(config.altTexture, currentAlt);
                        builder.setIconAlt(currentAlt);
                    }

                } catch (IOException ignored) {}

            }

            CTMIconManager ctmManager = builder.buildAndInit();
            if (!ctmManager.hasConnectionTexture()) {
                cir.setReturnValue(currentBase);
                return;
            }

            if (!randomManagers.isEmpty()) {
                ctmRandomMap.put(textureName, randomManagers);
            }

            if (currentAlt != null) {
                ctmAltMap.put(textureName, currentAlt.getIconName());
            }

            if (!config.equivalents.isEmpty()) {
                ctmReplaceMap.put(textureName, config.equivalents.toArray(new String[0]));
            }

            ctmIconMap.put(textureName, ctmManager);

            cir.setReturnValue(currentBase);
        } catch (Exception ignored) {}
    }

    /**
     * Returns whether a resource requests interpolated animation frames.
     */
    @Unique
    private boolean useInterpolation(SimpleResource simple) {
        if (simple.getMetadata("animation") == null) {
            return false;
        }

        JsonObject animationObj = ((AccessorSimpleResource) simple).getMcMetaJson()
            .getAsJsonObject("animation");
        return animationObj.has("interpolate") && animationObj.getAsJsonPrimitive("interpolate")
            .getAsBoolean();
    }

    @Unique
    private boolean isItemAtlas() {
        return basePath.contains("textures\\items") || basePath.contains("textures/items");
    }

    /**
     * Resolves a texture resource declared by metadata.
     */
    @Unique
    private IResource getResourceFromJson(JsonObject ctmObj, String fieldName) throws IOException {
        ResourceLocation res = completeResourceLocation(
            new ResourceLocation(
                ctmObj.getAsJsonPrimitive(fieldName)
                    .getAsString()),
            0);
        return Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(res);
    }

    /**
     * Resolves a texture resource by atlas name.
     */
    @Unique
    private IResource getResourceFromTextureName(String textureName) throws IOException {
        ResourceLocation res = completeResourceLocation(new ResourceLocation(textureName), 0);
        return Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(res);
    }
}

package com.github.wohaopa.MyCTMLib.mixins.early;

import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmAltMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmIconMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmRandomMap;
import static com.github.wohaopa.MyCTMLib.client.ctm.Textures.ctmReplaceMap;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
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

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.ctm.CTMConfig;
import com.github.wohaopa.MyCTMLib.client.ctm.CTMIconManager;
import com.github.wohaopa.MyCTMLib.client.ctm.CtmMethod;
import com.github.wohaopa.MyCTMLib.client.ctm.Textures;
import com.github.wohaopa.MyCTMLib.client.resource.MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection;
import com.github.wohaopa.MyCTMLib.client.texture.CtmSheetSprite;
import com.github.wohaopa.MyCTMLib.client.texture.InterpolatedIcon;
import com.github.wohaopa.MyCTMLib.client.texture.NewTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.config.ModConfig;
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

            MyCTMLibMetadataSection section = (MyCTMLibMetadataSection) resource.getMetadata("myctmlib");
            if (section == null) {
                return;
            }

            JsonObject ctmObj = section.getJson();

            if (ctmObj == null) {
                return;
            }

            CTMIconManager.Builder builder = CTMIconManager.builder();
            CTMConfig config = new CTMConfig(ctmObj);

            currentBase = useInterpolation(simple) ? new InterpolatedIcon(textureName, 2, 2)
                : new NewTextureAtlasSprite(textureName, 2, 2);
            builder.setIconSmall(currentBase);
            mapRegisteredSprites.put(textureName, currentBase);

            CtmMethod method = CtmMethod.fromName(config.method);

            if (method == CtmMethod.COMPACT) {
                method = null;
            }

            if (method != null && !method.isImplemented()) {
                // Recognized but not ported yet, so the original texture stays in place.
            } else if (config.connectionTexture != null && method == CtmMethod.FIXED) {

                try {
                    IResource resourceTile = getResourceFromJson(ctmObj, "connection");

                    if (resourceTile instanceof SimpleResource simpleTile) {
                        TextureAtlasSprite faceTile = useInterpolation(simpleTile)
                            ? new InterpolatedIcon(config.connectionTexture, 1, 1)
                            : new NewTextureAtlasSprite(config.connectionTexture, 1, 1);
                        mapRegisteredSprites.put(config.connectionTexture, faceTile);
                        builder.setFaceTile(faceTile);
                    }
                } catch (IOException ignored) {}

            } else if (config.connectionTexture != null && method == CtmMethod.REPEAT
                && config.width > 0
                && config.height > 0) {

                    try {
                        IResource resourceRepeat = getResourceFromJson(ctmObj, "connection");

                        if (resourceRepeat instanceof SimpleResource simpleRepeat) {
                            BufferedImage repeatImage = ImageIO.read(resourceRepeat.getInputStream());
                            if (repeatImage != null && repeatImage.getWidth() % config.width == 0
                                && repeatImage.getHeight() % config.height == 0
                                && repeatImage.getWidth() / config.width == repeatImage.getHeight() / config.height) {
                                CtmSheetSprite repeatSprite = new CtmSheetSprite(
                                    config.connectionTexture,
                                    completeResourceLocation(new ResourceLocation(config.connectionTexture), 0),
                                    repeatImage,
                                    config.width,
                                    config.height,
                                    CtmSheetSprite.DEFAULT_PADDING,
                                    useInterpolation(simpleRepeat));
                                mapRegisteredSprites.put(config.connectionTexture, repeatSprite);
                                builder.setFaceTiles(CtmMethod.REPEAT, repeatSprite.getUnitIcons());
                                builder.setRepeatOptions(config.width, config.height, config.symmetry);
                            }
                        }
                    } catch (IOException ignored) {}

                } else if (config.connectionTexture != null && method == CtmMethod.RANDOM) {

                    try {
                        IResource resourceRandom = getResourceFromJson(ctmObj, "connection");

                        if (resourceRandom instanceof SimpleResource simpleRandom) {
                            BufferedImage randomImage = ImageIO.read(resourceRandom.getInputStream());
                            if (randomImage != null && randomImage.getHeight() > 0) {
                                int columns = config.columns > 0 ? config.columns
                                    : randomImage.getWidth() / randomImage.getHeight();
                                int unitSize = columns > 0 ? randomImage.getWidth() / columns : 0;
                                int rows = unitSize > 0 ? randomImage.getHeight() / unitSize : 0;
                                if (columns > 0 && unitSize > 0
                                    && rows > 0
                                    && columns * unitSize == randomImage.getWidth()
                                    && rows * unitSize == randomImage.getHeight()) {
                                    CtmSheetSprite randomSprite = new CtmSheetSprite(
                                        config.connectionTexture,
                                        completeResourceLocation(new ResourceLocation(config.connectionTexture), 0),
                                        randomImage,
                                        columns,
                                        rows,
                                        CtmSheetSprite.DEFAULT_PADDING,
                                        useInterpolation(simpleRandom));
                                    mapRegisteredSprites.put(config.connectionTexture, randomSprite);
                                    builder.setFaceTiles(CtmMethod.RANDOM, randomSprite.getUnitIcons());
                                    builder.setRandomOptions(config.symmetry, config.weights);
                                }
                            }
                        }
                    } catch (IOException ignored) {}

                } else if (config.connectionTexture != null && method != null) {

                    try {
                        IResource resourceLayout = getResourceFromJson(ctmObj, "connection");

                        if (resourceLayout instanceof SimpleResource simpleLayout) {
                            BufferedImage layoutImage = ImageIO.read(resourceLayout.getInputStream());
                            // A sheet that cannot hold one pixel per cell would slice to nothing, so it is ignored.
                            if (layoutImage != null && layoutImage.getWidth() >= method.getColumns()
                                && layoutImage.getHeight() >= method.getRows()) {
                                CtmSheetSprite layoutSprite = new CtmSheetSprite(
                                    config.connectionTexture,
                                    completeResourceLocation(new ResourceLocation(config.connectionTexture), 0),
                                    layoutImage,
                                    method.getColumns(),
                                    method.getRows(),
                                    CtmSheetSprite.DEFAULT_PADDING,
                                    useInterpolation(simpleLayout));
                                mapRegisteredSprites.put(config.connectionTexture, layoutSprite);
                                IIcon[] units = layoutSprite.getUnitIcons();
                                IIcon[] tiles = new IIcon[units.length];
                                for (int tile = 0; tile < tiles.length; tile++) {
                                    tiles[tile] = units[method.getCellIndex(tile)];
                                }
                                builder.setFaceTiles(method, tiles);
                            }
                        }
                    } catch (IOException ignored) {}

                } else if (config.connectionTexture != null) {

                    try {
                        IResource resourceCTM = getResourceFromJson(ctmObj, "connection");

                        if (resourceCTM instanceof SimpleResource simpleCTM) {
                            BufferedImage sheet = ImageIO.read(resourceCTM.getInputStream());
                            if (sheet != null && sheet.getWidth() == sheet.getHeight()
                                && sheet.getWidth() >= 4
                                && (sheet.getWidth() & 1) == 0) {
                                boolean interpolate = useInterpolation(simpleCTM);
                                // One connection texture is shared by every icon that names it, so a second
                                // registration keeps using the sheet the atlas already holds. Replacing it would
                                // orphan the first manager and leave its sprite without atlas bounds. The atlas name
                                // doubles as the sprite name, so TextureMap bookkeeping stays consistent.
                                String sheetKey = connectionSheetKey(
                                    config.connectionTexture,
                                    textureName,
                                    sheet,
                                    interpolate);
                                Object registered = mapRegisteredSprites.get(sheetKey);
                                CtmSheetSprite sheetSprite = isCompatibleSheet(registered, sheet, interpolate)
                                    ? (CtmSheetSprite) registered
                                    : null;
                                if (sheetSprite == null) {
                                    sheetSprite = new CtmSheetSprite(
                                        sheetKey,
                                        completeResourceLocation(new ResourceLocation(config.connectionTexture), 0),
                                        sheet,
                                        CtmSheetSprite.DEFAULT_PADDING,
                                        interpolate);
                                    mapRegisteredSprites.put(sheetKey, sheetSprite);
                                }
                                builder.setIconVariants(sheetSprite.getUnitIcons());
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
        } catch (FileNotFoundException e) {
            // The base texture is not shipped by any resource pack, which is common for icon names that only exist as
            // metadata, so there is nothing to report.
        } catch (Exception e) {
            // A metadata section that cannot be read leaves the texture untouched, which is reported only in debug mode
            // because a broken resource pack would otherwise flood the log.
            if (ModConfig.debug) {
                MyCTMLib.LOG.warn("[MyCTMLib] Ignoring resource metadata for texture {}.", textureName, e);
            }
        }
    }

    /**
     * Returns whether an already registered sprite can serve the compact connection sheet being handled: it has to be
     * a compact sheet of the same source image and interpolation, so registrations that name the same sheet share one
     * sprite instead of replacing each other.
     *
     * @param candidate   the registered sprite, may be null or of another type
     * @param sheet       the sheet image of the registration being handled
     * @param interpolate whether that registration asks for interpolated animation frames
     * @return whether the candidate is the same connection sheet
     */
    @Unique
    private boolean isCompatibleSheet(Object candidate, BufferedImage sheet, boolean interpolate) {
        return candidate instanceof CtmSheetSprite && ((CtmSheetSprite) candidate).isSameSource(sheet, interpolate);
    }

    /**
     * Returns the atlas name a connection sheet is registered under. The connection texture name is used while it is
     * free or already holds a compatible sheet. Otherwise a name derived from the icon is probed, and a deterministic
     * numeric suffix keeps probing until a free or compatible name is found, so an incompatible sprite is never
     * overwritten.
     *
     * @param connectionTexture the connection texture name declared by the metadata
     * @param textureName       the icon name this sheet is being registered for
     * @param sheet             the sheet image of the registration being handled
     * @param interpolate       whether that registration asks for interpolated animation frames
     * @return the atlas name to register the sheet under
     */
    @Unique
    private String connectionSheetKey(String connectionTexture, String textureName, BufferedImage sheet,
        boolean interpolate) {
        Object owner = this.mapRegisteredSprites.get(connectionTexture);
        if (owner == null || isCompatibleSheet(owner, sheet, interpolate)) {
            return connectionTexture;
        }

        // concat and String.valueOf keep this class free of string concatenation bytecode, which the current JDK
        // would otherwise compile to an invokedynamic call and ship as an extra versions:17 class entry.
        String base = connectionTexture.concat("_")
            .concat(
                textureName.replace(':', '_')
                    .replace('/', '_'));
        String candidate = base;
        for (int suffix = 2;; suffix++) {
            Object conflict = this.mapRegisteredSprites.get(candidate);
            if (conflict == null || isCompatibleSheet(conflict, sheet, interpolate)) {
                return candidate;
            }
            candidate = base.concat("_")
                .concat(String.valueOf(suffix));
        }
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

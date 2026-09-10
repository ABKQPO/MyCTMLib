package com.github.wohaopa.MyCTMLib;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.mixins.AccessorTextureAtlasSprite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Connection sheet sprite that lays its variants out with padding before the atlas stitches it.
 * <p>
 * A resource pack keeps shipping one sheet texture, and the sprite stays registered under that real resource name, so
 * atlas resource resolution and missing texture tracking keep working. The image handed to the atlas is built here
 * instead: every variant is surrounded by copies of its own edge texels, so filtered or mipmapped sampling next to a
 * block border reads that variant again rather than whichever variant happens to sit next to it inside the sheet.
 * <p>
 * Animation frames are padded the same way. Sheets that request {@code interpolate} blend between frames, sheets that
 * do not keep the original frame stepping.
 */
@SideOnly(Side.CLIENT)
public class CtmSheetSprite extends InterpolatedIcon {

    // Edge pixels replicated around every variant so filtered sampling stays inside the variant.
    public static final int DEFAULT_PADDING = 8;

    private static final int VARIANT_GRID = 2;
    private static final int VARIANT_COUNT = VARIANT_GRID * VARIANT_GRID;

    private final ResourceLocation sheetLocation;
    private final BufferedImage sheet;
    private final int variantSize;
    private final int cellSize;
    private final boolean interpolate;
    private final IIcon[] variants = new IIcon[VARIANT_COUNT];

    /**
     * Creates a connection sheet sprite.
     *
     * @param name          the atlas name this sprite is registered under, which is the sheet resource name
     * @param sheetLocation the sheet resource, used to read the animation metadata
     * @param sheet         the already loaded sheet image
     * @param padding       the number of replicated edge pixels added around each variant
     * @param interpolate   whether animation frames should be blended
     */
    public CtmSheetSprite(String name, ResourceLocation sheetLocation, BufferedImage sheet, int padding,
        boolean interpolate) {
        super(name, VARIANT_GRID * VARIANT_GRID, VARIANT_GRID * VARIANT_GRID);
        this.sheetLocation = sheetLocation;
        this.sheet = sheet;
        this.variantSize = sheet.getWidth() / VARIANT_GRID;
        this.cellSize = variantSize + padding * 2;
        this.interpolate = interpolate;
        for (int variant = 0; variant < VARIANT_COUNT; variant++) {
            variants[variant] = new VariantIcon(variant);
        }
    }

    /**
     * Returns one icon per variant, each covering only that variant's content.
     *
     * @return the variant icons
     */
    public IIcon[] getVariantIcons() {
        return variants;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        try {
            IResource resource = manager.getResource(sheetLocation);
            int frameHeight = sheet.getWidth();
            int frameCount = Math.max(1, sheet.getHeight() / frameHeight);
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int frame = 0; frame < frameCount; frame++) {
                frames[frame] = layout(sheet, frame * frameHeight);
            }

            AnimationMetadataSection animation = (AnimationMetadataSection) resource.getMetadata("animation");
            loadSprite(frames, animation, false);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public void updateAnimation() {
        if (interpolate) {
            super.updateAnimation();
            return;
        }

        // Reproduces the plain frame stepping, because the interpolation of the parent cannot be switched off.
        AnimationMetadataSection animation = ((AccessorTextureAtlasSprite) this).getAnimationMetadata();
        tickCounter++;

        if (tickCounter >= animation.getFrameTimeSingle(frameCounter)) {
            int current = animation.getFrameIndex(frameCounter);
            int count = animation.getFrameCount() == 0 ? framesTextureData.size() : animation.getFrameCount();
            frameCounter = (frameCounter + 1) % count;
            tickCounter = 0;
            int next = animation.getFrameIndex(frameCounter);

            if (current != next && next >= 0 && next < framesTextureData.size()) {
                TextureUtil
                    .uploadTextureMipmap(framesTextureData.get(next), width, height, originX, originY, false, false);
            }
        }
    }

    private BufferedImage layout(BufferedImage sheet, int frameOffset) {
        int targetSize = cellSize * VARIANT_GRID;
        BufferedImage target = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        int padding = (cellSize - variantSize) / 2;
        for (int variant = 0; variant < VARIANT_COUNT; variant++) {
            int column = variant % VARIANT_GRID;
            int row = variant / VARIANT_GRID;
            int sourceX = column * variantSize;
            int sourceY = frameOffset + row * variantSize;
            int targetX = column * cellSize;
            int targetY = row * cellSize;
            for (int y = 0; y < cellSize; y++) {
                int variantY = sourceY + clamp(y - padding);
                for (int x = 0; x < cellSize; x++) {
                    int variantX = sourceX + clamp(x - padding);
                    target.setRGB(targetX + x, targetY + y, sheet.getRGB(variantX, variantY));
                }
            }
        }
        return target;
    }

    private int clamp(int offset) {
        if (offset < 0) {
            return 0;
        }
        return Math.min(offset, variantSize - 1);
    }

    private float contentOffset(int index, boolean atVariantStart) {
        int padding = (cellSize - variantSize) / 2;
        int total = cellSize * VARIANT_GRID;
        int offset = index * cellSize + (atVariantStart ? padding : padding + variantSize);
        return (float) offset / total;
    }

    /**
     * Exposes one variant of the sheet as an icon covering only that variant's content.
     */
    private class VariantIcon implements IIcon {

        private final int variant;

        VariantIcon(int variant) {
            this.variant = variant;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinU() {
            return CtmSheetSprite.this.getMinU() + (CtmSheetSprite.this.getMaxU() - CtmSheetSprite.this.getMinU())
                * contentOffset(variant % VARIANT_GRID, true);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            return CtmSheetSprite.this.getMinU() + (CtmSheetSprite.this.getMaxU() - CtmSheetSprite.this.getMinU())
                * contentOffset(variant % VARIANT_GRID, false);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedU(double value) {
            float min = getMinU();
            return (float) (min + (getMaxU() - min) * value / 16.0D);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinV() {
            return CtmSheetSprite.this.getMinV() + (CtmSheetSprite.this.getMaxV() - CtmSheetSprite.this.getMinV())
                * contentOffset(variant / VARIANT_GRID, true);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            return CtmSheetSprite.this.getMinV() + (CtmSheetSprite.this.getMaxV() - CtmSheetSprite.this.getMinV())
                * contentOffset(variant / VARIANT_GRID, false);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedV(double value) {
            float min = getMinV();
            return (float) (min + (getMaxV() - min) * value / 16.0D);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getIconName() {
            return CtmSheetSprite.super.getIconName();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconWidth() {
            return variantSize;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconHeight() {
            return variantSize;
        }
    }
}

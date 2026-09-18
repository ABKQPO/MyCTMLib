package com.github.wohaopa.MyCTMLib.client.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.mixins.early.AccessorTextureAtlasSprite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Connection sheet sprite that lays its units out with padding before the atlas stitches it.
 * <p>
 * A resource pack keeps shipping one sheet texture, and the sprite stays registered under that real resource name, so
 * atlas resource resolution and missing texture tracking keep working. The image handed to the atlas is built here
 * instead: every unit is surrounded by copies of its own edge texels, so filtered or mipmapped sampling next to a block
 * border reads that unit again rather than whichever unit happens to sit next to it inside the sheet.
 * <p>
 * A unit is the piece a face is drawn from: one cell for the full, horizontal, vertical, top, repeat and random
 * layouts, and a two by two block of cells for the compact layout, where one unit holds a whole connection variant.
 * Cells are always addressed top to bottom, left to right.
 * <p>
 * Animation frames are padded the same way. Sheets that request {@code interpolate} blend between frames, sheets that
 * do not keep the original frame stepping.
 */
@SideOnly(Side.CLIENT)
public class CtmSheetSprite extends InterpolatedIcon {

    // Edge pixels replicated around every unit so filtered sampling stays inside the unit.
    public static final int DEFAULT_PADDING = 8;

    private static final int COMPACT_GRID = 2;

    private final ResourceLocation sheetLocation;
    private final BufferedImage sheet;
    private final int columns;
    private final int rows;
    private final int unitSize;
    private final int cellSize;
    private final boolean interpolate;
    private final IIcon[] units;

    /**
     * Creates a connection sheet sprite laid out as two by two compact variants.
     *
     * @param name          the atlas name this sprite is registered under, which is the sheet resource name
     * @param sheetLocation the sheet resource, used to read the animation metadata
     * @param sheet         the already loaded sheet image
     * @param padding       the number of replicated edge pixels added around each unit
     * @param interpolate   whether animation frames should be blended
     */
    public CtmSheetSprite(String name, ResourceLocation sheetLocation, BufferedImage sheet, int padding,
        boolean interpolate) {
        this(name, sheetLocation, sheet, COMPACT_GRID, COMPACT_GRID, padding, interpolate);
    }

    /**
     * Creates a connection sheet sprite.
     *
     * @param name          the atlas name this sprite is registered under, which is the sheet resource name
     * @param sheetLocation the sheet resource, used to read the animation metadata
     * @param sheet         the already loaded sheet image
     * @param columns       the number of unit columns in the sheet
     * @param rows          the number of unit rows in the sheet
     * @param padding       the number of replicated edge pixels added around each unit
     * @param interpolate   whether animation frames should be blended
     */
    public CtmSheetSprite(String name, ResourceLocation sheetLocation, BufferedImage sheet, int columns, int rows,
        int padding, boolean interpolate) {
        // The parent halves the grid to get one mipmap region per unit, so ask for twice the unit grid.
        super(name, columns * COMPACT_GRID, rows * COMPACT_GRID);
        this.sheetLocation = sheetLocation;
        this.sheet = sheet;
        this.columns = columns;
        this.rows = rows;
        this.unitSize = Math.max(1, sheet.getWidth() / columns);
        this.cellSize = unitSize + padding * 2;
        this.interpolate = interpolate;
        this.units = new IIcon[columns * rows];
        for (int unit = 0; unit < units.length; unit++) {
            units[unit] = new UnitIcon(unit);
        }
    }

    /**
     * Returns one icon per unit, each covering only that unit's content.
     *
     * @return the unit icons in sheet order
     */
    public IIcon[] getUnitIcons() {
        return units;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        try {
            IResource resource = manager.getResource(sheetLocation);
            // One animation frame is one whole unit grid, which is not square for the sheet layouts that hold several
            // cells per unit.
            int frameHeight = unitSize * rows;
            int frameCount = Math.max(1, sheet.getHeight() / frameHeight);
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int frame = 0; frame < frameCount; frame++) {
                frames[frame] = layout(frame * frameHeight);
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
        if (animation == null) {
            return;
        }

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

    private BufferedImage layout(int frameOffset) {
        int padding = (cellSize - unitSize) / 2;
        BufferedImage target = new BufferedImage(cellSize * columns, cellSize * rows, BufferedImage.TYPE_INT_ARGB);
        int sheetWidth = sheet.getWidth();
        int sheetHeight = sheet.getHeight();
        for (int unit = 0; unit < units.length; unit++) {
            int column = unit % columns;
            int row = unit / columns;
            int sourceX = column * unitSize;
            int sourceY = frameOffset + row * unitSize;
            int targetX = column * cellSize;
            int targetY = row * cellSize;
            for (int y = 0; y < cellSize; y++) {
                // A sheet that is shorter than a whole frame keeps its last row instead of reading past the image.
                int unitY = Math.min(sheetHeight - 1, sourceY + clamp(y - padding));
                for (int x = 0; x < cellSize; x++) {
                    int unitX = Math.min(sheetWidth - 1, sourceX + clamp(x - padding));
                    target.setRGB(targetX + x, targetY + y, sheet.getRGB(unitX, unitY));
                }
            }
        }
        return target;
    }

    private int clamp(int offset) {
        if (offset < 0) {
            return 0;
        }
        return Math.min(offset, unitSize - 1);
    }

    private float contentOffset(int index, boolean atUnitStart, int unitCount) {
        int padding = (cellSize - unitSize) / 2;
        int total = cellSize * unitCount;
        int offset = index * cellSize + (atUnitStart ? padding : padding + unitSize);
        return (float) offset / total;
    }

    /**
     * Exposes one unit of the sheet as an icon covering only that unit's content.
     */
    private class UnitIcon implements IIcon {

        private final int unit;

        UnitIcon(int unit) {
            this.unit = unit;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinU() {
            return CtmSheetSprite.this.getMinU() + (CtmSheetSprite.this.getMaxU() - CtmSheetSprite.this.getMinU())
                * contentOffset(unit % columns, true, columns);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            return CtmSheetSprite.this.getMinU() + (CtmSheetSprite.this.getMaxU() - CtmSheetSprite.this.getMinU())
                * contentOffset(unit % columns, false, columns);
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
                * contentOffset(unit / columns, true, rows);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            return CtmSheetSprite.this.getMinV() + (CtmSheetSprite.this.getMaxV() - CtmSheetSprite.this.getMinV())
                * contentOffset(unit / columns, false, rows);
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
            return unitSize;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconHeight() {
            return unitSize;
        }
    }
}

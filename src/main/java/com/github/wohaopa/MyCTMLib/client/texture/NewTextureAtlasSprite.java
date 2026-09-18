package com.github.wohaopa.MyCTMLib.client.texture;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;

public class NewTextureAtlasSprite extends TextureAtlasSprite {

    private final int mipmapGridWidth;
    private final int mipmapGridHeight;

    public NewTextureAtlasSprite(String name) {
        this(name, 1, 1);
    }

    public NewTextureAtlasSprite(String name, int gridWidth, int gridHeight) {
        super(name);
        if (gridWidth < 1 || gridHeight < 1) {
            throw new IllegalArgumentException("Mipmap grid dimensions must be positive");
        }
        // A connection sheet stores one complete variant per two by two block of grid cells, so mipmaps are generated
        // per
        // variant. Generating them per cell would give every cell its own average at coarse levels, which turns into a
        // visible seam along every block border once distant terrain samples those levels.
        this.mipmapGridWidth = Math.max(1, gridWidth / 2);
        this.mipmapGridHeight = Math.max(1, gridHeight / 2);
    }

    @Override
    public void generateMipmaps(int mipmapLevels) {
        if (mipmapGridWidth == 1 && mipmapGridHeight == 1) {
            super.generateMipmaps(mipmapLevels);
            return;
        }

        List<int[][]> generatedFrames = new ArrayList<>(framesTextureData.size());
        for (int[][] frame : framesTextureData) {
            generatedFrames.add(generateGridMipmaps(frame, mipmapLevels));
        }
        setFramesTextureData(generatedFrames);
    }

    private int[][] generateGridMipmaps(int[][] frame, int mipmapLevels) {
        if (frame == null || frame.length == 0 || frame[0] == null) {
            return frame;
        }

        int tileWidth = width / mipmapGridWidth;
        int tileHeight = height / mipmapGridHeight;
        if (tileWidth < 1 || tileHeight < 1
            || tileWidth * mipmapGridWidth != width
            || tileHeight * mipmapGridHeight != height
            || !isPowerOfTwo(tileWidth)
            || !isPowerOfTwo(tileHeight)) {
            return superGenerate(frame, mipmapLevels);
        }

        int gridMipmapLevels = Math.min(mipmapLevels, getMaximumMipmapLevels(tileWidth, tileHeight));
        int[][] result = new int[mipmapLevels + 1][];
        int[][][] tileMipmaps = new int[mipmapGridWidth * mipmapGridHeight][][];
        for (int tileY = 0; tileY < mipmapGridHeight; tileY++) {
            for (int tileX = 0; tileX < mipmapGridWidth; tileX++) {
                int[] tile = new int[tileWidth * tileHeight];
                for (int y = 0; y < tileHeight; y++) {
                    System.arraycopy(
                        frame[0],
                        (tileY * tileHeight + y) * width + tileX * tileWidth,
                        tile,
                        y * tileWidth,
                        tileWidth);
                }
                int[][] tileFrame = new int[gridMipmapLevels + 1][];
                tileFrame[0] = tile;
                tileMipmaps[tileY * mipmapGridWidth + tileX] = TextureUtil
                    .generateMipmapData(gridMipmapLevels, tileWidth, tileFrame);
            }
        }

        for (int level = 0; level <= gridMipmapLevels; level++) {
            int levelWidth = width >> level;
            int levelHeight = height >> level;
            int levelTileWidth = Math.max(1, tileWidth >> level);
            int levelTileHeight = Math.max(1, tileHeight >> level);
            int[] combined = new int[levelWidth * levelHeight];
            for (int tileY = 0; tileY < mipmapGridHeight; tileY++) {
                for (int tileX = 0; tileX < mipmapGridWidth; tileX++) {
                    int[] tileLevel = tileMipmaps[tileY * mipmapGridWidth + tileX][level];
                    for (int y = 0; y < levelTileHeight; y++) {
                        System.arraycopy(
                            tileLevel,
                            y * levelTileWidth,
                            combined,
                            (tileY * levelTileHeight + y) * levelWidth + tileX * levelTileWidth,
                            levelTileWidth);
                    }
                }
            }
            result[level] = combined;
        }

        for (int level = gridMipmapLevels + 1; level <= mipmapLevels; level++) {
            result[level] = downsample(result[level - 1], width >> (level - 1), height >> (level - 1));
        }
        return result;
    }

    private int[] downsample(int[] source, int sourceWidth, int sourceHeight) {
        int targetWidth = Math.max(1, sourceWidth >> 1);
        int targetHeight = Math.max(1, sourceHeight >> 1);
        int[] target = new int[targetWidth * targetHeight];
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int x0 = Math.min(sourceWidth - 1, x * 2);
                int x1 = Math.min(sourceWidth - 1, x * 2 + 1);
                int y0 = Math.min(sourceHeight - 1, y * 2);
                int y1 = Math.min(sourceHeight - 1, y * 2 + 1);
                target[y * targetWidth + x] = average(
                    source[y0 * sourceWidth + x0],
                    source[y0 * sourceWidth + x1],
                    source[y1 * sourceWidth + x0],
                    source[y1 * sourceWidth + x1]);
            }
        }
        return target;
    }

    private int average(int first, int second, int third, int fourth) {
        int alpha = ((first >>> 24) + (second >>> 24) + (third >>> 24) + (fourth >>> 24)) / 4;
        int red = (((first >> 16) & 0xFF) + ((second >> 16) & 0xFF) + ((third >> 16) & 0xFF) + ((fourth >> 16) & 0xFF))
            / 4;
        int green = (((first >> 8) & 0xFF) + ((second >> 8) & 0xFF) + ((third >> 8) & 0xFF) + ((fourth >> 8) & 0xFF))
            / 4;
        int blue = ((first & 0xFF) + (second & 0xFF) + (third & 0xFF) + (fourth & 0xFF)) / 4;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private int getMaximumMipmapLevels(int tileWidth, int tileHeight) {
        return Math.min(getMipmapLevels(tileWidth), getMipmapLevels(tileHeight));
    }

    private int getMipmapLevels(int dimension) {
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(dimension);
    }

    private boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    private int[][] superGenerate(int[][] frame, int mipmapLevels) {
        List<int[][]> original = framesTextureData;
        try {
            framesTextureData = new ArrayList<>(1);
            framesTextureData.add(frame);
            super.generateMipmaps(mipmapLevels);
            return framesTextureData.get(0);
        } finally {
            framesTextureData = original;
        }
    }
}

package com.github.wohaopa.MyCTMLib;

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
        this.mipmapGridWidth = gridWidth;
        this.mipmapGridHeight = gridHeight;
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
        int[][] result = gridMipmapLevels == mipmapLevels ? new int[mipmapLevels + 1][]
            : superGenerate(frame, mipmapLevels);
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
        return result;
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

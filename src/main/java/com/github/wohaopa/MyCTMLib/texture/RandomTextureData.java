package com.github.wohaopa.MyCTMLib.texture;

import org.jetbrains.annotations.Nullable;

/**
 * type=random 的 mcmeta 解析结果：rows、columns、count、seed 等。
 * 用于随机纹理类型，从多个瓦片中随机选择一个显示。
 */
public class RandomTextureData implements TextureTypeData {

    private final int rows;
    private final int columns;
    private final int count;
    private final Long seed;

    public RandomTextureData(int rows, int columns, int count, @Nullable Long seed) {
        if (rows < 1 || rows > 10) {
            throw new IllegalArgumentException("rows must be between 1 and 10");
        }
        if (columns < 1 || columns > 10) {
            throw new IllegalArgumentException("columns must be between 1 and 10");
        }
        int finalCount = count < 0 ? rows * columns : count;
        if (finalCount < 1 || finalCount > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }
        if (finalCount > rows * columns) {
            throw new IllegalArgumentException("count cannot be greater than rows * columns");
        }
        this.rows = rows;
        this.columns = columns;
        this.count = finalCount;
        this.seed = seed;
    }

    @Override
    public String getType() {
        return "random";
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public Long getSeed() {
        return seed;
    }
}

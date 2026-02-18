package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * 连接谓词：给定世界、当前格、当前面、以及“邻格方向”，判定该方向是否“连接”。
 * 用于计算 8 方向连接掩码并选 layout 切片。
 */
public interface ConnectionPredicate {

    /**
     * 判定在给定邻格偏移上是否与当前格“连接”。
     *
     * @param world 世界
     * @param x     当前方块 x
     * @param y     当前方块 y
     * @param z     当前方块 z
     * @param face  当前渲染的面
     * @param block 当前方块
     * @param meta  当前方块 meta
     * @param dx    邻格相对当前格的 x 偏移（-1/0/1）
     * @param dy    邻格相对当前格的 y 偏移
     * @param dz    邻格相对当前格的 z 偏移
     * @return 该邻格是否连接
     */
    boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx, int dy,
        int dz);

    /**
     * 返回谓词的可读调试名。用于 Debug HUD。默认返回类名，子类可覆盖。
     */
    default String getDebugName() {
        return getClass().getSimpleName();
    }
}

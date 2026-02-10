package com.github.wohaopa.MyCTMLib.render;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;

/**
 * 8 方向连接状态：按当前面计算 8 个邻格方向的连接掩码，供 LayoutHandler.getTilePosition 使用。
 * 顺序：top, topRight, right, bottomRight, bottom, bottomLeft, left, topLeft（与 FullLayoutHandler 位序一致）。
 */
public final class ConnectionState {

    /** 每面 8 个方向的 (dx, dy, dz)。face 序与 ForgeDirection 一致：DOWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4, EAST=5。 */
    private static final int[][][] OFFSETS = new int[6][8][3];

    static {
        // NORTH (-Z): top=+Y, right=+X
        setFace(
            ForgeDirection.NORTH,
            new int[][] { { 0, 1, 0 }, { 1, 1, 0 }, { 1, 0, 0 }, { 1, -1, 0 }, { 0, -1, 0 }, { -1, -1, 0 },
                { -1, 0, 0 }, { -1, 1, 0 } });
        // SOUTH (+Z): top=+Y, right=-X
        setFace(
            ForgeDirection.SOUTH,
            new int[][] { { 0, 1, 0 }, { -1, 1, 0 }, { -1, 0, 0 }, { -1, -1, 0 }, { 0, -1, 0 }, { 1, -1, 0 },
                { 1, 0, 0 }, { 1, 1, 0 } });
        // DOWN (-Y): top=-Z, right=+X
        setFace(
            ForgeDirection.DOWN,
            new int[][] { { 0, 0, -1 }, { 1, 0, -1 }, { 1, 0, 0 }, { 1, 0, 1 }, { 0, 0, 1 }, { -1, 0, 1 }, { -1, 0, 0 },
                { -1, 0, -1 } });
        // UP (+Y): top=+Z, right=+X
        setFace(
            ForgeDirection.UP,
            new int[][] { { 0, 0, 1 }, { 1, 0, 1 }, { 1, 0, 0 }, { 1, 0, -1 }, { 0, 0, -1 }, { -1, 0, -1 },
                { -1, 0, 0 }, { -1, 0, 1 } });
        // WEST (-X): top=+Y, right=-Z
        setFace(
            ForgeDirection.WEST,
            new int[][] { { 0, 1, 0 }, { 0, 1, -1 }, { 0, 0, -1 }, { 0, -1, -1 }, { 0, -1, 0 }, { 0, -1, 1 },
                { 0, 0, 1 }, { 0, 1, 1 } });
        // EAST (+X): top=+Y, right=+Z
        setFace(
            ForgeDirection.EAST,
            new int[][] { { 0, 1, 0 }, { 0, 1, 1 }, { 0, 0, 1 }, { 0, -1, 1 }, { 0, -1, 0 }, { 0, -1, -1 },
                { 0, 0, -1 }, { 0, 1, -1 } });
    }

    private static void setFace(ForgeDirection face, int[][] dirs) {
        int i = face.ordinal();
        for (int d = 0; d < 8; d++) {
            OFFSETS[i][d][0] = dirs[d][0];
            OFFSETS[i][d][1] = dirs[d][1];
            OFFSETS[i][d][2] = dirs[d][2];
        }
    }

    /**
     * 计算当前面下的 8 方向连接掩码。
     *
     * @param world     世界
     * @param x         block x
     * @param y         block y
     * @param z         block z
     * @param face      当前渲染面
     * @param block     当前方块
     * @param meta      当前 meta
     * @param predicate 连接谓词
     * @return 8 位掩码（Simple 布局只用低 4 位）
     */
    public static int computeMask(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta,
        ConnectionPredicate predicate) {
        if (predicate == null) return 0;
        int mask = 0;
        int fi = face.ordinal();
        for (int d = 0; d < 8; d++) {
            int dx = OFFSETS[fi][d][0], dy = OFFSETS[fi][d][1], dz = OFFSETS[fi][d][2];
            if (predicate.connect(world, x, y, z, face, block, meta, dx, dy, dz)) {
                mask |= (1 << d);
            }
        }
        return mask;
    }
}

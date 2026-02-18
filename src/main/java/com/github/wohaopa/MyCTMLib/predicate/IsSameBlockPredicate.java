package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * condition: "is_same_block" — 邻格与当前方块为同一 Block 即连接。
 */
public enum IsSameBlockPredicate implements ConnectionPredicate {

    INSTANCE;

    @Override
    public String getDebugName() {
        return "is_same_block";
    }

    @Override
    public boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx,
        int dy, int dz) {
        int nx = x + dx, ny = y + dy, nz = z + dz;
        Block neighbor = world.getBlock(nx, ny, nz);
        if (neighbor == null || neighbor.isAir(world, nx, ny, nz)) return false;
        return neighbor == block;
    }
}

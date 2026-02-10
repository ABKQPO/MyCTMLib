package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * condition: "is_same_texture" — 与当前面纹理等价即连接。
 * 首版占位：按同方块同 meta 判定（与 is_same_block 等价），后续可改为按纹理/仓室等价表。
 */
public enum IsSameTexturePredicate implements ConnectionPredicate {

    INSTANCE;

    @Override
    public boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx,
        int dy, int dz) {
        int nx = x + dx, ny = y + dy, nz = z + dz;
        Block neighbor = world.getBlock(nx, ny, nz);
        if (neighbor == null || neighbor.isAir(world, nx, ny, nz)) return false;
        int neighborMeta = world.getBlockMetadata(nx, ny, nz);
        return neighbor == block && neighborMeta == meta;
    }
}

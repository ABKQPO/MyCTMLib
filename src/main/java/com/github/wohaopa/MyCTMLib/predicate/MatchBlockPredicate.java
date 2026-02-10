package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * condition: "match_block", "block": "modid:block_id" — 邻格为指定方块即连接。
 */
public class MatchBlockPredicate implements ConnectionPredicate {

    private final Block targetBlock;

    public MatchBlockPredicate(Block targetBlock) {
        this.targetBlock = targetBlock;
    }

    @Override
    public boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx,
        int dy, int dz) {
        int nx = x + dx, ny = y + dy, nz = z + dz;
        Block neighbor = world.getBlock(nx, ny, nz);
        return neighbor != null && neighbor == targetBlock;
    }
}

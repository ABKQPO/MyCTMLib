package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * condition: "is_same_texture" — 与当前面纹理等价即连接。
 * 基于 icon 比较，支持 GT BlockMachines 等由 TileEntity 决定纹理的方块。
 */
public enum IsSameTexturePredicate implements ConnectionPredicate {

    INSTANCE;

    @Override
    public boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx,
        int dy, int dz) {
        int nx = x + dx, ny = y + dy, nz = z + dz;
        Block neighbor = world.getBlock(nx, ny, nz);
        if (neighbor == null || neighbor.isAir(world, nx, ny, nz)) return false;

        IIcon currentIcon = ConnectionIconLookup.getIcon(world, x, y, z, face);
        IIcon neighborIcon = ConnectionIconLookup.getIcon(world, nx, ny, nz, face);
        return ConnectionIconLookup.iconsMatch(currentIcon, neighborIcon);
    }
}

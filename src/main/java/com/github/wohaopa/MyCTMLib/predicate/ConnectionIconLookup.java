package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;

import cpw.mods.fml.common.Loader;

/**
 * 新管线连接判定专用的 icon 取用逻辑，不依赖旧管线 Textures。
 * 供 is_same_block、is_same_texture 等谓词使用。
 */
public final class ConnectionIconLookup {

    private ConnectionIconLookup() {}

    /**
     * 获取指定坐标、指定面方向的方块渲染 icon。
     *
     * @param blockAccess 世界
     * @param x           x
     * @param y           y
     * @param z           z
     * @param face        面方向
     * @return 该面的 icon，无法获取时返回 null
     */
    public static IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection face) {
        Block block = blockAccess.getBlock(x, y, z);
        if (block == null || block instanceof BlockAir) return null;

        if (Loader.isModLoaded("gregtech")) {
            try {
                return GTNHIntegrationHelper.getIcon(blockAccess, x, y, z, face);
            } catch (Throwable t) {
                return null;
            }
        }

        return block.getIcon(blockAccess, x, y, z, face.ordinal());
    }

    /**
     * 判定两个 icon 是否等价（纹理相同）。
     * 当前仅比较 icon 名称，不包含 ctmReplaceMap 等价组。
     *
     * @param a 第一个 icon
     * @param b 第二个 icon
     * @return 两者等价且均非 null 时返回 true
     */
    public static boolean iconsMatch(IIcon a, IIcon b) {
        if (a == null || b == null) return false;
        return a.getIconName()
            .equals(b.getIconName());
    }
}

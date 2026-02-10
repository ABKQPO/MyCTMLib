package com.github.wohaopa.MyCTMLib.texture.layout;

/**
 * 连接纹理布局处理器：根据连接状态返回图集中的 (tileX, tileY)。
 * 4 邻布局使用低 4 位：bit0=top, bit1=right, bit2=bottom, bit3=left。
 * 8 方向布局使用 8 位：+ bit4=topRight, bit5=bottomRight, bit6=bottomLeft, bit7=topLeft。
 */
public interface LayoutHandler {

    int getWidth();

    int getHeight();

    /**
     * 根据连接掩码返回图集中的瓦片坐标 [tileX, tileY]。
     *
     * @param connectionMask 4 邻或 8 方向连接掩码
     * @return 长度为 2 的数组 { tileX, tileY }
     */
    int[] getTilePosition(int connectionMask);
}

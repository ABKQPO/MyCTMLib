package com.github.wohaopa.MyCTMLib.texture.layout;

/**
 * 4×4 连接布局：仅 4 邻。连接掩码 bit0=top, bit1=right, bit2=bottom, bit3=left。
 */
public class SimpleLayoutHandler implements LayoutHandler {

    public static final SimpleLayoutHandler INSTANCE = new SimpleLayoutHandler();

    private static final int TOP = 1, RIGHT = 2, BOTTOM = 4, LEFT = 8;

    @Override
    public int getWidth() {
        return 4;
    }

    @Override
    public int getHeight() {
        return 4;
    }

    @Override
    public int[] getTilePosition(int connectionMask) {
        boolean left = (connectionMask & LEFT) != 0;
        boolean top = (connectionMask & TOP) != 0;
        boolean right = (connectionMask & RIGHT) != 0;
        boolean bottom = (connectionMask & BOTTOM) != 0;

        if (!left && !top && !right && !bottom) return new int[] { 0, 0 };
        if (left && !top && !right && !bottom) return new int[] { 3, 0 };
        if (!left && top && !right && !bottom) return new int[] { 3, 1 };
        if (!left && !top && right && !bottom) return new int[] { 2, 1 };
        if (!left && !top && !right && bottom) return new int[] { 2, 0 };
        if (left && !top && right && !bottom) return new int[] { 0, 1 };
        if (!left && top && !right && bottom) return new int[] { 1, 1 };
        if (left && top && !right && !bottom) return new int[] { 3, 3 };
        if (!left && top && right && !bottom) return new int[] { 2, 3 };
        if (!left && !top && right && bottom) return new int[] { 2, 2 };
        if (left && !top && !right && bottom) return new int[] { 3, 2 };
        if (!left) return new int[] { 0, 2 };
        if (!top) return new int[] { 1, 2 };
        if (!right) return new int[] { 1, 3 };
        if (!bottom) return new int[] { 0, 3 };
        return new int[] { 1, 0 };
    }
}

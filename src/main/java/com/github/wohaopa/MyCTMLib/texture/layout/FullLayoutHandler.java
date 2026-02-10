package com.github.wohaopa.MyCTMLib.texture.layout;

/**
 * 8×6 连接布局：8 方向。连接掩码 bit0=top, bit1=topRight, bit2=right, bit3=bottomRight,
 * bit4=bottom, bit5=bottomLeft, bit6=left, bit7=topLeft。
 */
public class FullLayoutHandler implements LayoutHandler {

    public static final FullLayoutHandler INSTANCE = new FullLayoutHandler();

    private static final int TOP = 1, TOP_RIGHT = 2, RIGHT = 4, BOTTOM_RIGHT = 8;
    private static final int BOTTOM = 16, BOTTOM_LEFT = 32, LEFT = 64, TOP_LEFT = 128;

    @Override
    public int getWidth() {
        return 8;
    }

    @Override
    public int getHeight() {
        return 6;
    }

    @Override
    public int[] getTilePosition(int connectionMask) {
        boolean left = (connectionMask & LEFT) != 0;
        boolean top = (connectionMask & TOP) != 0;
        boolean right = (connectionMask & RIGHT) != 0;
        boolean bottom = (connectionMask & BOTTOM) != 0;
        boolean topLeft = (connectionMask & TOP_LEFT) != 0;
        boolean topRight = (connectionMask & TOP_RIGHT) != 0;
        boolean bottomRight = (connectionMask & BOTTOM_RIGHT) != 0;
        boolean bottomLeft = (connectionMask & BOTTOM_LEFT) != 0;

        if (!left && !top && !right && !bottom) return new int[] { 0, 0 };
        if (left && !top && !right && !bottom) return new int[] { 3, 0 };
        if (!left && top && !right && !bottom) return new int[] { 0, 3 };
        if (!left && !top && right && !bottom) return new int[] { 1, 0 };
        if (!left && !top && !right && bottom) return new int[] { 0, 1 };
        if (left && !top && right && !bottom) return new int[] { 2, 0 };
        if (!left && top && !right && bottom) return new int[] { 0, 2 };
        if (left && top && !right && !bottom) {
            return topLeft ? new int[] { 3, 3 } : new int[] { 5, 1 };
        }
        if (!left && top && right && !bottom) {
            return topRight ? new int[] { 1, 3 } : new int[] { 4, 1 };
        }
        if (!left && !top && right && bottom) {
            return bottomRight ? new int[] { 1, 1 } : new int[] { 4, 0 };
        }
        if (left && !top && !right && bottom) {
            return bottomLeft ? new int[] { 3, 1 } : new int[] { 5, 0 };
        }
        if (!left) {
            if (topRight && bottomRight) return new int[] { 1, 2 };
            if (topRight) return new int[] { 4, 2 };
            if (bottomRight) return new int[] { 6, 2 };
            return new int[] { 6, 0 };
        }
        if (!top) {
            if (bottomLeft && bottomRight) return new int[] { 2, 1 };
            if (bottomLeft) return new int[] { 7, 2 };
            if (bottomRight) return new int[] { 5, 2 };
            return new int[] { 7, 0 };
        }
        if (!right) {
            if (topLeft && bottomLeft) return new int[] { 3, 2 };
            if (topLeft) return new int[] { 7, 3 };
            if (bottomLeft) return new int[] { 5, 3 };
            return new int[] { 7, 1 };
        }
        if (!bottom) {
            if (topLeft && topRight) return new int[] { 2, 3 };
            if (topLeft) return new int[] { 4, 3 };
            if (topRight) return new int[] { 6, 3 };
            return new int[] { 6, 1 };
        }
        if (topLeft && topRight && bottomLeft && bottomRight) return new int[] { 2, 2 };
        if (!topLeft && topRight && bottomLeft && bottomRight) return new int[] { 7, 5 };
        if (topLeft && !topRight && bottomLeft && bottomRight) return new int[] { 6, 5 };
        if (topLeft && topRight && !bottomLeft && bottomRight) return new int[] { 7, 4 };
        if (topLeft && topRight && bottomLeft && !bottomRight) return new int[] { 6, 4 };
        if (!topLeft && topRight && !bottomRight && bottomLeft) return new int[] { 0, 4 };
        if (topLeft && !topRight && bottomRight && !bottomLeft) return new int[] { 0, 5 };
        if (!topLeft && !topRight && bottomRight && bottomLeft) return new int[] { 3, 4 };
        if (topLeft && !topRight && !bottomRight && bottomLeft) return new int[] { 3, 5 };
        if (topLeft && topRight && !bottomRight && !bottomLeft) return new int[] { 2, 5 };
        if (!topLeft && topRight && bottomRight && !bottomLeft) return new int[] { 2, 4 };
        if (topLeft) return new int[] { 5, 5 };
        if (topRight) return new int[] { 4, 5 };
        if (bottomRight) return new int[] { 4, 4 };
        if (bottomLeft) return new int[] { 5, 4 };
        return new int[] { 1, 4 };
    }
}

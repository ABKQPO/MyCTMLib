package com.github.wohaopa.MyCTMLib.render;

import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * 封装：连接掩码 + layout → (tileX, tileY)。
 */
public final class SliceSelector {

    /**
     * 根据 layout 与 8 方向连接掩码返回图集中的 (tileX, tileY)。
     */
    public static int[] getTilePosition(ConnectingLayout layout, int connectionMask) {
        LayoutHandler handler = LayoutHandlers.get(layout);
        return handler.getTilePosition(connectionMask);
    }
}

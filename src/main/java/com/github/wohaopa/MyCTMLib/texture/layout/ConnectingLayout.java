package com.github.wohaopa.MyCTMLib.texture.layout;

import java.util.Locale;

/**
 * 连接纹理布局枚举，与 mcmeta 中 "layout" 字段对应。
 * FULL 为 8×6 图集；SIMPLE 为 4×4。
 */
public enum ConnectingLayout {

    /** 8 方向连接，8×6 图集 */
    FULL,
    /** 仅 4 邻连接，4×4 图集 */
    SIMPLE,
    HORIZONTAL,
    VERTICAL,
    COMPACT,
    PIECED,
    OVERLAY;

    public static ConnectingLayout fromString(String s) {
        if (s == null || s.isEmpty()) return FULL;
        try {
            return valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return FULL;
        }
    }
}

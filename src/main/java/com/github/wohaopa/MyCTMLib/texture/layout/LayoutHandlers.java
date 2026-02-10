package com.github.wohaopa.MyCTMLib.texture.layout;

import java.util.EnumMap;
import java.util.Map;

/**
 * 根据 ConnectingLayout 获取对应的 LayoutHandler。仅 SIMPLE、FULL 有实现；其余暂回退到 FULL。
 */
public final class LayoutHandlers {

    private static final Map<ConnectingLayout, LayoutHandler> HANDLERS = new EnumMap<>(ConnectingLayout.class);

    static {
        HANDLERS.put(ConnectingLayout.SIMPLE, SimpleLayoutHandler.INSTANCE);
        HANDLERS.put(ConnectingLayout.FULL, FullLayoutHandler.INSTANCE);
        for (ConnectingLayout l : ConnectingLayout.values()) {
            if (!HANDLERS.containsKey(l)) {
                HANDLERS.put(l, FullLayoutHandler.INSTANCE);
            }
        }
    }

    public static LayoutHandler get(ConnectingLayout layout) {
        LayoutHandler h = HANDLERS.get(layout);
        return h != null ? h : FullLayoutHandler.INSTANCE;
    }
}

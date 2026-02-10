package com.github.wohaopa.MyCTMLib.texture;

import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;

/**
 * type=connecting 的 mcmeta 解析结果：layout、random 等。
 */
public class ConnectingTextureData implements TextureTypeData {

    private final ConnectingLayout layout;
    private final boolean random;

    public ConnectingTextureData(ConnectingLayout layout, boolean random) {
        this.layout = layout != null ? layout : ConnectingLayout.FULL;
        this.random = random;
    }

    @Override
    public String getType() {
        return "connecting";
    }

    public ConnectingLayout getLayout() {
        return layout;
    }

    public boolean isRandom() {
        return random;
    }
}

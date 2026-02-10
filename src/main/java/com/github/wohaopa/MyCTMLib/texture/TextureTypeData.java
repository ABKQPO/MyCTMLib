package com.github.wohaopa.MyCTMLib.texture;

/**
 * 纹理类型数据的标记接口。具体由 ConnectingTextureData、BaseTextureData 等实现。
 */
public interface TextureTypeData {

    /** 类型标识，如 "connecting", "base" */
    String getType();
}

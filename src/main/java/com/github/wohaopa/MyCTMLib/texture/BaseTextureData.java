package com.github.wohaopa.MyCTMLib.texture;

/**
 * type=base 的 mcmeta 解析结果。仅占位，后续可扩展 emissive、render_type、tinting、model 等。
 */
public class BaseTextureData implements TextureTypeData {

    @Override
    public String getType() {
        return "base";
    }
}

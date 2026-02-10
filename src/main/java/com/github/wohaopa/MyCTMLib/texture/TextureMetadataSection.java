package com.github.wohaopa.MyCTMLib.texture;

import net.minecraft.client.resources.data.IMetadataSection;

/**
 * 新 .mcmeta section（如 "ctmlib"）的 IMetadataSection 实现，持有解析后的 TextureTypeData。
 */
public class TextureMetadataSection implements IMetadataSection {

    private final TextureTypeData data;

    public TextureMetadataSection(TextureTypeData data) {
        this.data = data;
    }

    public TextureTypeData getData() {
        return data;
    }
}

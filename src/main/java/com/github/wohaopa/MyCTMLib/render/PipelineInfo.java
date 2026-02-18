package com.github.wohaopa.MyCTMLib.render;

import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;

/**
 * 单面渲染管线信息。用于 Debug HUD 展示实际走哪条管线及关键参数，不涉及绘制。
 */
public final class PipelineInfo {

    public enum PipelineType {
        /** 新管线 Model 分支：BlockStateRegistry → ModelRegistry */
        MODEL,
        /** 新管线 TextureRegistry 分支：iconName → ConnectingTextureData */
        TEXTURE_REGISTRY,
        /** 旧管线：Textures.renderWorldBlock */
        LEGACY,
        /** 原版 RenderBlocks */
        VANILLA
    }

    private final PipelineType type;
    private final String iconName;
    private final String modelId;
    private final String textureKey;
    private final ConnectingLayout layout;
    private final int mask;
    private final String skipReason;

    private PipelineInfo(PipelineType type, String iconName, String modelId, String textureKey, ConnectingLayout layout,
        int mask, String skipReason) {
        this.type = type;
        this.iconName = iconName != null ? iconName : "";
        this.modelId = modelId;
        this.textureKey = textureKey;
        this.layout = layout;
        this.mask = mask;
        this.skipReason = skipReason;
    }

    public static PipelineInfo model(String iconName, String modelId, String textureKey, ConnectingLayout layout,
        int mask) {
        return new PipelineInfo(PipelineType.MODEL, iconName, modelId, textureKey, layout, mask, null);
    }

    public static PipelineInfo textureRegistry(String iconName, ConnectingLayout layout, int mask) {
        return new PipelineInfo(PipelineType.TEXTURE_REGISTRY, iconName, null, null, layout, mask, null);
    }

    public static PipelineInfo legacy(String iconName) {
        return new PipelineInfo(PipelineType.LEGACY, iconName, null, null, null, -1, null);
    }

    public static PipelineInfo vanilla(String iconName, String skipReason) {
        return new PipelineInfo(PipelineType.VANILLA, iconName, null, null, null, -1, skipReason);
    }

    public PipelineType getType() {
        return type;
    }

    public String getIconName() {
        return iconName;
    }

    public String getModelId() {
        return modelId;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public ConnectingLayout getLayout() {
        return layout;
    }

    /** 连接掩码，非连接纹理时为 -1。 */
    public int getMask() {
        return mask;
    }

    /** Vanilla 时的简要 skip 原因。 */
    public String getSkipReason() {
        return skipReason;
    }
}

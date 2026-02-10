package com.github.wohaopa.MyCTMLib.model;

/**
 * 模型单个面的定义：纹理键与可选的连接谓词键。
 */
public class ModelFace {

    private final String textureKey;
    private final String connectionKey;

    public ModelFace(String textureKey, String connectionKey) {
        this.textureKey = textureKey;
        this.connectionKey = connectionKey;
    }

    public String getTextureKey() {
        return textureKey;
    }

    /** 用于 connection 类型模型，指定该面使用的 connections 中的谓词键；可为 null。 */
    public String getConnectionKey() {
        return connectionKey;
    }
}

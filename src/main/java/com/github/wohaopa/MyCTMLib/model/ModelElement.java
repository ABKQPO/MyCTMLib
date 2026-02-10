package com.github.wohaopa.MyCTMLib.model;

import java.util.EnumMap;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * 模型单个立方体元素：from、to、六面 faces。
 */
public class ModelElement {

    private final float[] from;
    private final float[] to;
    private final Map<ForgeDirection, ModelFace> faces;

    public ModelElement(float[] from, float[] to, Map<ForgeDirection, ModelFace> faces) {
        this.from = from;
        this.to = to;
        this.faces = faces != null ? new EnumMap<>(faces) : new EnumMap<>(ForgeDirection.class);
    }

    public float[] getFrom() {
        return from;
    }

    public float[] getTo() {
        return to;
    }

    public Map<ForgeDirection, ModelFace> getFaces() {
        return faces;
    }

    public ModelFace getFace(ForgeDirection direction) {
        return faces.get(direction);
    }
}

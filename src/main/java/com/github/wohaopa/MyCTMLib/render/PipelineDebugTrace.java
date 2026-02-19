package com.github.wohaopa.MyCTMLib.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管线决策过程追踪。用于 Debug HUD 展示决策步骤、退化原因、谓词、连接状态、瓦片坐标等。
 */
public final class PipelineDebugTrace {

    private final List<String> steps = new ArrayList<>();
    private String degradationReason;
    private String predicateUsed;
    private int[] tilePos;
    private int[] connectionBits;
    /** TexReg/TexMap 同步状态：null=不适用, true=同步, false=TexReg.getIcon 返回 null（不同步） */
    private Boolean texRegTexMapSynced;
    /** TexReg.getIcon 的 lookupKey，用于 debug 展示 */
    private String texRegGetIconLookupKey;
    /** 实际用于绘制的 sprite 的 getIconName()，用于排查紫黑块 */
    private String drawSpriteName;
    /** sprite 是否已加载（宽高>0），"WxH" 或 "0x0(unloaded)" */
    private String drawSpriteLoaded;

    public PipelineDebugTrace() {}

    public void addStep(String step) {
        if (step != null && !step.isEmpty()) {
            steps.add(step);
        }
    }

    public void setDegradationReason(String reason) {
        this.degradationReason = reason;
    }

    public void setPredicateUsed(String predicate) {
        this.predicateUsed = predicate;
    }

    public void setTilePos(int tileX, int tileY) {
        this.tilePos = new int[] { tileX, tileY };
    }

    public void setConnectionBits(int mask) {
        this.connectionBits = new int[8];
        for (int d = 0; d < 8; d++) {
            connectionBits[d] = (mask & (1 << d)) != 0 ? 1 : 0;
        }
    }

    public void setTexRegTexMapSync(boolean synced, String lookupKey) {
        this.texRegTexMapSynced = synced;
        this.texRegGetIconLookupKey = lookupKey;
    }

    public Boolean getTexRegTexMapSynced() {
        return texRegTexMapSynced;
    }

    public String getTexRegGetIconLookupKey() {
        return texRegGetIconLookupKey;
    }

    /** 设置实际用于绘制的 icon 的调试信息（名称与是否已加载），便于排查紫黑块。 */
    public void setDrawSpriteInfo(String iconName, int width, int height) {
        this.drawSpriteName = iconName;
        this.drawSpriteLoaded = (width > 0 && height > 0) ? (width + "x" + height) : "0x0(unloaded)";
    }

    public String getDrawSpriteName() {
        return drawSpriteName;
    }

    public String getDrawSpriteLoaded() {
        return drawSpriteLoaded;
    }

    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public String getDegradationReason() {
        return degradationReason;
    }

    public String getPredicateUsed() {
        return predicateUsed;
    }

    public int[] getTilePos() {
        return tilePos;
    }

    public int[] getConnectionBits() {
        return connectionBits;
    }
}

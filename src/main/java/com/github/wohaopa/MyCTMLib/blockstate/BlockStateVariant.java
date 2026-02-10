package com.github.wohaopa.MyCTMLib.blockstate;

/**
 * 单条方块状态 variant 的 POJO。
 * 对应 blockstates JSON 中 variants 下的一条，如 { "model": "modid:block/stone" }。
 */
public class BlockStateVariant {

    private final String model;

    public BlockStateVariant(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}

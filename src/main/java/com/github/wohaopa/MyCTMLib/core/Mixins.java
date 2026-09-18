package com.github.wohaopa.MyCTMLib.core;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    MINECRAFT_CLIENT(new MixinBuilder("Client texture mixins")
        .addClientMixins(
            "AccessorTextureAtlasSprite",
            "AccessorSimpleReloadableResourceManager",
            "AccessorMinecraft",
            "AccessorSimpleResource",
            "MixinItemRenderer",
            "MixinRenderBlocks",
            "MixinTextureMap",
            "MixinSimpleReloadableResourceManager")
        .setPhase(Phase.EARLY)),

    INDUSTRIAL_CRAFT_2(new MixinBuilder("IndustrialCraft 2 compatibility mixin").addCommonMixins("MixinBlockMetaData")
        .setPhase(Phase.EARLY)
        .addRequiredMod(Mods.INDUSTRIAL_CRAFT_2)),

    GREGTECH(new MixinBuilder("GregTech compatibility mixins").addCommonMixins("AccessorGTRenderedTexture")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.GREGTECH));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}

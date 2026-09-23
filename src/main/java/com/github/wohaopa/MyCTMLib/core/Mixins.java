package com.github.wohaopa.MyCTMLib.core;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    MINECRAFT_CLIENT(new MixinBuilder("Client texture mixins")
        .addClientMixins(
            "MixinItemRenderer",
            "MixinRenderBlocks",
            "MixinTextureMap",
            "MixinSimpleReloadableResourceManager")
        .setPhase(Phase.EARLY)),

    INDUSTRIAL_CRAFT_2(new MixinBuilder("IndustrialCraft 2 compatibility mixin").addCommonMixins("MixinBlockMetaData")
        .setPhase(Phase.EARLY)
        .addRequiredMod(Mods.IndustrialCraft2)),

    FORESTRY(new MixinBuilder("Forestry bee GUI item layer mixin").addClientMixins("MixinForestryLedger")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.Forestry)),

    APPLIED_ENERGISTICS_2(new MixinBuilder("AE2 JSON device models")
        .addClientMixins("MixinRenderDrive", "MixinRenderMEChest", "MixinRenderBlockWireless")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.AppliedEnergistics2)),

    LOGISTICS_PIPES(new MixinBuilder("LogisticsPipes bee GUI item layer mixins")
        .addClientMixins("MixinGuiApiaristSinkTypeSlot", "MixinGuiApiaristSink")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.LogisticsPipes)),

    GENDUSTRY(
        new MixinBuilder("gendustry bee GUI item layer mixin").addClientMixins("MixinGendustryWidgetContainerWindow")
            .setPhase(Phase.LATE)
            .addRequiredMod(Mods.Gendustry)),

    GREGTECH(new MixinBuilder("GregTech compatibility mixins").addCommonMixins("AccessorGTRenderedTexture")
        .addClientMixins("MixinSBRWorldContext")
        .setPhase(Phase.LATE)
        .addRequiredMod(Mods.Gregtech));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}

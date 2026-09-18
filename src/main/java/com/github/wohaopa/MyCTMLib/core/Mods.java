package com.github.wohaopa.MyCTMLib.core;

import java.util.Locale;

import com.gtnewhorizon.gtnhlib.util.data.IMod;
import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

import cpw.mods.fml.common.Loader;

public enum Mods implements IMod, ITargetMod {

    FORESTRY("Forestry"),
    INDUSTRIAL_CRAFT_2("IC2", "ic2.core.coremod.IC2core"),
    GREGTECH("gregtech");

    private final String id;
    private final String resourceDomain;
    private final TargetModBuilder targetModBuilder;
    private Boolean loaded;

    Mods(String id) {
        this(id, null);
    }

    Mods(String id, String coreModClass) {
        this.id = id;
        this.resourceDomain = id.toLowerCase(Locale.ROOT);
        this.targetModBuilder = new TargetModBuilder().setModId(id)
            .setCoreModClass(coreModClass);
    }

    @Override
    public TargetModBuilder getBuilder() {
        return targetModBuilder;
    }

    @Override
    public boolean isModLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(id);
        }
        return loaded;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getResourceLocation() {
        return resourceDomain;
    }
}

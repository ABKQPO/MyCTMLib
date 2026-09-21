package com.github.wohaopa.MyCTMLib.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({ "com.github.wohaopa.MyCTMLib.core" })
@IFMLLoadingPlugin.Name("MyCTMLib core plugin")
public class EarlyCoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader, IFMLCallHook {

    public static EarlyCoreMod INSTANCE;

    public EarlyCoreMod() {
        INSTANCE = this;
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return EarlyCoreMod.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        int v = Runtime.version()
            .feature();
        if (v >= 21) return "mixins.MyCTMLib.early.j21.json";
        if (v >= 17) return "mixins.MyCTMLib.early.j17.json";
        return "mixins.MyCTMLib.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixins.class, loadedCoreMods);
    }
}

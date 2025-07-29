package com.github.wohaopa.MyCTMLib;

import java.util.List;
import java.util.Map;

import com.github.wohaopa.MyCTMLib.mixins.EarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import io.github.tox1cozz.mixinbooterlegacy.IEarlyMixinLoader;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({ "com.github.wohaopa.MyCTMLib" })
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
        return "com.github.wohaopa.MyCTMLib.EarlyCoreMod";
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return EarlyMixinLoader.getMixinConfigs();
    }

    @Override
    public boolean shouldMixinConfigQueue(final String mixinConfig) {
        return EarlyMixinLoader.shouldMixinConfigQueue(mixinConfig);
    }
}

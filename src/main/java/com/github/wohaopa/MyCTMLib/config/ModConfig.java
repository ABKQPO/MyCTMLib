package com.github.wohaopa.MyCTMLib.config;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

@Config(modid = MyCTMLib.MODID, filename = "MyCTMLib", category = "general")
@Config.LangKeyPattern(pattern = "myctmlib.config.%cat.%field", fullyQualified = true)
public class ModConfig {

    @Config.Comment("Enable debug logging for resource-pack metadata errors.")
    @Config.DefaultBoolean(false)
    public static boolean debug = false;

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(ModConfig.class);
    }
}

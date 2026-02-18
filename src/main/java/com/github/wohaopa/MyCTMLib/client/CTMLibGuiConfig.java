package com.github.wohaopa.MyCTMLib.client;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CTMLibGuiConfig extends GuiConfig {

    public CTMLibGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), MyCTMLib.MODID, false, false, "MyCTMLib Configuration");
    }

    @SuppressWarnings("unchecked")
    private static List<IConfigElement> getConfigElements() {
        Configuration config = MyCTMLib.instance != null ? MyCTMLib.instance.getConfiguration() : null;
        if (config == null) {
            return java.util.Collections.emptyList();
        }
        return new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
    }
}

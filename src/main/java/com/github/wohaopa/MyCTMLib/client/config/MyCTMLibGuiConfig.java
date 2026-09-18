package com.github.wohaopa.MyCTMLib.client.config;

import net.minecraft.client.gui.GuiScreen;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.config.ModConfig;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MyCTMLibGuiConfig extends SimpleGuiConfig {

    public MyCTMLibGuiConfig(GuiScreen parentScreen) throws ConfigException {
        super(parentScreen, MyCTMLib.MODID, MyCTMLib.MODID, true, ModConfig.class);
    }
}

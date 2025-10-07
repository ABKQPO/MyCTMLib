package com.github.wohaopa.MyCTMLib;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wohaopa.MyCTMLib.mixins.AccessorMinecraft;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = MyCTMLib.MODID, version = "v1.2.5_28x", name = "MyCTMLib", acceptedMinecraftVersions = "[1.7.10]")
public class MyCTMLib {

    public static boolean isInit = false;
    public static final String MODID = "MyCTMLib";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static boolean debugMode = false;
    public Configuration configuration;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 只在客户端注册元数据序列化器
        if (FMLCommonHandler.instance()
            .getSide()
            .isClient()) {
            registerMetadataSerializer();
        }

        FMLCommonHandler.instance()
            .bus()
            .register(this);
        configuration = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
    }

    @SideOnly(Side.CLIENT)
    private void registerMetadataSerializer() {
        ((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer()
            .registerMetadataSectionType(
                new MyCTMLibMetadataSectionSerializer(),
                MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection.class);
    }

    @Mod.EventHandler
    public void completeInit(FMLLoadCompleteEvent event) {
        isInit = true;
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(MyCTMLib.MODID)) {
            loadConfig();
        }
    }

    private void loadConfig() {
        debugMode = configuration.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Enable debug mode");
        if (configuration.hasChanged()) {
            configuration.save();
        }

    }
}

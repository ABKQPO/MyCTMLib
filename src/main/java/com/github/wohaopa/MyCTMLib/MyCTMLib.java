package com.github.wohaopa.MyCTMLib;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = MyCTMLib.MODID, version = Tags.VERSION, name = "MyCTMLib", acceptedMinecraftVersions = "[1.7.10]")
public class MyCTMLib {

    public static final String MODID = "MyCTMLib";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public String[] textures;
    public Configuration configuration;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        configuration = new Configuration(event.getSuggestedConfigurationFile());
        // GameRegistry.registerBlock(new Block(Material.wood) {
        //
        // @Override
        // public void registerBlockIcons(IIconRegister reg) {
        // blockIcon = reg.registerIcon("myctmlib:iceiceice");
        // IIcon icon = reg.registerIcon("myctmlib:iceiceice-ctm");
        // Textures.ctmIconMap.put("myctmlib:iceiceice", new CTMIconManager(blockIcon, icon));
        // }
        //
        // @Override
        // public IIcon getIcon(int side, int meta) {
        // return super.getIcon(side, meta);
        // }
        // }, "iceiceice");
        loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Textures.register(textures);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(MyCTMLib.MODID)) {
            loadConfig();
        }
    }

    private void loadConfig() {
        textures = configuration.getStringList("textures", Configuration.CATEGORY_GENERAL, new String[] {}, "ctm list");
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}

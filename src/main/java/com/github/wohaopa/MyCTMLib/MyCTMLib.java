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

@Mod(modid = MyCTMLib.MODID, version = "v1.0.4_28x", name = "MyCTMLib", acceptedMinecraftVersions = "[1.7.10]")
public class MyCTMLib {

    public static final String MODID = "MyCTMLib";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public String[] textures;
    public static boolean debugMode = false;
    public Configuration configuration;
    public static String[] ctmList = new String[] { "glass", "ic2:blockAlloyGlass&0", "ic2:blockAlloyGlass&1",
        "ic2:blockAlloyGlass&2", "ic2:blockAlloyGlass&3", "ic2:blockAlloyGlass&4", "ic2:blockAlloyGlass&5",
        "sciencenotleisure:MetaBlockColumn/2_Bottom", "sciencenotleisure:MetaBlockColumn/2_Side",
        "sciencenotleisure:MetaBlockColumn/2_Top", "sciencenotleisure:MetaBlockColumn/3_Bottom",
        "sciencenotleisure:MetaBlockColumn/3_Side", "sciencenotleisure:MetaBlockColumn/3_Top",
        "sciencenotleisure:MetaBlockGlass/0", "sciencenotleisure:MetaBlockGlass/1",
        "sciencenotleisure:MetaBlockGlass/2", "sciencenotleisure:MetaBlockGlow/1", "sciencenotleisure:MetaBlockGlow/3",
        "sciencenotleisure:MetaBlockGlow/5", "sciencenotleisure:MetaBlockGlow/7", "sciencenotleisure:MetaBlockGlow/9",
        "sciencenotleisure:MetaBlockGlow/11", "sciencenotleisure:MetaBlockGlow/13",
        "sciencenotleisure:MetaBlockGlow/15", "sciencenotleisure:MetaBlockGlow/17",
        "sciencenotleisure:MetaBlockGlow/19", "sciencenotleisure:MetaBlockGlow/21",
        "sciencenotleisure:MetaBlockGlow/23", "sciencenotleisure:MetaBlockGlow/25",
        "sciencenotleisure:MetaBlockGlow/27", "sciencenotleisure:MetaBlockGlow/29",
        "sciencenotleisure:MetaBlockGlow/31", "sciencenotleisure:MetaBlocks/3", "sciencenotleisure:MetaBlocks/5",
        "sciencenotleisure:MetaBlocks/7", "sciencenotleisure:MetaBlocks/9", "sciencenotleisure:MetaBlocks/11",
        "sciencenotleisure:MetaBlocks/13", "sciencenotleisure:MetaBlocks/15", "sciencenotleisure:MetaBlocks/17",
        "sciencenotleisure:MetaBlocks/19", "sciencenotleisure:MetaBlocks/21", "sciencenotleisure:MetaBlocks/23",
        "sciencenotleisure:MetaBlocks/25", "sciencenotleisure:MetaBlocks/27", "sciencenotleisure:MetaBlocks/29",
        "sciencenotleisure:MetaBlocks/31", "sciencenotleisure:MetaBlocks/33", "sciencenotleisure:MetaCasing/7",
        "sciencenotleisure:MetaCasing/8", "sciencenotleisure:MetaCasing/13", "sciencenotleisure:MetaCasing/14",
        "sciencenotleisure:MetaCasing/15", "sciencenotleisure:MetaCasing/19", "sciencenotleisure:MetaCasing/20",
        "sciencenotleisure:MetaCasing/23", "sciencenotleisure:MetaCasing/24", "sciencenotleisure:MetaCasing/25",
        "sciencenotleisure:MetaCasing/27", "sciencenotleisure:MetaCasing/29", "sciencenotleisure:MetaCasing/30",
        "sciencenotleisure:MetaCasing02/0", "gregtech:iconsets/BLOCK_PLASCRETE",
        "gregtech:iconsets/MACHINE_CASING_DATA_DRIVE", "gregtech:iconsets/MACHINE_CASING_CONTAINMENT_FIELD",
        "gregtech:iconsets/WATER_PLANT_CONCRETE_CASING", "gregtech:iconsets/GODFORGE_ENERGY",
        "gregtech:iconsets/BLOCK_STEELPREIN", "gregtech:iconsets/MACHINE_CASING_HIGH_PRESSURE_RESISTANT",
        "gregtech:iconsets/BLOCK_NEUTRONIUMPREIN", "gregtech:iconsets/BLOCK_TITANIUMPREIN",
        "gregtech:iconsets/BLOCK_NAQUADAHPREIN", "gregtech:iconsets/BLOCK_TSREIN", "gregtech:iconsets/BLOCK_IRREIN",
        "gregtech:iconsets/BLOCK_BRONZEPREIN", "gregtech:iconsets/MACHINE_CASING_CHEMICALLY_INERT",
        "gregtech:iconsets/MACHINE_CASING_CLEAN_STAINLESSSTEEL", "gregtech:iconsets/MACHINE_CASING_FROST_PROOF",
        "gregtech:iconsets/MACHINE_COIL_AWAKENEDDRACONIUM",
        "gregtech:iconsets/MACHINE_COIL_AWAKENEDDRACONIUM_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_AWAKENEDDRACONIUM_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_ETERNAL",
        "gregtech:iconsets/MACHINE_COIL_ETERNAL_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_ETERNAL_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_HYPOGEN", "gregtech:iconsets/MACHINE_COIL_HYPOGEN_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_HYPOGEN_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_INFINITY",
        "gregtech:iconsets/MACHINE_COIL_INFINITY_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_INFINITY_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_CUPRONICKEL", "gregtech:iconsets/MACHINE_COIL_CUPRONICKEL_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_CUPRONICKEL_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_ELECTRUMFLUX",
        "gregtech:iconsets/MACHINE_COIL_ELECTRUMFLUX_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_ELECTRUMFLUX_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_HSSG",
        "gregtech:iconsets/MACHINE_COIL_HSSG_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_HSSG_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_HSSS", "gregtech:iconsets/MACHINE_COIL_HSSS_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_HSSS_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_KANTHAL",
        "gregtech:iconsets/MACHINE_COIL_KANTHAL_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_KANTHAL_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_NAQUADAH", "gregtech:iconsets/MACHINE_COIL_NAQUADAH_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_NAQUADAH_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_NAQUADAHALLOY",
        "gregtech:iconsets/MACHINE_COIL_NAQUADAHALLOY_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_NAQUADAHALLOY_FOREGROUND", "gregtech:iconsets/MACHINE_COIL_NICHROME",
        "gregtech:iconsets/MACHINE_COIL_NICHROME_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_NICHROME_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_SUPERCONDUCTOR", "gregtech:iconsets/MACHINE_COIL_TRINIUM",
        "gregtech:iconsets/MACHINE_COIL_TRINIUM_BACKGROUND", "gregtech:iconsets/MACHINE_COIL_TRINIUM_FOREGROUND",
        "gregtech:iconsets/MACHINE_COIL_TUNGSTENSTEEL", "gregtech:iconsets/MACHINE_COIL_TUNGSTENSTEEL_BACKGROUND",
        "gregtech:iconsets/MACHINE_COIL_TUNGSTENSTEEL_FOREGROUND", "gregtech:iconsets/MACHINE_HEATPROOFCASING",
        "gregtech:iconsets/MACHINE_CASING_STABLE_TITANIUM", "gregtech:iconsets/MACHINE_CASING_FIREBOX_TITANIUM",
        "gregtech:iconsets/MACHINE_BRONZEPLATEDBRICKS", "gregtech:iconsets/MACHINE_CASING_ADVANCEDGAS",
        "gregtech:iconsets/MACHINE_CASING_ADVANCEDRADIATIONPROOF", "gregtech:iconsets/MACHINE_CASING_FIREBOX_BRONZE",
        "gregtech:iconsets/MACHINE_CASING_FIREBOX_TUNGSTENSTEEL",
        "gregtech:iconsets/MACHINE_CASING_ROBUST_TUNGSTENSTEEL", "gregtech:iconsets/MACHINE_CASING_FIREBOX_STEEL",
        "gregtech:iconsets/MACHINE_CASING_IRIDIUM", "gregtech:iconsets/MACHINE_CASING_MAGICAL",
        "gregtech:iconsets/MACHINE_CASING_FUSION", "gregtech:iconsets/MACHINE_CASING_FUSION_2",
        "gregtech:iconsets/MACHINE_CASING_SOLID_STEEL", "gregtech:iconsets/MACHINE_CASING_RHODIUM_PALLADIUM",
        "gregtech:iconsets/MACHINE_CASING_STRIPES_A", "gregtech:iconsets/MACHINE_CASING_STRIPES_B",
        "gregtech:iconsets/MACHINE_CASING_MINING_OSMIRIDIUM", "gregtech:iconsets/MACHINE_CASING_MINING_BLACKPLUTONIUM",
        "gregtech:iconsets/MACHINE_CASING_MINING_NEUTRONIUM", "gregtech:iconsets/MACHINE_CASING_MOTOR",
        "gregtech:iconsets/MACHINE_CASING_PCB_TIER_1", "gregtech:iconsets/MACHINE_CASING_PCB_TIER_2",
        "gregtech:iconsets/MACHINE_CASING_PCB_TIER_3", "gregtech:iconsets/MACHINE_DIM_TRANS_CASING",
        "gregtech:iconsets/MACHINE_CASING_PROCESSOR", "gregtech:iconsets/MACHINE_CASING_PUMP",
        "gregtech:iconsets/MACHINE_CASING_RADIANT_NAQUADAH_ALLOY", "gregtech:iconsets/MACHINE_CASING_TURBINE",
        "gregtech:iconsets/MACHINE_DIM_INJECTOR", "gregtech:iconsets/MACHINE_CASING_VENT",
        "gregtech:iconsets/MACHINE_CASING_VENT_T2", "gregtech:iconsets/MACHINE_CASING_RADIATIONPROOF",
        "gregtech:iconsets/LONG_DISTANCE_PIPE_FLUID", "gregtech:iconsets/LONG_DISTANCE_PIPE_ITEM",
        "gregtech:iconsets/INFINITY_COOLED_CASING", "gregtech:iconsets/MACHINE_CASING_TURBINE_HSSS",
        "gregtech:iconsets/MACHINE_CASING_TURBINE_STEEL", "gregtech:iconsets/MACHINE_CASING_TURBINE_TITANIUM",
        "gregtech:iconsets/MACHINE_CASING_TURBINE_TUNGSTENSTEEL",
        "gregtech:iconsets/MACHINE_CASING_TURBINE_STAINLESSSTEEL", "gregtech:iconsets/MACHINE_CASING_LASER",
        "gregtech:iconsets/NAQUADRIA_REINFORCED_WATER_PLANT_CASING", "gregtech:iconsets/MACHINE_CASING_AUTOCLAVE",
        "gregtech:iconsets/MACHINE_CASING_OZONE", "gregtech:iconsets/MACHINE_CASING_NAQUADAH_REINFORCED_WATER_PLANT",
        "gregtech:iconsets/MACHINE_CASING_FLOCCULATION", "gregtech:iconsets/MACHINE_CASING_EMS",
        "gregtech:iconsets/COMPRESSOR_CASING", "gregtech:iconsets/EM_CASING", "gregtech:iconsets/EM_PC",
        "gregtech:iconsets/EM_PC_NONSIDE", "gregtech:iconsets/EM_PC_VENT", "gregtech:iconsets/EM_PC_VENT_NONSIDE",
        "gregtech:iconsets/EM_PC_ADV", "gregtech:iconsets/EM_PC_ADV_NONSIDE", "gregtech:iconsets/GLASS_PH_RESISTANT",
        "gregtech:iconsets/GLASS_QUARK_CONTAINMENT", "gregtech:iconsets/HAWKING_GLASS",
        "gregtech:iconsets/NEUTRONIUM_COATED_UV_RESISTANT_GLASS", "gregtech:iconsets/OMNI_PURPOSE_INFINITY_FUSED_GLASS",
        "gregtech:iconsets/GODFORGE_TRIM", "gregtech:iconsets/MACHINE_CASING_SHIELDED_ACCELERATOR",
        "gregtech:iconsets/GODFORGE_INNER", "gregtech:iconsets/GODFORGE_SUPPORT", "gregtech:iconsets/EM_POWER",
        "gregtech:iconsets/BLOCK_QUARK_CONTAINMENT_CASING", "gregtech:iconsets/NEUTRONIUM_CASING",
        "gregtech:iconsets/BLOCK_QUARK_RELEASE_CHAMBER", "gregtech:iconsets/TM_TESLA_BASE_TOP_BOTTOM",
        "gregtech:iconsets/TM_TESLA_BASE_SIDES", "gregtech:iconsets/MACHINE_CASING_MS160",
        "gregtech:iconsets/MACHINE_CASING_EXTREME_CORROSION_RESISTANT",
        "gregtech:iconsets/TM_TESLA_WINDING_SECONDARY_SIDES", "gregtech:iconsets/TM_TESLA_WINDING_SECONDARY_TOP_BOTTOM",
        "gregtech:iconsets/RADIATION_ABSORBENT_CASING", "gregtech:iconsets/EM_COIL_NONSIDE",
        "gregtech:iconsets/EM_COIL", "gregtech:icons/supercriticalFluidTurbineCasing", "tectech:blockQuantumGlass",
        "tectech:blockSpatiallyTranscendentGravitationalLens", "kekztech:LSCBase_top", "kekztech:LSCBase_side",
        "kekztech:LargeHexTile_0_0", "kekztech:LargeHexTile_0_1", "kekztech:LargeHexTile_0_2",
        "kekztech:LargeHexTile_0_3", "kekztech:LargeHexTile_1_0", "kekztech:LargeHexTile_1_1",
        "kekztech:LargeHexTile_1_2", "kekztech:LargeHexTile_1_3", "kekztech:LargeHexTile_2_0",
        "kekztech:LargeHexTile_2_1", "kekztech:LargeHexTile_2_2", "kekztech:LargeHexTile_2_3",
        "kekztech:LargeHexTile_3_0", "kekztech:LargeHexTile_3_1", "kekztech:LargeHexTile_3_2",
        "kekztech:LargeHexTile_3_3", "kekztech:TFFTCasing", "miscutils:chrono/MetalSheet",
        "miscutils:chrono/MetalPanel", "miscutils:chrono/MetalSheet9", "miscutils:chrono/MetalSheet8",
        "miscutils:iconsets/SC_TURBINE", "miscutils:iconsets/MACHINE_CASING_FUSION_3",
        "miscutils:iconsets/MACHINE_CASING_FUSION_4", "miscutils:metro/TEXTURE_MAGIC_A",
        "miscutils:metro/TEXTURE_MAGIC_B", "miscutils:metro/TEXTURE_TECH_PANEL_A",
        "miscutils:metro/TEXTURE_TECH_PANEL_D", "miscutils:metro/TEXTURE_METAL_PANEL_F",
        "miscutils:metro/TEXTURE_METAL_PANEL_A", "miscutils:metro/TEXTURE_TECH_A", "miscutils:metro/TEXTURE_TECH_B",
        "miscutils:metro/TEXTURE_METAL_PANEL_B", "miscutils:metro/TEXTURE_METAL_PANEL_C",
        "miscutils:metro/TEXTURE_METAL_PANEL_D", "miscutils:metro/TEXTURE_TECH_PANEL_H",
        "miscutils:metro/TEXTURE_STONE_RED_A", "miscutils:metro/TEXTURE_STONE_RED_B", "miscutils:special/block_1",
        "miscutils:special/block_2", "miscutils:special/block_3", "miscutils:special/block_4",
        "miscutils:special/block_5", "miscutils:special/block_6", "miscutils:special/block_7",
        "miscutils:special/block_8", "miscutils:TileEntities/MACHINE_CASING_FIREBOX_STABALLOY",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_HASTELLOY_N",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_HASTELLOY_X",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_ZERON100",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_STELLITE",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_MARAGINGSTEEL",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_TRINIUM_TITANIUM",
        "miscutils:TileEntities/MACHINE_CASING_LAURENIUM", "miscutils:TileEntities/MACHINE_CASING_FLOTATION",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_TALONITE", "miscutils:TileEntities/CASING_AMAZON",
        "miscutils:TileEntities/MACHINE_CASING_ROCKETDYNE", "miscutils:TileEntities/MACHINE_CASING_PIPE_T1",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_TUMBAGA",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_TANTALLOY61", "miscutils:TileEntities/machine_top",
        "miscutils:TileEntities/sterileCasing", "miscutils:TileEntities/MACHINE_CASING_ADVANCED_CRYOGENIC",
        "miscutils:TileEntities/MACHINE_CASING_ADVANCED_VOLCANUS", "miscutils:TileEntities/MACHINE_CASING_QFT_COIL",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_RED_STEEL", "miscutils:TileEntities/MACHINE_CASING_CENTRIFUGE",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_ZIRCONIUM_CARBIDE",
        "miscutils:TileEntities/MACHINE_CASING_STABLE_POTIN", "miscutils:TileEntities/MACHINE_CASING_STABLE_GRISIUM",
        "miscutils:qft/ForceFieldGlass", "miscutils:qft/blank", "bartworks:TransformerCoil",
        "gtnhlanth:casing.coolant_delivery", "gtnhlanth:casing.focus_manipulator", "gtnhlanth:casing.niobium_cavity",
        "gtnhlanth:casing.shielded_accelerator", "gtnhlanth:casing.target_receptacle", "GoodGenerator:MagicCasing",
        "GoodGenerator:essentiaOutputHatch_ME", "GoodGenerator:preciseUnitCasing/0",
        "GoodGenerator:preciseUnitCasing/1", "GoodGenerator:preciseUnitCasing/2", "GoodGenerator:preciseUnitCasing/3",
        "GoodGenerator:preciseUnitCasing/4", "GoodGenerator:antimatterContainmentCasing",
        "GoodGenerator:magneticFluxCasing", "GoodGenerator:supercriticalFluidTurbineCasing",
        "GoodGenerator:titaniumPlatedCylinder", "GoodGenerator:rawCylinder", "GoodGenerator:yottaFluidTankCasing_TOP",
        "GoodGenerator:yottaFluidTankCasing_SIDE", "GoodGenerator:fieldRestrictingGlass", "GoodGenerator:MAR_Casing",
        "galaxyspace:dysonSwarm/ControlSecondary", "galaxyspace:dysonSwarm/ControlSecondary_Side",
        "galaxyspace:dysonSwarm/Floor", "galaxyspace:dysonSwarm/Floor_Side", "galaxyspace:dysonSwarm/ReceiverDish",
        "galaxyspace:dysonSwarm/ReceiverDish_Side", "galaxyspace:dysonSwarm/DeploymentUnitCasing",
        "galaxyspace:dysonSwarm/DeploymentUnitCasing_Side", "galaxyspace:dysonSwarm/DeploymentUnitMagnet",
        "galaxyspace:dysonSwarm/DeploymentUnitMagnet_Side", "galaxyspace:dysonSwarm/ControlToroid",
        "galaxyspace:dysonSwarm/ControlToroid_Side", "galaxyspace:dysonSwarm/DeploymentUnitCore",
        "galaxyspace:dysonSwarm/DeploymentUnitCore_Side", "galaxyspace:dysonSwarm/ControlCasing",
        "galaxyspace:dysonSwarm/ControlCasing_Side", "galaxyspace:dysonSwarm/ReceiverCasing",
        "galaxyspace:dysonSwarm/ReceiverCasing_Side", "gtnhintergalactic:spaceElevator/InternalStructure",
        "gtnhintergalactic:spaceElevator/InternalStructure_Side", "gtnhintergalactic:spaceElevator/SupportStructure",
        "gtnhintergalactic:spaceElevator/SupportStructure_Side", "gtnhintergalactic:spaceElevator/BaseCasing",
        "gtnhintergalactic:dysonSwarm/ControlSecondary", "gtnhintergalactic:dysonSwarm/ControlSecondary_Side",
        "gtnhintergalactic:dysonSwarm/Floor", "gtnhintergalactic:dysonSwarm/Floor_Side",
        "gtnhintergalactic:dysonSwarm/ReceiverDish", "gtnhintergalactic:dysonSwarm/ReceiverDish_Side",
        "gtnhintergalactic:dysonSwarm/DeploymentUnitCasing", "gtnhintergalactic:dysonSwarm/DeploymentUnitCasing_Side",
        "gtnhintergalactic:dysonSwarm/DeploymentUnitMagnet", "gtnhintergalactic:dysonSwarm/DeploymentUnitMagnet_Side",
        "gtnhintergalactic:dysonSwarm/ControlToroid", "gtnhintergalactic:dysonSwarm/ControlToroid_Side",
        "gtnhintergalactic:dysonSwarm/DeploymentUnitCore", "gtnhintergalactic:dysonSwarm/DeploymentUnitCore_Side",
        "gtnhintergalactic:dysonSwarm/ControlCasing", "gtnhintergalactic:dysonSwarm/ControlCasing_Side",
        "gtnhintergalactic:dysonSwarm/ReceiverCasing", "gtnhintergalactic:dysonSwarm/ReceiverCasing_Side",
        "kubatech:casing/defc_0", "kubatech:casing/defc_1", "kubatech:casing/defc_2", "kubatech:casing/defc_3",
        "kubatech:casing/defc_4", "kubatech:casing/defc_5" };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        configuration = new Configuration(event.getSuggestedConfigurationFile());
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
        textures = configuration.getStringList("textures", Configuration.CATEGORY_GENERAL, ctmList, "ctm list");
        debugMode = configuration.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Enable debug mode");
        if (configuration.hasChanged()) {
            configuration.save();
        }

    }
}

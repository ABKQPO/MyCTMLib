package com.github.wohaopa.MyCTMLib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wohaopa.MyCTMLib.mixins.AccessorMinecraft;
import com.github.wohaopa.MyCTMLib.resource.CTMLibResourceLoader;
import com.github.wohaopa.MyCTMLib.texture.TextureMetadataSection;
import com.github.wohaopa.MyCTMLib.texture.TextureMetadataSectionSerializer;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(
    modid = MyCTMLib.MODID,
    version = "v1.2.5_28x",
    name = "MyCTMLib",
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.github.wohaopa.MyCTMLib.client.CTMLibGuiFactory")
public class MyCTMLib {

    public static boolean isInit = false;
    public static final String MODID = "MyCTMLib";
    /** 供 CTMLibGuiConfig 等客户端组件获取配置。 */
    public static MyCTMLib instance;

    @SidedProxy(
        clientSide = "com.github.wohaopa.MyCTMLib.client.ClientProxy",
        serverSide = "com.github.wohaopa.MyCTMLib.CommonProxy")
    public static com.github.wohaopa.MyCTMLib.CommonProxy proxy;
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static boolean debugMode = false;
    /** 为 true 时在资源重载后导出 block_texture_dump.json 供迁移脚本使用。 */
    public static boolean dumpBlockTextureMapping = false;
    public Configuration configuration;

    /** 供 CTMLibGuiConfig 等客户端组件获取配置。 */
    public Configuration getConfiguration() {
        return configuration;
    }

    /** 仅对 stone/cobblestone 打表与过滤 [CTMLibFusion] 日志，避免无效刷屏。支持 blockId、modelId、iconName 等格式。 */
    public static boolean isFusionTraceTarget(String name) {
        if (name == null) return false;
        String s = name.indexOf(':') >= 0 ? name.substring(name.indexOf(':') + 1) : name;
        return "stone".equals(s) || "cobblestone".equals(s)
            || "block/stone".equals(s)
            || "block/cobblestone".equals(s)
            || "blocks/stone".equals(s)
            || "blocks/cobblestone".equals(s);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
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
        IMetadataSerializer serializer = ((AccessorMinecraft) Minecraft.getMinecraft()).getMetadataSerializer();
        serializer.registerMetadataSectionType(
            new MyCTMLibMetadataSectionSerializer(),
            MyCTMLibMetadataSectionSerializer.MyCTMLibMetadataSection.class);
        // 新管线：ctmlib section，供 mcmeta 解析器与 TextureRegistry 使用
        serializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        // 资源重载时加载 BlockState/Model，并清空新注册表（清空在 MixinSimpleReloadableResourceManager.clearResources）
        if (Minecraft.getMinecraft()
            .getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getMinecraft()
                .getResourceManager()).registerReloadListener(new CTMLibResourceLoader());
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
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
        dumpBlockTextureMapping = configuration.getBoolean(
            "dumpBlockTextureMapping",
            Configuration.CATEGORY_GENERAL,
            false,
            "Export block ID and texture mapping to ctmlib_block_texture_dump.json on resource reload");
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}

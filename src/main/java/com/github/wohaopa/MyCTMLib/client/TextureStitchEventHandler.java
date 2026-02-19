package com.github.wohaopa.MyCTMLib.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.github.wohaopa.MyCTMLib.resource.CTMLibResourceLoader;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 参考 GTNHLib：在 TextureStitchEvent.Pre 中通过 registerIcon 注册模型所需的自定义纹理，
 * 确保纹理正确加入 mapRegisteredSprites，避免 loadTextureAtlas 内部逻辑覆盖。
 */
@SideOnly(Side.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.map;
        int textureType = map.getTextureType();
        boolean isBlocks = (textureType == 0);
        boolean isItems = (textureType == 1);
        if (!isBlocks && !isItems) return;

        CTMLibResourceLoader.ensureLoaded(Minecraft.getMinecraft().getResourceManager());

        Map<String, TextureTypeData> pathToData = TextureRegistry.getInstance()
            .getPathToDataForDump();
        for (Map.Entry<String, TextureTypeData> e : pathToData.entrySet()) {
            if (!(e.getValue() instanceof ConnectingTextureData)) continue;
            String key = e.getKey();
            TextureKeyNormalizer.TextureCategory cat = TextureKeyNormalizer.getTextureCategory(key);
            if (isBlocks && cat != TextureKeyNormalizer.TextureCategory.BLOCKS) continue;
            if (isItems && cat != TextureKeyNormalizer.TextureCategory.ITEMS) continue;

            try {
                map.registerIcon(toRegisterIconName(key, isBlocks, isItems));
            } catch (Exception ex) {
                com.github.wohaopa.MyCTMLib.MyCTMLib.LOG.warn(
                    "[CTMLib] TextureStitchEvent.Pre registerIcon failed key={}", key, ex);
            }
        }
    }

    /**
     * 将 canonicalKey 转为 registerIcon 接受的格式，避免 TextureMap 拼出错误路径。
     * 1.7.10 中 TextureMap 用 basePath + iconName 拼资源路径；blocks 的 basePath 已是 "textures/blocks"，
     * 若传入 "blocks/stone" 会变成 "textures/blocks/blocks/stone.png" 导致 load 失败。故对 blocks/items
     * 只传短名（去掉 "blocks/" 或 "items/" 前缀），例如 minecraft:blocks/stone → minecraft:stone（或 stone）。
     */
    private static String toRegisterIconName(String canonicalKey, boolean isBlocks, boolean isItems) {
        if (canonicalKey == null) return "";
        if (isBlocks && canonicalKey.contains(":blocks/")) {
            int i = canonicalKey.indexOf(":blocks/");
            String domain = canonicalKey.substring(0, i);
            String shortName = canonicalKey.substring(i + ":blocks/".length());
            return "minecraft".equalsIgnoreCase(domain) ? shortName : (domain + ":" + shortName);
        }
        if (isItems && canonicalKey.contains(":items/")) {
            int i = canonicalKey.indexOf(":items/");
            String domain = canonicalKey.substring(0, i);
            String shortName = canonicalKey.substring(i + ":items/".length());
            return "minecraft".equalsIgnoreCase(domain) ? shortName : (domain + ":" + shortName);
        }
        return canonicalKey.replaceFirst("^minecraft:", "");
    }
}

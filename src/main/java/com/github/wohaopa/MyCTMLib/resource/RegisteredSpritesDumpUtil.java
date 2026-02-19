package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.mixins.AccessorTextureMap;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 导出 TextureMap 的 mapRegisteredSprites 至 JSON 文件。
 * 每条目含 name、textureName、fullPath、width、height、category。
 * 由 /ctmlib dump_registered_sprites 命令调用。
 */
public class RegisteredSpritesDumpUtil {

    private static String toFullPath(String iconName, String basePath) {
        if (iconName == null || basePath == null) return "";
        int colon = iconName.indexOf(':');
        String domain = colon >= 0 ? iconName.substring(0, colon) : "minecraft";
        String pathPart = colon >= 0 ? iconName.substring(colon + 1) : iconName;
        return "assets/" + domain + "/" + basePath + "/" + pathPart + ".png";
    }

    private static void appendSprites(JsonArray out, Map<String, TextureAtlasSprite> sprites, String category) {
        String basePath = TextureKeyNormalizer.getBasePath(
            "blocks".equals(category) ? TextureKeyNormalizer.TextureCategory.BLOCKS : TextureKeyNormalizer.TextureCategory.ITEMS);
        for (Map.Entry<String, TextureAtlasSprite> e : sprites.entrySet()) {
            TextureAtlasSprite sprite = e.getValue();
            String mapKey = e.getKey();
            String textureName = sprite.getIconName();
            JsonObject obj = new JsonObject();
            obj.addProperty("name", mapKey);
            obj.addProperty("textureName", textureName);
            obj.addProperty("fullPath", toFullPath(textureName, basePath));
            obj.addProperty("width", sprite.getIconWidth());
            obj.addProperty("height", sprite.getIconHeight());
            obj.addProperty("category", category);
            out.add(obj);
        }
    }

    /**
     * 导出 blocks 和 items 两个 TextureMap 的 mapRegisteredSprites 到指定文件。
     */
    public static void dumpToFile(File outputFile) {
        try {
            JsonObject root = new JsonObject();
            JsonArray blocksArr = new JsonArray();
            JsonArray itemsArr = new JsonArray();

            Object blocksTex = Minecraft.getMinecraft()
                .getTextureManager()
                .getTexture(TextureMap.locationBlocksTexture);
            if (blocksTex instanceof TextureMap blocksMap) {
                appendSprites(blocksArr, ((AccessorTextureMap) blocksMap).getMapRegisteredSprites(), "blocks");
            }
            root.add("blocks", blocksArr);

            Object itemsTex = Minecraft.getMinecraft()
                .getTextureManager()
                .getTexture(TextureMap.locationItemsTexture);
            if (itemsTex instanceof TextureMap itemsMap) {
                appendSprites(itemsArr, ((AccessorTextureMap) itemsMap).getMapRegisteredSprites(), "items");
            }
            root.add("items", itemsArr);

            outputFile.getParentFile()
                .mkdirs();
            try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(outputFile),
                StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, w);
            }
            MyCTMLib.LOG.info("[CTMLibFusion] RegisteredSprites dump written to {} (blocks={} items={})",
                outputFile, blocksArr.size(), itemsArr.size());
        } catch (Exception e) {
            MyCTMLib.LOG.warn("[CTMLibFusion] RegisteredSprites dump failed", e);
        }
    }
}

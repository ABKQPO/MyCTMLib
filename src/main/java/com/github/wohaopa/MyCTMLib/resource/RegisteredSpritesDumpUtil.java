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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 导出 TextureMap 的 mapRegisteredSprites 至 JSON 文件。
 * 由 /ctmlib dump_registered_sprites 命令调用。
 */
public class RegisteredSpritesDumpUtil {

    /**
     * 导出 blocks 和 items 两个 TextureMap 的 mapRegisteredSprites 到指定文件。
     */
    public static void dumpToFile(File outputFile) {
        try {
            JsonObject root = new JsonObject();

            JsonObject blocksObj = new JsonObject();
            Object blocksTex = Minecraft.getMinecraft()
                .getTextureManager()
                .getTexture(TextureMap.locationBlocksTexture);
            if (blocksTex instanceof TextureMap blocksMap) {
                Map<String, TextureAtlasSprite> blocksSprites = ((AccessorTextureMap) blocksMap).getMapRegisteredSprites();
                for (Map.Entry<String, TextureAtlasSprite> e : blocksSprites.entrySet()) {
                    blocksObj.addProperty(e.getKey(), e.getValue()
                        .getIconName());
                }
            }
            root.add("blocks", blocksObj);

            JsonObject itemsObj = new JsonObject();
            Object itemsTex = Minecraft.getMinecraft()
                .getTextureManager()
                .getTexture(TextureMap.locationItemsTexture);
            if (itemsTex instanceof TextureMap itemsMap) {
                Map<String, TextureAtlasSprite> itemsSprites = ((AccessorTextureMap) itemsMap).getMapRegisteredSprites();
                for (Map.Entry<String, TextureAtlasSprite> e : itemsSprites.entrySet()) {
                    itemsObj.addProperty(e.getKey(), e.getValue()
                        .getIconName());
                }
            }
            root.add("items", itemsObj);

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
                outputFile,
                blocksObj.entrySet()
                    .size(),
                itemsObj.entrySet()
                    .size());
        } catch (Exception e) {
            MyCTMLib.LOG.warn("[CTMLibFusion] RegisteredSprites dump failed", e);
        }
    }
}

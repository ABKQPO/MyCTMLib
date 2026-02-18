package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.IIcon;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * 从 Forge 的 Block.blockRegistry 及 Block.getIcon(side, meta) 导出 blockId 与纹理映射至 JSON 文件。
 * 供迁移脚本消费。在 dumpBlockTextureMapping=true 时由 MixinTextureMap.afterLoadTextureAtlas 调用。
 */
public class BlockTextureDumpUtil {

    /**
     * 导出完整 dump 到指定文件。
     * 格式：{ "entries": [ { "blockId", "modid", "textures": [...] }, ... ] }
     */
    public static void dumpToFile(File outputFile) {
        try {
            JsonArray entries = new JsonArray();
            Iterator<?> it = Block.blockRegistry.getKeys()
                .iterator();

            while (it.hasNext()) {
                Object key = it.next();
                if (!(key instanceof String)) continue;
                String blockId = (String) key;
                Block block = (Block) Block.blockRegistry.getObject(blockId);
                if (block == null || block instanceof BlockAir) continue;

                Set<String> textures = new HashSet<>();
                for (int meta = 0; meta < 16; meta++) {
                    for (int side = 0; side < 6; side++) {
                        try {
                            IIcon icon = block.getIcon(side, meta);
                            if (icon != null) {
                                String name = icon.getIconName();
                                if (name != null && !name.isEmpty()) {
                                    textures.add(name);
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
                if (textures.isEmpty()) continue;

                String modid = blockId.indexOf(':') >= 0 ? blockId.substring(0, blockId.indexOf(':')) : "minecraft";
                JsonObject entry = new JsonObject();
                entry.addProperty("blockId", blockId);
                entry.addProperty("modid", modid);
                JsonArray texArr = new JsonArray();
                for (String t : textures) {
                    texArr.add(new JsonPrimitive(t));
                }
                entry.add("textures", texArr);
                entries.add(entry);
            }

            JsonObject root = new JsonObject();
            root.add("entries", entries);

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
            MyCTMLib.LOG.info("[CTMLibFusion] BlockTextureDump written to {} ({} entries)", outputFile, entries.size());
        } catch (Exception e) {
            MyCTMLib.LOG.warn("[CTMLibFusion] BlockTextureDump failed", e);
        }
    }
}

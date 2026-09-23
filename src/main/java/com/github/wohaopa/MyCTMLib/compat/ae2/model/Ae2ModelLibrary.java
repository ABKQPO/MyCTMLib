package com.github.wohaopa.MyCTMLib.compat.ae2.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel;
import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.client.resource.JsonModelResources;
import com.github.wohaopa.MyCTMLib.client.resource.ResourcePackFeature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc.ModelLoc;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Ae2ModelLibrary {

    public static final Ae2ModelLibrary INSTANCE = new Ae2ModelLibrary();
    public static final ResourcePackFeature FEATURE = new ResourcePackFeature("enableAe2JsonModels");
    private static final ResourceLocation MANIFEST = new ResourceLocation("myctmlib", "ae2/models.json");
    private static final String[] REQUIRED_DEVICES = { "drive", "chest", "wireless_access_point", "chest_lights_on",
        "chest_lights_off", "wireless_on", "wireless_off", "wireless_status_off", "wireless_status_on",
        "wireless_status_channel", "wireless_item", "chest_item", "drive_item", "cell_led" };

    private volatile Snapshot snapshot;
    private final Map<String, JsonBlockModel> pending = new LinkedHashMap<>();
    private JsonObject manifest;

    public Snapshot getSnapshot() {
        return FEATURE.isEnabled() ? snapshot : null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void beforeStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        snapshot = null;
        pending.clear();
        manifest = null;
        if (event.map.skipFirst) return;
        JsonModelResources resources = new JsonModelResources(
            Minecraft.getMinecraft()
                .getResourceManager());
        FEATURE.onResourceManagerReload(
            Minecraft.getMinecraft()
                .getResourceManager());
        if (!FEATURE.isEnabled()) return;
        try {
            manifest = resources.readObject(MANIFEST);
            JsonObject devices = manifest.getAsJsonObject("devices");
            for (String key : REQUIRED_DEVICES) {
                if (!devices.has(key)) throw new JsonParseException("Missing AE2 device model: " + key);
            }
            collect(devices, resources);
            JsonObject cells = manifest.getAsJsonObject("cells");
            for (String kind : new String[] { "item", "fluid", "essentia" }) {
                JsonObject tiers = cells.getAsJsonObject(kind);
                if (!tiers.has("default")) throw new JsonParseException("Missing default cell model: " + kind);
                collect(tiers, resources);
            }
            collect(manifest.getAsJsonObject("overrides"), resources);
            for (JsonBlockModel model : pending.values()) {
                for (String texture : model.getTextures()
                    .values()) {
                    if (!texture.startsWith("#")) event.map.registerIcon(texture);
                }
            }
        } catch (RuntimeException exception) {
            pending.clear();
            manifest = null;
            MyCTMLib.LOG.error("Cannot load AE2 JSON models; keeping the original renderers", exception);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterStitch(TextureStitchEvent.Post event) {
        if (event.map.getTextureType() != 0 || manifest == null) return;
        try {
            Map<String, BakedBlock> baked = new HashMap<>();
            for (Map.Entry<String, JsonBlockModel> entry : pending.entrySet()) {
                for (String texture : entry.getValue()
                    .getTextures()
                    .values()) {
                    if (!texture.startsWith("#") && "missingno".equals(
                        event.map.getAtlasSprite(texture)
                            .getIconName())) {
                        throw new JsonParseException("Missing AE2 model texture: " + texture);
                    }
                }
                baked.put(
                    entry.getKey(),
                    entry.getValue()
                        .bakeBlock());
            }
            Map<String, Map<String, BakedBlock>> cells = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : manifest.getAsJsonObject("cells")
                .entrySet()) {
                cells.put(
                    entry.getKey(),
                    resolve(
                        entry.getValue()
                            .getAsJsonObject(),
                        baked));
            }
            snapshot = new Snapshot(
                resolve(manifest.getAsJsonObject("devices"), baked),
                new Ae2CellModels(Map.copyOf(cells), resolve(manifest.getAsJsonObject("overrides"), baked)));
            MyCTMLib.LOG.info("Loaded {} AE2 JSON models with cached block orientations", baked.size());
        } catch (RuntimeException exception) {
            MyCTMLib.LOG.error("Cannot bake AE2 JSON models; keeping the original renderers", exception);
        } finally {
            pending.clear();
            manifest = null;
        }
    }

    private void collect(JsonObject entries, JsonModelResources resources) {
        if (entries == null) return;
        for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
            String location = entry.getValue()
                .getAsString();
            if (!pending.containsKey(location)) {
                pending.put(location, new JsonBlockModel(resources.load(ModelLoc.fromStr(location))));
            }
        }
    }

    private Map<String, BakedBlock> resolve(JsonObject entries, Map<String, BakedBlock> baked) {
        Map<String, BakedBlock> result = new HashMap<>();
        if (entries != null) {
            for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                result.put(
                    entry.getKey(),
                    baked.get(
                        entry.getValue()
                            .getAsString()));
            }
        }
        return Map.copyOf(result);
    }

    public record Snapshot(Map<String, BakedBlock> devices, Ae2CellModels cells) {}
}

package com.github.wohaopa.MyCTMLib.compat.ae2.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel;
import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.client.resource.JsonModelResources;
import com.github.wohaopa.MyCTMLib.client.resource.ResourcePackFeatureGroup;
import com.github.wohaopa.MyCTMLib.mixins.early.AccessorTextureMap;
import com.github.wohaopa.MyCTMLib.mixins.late.AccessorAEBaseBlock;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc.ModelLoc;

import appeng.api.AEApi;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Ae2ModelLibrary {

    public static final Ae2ModelLibrary INSTANCE = new Ae2ModelLibrary();
    public static final Map<String, Model> MODELS = new LinkedHashMap<>();

    static {
        model("drive", true, "drive", "drive_item", "cell_led");
        model("chest", true, "chest", "chest_lights_on", "chest_lights_off", "chest_item", "cell_led");
        model(
            "wireless_access_point",
            false,
            "wireless_access_point",
            "wireless_on",
            "wireless_off",
            "wireless_status_off",
            "wireless_status_on",
            "wireless_status_channel",
            "wireless_item");
        model("charger", false, "charger");
        model("inscriber", false, "inscriber");
        model("advanced_inscriber", false, "advanced_inscriber");
        model("molecular_assembler", false, "molecular_assembler", "molecular_assembler_lights");
        model("tiny_tnt", false, "tiny_tnt");
        model("crank", false, "crank_base", "crank_handle", "crank_item");
        model(
            "quartz_fixture",
            false,
            "quartz_fixture_standing",
            "quartz_fixture_standing_odd",
            "quartz_fixture_wall",
            "quartz_fixture_wall_odd",
            "quartz_fixture_item");
        model("sky_chest", false, "sky_chest_base", "sky_chest_lid");
        model("smooth_sky_chest", false, "smooth_sky_chest_base", "smooth_sky_chest_lid");
        model("meteorite_compass", false, "meteorite_compass_base", "meteorite_compass_pointer");
        model("matter_cannon", false, "matter_cannon_item");
        model("network_tool", false, "network_tool_item");
        model("advanced_network_tool", false, "advanced_network_tool_item");
        model("color_applicator", false, "color_applicator_item", "color_applicator_colored_item");
    }

    /** Manifest sprites that restyle AE2's own drawing for one model; they are optional. */
    private static final Map<String, String> SPRITE_OWNERS = Map.of("inscriber_inside", "inscriber");

    public static final ResourcePackFeatureGroup FEATURE = new ResourcePackFeatureGroup(
        "enableAe2JsonModels",
        MODELS.keySet());
    private static final ResourceLocation MANIFEST = new ResourceLocation("myctmlib", "ae2/models.json");

    private volatile Snapshot snapshot;
    private final Map<String, JsonBlockModel> pending = new LinkedHashMap<>();
    private final Map<String, String> sprites = new HashMap<>();
    private JsonObject manifest;

    public Snapshot getSnapshot() {
        return snapshot;
    }

    private static void model(String name, boolean cells, String... devices) {
        MODELS.put(name, new Model(List.of(devices), cells));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void beforeStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        snapshot = null;
        openAdvancedInscriber(false);
        pending.clear();
        sprites.clear();
        manifest = null;
        if (((AccessorTextureMap) event.map).myctmlib$isSkippingFirstLoad()) return;
        JsonModelResources resources = new JsonModelResources(
            Minecraft.getMinecraft()
                .getResourceManager());
        FEATURE.onResourceManagerReload(
            Minecraft.getMinecraft()
                .getResourceManager());
        Set<String> enabled = FEATURE.getEnabled();
        if (enabled.isEmpty()) return;
        try {
            manifest = resources.readObject(MANIFEST);
            JsonObject allDevices = manifest.getAsJsonObject("devices");
            if (allDevices == null) throw new JsonParseException("Missing AE2 device models");
            Set<String> loaded = new HashSet<>();
            JsonObject devices = new JsonObject();
            boolean cellsNeeded = false;
            for (String name : enabled) {
                Model model = MODELS.get(name);
                List<String> missing = model.devices()
                    .stream()
                    .filter(device -> !allDevices.has(device))
                    .toList();
                if (!missing.isEmpty()) {
                    MyCTMLib.LOG
                        .warn("AE2 JSON model {} is missing devices {}; keeping its original renderer", name, missing);
                    continue;
                }
                loaded.add(name);
                for (String device : model.devices()) devices.add(device, allDevices.get(device));
                cellsNeeded |= model.cells();
            }
            // afterStitch resolves the sections it reads back from the manifest, so keep only the enabled ones.
            manifest.add("devices", devices);
            collect(devices, resources);
            if (cellsNeeded) {
                JsonObject cells = manifest.getAsJsonObject("cells");
                if (cells == null) throw new JsonParseException("Missing AE2 cell models");
                for (String kind : new String[] { "item", "fluid", "essentia" }) {
                    JsonObject tiers = cells.getAsJsonObject(kind);
                    if (tiers == null || !tiers.has("default")) {
                        throw new JsonParseException("Missing default cell model: " + kind);
                    }
                    collect(tiers, resources);
                }
                collect(manifest.getAsJsonObject("overrides"), resources);
            } else {
                manifest.add("cells", new JsonObject());
                manifest.add("overrides", new JsonObject());
            }
            JsonObject extraSprites = manifest.getAsJsonObject("sprites");
            if (extraSprites != null) {
                for (Map.Entry<String, JsonElement> entry : extraSprites.entrySet()) {
                    String owner = SPRITE_OWNERS.get(entry.getKey());
                    if (owner != null && !loaded.contains(owner)) continue;
                    String texture = entry.getValue()
                        .getAsString();
                    event.map.registerIcon(texture);
                    sprites.put(entry.getKey(), texture);
                }
            }
            for (JsonBlockModel model : pending.values()) {
                for (String texture : model.getTextures()
                    .values()) {
                    if (!texture.startsWith("#")) event.map.registerIcon(texture);
                }
            }
        } catch (RuntimeException exception) {
            pending.clear();
            sprites.clear();
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
            Map<String, IIcon> stitched = new HashMap<>();
            for (Map.Entry<String, String> entry : sprites.entrySet()) {
                // Look the sprite up after stitching; the registered instance is not always the one that is uploaded.
                IIcon sprite = event.map.getAtlasSprite(entry.getValue());
                if ("missingno".equals(sprite.getIconName())) {
                    // Sprites only restyle AE2's own drawing, which keeps its original texture without them.
                    MyCTMLib.LOG.warn("Missing AE2 sprite {}; keeping the original texture", entry.getValue());
                } else {
                    stitched.put(entry.getKey(), sprite);
                }
            }
            snapshot = new Snapshot(
                resolve(manifest.getAsJsonObject("devices"), baked),
                new Ae2CellModels(Map.copyOf(cells), resolve(manifest.getAsJsonObject("overrides"), baked)),
                Map.copyOf(stitched));
            // The opening follows the advanced_inscriber switch, which decides whether its device was loaded.
            openAdvancedInscriber(
                snapshot.devices()
                    .get("advanced_inscriber") != null);
            MyCTMLib.LOG.info("Loaded {} AE2 JSON models with cached block orientations", baked.size());
        } catch (RuntimeException exception) {
            MyCTMLib.LOG.error("Cannot bake AE2 JSON models; keeping the original renderers", exception);
        } finally {
            pending.clear();
            sprites.clear();
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

    private static void openAdvancedInscriber(boolean open) {
        AEApi.instance()
            .definitions()
            .blocks()
            .advancedInscriber()
            .maybeBlock()
            .asSet()
            .forEach(block -> ((AccessorAEBaseBlock) block).myctmlib$setOpaque(!open));
    }

    public record Model(List<String> devices, boolean cells) {}

    public record Snapshot(Map<String, BakedBlock> devices, Ae2CellModels cells, Map<String, IIcon> sprites) {}
}

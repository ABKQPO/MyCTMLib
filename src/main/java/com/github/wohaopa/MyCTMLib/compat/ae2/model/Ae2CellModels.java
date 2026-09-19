package com.github.wohaopa.MyCTMLib.compat.ae2.model;

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.client.model.JsonBlockModel.BakedBlock;
import com.github.wohaopa.MyCTMLib.compat.thaumicenergistics.EssentiaCellModels;
import com.github.wohaopa.MyCTMLib.core.Mods;

import appeng.api.AEApi;
import appeng.api.definitions.IItems;
import appeng.api.implementations.items.IStorageCell;
import appeng.items.AEBaseInfiniteCell;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Ae2CellModels {

    private final Map<String, Map<String, BakedBlock>> models;
    private final Map<String, BakedBlock> overrides;
    private final ThreadLocal<Int2ObjectMap<BakedBlock[]>> cache = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

    public Ae2CellModels(Map<String, Map<String, BakedBlock>> models, Map<String, BakedBlock> overrides) {
        this.models = models;
        this.overrides = overrides;
    }

    public BakedBlock get(ItemStack stack, int type) {
        String kind = switch (type) {
            case 1 -> "fluid";
            case 2 -> "essentia";
            default -> "item";
        };
        if (stack == null) return models.get(kind)
            .get("default");
        int category = type >= 0 && type < 3 ? type : 0;
        int identity = stack.getItemDamage() << 16 | Item.getIdFromItem(stack.getItem());
        Int2ObjectMap<BakedBlock[]> localCache = cache.get();
        BakedBlock[] variants = localCache.get(identity);
        if (variants == null) {
            variants = new BakedBlock[3];
            localCache.put(identity, variants);
        }
        if (variants[category] == null) variants[category] = resolve(stack, kind);
        return variants[category];
    }

    private BakedBlock resolve(ItemStack stack, String kind) {
        String name = Item.itemRegistry.getNameForObject(stack.getItem());
        BakedBlock override = overrides.get(name + "@" + stack.getItemDamage());
        if (override == null) override = overrides.get(name);
        if (override != null) return override;
        Map<String, BakedBlock> tiers = models.get(kind);
        try {
            String tier = null;
            if (Mods.ThaumicEnergistics.isModLoaded()) tier = EssentiaCellModels.getTier(stack);
            if (tier == null) tier = getTier(stack);
            return tiers.getOrDefault(tier, tiers.get("default"));
        } catch (RuntimeException exception) {
            MyCTMLib.LOG.warn("Cannot resolve storage cell model for {}@{}", name, stack.getItemDamage(), exception);
            return tiers.get("default");
        }
    }

    private String getTier(ItemStack stack) {
        IItems items = AEApi.instance()
            .definitions()
            .items();
        String name = Item.itemRegistry.getNameForObject(stack.getItem());
        if (Mods.Ae2FluidCraft.isModLoaded()) {
            String fluidTier = getFluidCraftTier(name);
            if (fluidTier != null) return fluidTier;
        }
        if (items.cellContainer()
            .isSameAs(stack)) return "container";
        if (items.cellQuantum()
            .isSameAs(stack)) return "quantum";
        if (items.cellSingularity()
            .isSameAs(stack)) return "singularity";
        if (items.cellUniverse()
            .isSameAs(stack)) return "universe";
        if (items.cellVoid()
            .isSameAs(stack)) return "void";
        if (stack.getItem() instanceof AEBaseInfiniteCell) return "creative";
        if (stack.getItem() instanceof IStorageCell cell) {
            long bytes = cell.getBytesLong(stack);
            if (bytes > 0 && bytes % 1024 == 0) return bytes / 1024 + "k";
        }
        return "default";
    }

    private String getFluidCraftTier(String name) {
        if (name == null || !name.startsWith("ae2fc:")) return null;
        String path = name.substring("ae2fc:".length());
        if (path.startsWith("multi_fluid_storage")) {
            String capacity = path.substring("multi_fluid_storage".length());
            return capacity.isEmpty() ? null : capacity + "k_multi";
        }
        return switch (path) {
            case "fluid_storage.quantum" -> "quantum";
            case "fluid_storage.singularity" -> "singularity";
            case "fluid_storage.Universe" -> "universe";
            case "creative_fluid_storage" -> "creative";
            case "fluid_storage.void" -> "void";
            case "fluid_storage.infinity.water" -> "infinity";
            default -> null;
        };
    }

}

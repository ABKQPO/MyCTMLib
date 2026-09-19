package com.github.wohaopa.MyCTMLib.compat.thaumicenergistics;

import java.util.Locale;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.storage.EnumEssentiaStorageTypes;

public class EssentiaCellModels {

    @Optional.Method(modid = "thaumicenergistics")
    public static String getTier(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemEssentiaCell)) return null;
        int metadata = stack.getItemDamage();
        if (metadata < 0 || metadata >= EnumEssentiaStorageTypes.fromIndex.length) return null;
        EnumEssentiaStorageTypes type = EnumEssentiaStorageTypes.fromIndex[metadata];
        return type == null ? null : type.suffix.toLowerCase(Locale.ROOT);
    }
}

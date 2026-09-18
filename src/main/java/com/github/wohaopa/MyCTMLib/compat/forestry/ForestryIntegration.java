package com.github.wohaopa.MyCTMLib.compat.forestry;

import cpw.mods.fml.common.Optional;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IIndividual;
import forestry.plugins.PluginApiculture;

@SideOnly(Side.CLIENT)
public class ForestryIntegration {

    private static ItemStack droneGuiStack;
    private static ItemStack queenGuiStack;
    private static ItemStack princessGuiStack;
    private static ItemStack larvaeGuiStack;

    @Optional.Method(modid = "Forestry")
    public static ItemStack getGuiIconStack(String iconName) {
        if (iconName == null || PluginApiculture.items == null) {
            return null;
        }
        String normalized = iconName.startsWith("forestry:") ? iconName.substring("forestry:".length()) : iconName;
        return switch (normalized) {
            case "errors/noDrone", "errors/noSpecimen", "analyzer/bee", "analyzer/drone", "analyzer/item",
                 "analyzer/pure_breed" -> droneGuiStack;
            case "errors/noQueen", "analyzer/queen" -> queenGuiStack;
            case "analyzer/princess" -> princessGuiStack;
            case "analyzer/larvae" -> larvaeGuiStack;
            default -> null;
        };
    }

    @Optional.Method(modid = "Forestry")
    public static ItemStack getAlleleStack(String alleleId, EnumBeeType type) {
        if (alleleId == null || alleleId.isEmpty() || BeeManager.beeRoot == null || PluginApiculture.items == null) {
            return null;
        }
        if (!(AlleleManager.alleleRegistry.getAllele(alleleId) instanceof IAlleleBeeSpecies species)) {
            return null;
        }
        IAllele[] template = BeeManager.beeRoot.getDefaultTemplate()
            .clone();
        int speciesIndex = EnumBeeChromosome.SPECIES.ordinal();
        template[speciesIndex] = species;
        IIndividual individual = BeeManager.beeRoot.templateAsIndividual(template);
        return BeeManager.beeRoot.getMemberStack(individual, type.ordinal());
    }

    public static void registerResources(IReloadableResourceManager manager) {
        ModelRegistry.registerModid(MyCTMLib.MODID);
        manager.registerReloadListener(BeeJsonModelItemRenderer.INSTANCE);
    }

    @Optional.Method(modid = "Forestry")
    public static void registerItemRenderers() {
        if (PluginApiculture.items == null) {
            return;
        }
        BeeJsonModelItemRenderer renderer = BeeJsonModelItemRenderer.INSTANCE;
        droneGuiStack = new ItemStack(PluginApiculture.items.beeDroneGE);
        princessGuiStack = new ItemStack(PluginApiculture.items.beePrincessGE);
        queenGuiStack = new ItemStack(PluginApiculture.items.beeQueenGE);
        larvaeGuiStack = new ItemStack(PluginApiculture.items.beeLarvaeGE);
        renderer.register(droneGuiStack);
        renderer.register(princessGuiStack);
        renderer.register(queenGuiStack);
        renderer.register(larvaeGuiStack);
        MyCTMLib.LOG.info("Registered JSON bee item renderer.");
    }
}

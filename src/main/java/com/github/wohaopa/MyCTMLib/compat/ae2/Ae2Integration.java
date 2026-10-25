package com.github.wohaopa.MyCTMLib.compat.ae2;

import net.minecraftforge.common.MinecraftForge;

import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2ItemRenderer;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Ae2Integration {

    @Optional.Method(modid = "appliedenergistics2")
    public static void registerResources() {
        MinecraftForge.EVENT_BUS.register(Ae2ModelLibrary.INSTANCE);
    }

    @Optional.Method(modid = "appliedenergistics2")
    public static void registerItemRenderers() {
        IBlocks blocks = AEApi.instance()
            .definitions()
            .blocks();
        register(blocks.drive(), "drive_item", true);
        register(blocks.chest(), "chest_item", true);
        register(blocks.wireless(), "wireless_item", true);
        register(blocks.charger(), "charger", true);
        register(blocks.inscriber(), "inscriber", true);
        register(blocks.molecularAssembler(), "molecular_assembler", true);
        register(blocks.tinyTNT(), "tiny_tnt", true);
        register(blocks.crankHandle(), "crank_item", true);
        register(blocks.quartzTorch(), "quartz_fixture_item", true);
        register(blocks.lightDetector(), "quartz_fixture_item", true);
        register(blocks.skyCompass(), "meteorite_compass_base", true);
        register(blocks.advancedInscriber(), "advanced_inscriber", true);
        IItems items = AEApi.instance()
            .definitions()
            .items();
        register(items.massCannon(), "matter_cannon_item", false);
        register(items.networkTool(), "network_tool_item", false);
        register(items.advancedNetworkTool(), "advanced_network_tool_item", false);
        register(items.colorApplicator(), "color_applicator_item", false);
    }

    private static void register(IItemDefinition definition, String model, boolean block) {
        definition.maybeStack(1)
            .asSet()
            .forEach(stack -> Ae2ItemRenderer.INSTANCE.register(stack, model, block));
    }
}

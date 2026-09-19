package com.github.wohaopa.MyCTMLib.compat.ae2;

import net.minecraftforge.common.MinecraftForge;

import com.github.wohaopa.MyCTMLib.compat.ae2.model.Ae2ModelLibrary;
import com.github.wohaopa.MyCTMLib.compat.ae2.render.Ae2ItemRenderer;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
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
        blocks.drive()
            .maybeStack(1)
            .asSet()
            .forEach(stack -> Ae2ItemRenderer.INSTANCE.register(stack, "drive_item"));
        blocks.chest()
            .maybeStack(1)
            .asSet()
            .forEach(stack -> Ae2ItemRenderer.INSTANCE.register(stack, "chest_item"));
        blocks.wireless()
            .maybeStack(1)
            .asSet()
            .forEach(stack -> Ae2ItemRenderer.INSTANCE.register(stack, "wireless_item"));
    }
}

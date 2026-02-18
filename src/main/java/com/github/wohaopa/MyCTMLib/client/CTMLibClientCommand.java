package com.github.wohaopa.MyCTMLib.client;

import java.io.File;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.resource.BlockTextureDumpUtil;
import com.github.wohaopa.MyCTMLib.resource.DebugErrorCollector;
import com.github.wohaopa.MyCTMLib.resource.RegistryDumpUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CTMLibClientCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "ctmlib";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ctmlib <debug|dump_registry|dump_textures|dump_debug_errors>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "debug", "dump_registry", "dump_textures", "dump_debug_errors");
        }
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            send(sender, EnumChatFormatting.RED + getCommandUsage(sender));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "debug" -> processDebug(sender);
            case "dump_registry" -> processDumpRegistry(sender);
            case "dump_textures" -> processDumpTextures(sender);
            case "dump_debug_errors" -> processDumpDebugErrors(sender);
            default -> send(sender, EnumChatFormatting.RED + "Unknown subcommand: " + args[0]);
        }
    }

    private void processDebug(ICommandSender sender) {
        MyCTMLib.debugMode = !MyCTMLib.debugMode;
        send(sender, "debug = " + MyCTMLib.debugMode);
    }

    private void processDumpRegistry(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_registry_dump.json");
        RegistryDumpUtil.dumpToFile(f);
        send(sender, "Registry dump written to " + f.getAbsolutePath());
    }

    private void processDumpTextures(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_block_texture_dump.json");
        BlockTextureDumpUtil.dumpToFile(f);
        send(sender, "Block texture dump written to " + f.getAbsolutePath());
    }

    private void processDumpDebugErrors(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_debug_errors.json");
        DebugErrorCollector.getInstance()
            .flushToFile(f);
        send(sender, "Debug errors written to " + f.getAbsolutePath());
    }

    private static void send(ICommandSender sender, String msg) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI()
            .printChatMessage(new ChatComponentText(msg));
    }
}

package com.github.wohaopa.MyCTMLib.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.PipelineInfo;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugOverlayHandler {

    private static final String[] SIDE_NAMES = { "D", "U", "N", "S", "W", "E" };
    private static final String[] SIDE_NAMES_FULL = { "DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST" };

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!MyCTMLib.debugMode || event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
            return;
        }
        int x = mop.blockX;
        int y = mop.blockY;
        int z = mop.blockZ;
        World world = mc.theWorld;
        Block block = world.getBlock(x, y, z);

        List<String> lines = new ArrayList<>();
        String blockId = getBlockId(block);
        lines.add(
            "block: " + (blockId != null ? blockId
                : block.getClass()
                    .getSimpleName()));
        lines.add("meta: " + world.getBlockMetadata(x, y, z));

        int hitSide = Math.min(mop.sideHit, 5);
        ForgeDirection hitFace = ForgeDirection.getOrientation(hitSide);

        // 六面管线摘要
        StringBuilder summary = new StringBuilder("pipeline: ");
        for (int s = 0; s < 6; s++) {
            if (s > 0) summary.append(" ");
            ForgeDirection face = ForgeDirection.getOrientation(s);
            PipelineInfo info = CTMRenderEntry.getPipelineInfo(world, block, x, y, z, face);
            summary.append(SIDE_NAMES[s])
                .append(":")
                .append(shortName(info.getType()));
        }
        lines.add(summary.toString());

        // 当前面（准星所指）完整管线信息
        PipelineDebugTrace trace = new PipelineDebugTrace();
        PipelineInfo hitInfo = CTMRenderEntry.getPipelineInfo(world, block, x, y, z, hitFace, trace);
        lines.add("--- " + SIDE_NAMES_FULL[hitSide] + " ---");
        lines.add("pipeline: " + hitInfo.getType());
        lines.add("icon: " + hitInfo.getIconName());
        switch (hitInfo.getType()) {
            case MODEL:
                lines.add("modelId: " + hitInfo.getModelId());
                lines.add("textureKey: " + hitInfo.getTextureKey());
                lines.add("layout: " + hitInfo.getLayout());
                if (hitInfo.getMask() >= 0) {
                    lines.add("mask: 0x" + Integer.toHexString(hitInfo.getMask()));
                }
                break;
            case TEXTURE_REGISTRY:
                lines.add("layout: " + hitInfo.getLayout());
                if (hitInfo.getMask() >= 0) {
                    lines.add("mask: 0x" + Integer.toHexString(hitInfo.getMask()));
                }
                break;
            case LEGACY:
                lines.add("legacy: ctmIconMap");
                break;
            case VANILLA:
                if (hitInfo.getSkipReason() != null && !hitInfo.getSkipReason()
                    .isEmpty()) {
                    lines.add("skip: " + hitInfo.getSkipReason());
                }
                break;
        }

        // 决策流程与退化原因
        if (trace.getPredicateUsed() != null) {
            lines.add("predicate: " + trace.getPredicateUsed());
        }
        if (trace.getTilePos() != null) {
            lines.add("tile: (" + trace.getTilePos()[0] + ", " + trace.getTilePos()[1] + ")");
        }
        if (trace.getConnectionBits() != null) {
            int[] bits = trace.getConnectionBits();
            StringBuilder conn = new StringBuilder("conn: T TR R BR B BL L TL = ");
            for (int i = 0; i < 8; i++) {
                if (i > 0) conn.append(" ");
                conn.append(bits[i]);
            }
            lines.add(conn.toString());
        }
        if (trace.getDegradationReason() != null && !trace.getDegradationReason()
            .isEmpty()) {
            lines.add("degrade: " + trace.getDegradationReason());
        }
        if (!trace.getSteps().isEmpty()) {
            lines.add("--- decision ---");
            int maxSteps = 8;
            for (int i = 0; i < Math.min(trace.getSteps().size(), maxSteps); i++) {
                lines.add(trace.getSteps().get(i));
            }
            if (trace.getSteps().size() > maxSteps) {
                lines.add("... (" + (trace.getSteps().size() - maxSteps) + " more)");
            }
        }

        ScaledResolution res = event.resolution;
        int lineHeight = mc.fontRenderer.FONT_HEIGHT;
        int xPos = 4;
        int yPos = 4;
        for (String line : lines) {
            mc.fontRenderer.drawStringWithShadow(line, xPos, yPos, 0xFFFFFF);
            yPos += lineHeight + 2;
        }
    }

    private static String shortName(PipelineInfo.PipelineType t) {
        switch (t) {
            case MODEL:
                return "Model";
            case TEXTURE_REGISTRY:
                return "TexReg";
            case LEGACY:
                return "Legacy";
            case VANILLA:
                return "Vanilla";
            default:
                return t.name();
        }
    }

    private static String getBlockId(Block block) {
        if (block == null) return null;
        Iterator<?> it = Block.blockRegistry.getKeys()
            .iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if (key instanceof String && Block.blockRegistry.getObject(key) == block) {
                return (String) key;
            }
        }
        return null;
    }
}

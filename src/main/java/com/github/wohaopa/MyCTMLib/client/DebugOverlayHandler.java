package com.github.wohaopa.MyCTMLib.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
        int meta = world.getBlockMetadata(x, y, z);
        lines.add(
            "block: " + (blockId != null ? blockId
                : block.getClass()
                    .getSimpleName()) + " meta: " + meta);

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
        lines.add("face: " + SIDE_NAMES_FULL[hitSide] + " | pipeline: " + shortName(hitInfo.getType()));
        lines.add("icon: " + hitInfo.getIconName());
        switch (hitInfo.getType()) {
            case MODEL:
                lines.add("modelId: " + hitInfo.getModelId());
                lines.add("textureKey: " + hitInfo.getTextureKey());
                if (trace.getTexRegGetIconLookupKey() != null) {
                    Boolean synced = trace.getTexRegTexMapSynced();
                    lines.add(
                        "TexReg/TexMap: " + (Boolean.TRUE.equals(synced) ? "synced" : "OUT OF SYNC (getIcon null, fallback to block icon)"));
                }
                addDrawSpriteLine(lines, trace);
                addLayoutMaskLine(lines, hitInfo);
                break;
            case TEXTURE_REGISTRY:
                lines.add("lookupKey: " + hitInfo.getIconName());
                addDrawSpriteLine(lines, trace);
                addLayoutMaskLine(lines, hitInfo);
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

        // predicate / tile / conn 合并一行
        addPredicateTileConnLine(lines, trace);
        if (trace.getDegradationReason() != null && !trace.getDegradationReason()
            .isEmpty()) {
            lines.add("degrade: " + trace.getDegradationReason());
        }
        addDecisionSteps(lines, trace);

        int lineHeight = mc.fontRenderer.FONT_HEIGHT;
        int xPos = 4;
        int yPos = 4;
        for (String line : lines) {
            mc.fontRenderer.drawStringWithShadow(line, xPos, yPos, 0xFFFFFF);
            yPos += lineHeight + 2;
        }
    }

    private static void addDrawSpriteLine(List<String> lines, PipelineDebugTrace trace) {
        if (trace.getDrawSpriteName() == null) return;
        String loaded = trace.getDrawSpriteLoaded();
        String suffix = "";
        if (loaded != null) {
            if (loaded.contains("0x0")) suffix = " (unloaded?)";
            else if ("16x16".equals(loaded)) suffix = " (16x16?)";
        }
        lines.add("drawSprite: " + trace.getDrawSpriteName() + (loaded != null && !loaded.isEmpty() ? " " + loaded : "") + suffix);
    }

    private static void addLayoutMaskLine(List<String> lines, PipelineInfo hitInfo) {
        if (hitInfo.getLayout() == null) return;
        if (hitInfo.getMask() >= 0) {
            lines.add("layout: " + hitInfo.getLayout() + " mask: 0x" + Integer.toHexString(hitInfo.getMask()));
        } else {
            lines.add("layout: " + hitInfo.getLayout());
        }
    }

    private static void addPredicateTileConnLine(List<String> lines, PipelineDebugTrace trace) {
        String pred = trace.getPredicateUsed();
        int[] tile = trace.getTilePos();
        int[] bits = trace.getConnectionBits();
        if (pred == null && tile == null && bits == null) return;
        StringBuilder sb = new StringBuilder();
        if (pred != null) sb.append("pred: ").append(pred);
        if (tile != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("tile:(").append(tile[0]).append(",").append(tile[1]).append(")");
        }
        if (bits != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("conn:");
            for (int i = 0; i < 8; i++) {
                if (i > 0) sb.append(" ");
                sb.append(bits[i]);
            }
        }
        if (sb.length() > 0) lines.add(sb.toString());
    }

    private static final String[] DECISION_KEYWORDS = {
        "getIcon", "HIT", "null", "miss", "degrade", "OUT OF SYNC", "不同步"
    };

    private static void addDecisionSteps(List<String> lines, PipelineDebugTrace trace) {
        List<String> steps = trace.getSteps();
        if (steps.isEmpty()) return;
        lines.add("--- decision ---");
        List<String> keySteps = new ArrayList<>();
        List<String> rest = new ArrayList<>();
        for (String s : steps) {
            boolean key = false;
            if (s != null) {
                for (String kw : DECISION_KEYWORDS) {
                    if (s.contains(kw)) {
                        key = true;
                        break;
                    }
                }
            }
            if (key) keySteps.add(s);
            else rest.add(s);
        }
        int maxSteps = 5;
        int shown = 0;
        for (String s : keySteps) {
            if (shown >= maxSteps) break;
            lines.add(s);
            shown++;
        }
        for (String s : rest) {
            if (shown >= maxSteps) break;
            lines.add(s);
            shown++;
        }
        if (steps.size() > maxSteps) {
            lines.add("... (" + (steps.size() - maxSteps) + " more)");
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

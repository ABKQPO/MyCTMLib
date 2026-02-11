package com.github.wohaopa.MyCTMLib.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

/**
 * 根据 (tileX, tileY) 与 layout 网格尺寸将纹理切成单格 UV，用 Tessellator 绘制单面一个 quad。
 */
public final class FaceRenderer {

    /**
     * 绘制一个面，纹理取 layout 网格中的 (tileX, tileY) 格。
     *
     * @param renderBlocks 当前 RenderBlocks（含 renderMin/Max）
     * @param x            block x
     * @param y            block y
     * @param z            block z
     * @param face         面方向
     * @param icon         整图 icon
     * @param tileX        格 x
     * @param tileY        格 y
     * @param gridW        layout 宽度
     * @param gridH        layout 高度
     */
    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        IIcon icon, int tileX, int tileY, int gridW, int gridH) {
        double minU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * tileX / gridW;
        double maxU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * (tileX + 1) / gridW;
        double minV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * tileY / gridH;
        double maxV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * (tileY + 1) / gridH;

        if (MyCTMLib.debugMode && icon != null && MyCTMLib.isFusionTraceTarget(icon.getIconName())) {
            double iconSpanU = icon.getMaxU() - icon.getMinU();
            double iconSpanV = icon.getMaxV() - icon.getMinV();
            MyCTMLib.LOG.info(
                "[CTMLibFusion] FaceRenderer UV | iconName={} iconSize={}x{}",
                icon.getIconName(),
                icon.getIconWidth(),
                icon.getIconHeight());
            MyCTMLib.LOG.info(
                "[CTMLibFusion] FaceRenderer UV | iconBounds: minU={} maxU={} minV={} maxV={} spanU={} spanV={}",
                icon.getMinU(),
                icon.getMaxU(),
                icon.getMinV(),
                icon.getMaxV(),
                iconSpanU,
                iconSpanV);
            MyCTMLib.LOG.info(
                "[CTMLibFusion] FaceRenderer UV | tile=({},{}) grid={}x{} -> quadUV: minU={} maxU={} minV={} maxV={}",
                tileX,
                tileY,
                gridW,
                gridH,
                minU,
                maxU,
                minV,
                maxV);
        }

        drawFace(renderBlocks, x, y, z, face, minU, maxU, minV, maxV);
    }

    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        double minU, double maxU, double minV, double maxV) {
        double minX = x + renderBlocks.renderMinX;
        double maxX = x + renderBlocks.renderMaxX;
        double minY = y + renderBlocks.renderMinY;
        double maxY = y + renderBlocks.renderMaxY;
        double minZ = z + renderBlocks.renderMinZ;
        double maxZ = z + renderBlocks.renderMaxZ;

        Tessellator tes = Tessellator.instance;
        if (renderBlocks.renderFromInside) {
            double t;
            t = minU;
            minU = maxU;
            maxU = t;
        }

        switch (face) {
            case DOWN:
                tes.addVertexWithUV(minX, minY, maxZ, minU, maxV);
                tes.addVertexWithUV(minX, minY, minZ, minU, minV);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, minV);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
                break;
            case UP:
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
                break;
            case NORTH:
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
                break;
            case SOUTH:
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;
            case WEST:
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;
            case EAST:
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
                break;
            default:
                break;
        }
    }
}

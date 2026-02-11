package com.github.wohaopa.MyCTMLib.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;

/**
 * 根据 (tileX, tileY) 与 layout 网格尺寸将纹理切成单格 UV，用 Tessellator 绘制单面一个 quad。
 * 当 renderBlocks.enableAO 为 true 时使用四角亮度/颜色（与环境光遮挡一致），否则使用 fallback 单值。
 * 各面顶点顺序与 UV 与旧 mod Textures.renderFace* 对齐，以保证正面朝向正确（不被背面剔除）且纹理不翻转。
 * 顶面/底面（UP/DOWN）需交换 U 以修正水平翻转。
 */
public final class FaceRenderer {

    /** 四角顺序：0=TopLeft, 1=TopRight, 2=BottomLeft, 3=BottomRight。用于从 RenderBlocks 取亮度与颜色。 */
    private static final int CORNER_TOP_LEFT = 0;
    private static final int CORNER_TOP_RIGHT = 1;
    private static final int CORNER_BOTTOM_LEFT = 2;
    private static final int CORNER_BOTTOM_RIGHT = 3;

    /**
     * 各面顶点顺序对应的角索引（与 addVertexWithUV 的 4 个顶点一致）。
     * 与旧 mod Textures.renderFace* 的顶点顺序对齐，保证正面朝向与 UV 不翻转。
     */
    private static final int[][] CORNER_ORDER_BY_FACE = {
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT },     // DOWN
        { CORNER_TOP_RIGHT, CORNER_BOTTOM_RIGHT, CORNER_BOTTOM_LEFT, CORNER_TOP_LEFT },     // UP
        { CORNER_TOP_LEFT, CORNER_TOP_RIGHT, CORNER_BOTTOM_RIGHT, CORNER_BOTTOM_LEFT },     // NORTH（与 Textures.renderFaceZNeg 一致：左上→右上→右下→左下）
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT },     // SOUTH（与 Textures.renderFaceZPos 一致：左上→左下→右下→右上）
        { CORNER_TOP_RIGHT, CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT },     // WEST（与 Textures.renderFaceXNeg 一致）
        { CORNER_BOTTOM_RIGHT, CORNER_BOTTOM_LEFT, CORNER_TOP_LEFT, CORNER_TOP_RIGHT },     // EAST（与 Textures.renderFaceXPos 一致）
    };

    /** 在添加一个顶点前设置 Tessellator 的亮度与颜色（来自 RenderBlocks 四角 AO）。 */
    private static void setTessellatorAO(RenderBlocks rb, int corner) {
        float r, g, b;
        int bright;
        switch (corner) {
            case CORNER_TOP_LEFT:
                r = rb.colorRedTopLeft;
                g = rb.colorGreenTopLeft;
                b = rb.colorBlueTopLeft;
                bright = rb.brightnessTopLeft;
                break;
            case CORNER_TOP_RIGHT:
                r = rb.colorRedTopRight;
                g = rb.colorGreenTopRight;
                b = rb.colorBlueTopRight;
                bright = rb.brightnessTopRight;
                break;
            case CORNER_BOTTOM_LEFT:
                r = rb.colorRedBottomLeft;
                g = rb.colorGreenBottomLeft;
                b = rb.colorBlueBottomLeft;
                bright = rb.brightnessBottomLeft;
                break;
            case CORNER_BOTTOM_RIGHT:
                r = rb.colorRedBottomRight;
                g = rb.colorGreenBottomRight;
                b = rb.colorBlueBottomRight;
                bright = rb.brightnessBottomRight;
                break;
            default:
                r = g = b = 1.0F;
                bright = 0;
                break;
        }
        Tessellator.instance.setColorOpaque_F(r, g, b);
        Tessellator.instance.setBrightness(bright);
    }

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
     * @param fallbackBrightness 非 AO 时使用的整面亮度
     */
    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        IIcon icon, int tileX, int tileY, int gridW, int gridH, int fallbackBrightness) {
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

        drawFace(renderBlocks, x, y, z, face, minU, maxU, minV, maxV, fallbackBrightness);
    }

    /**
     * 绘制单面一个 quad。enableAO 时每顶点使用 RenderBlocks 四角亮度/颜色，否则使用 fallbackBrightness 与 (1,1,1)。
     */
    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        double minU, double maxU, double minV, double maxV, int fallbackBrightness) {
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

        if (!renderBlocks.enableAO) {
            tes.setBrightness(fallbackBrightness);
            tes.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        }

        int fi = face.ordinal();
        int[] corners = CORNER_ORDER_BY_FACE[fi];

        switch (face) {
            case DOWN:
                // 与旧 mod Textures.renderFaceYNeg 顶点顺序一致；交换 U 以修正朝下看时的水平翻转。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, minY, minZ, maxU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, minZ, minU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
                break;
            case UP:
                // 与旧 mod Textures.renderFaceYPos 顶点顺序一致；交换 U 与 V 以修正从上往下看时的翻转。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, maxY, minZ, minU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(minX, maxY, minZ, maxU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
                break;
            case NORTH:
                // 与旧 mod Textures.renderFaceZNeg 顶点顺序与 UV 一致：左上→右上→右下→左下，朝南看时不翻转。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, minZ, maxU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, maxY, minZ, minU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, minZ, minU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, minY, minZ, maxU, maxV);
                break;
            case SOUTH:
                // 与旧 mod Textures.renderFaceZPos 顶点顺序一致：左上→左下→右下→右上，正面从 -Z 看为 CCW，UV 对应不翻转。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, minY, maxZ, minU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, minV);
                break;
            case WEST:
                // 与旧 mod Textures.renderFaceXNeg 顶点顺序与 UV 一致：右上→左上→左下→右下，从 +X 看为 CCW，避免背面剔除。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;
            case EAST:
                // 与旧 mod Textures.renderFaceXPos 顶点顺序一致：右下→左下→左上→右上，从 -X 看为 CCW，避免背面剔除，UV 与顶点对应正确。
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                break;
            default:
                break;
        }
    }
}

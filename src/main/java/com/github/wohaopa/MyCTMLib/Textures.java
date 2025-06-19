package com.github.wohaopa.MyCTMLib;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.mixins.GTRenderedTextureAccessor;

import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.IBlockWithClientMeta;
import gregtech.api.interfaces.IBlockWithTextures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ITexturedTileEntity;
import gregtech.common.blocks.BlockMachines;
import gregtech.common.render.GTCopiedBlockTextureRender;
import gregtech.common.render.GTRenderedTexture;

@SuppressWarnings("DuplicatedCode")
public class Textures {

    public static Map<String, CTMIconManager> ctmIconMap = new HashMap<>();

    public static boolean contain(String icon) {
        int firstColon = icon.indexOf(':');
        int secondColon = icon.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            icon = icon.substring(0, secondColon) + "&"
                + icon.substring(secondColon + 1)
                    .replace(":", "&");
        }

        boolean result = ctmIconMap.containsKey(icon);
        if (MyCTMLib.debugMode) System.out.println("[CTM] contain(\"" + icon + "\") = " + result);
        return result;
    }

    public static boolean renderWorldBlock(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon iIcon, ForgeDirection forgeDirection) {

        String icon = iIcon.getIconName();
        int firstColon = icon.indexOf(':');
        int secondColon = icon.indexOf(':', firstColon + 1);

        if (secondColon != -1) {
            icon = icon.substring(0, secondColon) + "&"
                + icon.substring(secondColon + 1)
                    .replace(":", "&");
        }

        int[] iconIdx = new int[4];
        buildConnect(blockAccess, (int) x, (int) y, (int) z, iIcon, forgeDirection, iconIdx);

        CTMIconManager manager = ctmIconMap.get(icon);
        if (!manager.hasInited()) manager.init();

        float offset = 1e-3f;
        switch (forgeDirection) {
            case DOWN -> renderFaceYNeg(renderBlocks, x, y + offset, z, manager, iconIdx);
            case UP -> renderFaceYPos(renderBlocks, x, y - offset, z, manager, iconIdx);
            case NORTH -> renderFaceZNeg(renderBlocks, x, y, z + offset, manager, iconIdx);
            case SOUTH -> renderFaceZPos(renderBlocks, x, y, z - offset, manager, iconIdx);
            case WEST -> renderFaceXNeg(renderBlocks, x + offset, y, z, manager, iconIdx);
            case EAST -> renderFaceXPos(renderBlocks, x - offset, y, z, manager, iconIdx);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void renderFaceYNeg(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {
            IIcon iIcon = manager.getIcon(iconIdxOut[i + j * 2]);
            double minU = iIcon.getInterpolatedU(renderBlocks.renderMinX * 16.0D);
            double maxU = iIcon.getInterpolatedU(renderBlocks.renderMaxX * 16.0D);
            double minV = iIcon.getInterpolatedV(renderBlocks.renderMinZ * 16.0D);
            double maxV = iIcon.getInterpolatedV(renderBlocks.renderMaxZ * 16.0D);

            if (renderBlocks.renderMinX < 0.0D || renderBlocks.renderMaxX > 1.0D) {
                minU = iIcon.getMinU();
                maxU = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinZ < 0.0D || renderBlocks.renderMaxZ > 1.0D) {
                minV = iIcon.getMinV();
                maxV = iIcon.getMaxV();
            }

            double d7 = maxU;
            double d8 = minU;
            double d9 = minV;
            double d10 = maxV;

            if (renderBlocks.uvRotateBottom == 2) {
                minU = iIcon.getInterpolatedU(renderBlocks.renderMinZ * 16.0D);
                minV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxX * 16.0D);
                maxU = iIcon.getInterpolatedU(renderBlocks.renderMaxZ * 16.0D);
                maxV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinX * 16.0D);
                d9 = minV;
                d10 = maxV;
                d7 = minU;
                d8 = maxU;
                minV = maxV;
                maxV = d9;
            } else if (renderBlocks.uvRotateBottom == 1) {
                minU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxZ * 16.0D);
                minV = iIcon.getInterpolatedV(renderBlocks.renderMinX * 16.0D);
                maxU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinZ * 16.0D);
                maxV = iIcon.getInterpolatedV(renderBlocks.renderMaxX * 16.0D);
                d7 = maxU;
                d8 = minU;
                minU = maxU;
                maxU = d8;
                d9 = maxV;
                d10 = minV;
            } else if (renderBlocks.uvRotateBottom == 3) {
                minU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinX * 16.0D);
                maxU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxX * 16.0D);
                minV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinZ * 16.0D);
                maxV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d7 = maxU;
                d8 = minU;
                d9 = minV;
                d10 = maxV;
            }

            double minX = x + renderBlocks.renderMinX + 0.5 * i;
            double maxX = x + renderBlocks.renderMaxX - (i == 0 ? 0.5 : 0);
            double minY = y + renderBlocks.renderMinY;
            double minZ = z + renderBlocks.renderMinZ + 0.5 * j;
            double maxZ = z + renderBlocks.renderMaxZ - (j == 0 ? 0.5 : 0);

            if (renderBlocks.renderFromInside) {
                double d = minX;
                minX = maxX;
                maxX = d;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(minX, minY, maxZ, d8, d10);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(minX, minY, minZ, minU, minV);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(maxX, minY, minZ, d7, d9);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
            } else {
                tessellator.addVertexWithUV(minX, minY, maxZ, d8, d10);
                tessellator.addVertexWithUV(minX, minY, minZ, minU, minV);
                tessellator.addVertexWithUV(maxX, minY, minZ, d7, d9);
                tessellator.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
            }
        }
    }

    private static void renderFaceYPos(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {

            IIcon iIcon = manager.getIcon(iconIdxOut[i + j * 2]);
            double minU = iIcon.getInterpolatedU(renderBlocks.renderMinX * 16.0D);
            double maxU = iIcon.getInterpolatedU(renderBlocks.renderMaxX * 16.0D);
            double minV = iIcon.getInterpolatedV(renderBlocks.renderMinZ * 16.0D);
            double maxV = iIcon.getInterpolatedV(renderBlocks.renderMaxZ * 16.0D);

            if (renderBlocks.renderMinX < 0.0D || renderBlocks.renderMaxX > 1.0D) {
                minU = iIcon.getMinU();
                maxU = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinZ < 0.0D || renderBlocks.renderMaxZ > 1.0D) {
                minV = iIcon.getMinV();
                maxV = iIcon.getMaxV();
            }

            double d7 = maxU;
            double d8 = minU;
            double d9 = minV;
            double d10 = maxV;

            if (renderBlocks.uvRotateTop == 1) {
                minU = iIcon.getInterpolatedU(renderBlocks.renderMinZ * 16.0D);
                minV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxX * 16.0D);
                maxU = iIcon.getInterpolatedU(renderBlocks.renderMaxZ * 16.0D);
                maxV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinX * 16.0D);
                d9 = minV;
                d10 = maxV;
                d7 = minU;
                d8 = maxU;
                minV = maxV;
                maxV = d9;
            } else if (renderBlocks.uvRotateTop == 2) {
                minU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxZ * 16.0D);
                minV = iIcon.getInterpolatedV(renderBlocks.renderMinX * 16.0D);
                maxU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinZ * 16.0D);
                maxV = iIcon.getInterpolatedV(renderBlocks.renderMaxX * 16.0D);
                d7 = maxU;
                d8 = minU;
                minU = maxU;
                maxU = d8;
                d9 = maxV;
                d10 = minV;
            } else if (renderBlocks.uvRotateTop == 3) {
                minU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinX * 16.0D);
                maxU = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxX * 16.0D);
                minV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinZ * 16.0D);
                maxV = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d7 = maxU;
                d8 = minU;
                d9 = minV;
                d10 = maxV;
            }

            double minX = x + renderBlocks.renderMinX + 0.5 * i;
            double maxX = x + renderBlocks.renderMaxX - (i == 0 ? 0.5 : 0);
            double maxY = y + renderBlocks.renderMaxY;
            double minZ = z + renderBlocks.renderMinZ + 0.5 * j;
            double maxZ = z + renderBlocks.renderMaxZ - (j == 0 ? 0.5 : 0);

            if (renderBlocks.renderFromInside) {
                double d = minX;
                minX = maxX;
                maxX = d;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(maxX, maxY, minZ, d7, d9);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(minX, maxY, minZ, minU, minV);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(minX, maxY, maxZ, d8, d10);
            } else {
                tessellator.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
                tessellator.addVertexWithUV(maxX, maxY, minZ, d7, d9);
                tessellator.addVertexWithUV(minX, maxY, minZ, minU, minV);
                tessellator.addVertexWithUV(minX, maxY, maxZ, d8, d10);
            }
        }

    }

    private static void renderFaceZNeg(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {
            IIcon iIcon = manager.getIcon(iconIdxOut[i + j * 2]);

            double d3 = iIcon.getInterpolatedU(renderBlocks.renderMinX * 16.0D);
            double d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxX * 16.0D);

            if (renderBlocks.field_152631_f) {
                d4 = iIcon.getInterpolatedU((1.0D - renderBlocks.renderMinX) * 16.0D);
                d3 = iIcon.getInterpolatedU((1.0D - renderBlocks.renderMaxX) * 16.0D);
            }

            double d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxY * 16.0D);
            double d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinY * 16.0D);
            double d7;

            if (renderBlocks.flipTexture) {
                d7 = d3;
                d3 = d4;
                d4 = d7;
            }

            if (renderBlocks.renderMinX < 0.0D || renderBlocks.renderMaxX > 1.0D) {
                d3 = iIcon.getMinU();
                d4 = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinY < 0.0D || renderBlocks.renderMaxY > 1.0D) {
                d5 = iIcon.getMinV();
                d6 = iIcon.getMaxV();
            }

            d7 = d4;
            double d8 = d3;
            double d9 = d5;
            double d10 = d6;

            if (renderBlocks.uvRotateEast == 2) {
                d3 = iIcon.getInterpolatedU(renderBlocks.renderMinY * 16.0D);
                d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxY * 16.0D);
                d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinX * 16.0D);
                d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxX * 16.0D);
                d9 = d5;
                d10 = d6;
                d7 = d3;
                d8 = d4;
                d5 = d6;
                d6 = d9;
            } else if (renderBlocks.uvRotateEast == 1) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxY * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinY * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxX * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinX * 16.0D);
                d7 = d4;
                d8 = d3;
                d3 = d4;
                d4 = d8;
                d9 = d6;
                d10 = d5;
            } else if (renderBlocks.uvRotateEast == 3) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinX * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxX * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinY * 16.0D);
                d7 = d4;
                d8 = d3;
                d9 = d5;
                d10 = d6;
            }

            double d11 = x + renderBlocks.renderMinX + (i == 0 ? 0.5 : 0);
            double d12 = x + renderBlocks.renderMaxX - 0.5 * i;
            double d13 = y + renderBlocks.renderMinY + (j == 0 ? 0.5 : 0);
            double d14 = y + renderBlocks.renderMaxY - 0.5 * j;
            double d15 = z + renderBlocks.renderMinZ;

            if (renderBlocks.renderFromInside) {
                double d = d11;
                d11 = d12;
                d12 = d;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(d11, d14, d15, d7, d9);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(d12, d14, d15, d3, d5);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(d12, d13, d15, d8, d10);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(d11, d13, d15, d4, d6);
            } else {
                tessellator.addVertexWithUV(d11, d14, d15, d7, d9);
                tessellator.addVertexWithUV(d12, d14, d15, d3, d5);
                tessellator.addVertexWithUV(d12, d13, d15, d8, d10);
                tessellator.addVertexWithUV(d11, d13, d15, d4, d6);
            }
        }
    }

    private static void renderFaceZPos(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {

            IIcon iIcon = manager.getIcon(iconIdxOut[i + j * 2]);

            if (renderBlocks.hasOverrideBlockTexture()) {
                iIcon = renderBlocks.overrideBlockTexture;
            }

            double d3 = iIcon.getInterpolatedU(renderBlocks.renderMinX * 16.0D);
            double d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxX * 16.0D);
            double d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxY * 16.0D);
            double d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinY * 16.0D);
            double d7;

            if (renderBlocks.flipTexture) {
                d7 = d3;
                d3 = d4;
                d4 = d7;
            }

            if (renderBlocks.renderMinX < 0.0D || renderBlocks.renderMaxX > 1.0D) {
                d3 = iIcon.getMinU();
                d4 = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinY < 0.0D || renderBlocks.renderMaxY > 1.0D) {
                d5 = iIcon.getMinV();
                d6 = iIcon.getMaxV();
            }

            d7 = d4;
            double d8 = d3;
            double d9 = d5;
            double d10 = d6;

            if (renderBlocks.uvRotateWest == 1) {
                d3 = iIcon.getInterpolatedU(renderBlocks.renderMinY * 16.0D);
                d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinX * 16.0D);
                d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxY * 16.0D);
                d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxX * 16.0D);
                d9 = d5;
                d10 = d6;
                d7 = d3;
                d8 = d4;
                d5 = d6;
                d6 = d9;
            } else if (renderBlocks.uvRotateWest == 2) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxY * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMinX * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMaxX * 16.0D);
                d7 = d4;
                d8 = d3;
                d3 = d4;
                d4 = d8;
                d9 = d6;
                d10 = d5;
            } else if (renderBlocks.uvRotateWest == 3) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinX * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxX * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinY * 16.0D);
                d7 = d4;
                d8 = d3;
                d9 = d5;
                d10 = d6;
            }

            double d11 = x + renderBlocks.renderMinX + 0.5 * i;
            double d12 = x + renderBlocks.renderMaxX - (i == 0 ? 0.5 : 0);
            double d13 = y + renderBlocks.renderMinY + (j == 0 ? 0.5 : 0);
            double d14 = y + renderBlocks.renderMaxY - 0.5 * j;
            double d15 = z + renderBlocks.renderMaxZ;

            if (renderBlocks.renderFromInside) {
                d11 = x + renderBlocks.renderMaxX;
                d12 = x + renderBlocks.renderMinX;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(d11, d14, d15, d3, d5);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(d12, d14, d15, d7, d9);
            } else {
                tessellator.addVertexWithUV(d11, d14, d15, d3, d5);
                tessellator.addVertexWithUV(d11, d13, d15, d8, d10);
                tessellator.addVertexWithUV(d12, d13, d15, d4, d6);
                tessellator.addVertexWithUV(d12, d14, d15, d7, d9);
            }
        }
    }

    private static void renderFaceXNeg(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {

            IIcon iIcon = manager.getIcon(iconIdxOut[i * 2 + j]);

            if (renderBlocks.hasOverrideBlockTexture()) {
                iIcon = renderBlocks.overrideBlockTexture;
            }

            double d3 = iIcon.getInterpolatedU(renderBlocks.renderMinZ * 16.0D);
            double d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxZ * 16.0D);
            double d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxY * 16.0D);
            double d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinY * 16.0D);
            double d7;

            if (renderBlocks.flipTexture) {
                d7 = d3;
                d3 = d4;
                d4 = d7;
            }

            if (renderBlocks.renderMinZ < 0.0D || renderBlocks.renderMaxZ > 1.0D) {
                d3 = iIcon.getMinU();
                d4 = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinY < 0.0D || renderBlocks.renderMaxY > 1.0D) {
                d5 = iIcon.getMinV();
                d6 = iIcon.getMaxV();
            }

            d7 = d4;
            double d8 = d3;
            double d9 = d5;
            double d10 = d6;

            if (renderBlocks.uvRotateNorth == 1) {
                d3 = iIcon.getInterpolatedU(renderBlocks.renderMinY * 16.0D);
                d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinZ * 16.0D);
                d9 = d5;
                d10 = d6;
                d7 = d3;
                d8 = d4;
                d5 = d6;
                d6 = d9;
            } else if (renderBlocks.uvRotateNorth == 2) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxY * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMinZ * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMaxZ * 16.0D);
                d7 = d4;
                d8 = d3;
                d3 = d4;
                d4 = d8;
                d9 = d6;
                d10 = d5;
            } else if (renderBlocks.uvRotateNorth == 3) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinZ * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinY * 16.0D);
                d7 = d4;
                d8 = d3;
                d9 = d5;
                d10 = d6;
            }

            double d11 = x + renderBlocks.renderMinX;
            double d12 = y + renderBlocks.renderMinY + (i == 0 ? 0.5 : 0);
            double d13 = y + renderBlocks.renderMaxY - 0.5 * i;
            double d14 = z + renderBlocks.renderMinZ + 0.5 * j;
            double d15 = z + renderBlocks.renderMaxZ - (j == 0 ? 0.5 : 0);

            if (renderBlocks.renderFromInside) {
                d14 = z + renderBlocks.renderMaxZ;
                d15 = z + renderBlocks.renderMinZ;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(d11, d13, d15, d7, d9);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(d11, d12, d14, d8, d10);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(d11, d12, d15, d4, d6);
            } else {
                tessellator.addVertexWithUV(d11, d13, d15, d7, d9);
                tessellator.addVertexWithUV(d11, d13, d14, d3, d5);
                tessellator.addVertexWithUV(d11, d12, d14, d8, d10);
                tessellator.addVertexWithUV(d11, d12, d15, d4, d6);
            }
        }
    }

    private static void renderFaceXPos(RenderBlocks renderBlocks, double x, double y, double z, CTMIconManager manager,
        int[] iconIdxOut) {
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {

            IIcon iIcon = manager.getIcon(iconIdxOut[i * 2 + j]);

            double d3 = iIcon.getInterpolatedU(renderBlocks.renderMinZ * 16.0D);
            double d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxZ * 16.0D);

            if (renderBlocks.field_152631_f) {
                d4 = iIcon.getInterpolatedU((1.0D - renderBlocks.renderMinZ) * 16.0D);
                d3 = iIcon.getInterpolatedU((1.0D - renderBlocks.renderMaxZ) * 16.0D);
            }

            double d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxY * 16.0D);
            double d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinY * 16.0D);
            double d7;

            if (renderBlocks.flipTexture) {
                d7 = d3;
                d3 = d4;
                d4 = d7;
            }

            if (renderBlocks.renderMinZ < 0.0D || renderBlocks.renderMaxZ > 1.0D) {
                d3 = iIcon.getMinU();
                d4 = iIcon.getMaxU();
            }

            if (renderBlocks.renderMinY < 0.0D || renderBlocks.renderMaxY > 1.0D) {
                d5 = iIcon.getMinV();
                d6 = iIcon.getMaxV();
            }

            d7 = d4;
            double d8 = d3;
            double d9 = d5;
            double d10 = d6;

            if (renderBlocks.uvRotateSouth == 2) {
                d3 = iIcon.getInterpolatedU(renderBlocks.renderMinY * 16.0D);
                d5 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMinZ * 16.0D);
                d4 = iIcon.getInterpolatedU(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d9 = d5;
                d10 = d6;
                d7 = d3;
                d8 = d4;
                d5 = d6;
                d6 = d9;
            } else if (renderBlocks.uvRotateSouth == 1) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxY * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxZ * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinZ * 16.0D);
                d7 = d4;
                d8 = d3;
                d3 = d4;
                d4 = d8;
                d9 = d6;
                d10 = d5;
            } else if (renderBlocks.uvRotateSouth == 3) {
                d3 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMinZ * 16.0D);
                d4 = iIcon.getInterpolatedU(16.0D - renderBlocks.renderMaxZ * 16.0D);
                d5 = iIcon.getInterpolatedV(renderBlocks.renderMaxY * 16.0D);
                d6 = iIcon.getInterpolatedV(renderBlocks.renderMinY * 16.0D);
                d7 = d4;
                d8 = d3;
                d9 = d5;
                d10 = d6;
            }

            double d11 = x + renderBlocks.renderMaxX;
            double d12 = y + renderBlocks.renderMinY + (i == 0 ? 0.5 : 0);
            double d13 = y + renderBlocks.renderMaxY - 0.5 * i;
            double d14 = z + renderBlocks.renderMinZ + (j == 0 ? 0.5 : 0);
            double d15 = z + renderBlocks.renderMaxZ - 0.5 * j;

            if (renderBlocks.renderFromInside) {
                d14 = z + renderBlocks.renderMaxZ;
                d15 = z + renderBlocks.renderMinZ;
            }

            if (renderBlocks.enableAO) {
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopLeft,
                    renderBlocks.colorGreenTopLeft,
                    renderBlocks.colorBlueTopLeft);
                tessellator.setBrightness(renderBlocks.brightnessTopLeft);
                tessellator.addVertexWithUV(d11, d12, d15, d8, d10);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomLeft,
                    renderBlocks.colorGreenBottomLeft,
                    renderBlocks.colorBlueBottomLeft);
                tessellator.setBrightness(renderBlocks.brightnessBottomLeft);
                tessellator.addVertexWithUV(d11, d12, d14, d4, d6);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedBottomRight,
                    renderBlocks.colorGreenBottomRight,
                    renderBlocks.colorBlueBottomRight);
                tessellator.setBrightness(renderBlocks.brightnessBottomRight);
                tessellator.addVertexWithUV(d11, d13, d14, d7, d9);
                tessellator.setColorOpaque_F(
                    renderBlocks.colorRedTopRight,
                    renderBlocks.colorGreenTopRight,
                    renderBlocks.colorBlueTopRight);
                tessellator.setBrightness(renderBlocks.brightnessTopRight);
                tessellator.addVertexWithUV(d11, d13, d15, d3, d5);
            } else {
                tessellator.addVertexWithUV(d11, d12, d15, d8, d10);
                tessellator.addVertexWithUV(d11, d12, d14, d4, d6);
                tessellator.addVertexWithUV(d11, d13, d14, d7, d9);
                tessellator.addVertexWithUV(d11, d13, d15, d3, d5);
            }
        }
    }

    /**
     * 根据某个方向上的四个相邻方块判断连接情况，并生成连接纹理的四个象限的 iconIdx。
     * <p>
     * connections[0-3]：表示主方向四周是否连接。
     * connections[4-7]：表示对角线是否连接（需要两个邻居都连接才视为连接）。
     * <p>
     * iconIdx[0-3]：表示象限使用的纹理索引，按逆时针顺序：左上、右上、右下、左下。
     */
    private static void buildConnect(IBlockAccess blockAccess, int x, int y, int z, IIcon iIcon,
        ForgeDirection forgeDirection, int[] iconIdxOut) {

        boolean[] connections = new boolean[8];
        ForgeDirection[] forgeDirections1 = forgeDirections[forgeDirection.ordinal()];

        for (int i = 0; i < 4; i++) {
            IIcon i2 = getIcon(
                blockAccess,
                x + forgeDirections1[i].offsetX,
                y + forgeDirections1[i].offsetY,
                z + forgeDirections1[i].offsetZ,
                forgeDirection);
            connections[i] = i2 != null && i2.getIconName()
                .equals(iIcon.getIconName());
        }

        for (int i = 4; i < 8; i++) {
            int i1 = i - 4;
            int i2 = (i - 3 == 4) ? 0 : i - 3;

            if (connections[i1] && connections[i2]) {
                IIcon ic = getIcon(
                    blockAccess,
                    x + forgeDirections1[i1].offsetX + forgeDirections1[i2].offsetX,
                    y + forgeDirections1[i1].offsetY + forgeDirections1[i2].offsetY,
                    z + forgeDirections1[i1].offsetZ + forgeDirections1[i2].offsetZ,
                    forgeDirection);
                connections[i] = ic != null && ic.getIconName()
                    .equals(iIcon.getIconName());
            } else connections[i] = false;
        }

        // 输出写入传入的 iconIdxOut
        iconIdxOut[0] = connections[7] ? 1
            : (connections[3] && connections[0]) ? 11 : (connections[3]) ? 9 : (connections[0]) ? 3 : 17;

        iconIdxOut[1] = connections[4] ? 2
            : (connections[0] && connections[1]) ? 12 : (connections[0]) ? 4 : (connections[1]) ? 10 : 18;

        iconIdxOut[2] = connections[6] ? 5
            : (connections[2] && connections[3]) ? 15 : (connections[2]) ? 7 : (connections[3]) ? 13 : 19;

        iconIdxOut[3] = connections[5] ? 6
            : (connections[1] && connections[2]) ? 16 : (connections[1]) ? 14 : (connections[2]) ? 8 : 20;
    }

    private static IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection forgeDirection) {
        Block block = blockAccess.getBlock(x, y, z);
        if (block == null || block instanceof BlockAir) return null;
        int renderMetadata;

        if (block instanceof IBlockWithClientMeta clientMetaBlock) {
            World world = Minecraft.getMinecraft().theWorld;
            renderMetadata = clientMetaBlock.getClientMeta(world, x, y, z);
        } else {
            renderMetadata = blockAccess.getBlockMetadata(x, y, z);
        }

        if (block instanceof IBlockWithTextures texturedBlock) {
            ITexture[][] textures = texturedBlock.getTextures(renderMetadata);
            if (textures != null && textures.length > forgeDirection.ordinal()
                && textures[forgeDirection.ordinal()] != null) {
                if (textures[forgeDirection.ordinal()].length > 0) {
                    ITexture firstTexture = textures[forgeDirection.ordinal()][0];
                    if (firstTexture instanceof GTCopiedBlockTextureRender gtCopiedBlockTextureRender) {
                        return gtCopiedBlockTextureRender.getBlock()
                            .getIcon(forgeDirection.ordinal(), renderMetadata);
                    } else if (firstTexture instanceof GTRenderedTexture gtRenderedTexture) {
                        IIconContainer container = ((GTRenderedTextureAccessor) gtRenderedTexture).getIconContainer();
                        if (container != null) {
                            return container.getIcon();
                        }
                    }
                }
            }
        }

        if (block instanceof BlockMachines) {
            TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
            if (tileEntity instanceof ITexturedTileEntity texturedTileEntity) {
                ITexture[] iTextures = texturedTileEntity.getTexture(block, forgeDirection);
                for (ITexture texture : iTextures) {
                    if (texture instanceof GTCopiedBlockTextureRender gtCopiedBlockTextureRender) {
                        return gtCopiedBlockTextureRender.getBlock()
                            .getIcon(forgeDirection.ordinal(), gtCopiedBlockTextureRender.getMeta());
                    }
                }
            }
        } else {
            return block.getIcon(blockAccess, x, y, z, forgeDirection.ordinal());
        }
        return null;
    }

    private static final ForgeDirection[][] forgeDirections = new ForgeDirection[][] {
        { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST }, // DOWN -Y
        { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST }, // UP +Y
        { ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.DOWN, ForgeDirection.EAST }, // NORTH -Z
        { ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.WEST }, // SOUTH +Z
        { ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.NORTH }, // WEST -X
        { ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.SOUTH } // EAST +X
    };

    public static void register(String[] list) {
        GregTechAPI.sGTBlockIconload.add(() -> {
            for (String s : list) {
                if (s != null && !s.trim()
                    .isEmpty() && !s.startsWith("#") && !s.startsWith("//")) {
                    IIcon icon2 = GregTechAPI.sBlockIcons.registerIcon(s + "_ctm");
                    IIcon icon1 = GregTechAPI.sBlockIcons.registerIcon(s);
                    ctmIconMap.put(s, new CTMIconManager(icon1, icon2));
                }
            }
        });
    }
}

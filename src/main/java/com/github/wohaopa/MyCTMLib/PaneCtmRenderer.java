package com.github.wohaopa.MyCTMLib;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class PaneCtmRenderer {

    private static final double HALF_THICKNESS = 0.0625D;
    private static final double VERTICAL_OFFSET = 0.001D;
    private static final double EDGE_OFFSET = 0.005D;

    public static boolean render(RenderBlocks renderBlocks, IBlockAccess blockAccess, BlockPane pane, int x, int y,
        int z, IIcon icon, CTMIconManager manager) {
        return renderThin(renderBlocks, blockAccess, pane, x, y, z, icon, manager);
    }

    public static boolean renderThin(RenderBlocks renderBlocks, IBlockAccess blockAccess, BlockPane pane, int x, int y,
        int z, IIcon icon, CTMIconManager manager) {
        return renderPane(renderBlocks, blockAccess, pane, x, y, z, icon, manager, 0.0D, 0.0D);
    }

    public static boolean renderThick(RenderBlocks renderBlocks, IBlockAccess blockAccess, BlockPane pane, int x, int y,
        int z, IIcon icon, CTMIconManager manager) {
        return renderPane(renderBlocks, blockAccess, pane, x, y, z, icon, manager, HALF_THICKNESS, VERTICAL_OFFSET);
    }

    private static boolean renderPane(RenderBlocks renderBlocks, IBlockAccess blockAccess, BlockPane pane, int x, int y,
        int z, IIcon icon, CTMIconManager manager, double thickness, double verticalInset) {
        if (renderBlocks == null || blockAccess == null
            || pane == null
            || icon == null
            || manager == null
            || !manager.hasConnectionTexture()) {
            return false;
        }

        configureTessellator(blockAccess, pane, x, y, z);

        boolean north = pane.canPaneConnectTo(blockAccess, x, y, z - 1, ForgeDirection.NORTH);
        boolean south = pane.canPaneConnectTo(blockAccess, x, y, z + 1, ForgeDirection.SOUTH);
        boolean west = pane.canPaneConnectTo(blockAccess, x - 1, y, z, ForgeDirection.WEST);
        boolean east = pane.canPaneConnectTo(blockAccess, x + 1, y, z, ForgeDirection.EAST);
        boolean isolated = !north && !south && !west && !east;

        if (isolated) {
            north = true;
            south = true;
            west = true;
            east = true;
        }

        CTMIconManager selectedManager = Textures
            .selectTextureManager(blockAccess, x, y, z, icon.getIconName(), manager);
        double minY = y + verticalInset;
        double maxY = y + 1.0D - verticalInset;
        double eastWestMinX = west ? x : x + 0.5D;
        double eastWestMaxX = east ? x + 1.0D : x + 0.5D;
        double northSouthMinZ = north ? z : z + 0.5D;
        double northSouthMaxZ = south ? z + 1.0D : z + 0.5D;

        if (west || east) {
            drawEastWestPanel(
                blockAccess,
                x,
                y,
                z,
                icon,
                selectedManager,
                minY,
                maxY,
                thickness,
                north,
                south,
                west,
                east,
                isolated);
        }

        if (north || south) {
            drawNorthSouthPanel(
                blockAccess,
                x,
                y,
                z,
                icon,
                selectedManager,
                minY,
                maxY,
                thickness,
                north,
                south,
                west,
                east,
                isolated);
        }

        drawHorizontalEdges(
            blockAccess,
            pane,
            x,
            y,
            z,
            icon,
            selectedManager,
            west,
            east,
            north,
            south,
            eastWestMinX,
            eastWestMaxX,
            northSouthMinZ,
            northSouthMaxZ);

        return true;
    }

    private static void configureTessellator(IBlockAccess blockAccess, Block pane, int x, int y, int z) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(pane.getMixedBrightnessForBlock(blockAccess, x, y, z));

        int color = pane.colorMultiplier(blockAccess, x, y, z);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        if (EntityRenderer.anaglyphEnable) {
            float anaglyphRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float anaglyphGreen = (red * 30.0F + green * 70.0F) / 100.0F;
            float anaglyphBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = anaglyphRed;
            green = anaglyphGreen;
            blue = anaglyphBlue;
        }

        tessellator.setColorOpaque_F(red, green, blue);
    }

    private static void drawEastWestPanel(IBlockAccess blockAccess, int x, int y, int z, IIcon icon,
        CTMIconManager manager, double minY, double maxY, double thickness, boolean north, boolean south, boolean west,
        boolean east, boolean isolated) {
        double westX = x;
        double westThin = x + 0.5D - thickness;
        double eastThin = x + 0.5D + thickness;
        double eastX = x + 1.0D;
        double wallNorthZ = z + 0.5D - thickness;
        double wallSouthZ = z + 0.5D + thickness;
        double edgeNorthZ = z + 0.5D - HALF_THICKNESS;
        double edgeSouthZ = z + 0.5D + HALF_THICKNESS;

        if (isolated) {
            drawFaceX(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.WEST,
                westX,
                edgeNorthZ,
                edgeSouthZ,
                minY,
                maxY,
                true);
            drawFaceX(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.EAST,
                eastX,
                edgeNorthZ,
                edgeSouthZ,
                minY,
                maxY,
                true);
        }

        if (west && east) {
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.SOUTH, westX, eastX, minY, maxY, wallSouthZ, false);
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.NORTH, westX, eastX, minY, maxY, wallNorthZ, false);
            return;
        }

        if (west) {
            double southEnd = south ? westThin : eastThin;
            double northEnd = north ? westThin : eastThin;
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.SOUTH, westX, southEnd, minY, maxY, wallSouthZ, false);
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.NORTH, westX, northEnd, minY, maxY, wallNorthZ, false);
            if (!north && !south) {
                drawFaceX(
                    blockAccess,
                    x,
                    y,
                    z,
                    icon,
                    manager,
                    ForgeDirection.EAST,
                    eastThin,
                    edgeNorthZ,
                    edgeSouthZ,
                    minY,
                    maxY,
                    true);
            }
            return;
        }

        if (east) {
            double southStart = south ? eastThin : westThin;
            double northStart = north ? eastThin : westThin;
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.SOUTH, southStart, eastX, minY, maxY, wallSouthZ, false);
            drawFaceZ(blockAccess, x, y, z, icon, manager, ForgeDirection.NORTH, northStart, eastX, minY, maxY, wallNorthZ, false);
            if (!north && !south) {
                drawFaceX(
                    blockAccess,
                    x,
                    y,
                    z,
                    icon,
                    manager,
                    ForgeDirection.WEST,
                    westThin,
                    edgeNorthZ,
                    edgeSouthZ,
                    minY,
                    maxY,
                    true);
            }
        }
    }

    private static void drawNorthSouthPanel(IBlockAccess blockAccess, int x, int y, int z, IIcon icon,
        CTMIconManager manager, double minY, double maxY, double thickness, boolean north, boolean south, boolean west,
        boolean east, boolean isolated) {
        double wallWestX = x + 0.5D - thickness;
        double wallEastX = x + 0.5D + thickness;
        double edgeWestX = x + 0.5D - HALF_THICKNESS;
        double edgeEastX = x + 0.5D + HALF_THICKNESS;
        double northThin = z + 0.5D - thickness;
        double southThin = z + 0.5D + thickness;
        double northZ = z;
        double southZ = z + 1.0D;

        if (isolated) {
            drawFaceZ(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.NORTH,
                edgeWestX,
                edgeEastX,
                minY,
                maxY,
                northZ,
                true);
            drawFaceZ(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.SOUTH,
                edgeWestX,
                edgeEastX,
                minY,
                maxY,
                southZ,
                true);
        }

        if (north && south) {
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.WEST, wallWestX, northZ, southZ, minY, maxY, false);
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.EAST, wallEastX, northZ, southZ, minY, maxY, false);
            return;
        }

        if (north) {
            double westEnd = west ? northThin : southThin;
            double eastEnd = east ? northThin : southThin;
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.WEST, wallWestX, northZ, westEnd, minY, maxY, false);
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.EAST, wallEastX, northZ, eastEnd, minY, maxY, false);
            if (!west && !east) {
                drawFaceZ(
                    blockAccess,
                    x,
                    y,
                    z,
                    icon,
                    manager,
                    ForgeDirection.SOUTH,
                    edgeWestX,
                    edgeEastX,
                    minY,
                    maxY,
                    southThin,
                    true);
            }
            return;
        }

        if (south) {
            double westStart = west ? southThin : northThin;
            double eastStart = east ? southThin : northThin;
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.WEST, wallWestX, westStart, southZ, minY, maxY, false);
            drawFaceX(blockAccess, x, y, z, icon, manager, ForgeDirection.EAST, wallEastX, eastStart, southZ, minY, maxY, false);
            if (!west && !east) {
                drawFaceZ(
                    blockAccess,
                    x,
                    y,
                    z,
                    icon,
                    manager,
                    ForgeDirection.NORTH,
                    edgeWestX,
                    edgeEastX,
                    minY,
                    maxY,
                    northThin,
                    true);
            }
        }
    }

    private static void drawHorizontalEdges(IBlockAccess blockAccess, BlockPane pane, int x, int y, int z, IIcon icon,
        CTMIconManager manager, boolean west, boolean east, boolean north, boolean south, double eastWestMinX,
        double eastWestMaxX, double northSouthMinZ, double northSouthMaxZ) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        if (!isMatchingPane(blockAccess, pane, x, y + 1, z, metadata)) {
            drawHorizontalTopOrBottom(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.UP,
                y + 1.0D + EDGE_OFFSET,
                west,
                east,
                north,
                south,
                eastWestMinX,
                eastWestMaxX,
                northSouthMinZ,
                northSouthMaxZ);
        }
        if (!isMatchingPane(blockAccess, pane, x, y - 1, z, metadata)) {
            drawHorizontalTopOrBottom(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                ForgeDirection.DOWN,
                y - EDGE_OFFSET,
                west,
                east,
                north,
                south,
                eastWestMinX,
                eastWestMaxX,
                northSouthMinZ,
                northSouthMaxZ);
        }
    }

    private static void drawHorizontalTopOrBottom(IBlockAccess blockAccess, int x, int y, int z, IIcon icon,
        CTMIconManager manager, ForgeDirection direction, double faceY, boolean west, boolean east, boolean north,
        boolean south, double eastWestMinX, double eastWestMaxX, double northSouthMinZ, double northSouthMaxZ) {
        double edgeMinZ = z + 0.5D - HALF_THICKNESS;
        double edgeMaxZ = z + 0.5D + HALF_THICKNESS;
        double edgeMinX = x + 0.5D - HALF_THICKNESS;
        double edgeMaxX = x + 0.5D + HALF_THICKNESS;

        if (west || east) {
            drawFaceY(
                blockAccess,
                x,
                y,
                z,
                icon,
                manager,
                direction,
                eastWestMinX,
                eastWestMaxX,
                edgeMinZ,
                edgeMaxZ,
                faceY);
        }
        if (north || south) {
            if (west || east) {
                if (northSouthMinZ < edgeMinZ) {
                    drawFaceY(
                        blockAccess,
                        x,
                        y,
                        z,
                        icon,
                        manager,
                        direction,
                        edgeMinX,
                        edgeMaxX,
                        northSouthMinZ,
                        edgeMinZ,
                        faceY);
                }
                if (edgeMaxZ < northSouthMaxZ) {
                    drawFaceY(
                        blockAccess,
                        x,
                        y,
                        z,
                        icon,
                        manager,
                        direction,
                        edgeMinX,
                        edgeMaxX,
                        edgeMaxZ,
                        northSouthMaxZ,
                        faceY);
                }
            } else {
                drawFaceY(
                    blockAccess,
                    x,
                    y,
                    z,
                    icon,
                    manager,
                    direction,
                    edgeMinX,
                    edgeMaxX,
                    northSouthMinZ,
                    northSouthMaxZ,
                    faceY);
            }
        }
    }

    private static boolean isMatchingPane(IBlockAccess blockAccess, BlockPane pane, int x, int y, int z, int metadata) {
        return blockAccess.getBlock(x, y, z) == pane && blockAccess.getBlockMetadata(x, y, z) == metadata;
    }

    private static void drawFaceZ(IBlockAccess blockAccess, int x, int y, int z, IIcon icon, CTMIconManager manager,
        ForgeDirection direction, double minX, double maxX, double minY, double maxY, double faceZ, boolean twoSided) {
        int[] indices = Textures.threadLocalIconIdx.get();
        Textures.buildConnect(blockAccess, x, y, z, icon, direction, indices);
        Tessellator tessellator = Tessellator.instance;
        for (int horizontal = 0; horizontal < 2; horizontal++) {
            for (int vertical = 0; vertical < 2; vertical++) {
                double left = Math.max(minX, x + horizontal * 0.5D);
                double right = Math.min(maxX, x + (horizontal + 1) * 0.5D);
                double bottom = Math.max(minY, y + (vertical == 0 ? 0.5D : 0.0D));
                double top = Math.min(maxY, y + (vertical == 0 ? 1.0D : 0.5D));
                if (left >= right || bottom >= top) {
                    continue;
                }
                IIcon quadrant = manager.getIcon(getQuadrantIndex(direction, horizontal, vertical, indices));
                drawFaceZ(
                    tessellator,
                    direction,
                    left,
                    right,
                    bottom,
                    top,
                    faceZ,
                    quadrant,
                    horizontal,
                    vertical,
                    x,
                    y,
                    twoSided);
            }
        }
    }

    private static void drawFaceX(IBlockAccess blockAccess, int x, int y, int z, IIcon icon, CTMIconManager manager,
        ForgeDirection direction, double faceX, double minZ, double maxZ, double minY, double maxY, boolean twoSided) {
        int[] indices = Textures.threadLocalIconIdx.get();
        Textures.buildConnect(blockAccess, x, y, z, icon, direction, indices);
        Tessellator tessellator = Tessellator.instance;
        for (int horizontal = 0; horizontal < 2; horizontal++) {
            for (int vertical = 0; vertical < 2; vertical++) {
                double near = Math.max(minZ, z + horizontal * 0.5D);
                double far = Math.min(maxZ, z + (horizontal + 1) * 0.5D);
                double bottom = Math.max(minY, y + (vertical == 0 ? 0.5D : 0.0D));
                double top = Math.min(maxY, y + (vertical == 0 ? 1.0D : 0.5D));
                if (near >= far || bottom >= top) {
                    continue;
                }
                IIcon quadrant = manager.getIcon(getQuadrantIndex(direction, horizontal, vertical, indices));
                drawFaceX(
                    tessellator,
                    direction,
                    faceX,
                    near,
                    far,
                    bottom,
                    top,
                    quadrant,
                    horizontal,
                    vertical,
                    y,
                    z,
                    twoSided);
            }
        }
    }

    private static void drawFaceY(IBlockAccess blockAccess, int x, int y, int z, IIcon icon, CTMIconManager manager,
        ForgeDirection direction, double minX, double maxX, double minZ, double maxZ, double faceY) {
        int[] indices = Textures.threadLocalIconIdx.get();
        Textures.buildConnect(blockAccess, x, y, z, icon, direction, indices);
        Tessellator tessellator = Tessellator.instance;
        for (int horizontal = 0; horizontal < 2; horizontal++) {
            for (int vertical = 0; vertical < 2; vertical++) {
                double left = Math.max(minX, x + horizontal * 0.5D);
                double right = Math.min(maxX, x + (horizontal + 1) * 0.5D);
                double near = Math.max(minZ, z + vertical * 0.5D);
                double far = Math.min(maxZ, z + (vertical + 1) * 0.5D);
                if (left >= right || near >= far) {
                    continue;
                }
                IIcon quadrant = manager.getIcon(getQuadrantIndex(direction, horizontal, vertical, indices));
                drawFaceY(
                    tessellator,
                    direction,
                    left,
                    right,
                    near,
                    far,
                    faceY,
                    quadrant,
                    horizontal,
                    vertical,
                    x,
                    z);
            }
        }
    }

    private static int getQuadrantIndex(ForgeDirection direction, int horizontal, int vertical, int[] indices) {
        return switch (direction) {
            case NORTH -> indices[1 - horizontal + vertical * 2];
            case WEST -> indices[vertical * 2 + 1 - horizontal];
            case DOWN, UP, SOUTH, EAST -> indices[horizontal + vertical * 2];
            default -> throw new IllegalArgumentException("Unsupported pane face: " + direction);
        };
    }

    private static void drawFaceZ(Tessellator tessellator, ForgeDirection direction, double minX, double maxX,
        double minY, double maxY, double z, IIcon icon, int horizontal, int vertical, int x, int y, boolean twoSided) {
        double minXFraction = getTileFraction(minX, x + horizontal * 0.5D);
        double maxXFraction = getTileFraction(maxX, x + horizontal * 0.5D);
        double minYFraction = getTileFraction(minY, y + (vertical == 0 ? 0.5D : 0.0D));
        double maxYFraction = getTileFraction(maxY, y + (vertical == 0 ? 0.5D : 0.0D));
        if (direction == ForgeDirection.NORTH) {
            drawQuad(
                tessellator,
                maxX,
                maxY,
                z,
                getU(icon, 1.0D - maxXFraction),
                getV(icon, 1.0D - maxYFraction),
                minX,
                minY,
                z,
                getU(icon, 1.0D - minXFraction),
                getV(icon, 1.0D - minYFraction),
                twoSided);
        } else {
            drawQuad(
                tessellator,
                minX,
                maxY,
                z,
                getU(icon, minXFraction),
                getV(icon, 1.0D - maxYFraction),
                maxX,
                minY,
                z,
                getU(icon, maxXFraction),
                getV(icon, 1.0D - minYFraction),
                twoSided);
        }
    }

    private static void drawFaceX(Tessellator tessellator, ForgeDirection direction, double x, double minZ, double maxZ,
        double minY, double maxY, IIcon icon, int horizontal, int vertical, int y, int z, boolean twoSided) {
        double minZFraction = getTileFraction(minZ, z + horizontal * 0.5D);
        double maxZFraction = getTileFraction(maxZ, z + horizontal * 0.5D);
        double minYFraction = getTileFraction(minY, y + (vertical == 0 ? 0.5D : 0.0D));
        double maxYFraction = getTileFraction(maxY, y + (vertical == 0 ? 0.5D : 0.0D));
        if (direction == ForgeDirection.WEST) {
            drawQuad(
                tessellator,
                x,
                maxY,
                minZ,
                getU(icon, minZFraction),
                getV(icon, 1.0D - maxYFraction),
                x,
                minY,
                maxZ,
                getU(icon, maxZFraction),
                getV(icon, 1.0D - minYFraction),
                twoSided);
        } else {
            drawQuad(
                tessellator,
                x,
                maxY,
                maxZ,
                getU(icon, 1.0D - maxZFraction),
                getV(icon, 1.0D - maxYFraction),
                x,
                minY,
                minZ,
                getU(icon, 1.0D - minZFraction),
                getV(icon, 1.0D - minYFraction),
                twoSided);
        }
    }

    private static void drawFaceY(Tessellator tessellator, ForgeDirection direction, double minX, double maxX,
        double minZ, double maxZ, double y, IIcon icon, int horizontal, int vertical, int x, int z) {
        double minXFraction = getTileFraction(minX, x + horizontal * 0.5D);
        double maxXFraction = getTileFraction(maxX, x + horizontal * 0.5D);
        double minZFraction = getTileFraction(minZ, z + vertical * 0.5D);
        double maxZFraction = getTileFraction(maxZ, z + vertical * 0.5D);
        if (direction == ForgeDirection.DOWN) {
            drawQuad(
                tessellator,
                minX,
                y,
                maxZ,
                getU(icon, minXFraction),
                getV(icon, maxZFraction),
                maxX,
                y,
                minZ,
                getU(icon, maxXFraction),
                getV(icon, minZFraction),
                true);
        } else {
            drawQuad(
                tessellator,
                maxX,
                y,
                maxZ,
                getU(icon, maxXFraction),
                getV(icon, maxZFraction),
                minX,
                y,
                minZ,
                getU(icon, minXFraction),
                getV(icon, minZFraction),
                true);
        }
    }

    private static double getTileFraction(double coordinate, double tileStart) {
        return (coordinate - tileStart) * 2.0D;
    }

    private static double getU(IIcon icon, double fraction) {
        return icon.getInterpolatedU(fraction * 16.0D);
    }

    private static double getV(IIcon icon, double fraction) {
        return icon.getInterpolatedV(fraction * 16.0D);
    }

    private static void drawQuad(Tessellator tessellator, double x0, double y0, double z0, double u0, double v0,
        double x1, double y1, double z1, double u1, double v1, boolean twoSided) {
        tessellator.addVertexWithUV(x0, y0, z0, u0, v0);
        tessellator.addVertexWithUV(x0, y1, z0, u0, v1);
        tessellator.addVertexWithUV(x1, y1, z1, u1, v1);
        tessellator.addVertexWithUV(x1, y0, z1, u1, v0);
        if (twoSided) {
            tessellator.addVertexWithUV(x1, y0, z1, u1, v0);
            tessellator.addVertexWithUV(x1, y1, z1, u1, v1);
            tessellator.addVertexWithUV(x0, y1, z0, u0, v1);
            tessellator.addVertexWithUV(x0, y0, z0, u0, v0);
        }
    }
}

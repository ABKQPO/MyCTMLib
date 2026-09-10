package com.github.wohaopa.MyCTMLib;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class CtmFaceRenderer {

    private static final ThreadLocal<FaceLighting> faceLighting = ThreadLocal.withInitial(FaceLighting::new);
    private static final ThreadLocal<CtmQuadrantIcon> quadrantIcons = ThreadLocal.withInitial(CtmQuadrantIcon::new);
    private static final int[] QUADRANT_ORDER = { 0, 1, 3, 2 };

    public static boolean render(RenderBlocks renderBlocks, Block block, double x, double y, double z,
        CTMIconManager manager, ForgeDirection direction, int[] iconIndices) {
        if (renderBlocks == null || block == null || manager == null || direction == null || iconIndices == null) {
            return false;
        }

        double minX = renderBlocks.renderMinX;
        double minY = renderBlocks.renderMinY;
        double minZ = renderBlocks.renderMinZ;
        double maxX = renderBlocks.renderMaxX;
        double maxY = renderBlocks.renderMaxY;
        double maxZ = renderBlocks.renderMaxZ;
        IIcon wholeFace = manager.getWholeFaceIcon(iconIndices);
        if (wholeFace != null) {
            // Draw the face as one quad so coplanar overlay layers keep the same geometry and stop fighting.
            IIcon previousWholeOverride = renderBlocks.overrideBlockTexture;
            renderBlocks.overrideBlockTexture = wholeFace;
            try {
                renderFace(renderBlocks, block, x, y, z, wholeFace, direction);
            } finally {
                renderBlocks.overrideBlockTexture = previousWholeOverride;
            }
            return true;
        }

        IIcon previousOverride = renderBlocks.overrideBlockTexture;
        boolean previousFaceFlip = renderBlocks.field_152631_f;
        FaceLighting lighting = renderBlocks.enableAO ? faceLighting.get() : null;
        if (lighting != null) {
            lighting.capture(renderBlocks);
        }

        try {
            for (int quadrant = 0; quadrant < 4; quadrant++) {
                IIcon sourceIcon = manager.getIcon(iconIndices[QUADRANT_ORDER[quadrant]]);
                if (sourceIcon == null) {
                    continue;
                }
                IIcon icon = quadrantIcons.get()
                    .setSource(sourceIcon, quadrant == 1 || quadrant == 2 ? 8.0D : 0.0D, quadrant >= 2 ? 8.0D : 0.0D);
                if (lighting != null) {
                    lighting.apply(renderBlocks, direction.ordinal(), quadrant);
                }
                setQuadrantBounds(renderBlocks, direction.ordinal(), quadrant, minX, minY, minZ, maxX, maxY, maxZ);
                renderBlocks.overrideBlockTexture = icon;
                renderBlocks.field_152631_f = direction == ForgeDirection.NORTH || direction == ForgeDirection.EAST;
                renderFace(renderBlocks, block, x, y, z, icon, direction);
            }
            return true;
        } finally {
            if (lighting != null) {
                lighting.restore(renderBlocks);
            }
            renderBlocks.overrideBlockTexture = previousOverride;
            renderBlocks.field_152631_f = previousFaceFlip;
            renderBlocks.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static void renderFace(RenderBlocks renderBlocks, Block block, double x, double y, double z, IIcon icon,
        ForgeDirection direction) {
        switch (direction) {
            case DOWN -> renderBlocks.renderFaceYNeg(block, x, y, z, icon);
            case UP -> renderBlocks.renderFaceYPos(block, x, y, z, icon);
            case NORTH -> renderBlocks.renderFaceZNeg(block, x, y, z, icon);
            case SOUTH -> renderBlocks.renderFaceZPos(block, x, y, z, icon);
            case WEST -> renderBlocks.renderFaceXNeg(block, x, y, z, icon);
            case EAST -> renderBlocks.renderFaceXPos(block, x, y, z, icon);
            default -> throw new IllegalArgumentException("Unsupported CTM face: " + direction);
        }
    }

    private static void setQuadrantBounds(RenderBlocks renderBlocks, int face, int quadrant, double minX, double minY,
        double minZ, double maxX, double maxY, double maxZ) {
        double middleX = (minX + maxX) * 0.5D;
        double middleY = (minY + maxY) * 0.5D;
        double middleZ = (minZ + maxZ) * 0.5D;

        switch (face) {
            case 0, 1 -> setHorizontalBounds(
                renderBlocks,
                quadrant,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                middleX,
                middleZ);
            case 2 -> setNorthBounds(renderBlocks, quadrant, minX, minY, minZ, maxX, maxY, maxZ, middleX, middleY);
            case 3 -> setSouthBounds(renderBlocks, quadrant, minX, minY, minZ, maxX, maxY, maxZ, middleX, middleY);
            case 4 -> setWestBounds(renderBlocks, quadrant, minX, minY, minZ, maxX, maxY, maxZ, middleY, middleZ);
            case 5 -> setEastBounds(renderBlocks, quadrant, minX, minY, minZ, maxX, maxY, maxZ, middleY, middleZ);
            default -> throw new IllegalArgumentException("Unsupported CTM face: " + face);
        }
    }

    private static void setHorizontalBounds(RenderBlocks renderBlocks, int quadrant, double minX, double minY,
        double minZ, double maxX, double maxY, double maxZ, double middleX, double middleZ) {
        switch (quadrant) {
            case 0 -> renderBlocks.setRenderBounds(minX, minY, minZ, middleX, maxY, middleZ);
            case 1 -> renderBlocks.setRenderBounds(middleX, minY, minZ, maxX, maxY, middleZ);
            case 2 -> renderBlocks.setRenderBounds(middleX, minY, middleZ, maxX, maxY, maxZ);
            case 3 -> renderBlocks.setRenderBounds(minX, minY, middleZ, middleX, maxY, maxZ);
            default -> throw new IllegalArgumentException("Unsupported CTM quadrant: " + quadrant);
        }
    }

    private static void setNorthBounds(RenderBlocks renderBlocks, int quadrant, double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ, double middleX, double middleY) {
        switch (quadrant) {
            case 0 -> renderBlocks.setRenderBounds(middleX, middleY, minZ, maxX, maxY, maxZ);
            case 1 -> renderBlocks.setRenderBounds(minX, middleY, minZ, middleX, maxY, maxZ);
            case 2 -> renderBlocks.setRenderBounds(minX, minY, minZ, middleX, middleY, maxZ);
            case 3 -> renderBlocks.setRenderBounds(middleX, minY, minZ, maxX, middleY, maxZ);
            default -> throw new IllegalArgumentException("Unsupported CTM quadrant: " + quadrant);
        }
    }

    private static void setSouthBounds(RenderBlocks renderBlocks, int quadrant, double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ, double middleX, double middleY) {
        switch (quadrant) {
            case 0 -> renderBlocks.setRenderBounds(minX, middleY, minZ, middleX, maxY, maxZ);
            case 1 -> renderBlocks.setRenderBounds(middleX, middleY, minZ, maxX, maxY, maxZ);
            case 2 -> renderBlocks.setRenderBounds(middleX, minY, minZ, maxX, middleY, maxZ);
            case 3 -> renderBlocks.setRenderBounds(minX, minY, minZ, middleX, middleY, maxZ);
            default -> throw new IllegalArgumentException("Unsupported CTM quadrant: " + quadrant);
        }
    }

    private static void setWestBounds(RenderBlocks renderBlocks, int quadrant, double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ, double middleY, double middleZ) {
        switch (quadrant) {
            case 0 -> renderBlocks.setRenderBounds(minX, middleY, minZ, maxX, maxY, middleZ);
            case 1 -> renderBlocks.setRenderBounds(minX, middleY, middleZ, maxX, maxY, maxZ);
            case 2 -> renderBlocks.setRenderBounds(minX, minY, middleZ, maxX, middleY, maxZ);
            case 3 -> renderBlocks.setRenderBounds(minX, minY, minZ, maxX, middleY, middleZ);
            default -> throw new IllegalArgumentException("Unsupported CTM quadrant: " + quadrant);
        }
    }

    private static void setEastBounds(RenderBlocks renderBlocks, int quadrant, double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ, double middleY, double middleZ) {
        switch (quadrant) {
            case 0 -> renderBlocks.setRenderBounds(minX, middleY, middleZ, maxX, maxY, maxZ);
            case 1 -> renderBlocks.setRenderBounds(minX, middleY, minZ, maxX, maxY, middleZ);
            case 2 -> renderBlocks.setRenderBounds(minX, minY, minZ, maxX, middleY, middleZ);
            case 3 -> renderBlocks.setRenderBounds(minX, minY, middleZ, maxX, middleY, maxZ);
            default -> throw new IllegalArgumentException("Unsupported CTM quadrant: " + quadrant);
        }
    }

    private static class FaceLighting {

        private static final int[][] FACE_QUADRANTS = { { 3, 2, 1, 0 }, { 2, 3, 0, 1 }, { 3, 0, 1, 2 }, { 0, 1, 2, 3 },
            { 3, 0, 1, 2 }, { 1, 2, 3, 0 } };

        private float redTopLeft;
        private float redTopRight;
        private float redBottomLeft;
        private float redBottomRight;
        private float greenTopLeft;
        private float greenTopRight;
        private float greenBottomLeft;
        private float greenBottomRight;
        private float blueTopLeft;
        private float blueTopRight;
        private float blueBottomLeft;
        private float blueBottomRight;
        private int brightnessTopLeft;
        private int brightnessTopRight;
        private int brightnessBottomLeft;
        private int brightnessBottomRight;

        private void capture(RenderBlocks renderBlocks) {
            redTopLeft = renderBlocks.colorRedTopLeft;
            redTopRight = renderBlocks.colorRedTopRight;
            redBottomLeft = renderBlocks.colorRedBottomLeft;
            redBottomRight = renderBlocks.colorRedBottomRight;
            greenTopLeft = renderBlocks.colorGreenTopLeft;
            greenTopRight = renderBlocks.colorGreenTopRight;
            greenBottomLeft = renderBlocks.colorGreenBottomLeft;
            greenBottomRight = renderBlocks.colorGreenBottomRight;
            blueTopLeft = renderBlocks.colorBlueTopLeft;
            blueTopRight = renderBlocks.colorBlueTopRight;
            blueBottomLeft = renderBlocks.colorBlueBottomLeft;
            blueBottomRight = renderBlocks.colorBlueBottomRight;
            brightnessTopLeft = renderBlocks.brightnessTopLeft;
            brightnessTopRight = renderBlocks.brightnessTopRight;
            brightnessBottomLeft = renderBlocks.brightnessBottomLeft;
            brightnessBottomRight = renderBlocks.brightnessBottomRight;
        }

        private void apply(RenderBlocks renderBlocks, int face, int quadrant) {
            int faceQuadrant = FACE_QUADRANTS[face][quadrant];
            float minU = faceQuadrant == 1 || faceQuadrant == 2 ? 0.5F : 0.0F;
            float minV = faceQuadrant >= 2 ? 0.5F : 0.0F;
            float maxU = minU + 0.5F;
            float maxV = minV + 0.5F;
            renderBlocks.colorRedTopLeft = interpolate(
                redTopLeft,
                redTopRight,
                redBottomLeft,
                redBottomRight,
                minU,
                minV);
            renderBlocks.colorRedTopRight = interpolate(
                redTopLeft,
                redTopRight,
                redBottomLeft,
                redBottomRight,
                maxU,
                minV);
            renderBlocks.colorRedBottomLeft = interpolate(
                redTopLeft,
                redTopRight,
                redBottomLeft,
                redBottomRight,
                minU,
                maxV);
            renderBlocks.colorRedBottomRight = interpolate(
                redTopLeft,
                redTopRight,
                redBottomLeft,
                redBottomRight,
                maxU,
                maxV);
            renderBlocks.colorGreenTopLeft = interpolate(
                greenTopLeft,
                greenTopRight,
                greenBottomLeft,
                greenBottomRight,
                minU,
                minV);
            renderBlocks.colorGreenTopRight = interpolate(
                greenTopLeft,
                greenTopRight,
                greenBottomLeft,
                greenBottomRight,
                maxU,
                minV);
            renderBlocks.colorGreenBottomLeft = interpolate(
                greenTopLeft,
                greenTopRight,
                greenBottomLeft,
                greenBottomRight,
                minU,
                maxV);
            renderBlocks.colorGreenBottomRight = interpolate(
                greenTopLeft,
                greenTopRight,
                greenBottomLeft,
                greenBottomRight,
                maxU,
                maxV);
            renderBlocks.colorBlueTopLeft = interpolate(
                blueTopLeft,
                blueTopRight,
                blueBottomLeft,
                blueBottomRight,
                minU,
                minV);
            renderBlocks.colorBlueTopRight = interpolate(
                blueTopLeft,
                blueTopRight,
                blueBottomLeft,
                blueBottomRight,
                maxU,
                minV);
            renderBlocks.colorBlueBottomLeft = interpolate(
                blueTopLeft,
                blueTopRight,
                blueBottomLeft,
                blueBottomRight,
                minU,
                maxV);
            renderBlocks.colorBlueBottomRight = interpolate(
                blueTopLeft,
                blueTopRight,
                blueBottomLeft,
                blueBottomRight,
                maxU,
                maxV);
            renderBlocks.brightnessTopLeft = interpolateBrightness(
                brightnessTopLeft,
                brightnessTopRight,
                brightnessBottomLeft,
                brightnessBottomRight,
                minU,
                minV);
            renderBlocks.brightnessTopRight = interpolateBrightness(
                brightnessTopLeft,
                brightnessTopRight,
                brightnessBottomLeft,
                brightnessBottomRight,
                maxU,
                minV);
            renderBlocks.brightnessBottomLeft = interpolateBrightness(
                brightnessTopLeft,
                brightnessTopRight,
                brightnessBottomLeft,
                brightnessBottomRight,
                minU,
                maxV);
            renderBlocks.brightnessBottomRight = interpolateBrightness(
                brightnessTopLeft,
                brightnessTopRight,
                brightnessBottomLeft,
                brightnessBottomRight,
                maxU,
                maxV);
        }

        private void restore(RenderBlocks renderBlocks) {
            renderBlocks.colorRedTopLeft = redTopLeft;
            renderBlocks.colorRedTopRight = redTopRight;
            renderBlocks.colorRedBottomLeft = redBottomLeft;
            renderBlocks.colorRedBottomRight = redBottomRight;
            renderBlocks.colorGreenTopLeft = greenTopLeft;
            renderBlocks.colorGreenTopRight = greenTopRight;
            renderBlocks.colorGreenBottomLeft = greenBottomLeft;
            renderBlocks.colorGreenBottomRight = greenBottomRight;
            renderBlocks.colorBlueTopLeft = blueTopLeft;
            renderBlocks.colorBlueTopRight = blueTopRight;
            renderBlocks.colorBlueBottomLeft = blueBottomLeft;
            renderBlocks.colorBlueBottomRight = blueBottomRight;
            renderBlocks.brightnessTopLeft = brightnessTopLeft;
            renderBlocks.brightnessTopRight = brightnessTopRight;
            renderBlocks.brightnessBottomLeft = brightnessBottomLeft;
            renderBlocks.brightnessBottomRight = brightnessBottomRight;
        }

        private float interpolate(float topLeft, float topRight, float bottomLeft, float bottomRight, float u,
            float v) {
            float top = topLeft + (topRight - topLeft) * u;
            float bottom = bottomLeft + (bottomRight - bottomLeft) * u;
            return top + (bottom - top) * v;
        }

        private int interpolateBrightness(int topLeft, int topRight, int bottomLeft, int bottomRight, float u,
            float v) {
            int blockTopLeft = topLeft & 0xFFFF;
            int blockTopRight = topRight & 0xFFFF;
            int blockBottomLeft = bottomLeft & 0xFFFF;
            int blockBottomRight = bottomRight & 0xFFFF;
            int skyTopLeft = topLeft >>> 16;
            int skyTopRight = topRight >>> 16;
            int skyBottomLeft = bottomLeft >>> 16;
            int skyBottomRight = bottomRight >>> 16;
            int block = Math.round(interpolate(blockTopLeft, blockTopRight, blockBottomLeft, blockBottomRight, u, v));
            int sky = Math.round(interpolate(skyTopLeft, skyTopRight, skyBottomLeft, skyBottomRight, u, v));
            return sky << 16 | block & 0xFFFF;
        }
    }

    private static class CtmQuadrantIcon implements IIcon {

        private IIcon source;
        private double uOffset;
        private double vOffset;

        private IIcon setSource(IIcon source, double uOffset, double vOffset) {
            this.source = source;
            this.uOffset = uOffset;
            this.vOffset = vOffset;
            return this;
        }

        @Override
        public float getMinU() {
            return source.getMinU();
        }

        @Override
        public float getMaxU() {
            return source.getMaxU();
        }

        @Override
        public float getInterpolatedU(double value) {
            return source.getInterpolatedU((value - uOffset) * 2.0D);
        }

        @Override
        public float getMinV() {
            return source.getMinV();
        }

        @Override
        public float getMaxV() {
            return source.getMaxV();
        }

        @Override
        public float getInterpolatedV(double value) {
            return source.getInterpolatedV((value - vOffset) * 2.0D);
        }

        @Override
        public String getIconName() {
            return source.getIconName();
        }

        @Override
        public int getIconWidth() {
            return source.getIconWidth() * 2;
        }

        @Override
        public int getIconHeight() {
            return source.getIconHeight() * 2;
        }

    }
}

package com.github.wohaopa.MyCTMLib;

import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Provides CTM sub-icons derived from connection and fallback texture sheets.
 */
public class CTMIconManager {

    /**
     * Defines the neighborhood diameter used by a CTM layout.
     */
    public enum DetectionDiameter {

        DIAMETER_1(1),

        DIAMETER_3(3),

        DIAMETER_5(5);

        private final int value;

        DetectionDiameter(int value) {
            this.value = value;
        }

        /**
         * Returns the neighborhood diameter.
         *
         * @return the diameter value
         */
        public int getValue() {
            return value;
        }
    }

    // Stores generated sub-icons at their CTM indices.
    public IIcon[] icons = new CTMIcon[25];

    // Stores the regular 4 by 4 connection texture sheet.
    public IIcon iconCTM;

    // Stores the 2 by 2 fallback texture sheet.
    public IIcon iconSmall;

    // Stores the 2 by 2 alternate fallback texture sheet.
    public IIcon iconAlt;

    // Stores the ring texture sheet.
    public IIcon iconRing;

    // Stores the per-variant sprites of the connection texture.
    private IIcon[] iconVariants;

    // Maps every sub-icon index to the variant icon it belongs to, or null when the index is unused.
    private IIcon[] variantIcons = new IIcon[25];

    // Maps every sub-icon index to its quarter inside that variant, coded as left plus top times two.
    private int[] variantQuarters = new int[25];

    // Stores the active CTM neighborhood diameter.
    public DetectionDiameter detectionDiameter = DetectionDiameter.DIAMETER_1;

    /**
     * Creates an empty manager for the builder.
     */
    private CTMIconManager() {}

    /**
     * Creates a fallback-only manager for compatibility.
     *
     * @param iconSmall the source icon for the fallback sheet
     */
    public CTMIconManager(IIcon iconSmall) {
        this.iconSmall = iconSmall;
    }

    // Tracks whether sub-icons have been generated.
    private boolean inited = false;

    /**
     * Generates sub-icons from the configured texture sheets.
     */
    public void init() {

        if (iconVariants != null && iconVariants.length == 4) {
            // Build the connection sub-icons from the per-variant sprites of the sheet.
            for (int i = 1; i <= 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int column = i - 1;
                    int row = j;
                    IIcon parent = iconVariants[row / 2 * 2 + column / 2];
                    if (parent != null) {
                        setIcon(i + j * 4, new CTMIcon(parent, 2, 2, column % 2, row % 2));
                        setVariant(i + j * 4, parent, column % 2 + row % 2 * 2);
                    }
                }
            }
        } else if (iconCTM != null) {
            // Build the regular 4 by 4 connection sub-icons, one variant per two by two block of cells.
            for (int i = 1; i <= 4; i++) {
                for (int j = 0; j < 4; j++) {
                    setIcon(i + j * 4, new CTMIcon(iconCTM, 4, 4, i - 1, j));
                    setVariant(i + j * 4, new CTMIcon(iconCTM, 2, 2, (i - 1) / 2, j / 2), (i - 1) % 2 + j % 2 * 2);
                }
            }
        }

        if (iconSmall != null) {
            // Build the regular 2 by 2 fallback sub-icons, whose variant is the whole base texture.
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 16, new CTMIcon(iconSmall, 2, 2, i - 1, j));
                    setVariant(i + j * 2 + 16, iconSmall, i - 1 + j * 2);
                }
            }
        }

        // Build the alternate 2 by 2 fallback sub-icons.
        if (iconAlt != null) {
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 20, new CTMIcon(iconAlt, 2, 2, i - 1, j));
                    setVariant(i + j * 2 + 20, iconAlt, i - 1 + j * 2);
                }
            }
        }

        inited = true;
    }

    /**
     * Returns the icon covering the whole face when the four quadrant indices are the four quarters of one variant.
     * <p>
     * Rendering such a face as a single quad keeps its geometry identical to a vanilla face, which is what lets
     * coplanar overlay layers stay in front of it instead of fighting over depth.
     *
     * @param iconIndices the quadrant indices ordered top-left, top-right, bottom-left, bottom-right
     * @return the whole face icon, or null when the quadrants come from different variants
     */
    public IIcon getWholeFaceIcon(int[] iconIndices) {
        if (iconIndices == null || iconIndices.length < 4) {
            return null;
        }

        IIcon variant = getVariantIcon(iconIndices[0]);
        if (variant == null) {
            return null;
        }

        for (int quarter = 0; quarter < 4; quarter++) {
            int index = iconIndices[quarter];
            if (getVariantIcon(index) != variant || variantQuarters[index] != quarter) {
                return null;
            }
        }

        return variant;
    }

    private IIcon getVariantIcon(int index) {
        if (index <= 0 || index >= variantIcons.length) {
            return null;
        }
        return variantIcons[index];
    }

    private void setVariant(int index, IIcon variant, int quarter) {
        variantIcons[index] = variant;
        variantQuarters[index] = quarter;
    }

    /**
     * Returns a generated sub-icon.
     *
     * @param index the CTM sub-icon index
     * @return the requested sub-icon
     */
    public IIcon getIcon(int index) {
        if (index > 0 && index < 25) return icons[index];
        throw new RuntimeException("Invalid index: " + index);
    }

    private void setIcon(int index, IIcon icon) {
        icons[index] = icon;
    }

    /**
     * Returns whether sub-icons have been generated.
     *
     * @return whether this manager is initialized
     */
    public boolean hasInited() {
        return inited;
    }

    /**
     * Returns whether this manager owns a connection texture.
     *
     * @return whether a regular connection texture is available
     */
    public boolean hasConnectionTexture() {
        return iconCTM != null || iconVariants != null;
    }

    /**
     * Builds CTM icon managers.
     */
    public static class Builder {

        private final CTMIconManager manager;

        public Builder() {
            this.manager = new CTMIconManager();
        }

        /**
         * Sets the regular connection texture sheet.
         */
        public Builder setIconCTM(IIcon iconCTM) {
            manager.iconCTM = iconCTM;
            return this;
        }

        /**
         * Sets the per-variant sprites of the connection texture.
         */
        public Builder setIconVariants(IIcon[] iconVariants) {
            manager.iconVariants = iconVariants;
            return this;
        }

        /**
         * Sets the fallback texture sheet.
         */
        public Builder setIconSmall(IIcon iconSmall) {
            manager.iconSmall = iconSmall;
            return this;
        }

        /**
         * Sets the alternate fallback texture sheet.
         */
        public Builder setIconAlt(IIcon iconAlt) {
            manager.iconAlt = iconAlt;
            return this;
        }

        /**
         * Sets the ring texture sheet.
         */
        public Builder setIconRing(IIcon iconRing) {
            manager.iconRing = iconRing;
            return this;
        }

        /**
         * Sets the requested neighborhood diameter.
         */
        public Builder setDetectionDiameter(DetectionDiameter diameter) {
            manager.detectionDiameter = diameter;
            return this;
        }

        /**
         * Builds a manager.
         */
        public CTMIconManager build() {
            if (manager.iconSmall == null) {
                throw new IllegalStateException("iconSmall is required");
            }

            if (manager.iconCTM != null || manager.iconVariants != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_3;
            } else if (manager.iconRing != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_5;
            } else {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_1;
            }

            return manager;
        }

        /**
         * Builds and initializes a manager.
         */
        public CTMIconManager buildAndInit() {
            CTMIconManager result = build();
            result.init();
            return result;
        }
    }

    /**
     * Creates a manager builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Derives UV coordinates dynamically from a parent icon.
     */
    private static class CTMIcon implements IIcon {

        // Stores this sub-icon position in the source grid.
        private final int subTextureX;
        private final int subTextureY;

        // Stores the source grid dimensions.
        private final int gridWidth;
        private final int gridHeight;

        // Stores the parent icon.
        private final IIcon parentIcon;

        // Caches the resolved bounds once the parent icon has been stitched into the atlas.
        private boolean boundsResolved;
        private float minU;
        private float maxU;
        private float minV;
        private float maxV;

        /**
         * Creates a sub-icon view of a parent icon.
         *
         * @param parent the parent icon
         * @param w      the source grid width
         * @param h      the source grid height
         * @param x      the horizontal grid position
         * @param y      the vertical grid position
         */
        private CTMIcon(IIcon parent, int w, int h, int x, int y) {
            this.parentIcon = parent;
            this.gridWidth = w;
            this.gridHeight = h;
            this.subTextureX = x;
            this.subTextureY = y;

        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinU() {
            resolveBounds();
            return minU;
        }

        /**
         * Resolves and caches this sub-icon's atlas bounds.
         */
        private void resolveBounds() {
            if (boundsResolved) {
                return;
            }

            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            float parentMinV = parentIcon.getMinV();
            float parentMaxV = parentIcon.getMaxV();
            float spanU = parentMaxU - parentMinU;
            float spanV = parentMaxV - parentMinV;
            if (spanU <= 0.0F || spanV <= 0.0F) {
                // The parent is not stitched into the atlas yet, so its coordinates cannot be cached.
                minU = parentMinU;
                maxU = parentMaxU;
                minV = parentMinV;
                maxV = parentMaxV;
                return;
            }

            float insetU = getPixelInset(spanU, parentIcon.getIconWidth());
            float insetV = getPixelInset(spanV, parentIcon.getIconHeight());
            minU = parentMinU + spanU * subTextureX / gridWidth + insetU;
            maxU = parentMinU + spanU * (subTextureX + 1) / gridWidth - insetU;
            minV = parentMinV + spanV * subTextureY / gridHeight + insetV;
            maxV = parentMinV + spanV * (subTextureY + 1) / gridHeight - insetV;
            boundsResolved = true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            resolveBounds();
            return maxU;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedU(double d0) {
            resolveBounds();
            return (float) (minU + (maxU - minU) * d0 / 16.0);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinV() {
            resolveBounds();
            return minV;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            resolveBounds();
            return maxV;
        }

        private float getPixelInset(float span, int pixels) {
            return pixels > 0 ? span / pixels * 0.01F : 0.0F;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedV(double d0) {
            float subVmin = getMinV();
            float subVmax = getMaxV();
            return (float) (subVmin + (subVmax - subVmin) * d0 / 16.0);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getIconName() {
            return parentIcon.getIconName();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconWidth() {
            return parentIcon.getIconWidth() / gridWidth;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconHeight() {
            return parentIcon.getIconHeight() / gridHeight;
        }
    }

}

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

        if (iconCTM != null) {
            // Build the regular 4 by 4 connection sub-icons.
            for (int i = 1; i <= 4; i++) {
                for (int j = 0; j < 4; j++) {
                    setIcon(i + j * 4, new CTMIcon(iconCTM, 4, 4, i - 1, j));
                }
            }
        }

        if (iconSmall != null) {
            // Build the regular 2 by 2 fallback sub-icons.
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 16, new CTMIcon(iconSmall, 2, 2, i - 1, j));
                }
            }
        }

        // Build the alternate 2 by 2 fallback sub-icons.
        if (iconAlt != null) {
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    setIcon(i + j * 2 + 20, new CTMIcon(iconAlt, 2, 2, i - 1, j));
                }
            }
        }

        inited = true;
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
        return iconCTM != null;
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

            if (manager.iconCTM != null) {
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
            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            float rawMin = parentMinU + (parentMaxU - parentMinU) * subTextureX / gridWidth;
            return rawMin + getPixelInset(parentMaxU - parentMinU, parentIcon.getIconWidth());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            float rawMax = parentMinU + (parentMaxU - parentMinU) * (subTextureX + 1) / gridWidth;
            return rawMax - getPixelInset(parentMaxU - parentMinU, parentIcon.getIconWidth());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getInterpolatedU(double d0) {
            float subUmin = getMinU();
            float subUmax = getMaxU();
            return (float) (subUmin + (subUmax - subUmin) * d0 / 16.0);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinV() {
            float parentMinV = parentIcon.getMinV();
            float parentMaxV = parentIcon.getMaxV();
            float rawMin = parentMinV + (parentMaxV - parentMinV) * subTextureY / gridHeight;
            return rawMin + getPixelInset(parentMaxV - parentMinV, parentIcon.getIconHeight());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            float parentMinV = parentIcon.getMinV();
            float parentMaxV = parentIcon.getMaxV();
            float rawMax = parentMinV + (parentMaxV - parentMinV) * (subTextureY + 1) / gridHeight;
            return rawMax - getPixelInset(parentMaxV - parentMinV, parentIcon.getIconHeight());
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

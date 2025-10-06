package com.github.wohaopa.MyCTMLib;

import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 管理连接纹理（CTM）的图块图标，用于从主纹理图中裁切子区域以实现连纹渲染。
 * <p>
 * 支持 16 种常规图块连接组合（4x4）和 4 个小图标（2x2），共 20 个图标索引，索引从 1 到 20。
 * 下标为 0 的图标未使用。
 */
public class CTMIconManager {

    /**
     * 检测直径枚举，定义CTM纹理检测的范围大小。
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
         * 获取检测直径的数值。
         *
         * @return 直径值
         */
        public int getValue() {
            return value;
        }
    }

    // 存储所有子图块图标（索引 1 ~ 20）
    public IIcon[] icons = new CTMIcon[25];

    // 主纹理图（通常为 4x4）
    public IIcon iconCTM;

    // 小型图（通常为 2x2，用于特殊边角等）
    public IIcon iconSmall;

    // ctm专用小型图（通常为 2x2，用于特殊CTM等）
    public IIcon iconAlt;

    // 环形图（通常为 10x10，用于特殊CTM等）
    public IIcon iconRing;

    // 检测直径
    public DetectionDiameter detectionDiameter = DetectionDiameter.DIAMETER_1;

    /**
     * 私有构造函数，通过Builder创建实例
     */
    private CTMIconManager() {}

    /**
     * 构造函数 - 仅小图标（向后兼容）
     *
     * @param iconSmall 用于 2x2 图块裁切的图标
     */
    public CTMIconManager(IIcon iconSmall) {
        this.iconSmall = iconSmall;
    }

    // 是否初始化完成
    private boolean inited = false;

    /**
     * 初始化 CTM 图标，将 icon 和 iconSmall 切割成多个子图标。
     * 调用后才能使用 getIcon。
     */
    public void init() {
        // 构造主纹理的子图标：4x4 网格，索引从 1 到 16（注意：下标 0 未使用）
        for (int i = 1; i <= 4; i++) {
            for (int j = 0; j < 4; j++) {
                icons[i + j * 4] = new CTMIcon(iconCTM, 4, 4, i - 1, j);
            }
        }

        // 构造小图标：2x2 网格，索引从 17 到 20
        for (int i = 1; i <= 2; i++) {
            for (int j = 0; j < 2; j++) {
                icons[i + j * 2 + 16] = new CTMIcon(iconSmall, 2, 2, i - 1, j);
            }
        }

        // 构造CTM专用小图标：2x2 网格，索引从 21 到 25
        if (iconAlt != null) {
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < 2; j++) {
                    icons[i + j * 2 + 20] = new CTMIcon(iconAlt, 2, 2, i - 1, j);
                }
            }
        }

        inited = true;
    }

    /**
     * 获取子图标。
     *
     * @param index 图标索引，范围为 1 到 25
     * @return 指定子图标
     * @throws RuntimeException 索引非法时抛出异常
     */
    public IIcon getIcon(int index) {
        if (index > 0 && index < 25) return icons[index];
        throw new RuntimeException("Invalid index: " + index);
    }

    /**
     * 检查是否已初始化图标。
     *
     * @return 是否已初始化
     */
    public boolean hasInited() {
        return inited;
    }

    /**
     * CTMIconManager的Builder类
     */
    public static class Builder {

        private final CTMIconManager manager;

        public Builder() {
            this.manager = new CTMIconManager();
        }

        /**
         * 设置CTM主图标
         */
        public Builder setIconCTM(IIcon iconCTM) {
            manager.iconCTM = iconCTM;
            return this;
        }

        /**
         * 设置小图标
         */
        public Builder setIconSmall(IIcon iconSmall) {
            manager.iconSmall = iconSmall;
            return this;
        }

        /**
         * 设置ALt图标
         */
        public Builder setIconAlt(IIcon iconAlt) {
            manager.iconAlt = iconAlt;
            return this;
        }

        /**
         * 设置环形图标
         */
        public Builder setIconRing(IIcon iconRing) {
            manager.iconRing = iconRing;
            return this;
        }

        /**
         * 设置检测直径
         */
        public Builder setDetectionDiameter(DetectionDiameter diameter) {
            // 可以在这里添加检测直径相关的逻辑
            return this;
        }

        /**
         * 构建CTMIconManager实例
         */
        public CTMIconManager build() {
            // 验证必需的图标
            if (manager.iconSmall == null) {
                throw new IllegalStateException("iconSmall is required");
            }

            // 根据图标类型自动设置检测直径
            if (manager.iconCTM != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_3; // 3x3类型
            } else if (manager.iconRing != null) {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_5; // 5x5类型
            } else {
                manager.detectionDiameter = DetectionDiameter.DIAMETER_1; // 其余为1
            }

            return manager;
        }

        /**
         * 构建并自动初始化CTMIconManager实例
         */
        public CTMIconManager buildAndInit() {
            CTMIconManager result = build();
            result.init();
            return result;
        }
    }

    /**
     * 创建Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 子图标类，从父图标中动态计算 UV 坐标。
     * 支持动态纹理（例如动画帧）的实时 UV 获取。
     */
    private static class CTMIcon implements IIcon {

        // 父图标的总宽度、高度（像素）
        private final int totalWidth;
        private final int totalHeight;

        // 当前子图标在网格中的位置（X、Y）
        private final int subTextureX;
        private final int subTextureY;

        // 网格划分数（总列数、总行数）
        private final int gridWidth;
        private final int gridHeight;

        // 父图标
        private final IIcon parentIcon;

        /**
         * 构造一个子图标。
         *
         * @param parent 父图标
         * @param w      水平划分的格数
         * @param h      垂直划分的格数
         * @param x      当前子图在网格中的横向索引（从 0 开始）
         * @param y      当前子图在网格中的纵向索引（从 0 开始）
         */
        private CTMIcon(IIcon parent, int w, int h, int x, int y) {
            this.parentIcon = parent;
            this.gridWidth = w;
            this.gridHeight = h;
            this.subTextureX = x;
            this.subTextureY = y;

            this.totalWidth = parentIcon.getIconWidth();
            this.totalHeight = parentIcon.getIconHeight();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMinU() {
            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            return parentMinU + (parentMaxU - parentMinU) * (float) subTextureX / gridWidth;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxU() {
            float parentMinU = parentIcon.getMinU();
            float parentMaxU = parentIcon.getMaxU();
            return parentMinU + (parentMaxU - parentMinU) * (float) (subTextureX + 1) / gridWidth;
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
            return parentMinV + (parentMaxV - parentMinV) * (float) subTextureY / gridHeight;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public float getMaxV() {
            float parentMinV = parentIcon.getMinV();
            float parentMaxV = parentIcon.getMaxV();
            return parentMinV + (parentMaxV - parentMinV) * (float) (subTextureY + 1) / gridHeight;
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
            return totalWidth / gridWidth;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getIconHeight() {
            return totalHeight / gridHeight;
        }
    }
}

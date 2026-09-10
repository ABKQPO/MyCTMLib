package com.github.wohaopa.MyCTMLib;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.Loader;

public class Textures {

    public static Map<String, CTMIconManager> ctmIconMap = new ConcurrentHashMap<>();
    public static Map<String, String[]> ctmReplaceMap = new ConcurrentHashMap<>();
    public static Map<String, String> ctmAltMap = new ConcurrentHashMap<>();
    public static Map<String, List<CTMIconManager>> ctmRandomMap = new ConcurrentHashMap<>();

    public static final ForgeDirection[][] forgeDirections = new ForgeDirection[][] {
        { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST },
        { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST },
        { ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.DOWN, ForgeDirection.EAST },
        { ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.WEST },
        { ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.NORTH },
        { ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.SOUTH } };
    public static final ThreadLocal<int[]> threadLocalIconIdx = ThreadLocal.withInitial(() -> new int[4]);
    public static final ThreadLocal<boolean[]> threadLocalConnections = ThreadLocal.withInitial(() -> new boolean[8]);

    public static void clearTextureRegistrations() {
        ctmIconMap.clear();
        ctmReplaceMap.clear();
        ctmAltMap.clear();
        ctmRandomMap.clear();
    }

    public static boolean contain(String iconName) {
        return findConnectionManager(iconName) != null;
    }

    public static CTMIconManager findConnectionManager(IIcon icon) {
        return icon == null ? null : findConnectionManager(icon.getIconName());
    }

    public static CTMIconManager findConnectionManager(String iconName) {
        if (iconName == null) {
            return null;
        }
        CTMIconManager manager = ctmIconMap.get(normalizeIconName(iconName));
        return manager != null && manager.hasConnectionTexture() ? manager : null;
    }

    public static String normalizeIconName(String iconName) {
        int firstColon = iconName.indexOf(':');
        int secondColon = iconName.indexOf(':', firstColon + 1);
        if (secondColon == -1) {
            return iconName;
        }
        return iconName.substring(0, secondColon) + "&"
            + iconName.substring(secondColon + 1)
                .replace(':', '&');
    }

    public static boolean renderWorldBlock(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon iIcon, ForgeDirection forgeDirection) {
        CTMIconManager manager = findConnectionManager(iIcon);
        if (manager == null) {
            return false;
        }
        return renderWorldBlock(renderBlocks, blockAccess, block, x, y, z, iIcon, manager, forgeDirection);
    }

    public static boolean renderWorldBlock(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon iIcon, CTMIconManager manager, ForgeDirection forgeDirection) {
        if (renderBlocks == null || blockAccess == null
            || block == null
            || iIcon == null
            || manager == null
            || forgeDirection == null
            || !manager.hasConnectionTexture()) {
            return false;
        }

        String icon = normalizeIconName(iIcon.getIconName());
        int[] iconIdx = threadLocalIconIdx.get();

        if (manager.detectionDiameter == CTMIconManager.DetectionDiameter.DIAMETER_1) {
            iconIdx[0] = 17;
            iconIdx[1] = 18;
            iconIdx[2] = 19;
            iconIdx[3] = 20;
        } else {
            buildConnect(blockAccess, (int) x, (int) y, (int) z, iIcon, forgeDirection, iconIdx);
        }

        manager = selectTextureManager(blockAccess, (int) x, (int) y, (int) z, icon, manager);

        return CtmFaceRenderer.render(renderBlocks, block, x, y, z, manager, forgeDirection, iconIdx);
    }

    public static CTMIconManager selectTextureManager(IBlockAccess blockAccess, int x, int y, int z, String iconName,
        CTMIconManager primaryManager) {
        List<CTMIconManager> randomManagers = ctmRandomMap.get(normalizeIconName(iconName));
        if (randomManagers == null || randomManagers.isEmpty()) {
            return primaryManager;
        }

        long worldSeed = 0;
        if (blockAccess instanceof World world) {
            worldSeed = world.getSeed();
        }

        int randomIndex = FastRandom.getRandomIndex(worldSeed, x, y, z, randomManagers.size() + 1);
        return randomIndex < randomManagers.size() ? randomManagers.get(randomIndex) : primaryManager;
    }

    /**
     * Builds four connection-texture quadrant indices for a block face.
     * Connections 0 through 3 represent the cardinal neighbors and connections 4 through 7 represent diagonals.
     * The output is ordered top-left, top-right, bottom-left, bottom-right.
     */
    public static void buildConnect(IBlockAccess blockAccess, int x, int y, int z, IIcon iIcon,
        ForgeDirection forgeDirection, int[] iconIdxOut) {

        boolean[] connections = threadLocalConnections.get();
        ForgeDirection[] forgeDirections1 = forgeDirections[forgeDirection.ordinal()];

        for (int i = 0; i < 4; i++) {
            IIcon i2 = getIcon(
                blockAccess,
                x + forgeDirections1[i].offsetX,
                y + forgeDirections1[i].offsetY,
                z + forgeDirections1[i].offsetZ,
                forgeDirection);
            connections[i] = isIconMatch(i2, iIcon);
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
                connections[i] = isIconMatch(ic, iIcon);
            } else {
                connections[i] = false;
            }
        }

        boolean hasThird = ctmAltMap.containsKey(normalizeIconName(iIcon.getIconName()));

        if (connections[7]) {
            iconIdxOut[0] = 1;
        } else if (connections[3] && connections[0]) {
            iconIdxOut[0] = 11;
        } else if (connections[3]) {
            iconIdxOut[0] = 9;
        } else if (connections[0]) {
            iconIdxOut[0] = 3;
        } else {
            iconIdxOut[0] = 17;
        }

        if (connections[4]) {
            iconIdxOut[1] = 2;
        } else if (connections[0] && connections[1]) {
            iconIdxOut[1] = 12;
        } else if (connections[0]) {
            iconIdxOut[1] = 4;
        } else if (connections[1]) {
            iconIdxOut[1] = 10;
        } else {
            iconIdxOut[1] = 18;
        }

        if (connections[6]) {
            iconIdxOut[2] = 5;
        } else if (connections[2] && connections[3]) {
            iconIdxOut[2] = 15;
        } else if (connections[2]) {
            iconIdxOut[2] = 7;
        } else if (connections[3]) {
            iconIdxOut[2] = 13;
        } else {
            iconIdxOut[2] = 19;
        }

        if (connections[5]) {
            iconIdxOut[3] = 6;
        } else if (connections[1] && connections[2]) {
            iconIdxOut[3] = 16;
        } else if (connections[1]) {
            iconIdxOut[3] = 14;
        } else if (connections[2]) {
            iconIdxOut[3] = 8;
        } else {
            iconIdxOut[3] = 20;
        }

        boolean allDefault = true;
        for (int i = 0; i < 4; i++) {
            if (iconIdxOut[i] < 17 || iconIdxOut[i] > 20) {
                allDefault = false;
                break;
            }
        }

        if (!allDefault && hasThird) {
            for (int i = 0; i < 4; i++) {
                switch (iconIdxOut[i]) {
                    case 17 -> iconIdxOut[i] = 21;
                    case 18 -> iconIdxOut[i] = 22;
                    case 19 -> iconIdxOut[i] = 23;
                    case 20 -> iconIdxOut[i] = 24;
                }
            }
        }
    }

    public static boolean isIconMatch(IIcon target, IIcon candidate) {
        if (target == null || candidate == null) return false;

        String targetName = normalizeIconName(target.getIconName());
        String candidateName = normalizeIconName(candidate.getIconName());

        if (targetName.equals(candidateName)) return true;

        String[] targetGroup = ctmReplaceMap.get(targetName);
        if (targetGroup != null) {
            for (String v : targetGroup) {
                if (v.equals(candidateName)) return true;
            }
        }

        String[] candidateGroup = ctmReplaceMap.get(candidateName);
        if (candidateGroup != null) {
            for (String v : candidateGroup) {
                if (v.equals(targetName)) return true;
            }
        }

        return false;
    }

    public static IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection direction) {
        Block block = blockAccess.getBlock(x, y, z);
        if (block == null || block instanceof BlockAir) return null;

        if (Loader.isModLoaded("gregtech")) {
            try {
                return GTNHIntegrationHelper.getIcon(blockAccess, x, y, z, direction);
            } catch (Throwable t) {
                return null;
            }
        }

        return block.getIcon(blockAccess, x, y, z, direction.ordinal());
    }
}

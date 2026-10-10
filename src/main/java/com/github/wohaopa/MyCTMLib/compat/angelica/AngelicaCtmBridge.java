package com.github.wohaopa.MyCTMLib.compat.angelica;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.github.wohaopa.MyCTMLib.core.Mods;
import com.prupe.mcpatcher.ctm.CTMUtils;

import cpw.mods.fml.common.Optional;

public class AngelicaCtmBridge {

    private static final ThreadLocal<boolean[]> yielding = ThreadLocal.withInitial(() -> new boolean[1]);

    public static boolean isYielding() {
        return yielding.get()[0];
    }

    public static IIcon resolveIcon(IIcon icon, Block block, IBlockAccess blockAccess, int x, int y, int z, int face) {
        if (!Mods.Angelica.isModLoaded() || icon == null || block == null || blockAccess == null) {
            return icon;
        }

        boolean[] flag = yielding.get();
        if (flag[0]) {
            return icon;
        }
        flag[0] = true;
        try {
            return askAngelica(icon, block, blockAccess, x, y, z, face);
        } finally {
            flag[0] = false;
        }
    }

    public static void dropQueuedFace() {
        if (Mods.Angelica.isModLoaded()) {
            clearAngelicaQueue();
        }
    }

    @Optional.Method(modid = "angelica")
    private static IIcon askAngelica(IIcon icon, Block block, IBlockAccess blockAccess, int x, int y, int z, int face) {
        return CTMUtils.getBlockIcon(icon, block, blockAccess, x, y, z, face);
    }

    @Optional.Method(modid = "angelica")
    private static void clearAngelicaQueue() {
        CTMUtils.clearCurrentCompact();
    }
}

package com.github.wohaopa.MyCTMLib;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ITexturedTileEntity;
import gregtech.common.blocks.BlockMachines;
import gregtech.common.render.GTCopiedBlockTextureRender;

public class GTNHIntegrationHelper {

    public static Tessellator getGTNHLibTessellator() {
        return TessellatorManager.get();
    }

    public static IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection forgeDirection) {
        Block block = blockAccess.getBlock(x, y, z);
        if (block instanceof BlockAir) return null;
        if (block instanceof BlockMachines) {
            TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
            if (tileEntity instanceof ITexturedTileEntity texturedTileEntity) {
                ITexture[] iTextures = texturedTileEntity.getTexture(block, forgeDirection);
                for (ITexture texture : iTextures) {
                    if (texture instanceof GTCopiedBlockTextureRender gtCopiedBlockTextureRender) {
                        return gtCopiedBlockTextureRender.getBlock()
                            .getIcon(forgeDirection.ordinal(), (int) gtCopiedBlockTextureRender.getMeta());
                    }
                }
            }
        } else {
            return block.getIcon(blockAccess, x, y, z, forgeDirection.ordinal());
        }
        return null;
    }
}

package com.github.wohaopa.MyCTMLib;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.mixins.AccessorGTRenderedTexture;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;

import gregtech.api.interfaces.IBlockWithClientMeta;
import gregtech.api.interfaces.IBlockWithTextures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ITexturedTileEntity;
import gregtech.common.blocks.BlockCasings5;
import gregtech.common.blocks.BlockMachines;
import gregtech.common.render.GTCopiedBlockTextureRender;
import gregtech.common.render.GTRenderedTexture;

public class GTNHIntegrationHelper {

    public static Tessellator getGTNHLibTessellator() {
        return TessellatorManager.get();
    }

    public static IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection forgeDirection) {
        Block block = blockAccess.getBlock(x, y, z);

        int blockMetadata;

        if (block instanceof IBlockWithClientMeta clientMetaBlock) {
            World world = Minecraft.getMinecraft().theWorld;
            blockMetadata = clientMetaBlock.getClientMeta(world, x, y, z);
        } else {
            blockMetadata = blockAccess.getBlockMetadata(x, y, z);
        }

        if (block instanceof IBlockWithTextures texturedBlock) {
            ITexture[][] textures = texturedBlock.getTextures(blockMetadata);
            if (textures != null && forgeDirection.ordinal() < textures.length) {
                ITexture[] sideTextures = textures[forgeDirection.ordinal()];
                if (sideTextures != null) {
                    int textureIndex = 0;
                    if (block instanceof BlockCasings5 && blockMetadata >= 16) {
                        textureIndex = sideTextures.length > 1 ? 1 : 0;
                    }
                    if (sideTextures.length > textureIndex) {
                        ITexture selectedTexture = sideTextures[textureIndex];

                        if (selectedTexture instanceof GTCopiedBlockTextureRender gtCopiedBlockTextureRender) {
                            return gtCopiedBlockTextureRender.getBlock()
                                .getIcon(forgeDirection.ordinal(), blockMetadata);
                        } else if (selectedTexture instanceof GTRenderedTexture gtRenderedTexture) {
                            IIconContainer container = ((AccessorGTRenderedTexture) gtRenderedTexture)
                                .getIconContainer();
                            if (container != null) {
                                return container.getIcon();
                            }
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
}

package com.github.wohaopa.MyCTMLib.compat.gregtech;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.mixins.late.AccessorGTMultiTextureRender;
import com.github.wohaopa.MyCTMLib.mixins.late.AccessorGTSidedTextureRender;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IBlockContainer;
import gregtech.api.interfaces.IBlockWithClientMeta;
import gregtech.api.interfaces.IBlockWithTextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ITexturedTileEntity;
import gregtech.common.blocks.BlockCasings5;
import gregtech.common.blocks.BlockMachines;
import gregtech.common.render.GTMultiTextureRender;
import gregtech.common.render.GTSidedTextureRender;
import gregtech.common.render.IIconTexture;

public class GTNHIntegrationHelper {

    private static final int MAX_WRAPPER_DEPTH = 8;

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
                // Active coils keep their connection texture on the foreground layer, every other block on the base.
                int layer = block instanceof BlockCasings5 && blockMetadata >= 16 ? 1 : 0;
                IIcon icon = resolveLayerIcon(textures[forgeDirection.ordinal()], layer, forgeDirection);
                if (icon != null) {
                    return icon;
                }
            }
        }

        if (block instanceof BlockMachines) {
            TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
            if (tileEntity instanceof ITexturedTileEntity texturedTileEntity) {
                return resolveLayerIcon(texturedTileEntity.getTexture(block, forgeDirection), 0, forgeDirection);
            }
            return null;
        }

        return block.getIcon(blockAccess, x, y, z, forgeDirection.ordinal());
    }

    public static IIcon resolveTextureIcon(ITexture texture, ForgeDirection side) {
        return resolveIcon(texture, side, 0);
    }

    private static IIcon resolveLayerIcon(ITexture[] layers, int preferredLayer, ForgeDirection side) {
        if (layers == null || layers.length == 0) {
            return null;
        }
        int index = preferredLayer < layers.length ? preferredLayer : 0;
        return resolveIcon(layers[index], side, 0);
    }

    private static IIcon resolveIcon(ITexture texture, ForgeDirection side, int depth) {
        if (texture == null || depth > MAX_WRAPPER_DEPTH) {
            return null;
        }

        if (texture instanceof GTSidedTextureRender sidedTexture) {
            ITexture[] sides = ((AccessorGTSidedTextureRender) sidedTexture).getTextures();
            if (sides == null || side.ordinal() >= sides.length) {
                return null;
            }
            return resolveIcon(sides[side.ordinal()], side, depth + 1);
        }

        if (texture instanceof GTMultiTextureRender multiTexture) {
            ITexture[] layers = ((AccessorGTMultiTextureRender) multiTexture).getTextures();
            if (layers == null) {
                return null;
            }
            for (ITexture layer : layers) {
                IIcon icon = resolveIcon(layer, side, depth + 1);
                if (icon != null) {
                    return icon;
                }
            }
            return null;
        }

        // Covers both GTRenderedTexture and GTCopiedBlockTextureRender, and any subclass of either. A null context
        // keeps GregTech from re-entering the mcpatcher lookup, so this stays the raw atlas icon.
        if (texture instanceof IIconTexture iconTexture) {
            IIcon icon = iconTexture.getIcon(side.ordinal(), null);
            return icon == Textures.InvisibleIcon.INVISIBLE_ICON ? null : icon;
        }

        // Textures that only expose the block they copy, such as the mcpatcher backed casing texture.
        if (texture instanceof IBlockContainer blockContainer) {
            Block copiedBlock = blockContainer.getBlock();
            return copiedBlock == null ? null : copiedBlock.getIcon(side.ordinal(), blockContainer.getMeta());
        }

        return null;
    }
}

package com.github.wohaopa.MyCTMLib.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 新管线渲染入口：在 MixinRenderBlocks 六面 HEAD 处调用。
 * 优先查 BlockStateRegistry → ModelRegistry 得该面纹理与谓词；若无匹配，再查 TextureRegistry(iconName)。
 */
@SideOnly(Side.CLIENT)
public final class CTMRenderEntry {

    /**
     * 尝试用新管线渲染该面。若应由新管线处理则绘制并返回 true，否则返回 false。
     */
    public static boolean tryRender(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon icon, ForgeDirection face) {
        if (blockAccess == null || icon == null) return false;
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        int meta = blockAccess.getBlockMetadata((int) x, (int) y, (int) z);
        String blockId = getBlockId(block);

        // 优先 Model 分支：blockId + meta → BlockStateRegistry → ModelRegistry
        if (blockId != null) {
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (modelId != null) {
                ModelData modelData = ModelRegistry.getInstance()
                    .get(modelId);
                if (modelData != null) {
                    List<ModelElement> elements = getElementsWithFace(modelData, face);
                    if (!elements.isEmpty()) {
                        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
                        ModelFace firstFace = elements.get(0)
                            .getFace(face);
                        if (firstFace != null && firstFace.getConnectionKey() != null) {
                            ConnectionPredicate p = PredicateRegistry
                                .getPredicate(firstFace.getConnectionKey(), modelData.getConnections());
                            if (p != null) predicate = p;
                        }
                        int mask = ConnectionState
                            .computeMask(blockAccess, (int) x, (int) y, (int) z, face, block, meta, predicate);
                        int brightness = block.getMixedBrightnessForBlock(blockAccess, (int) x, (int) y, (int) z);
                        String domain = modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':'))
                            : "minecraft";
                        boolean drewAny = false;
                        for (ModelElement el : elements) {
                            ModelFace faceData = el.getFace(face);
                            if (faceData == null) continue;
                            String textureKey = faceData.getTextureKey();
                            if (textureKey == null) continue;
                            String texturePath = resolveTexturePath(textureKey, modelData.getTextures());
                            if (texturePath == null) continue;
                            String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                            TextureTypeData data = getConnectingData(textureLookupKey);
                            if (!(data instanceof ConnectingTextureData texData)) continue;
                            ConnectingLayout layout = texData.getLayout();
                            LayoutHandler handler = LayoutHandlers.get(layout);
                            int[] pos = handler.getTilePosition(mask);
                            float[] f = el.getFrom(), t = el.getTo();
                            double relMinX = Math.min(f[0], t[0]) / 16.0;
                            double relMaxX = Math.max(f[0], t[0]) / 16.0;
                            double relMinY = Math.min(f[1], t[1]) / 16.0;
                            double relMaxY = Math.max(f[1], t[1]) / 16.0;
                            double relMinZ = Math.min(f[2], t[2]) / 16.0;
                            double relMaxZ = Math.max(f[2], t[2]) / 16.0;
                            FaceRenderer.drawFace(
                                renderBlocks,
                                x,
                                y,
                                z,
                                face,
                                icon,
                                pos[0],
                                pos[1],
                                handler.getWidth(),
                                handler.getHeight(),
                                brightness,
                                relMinX,
                                relMaxX,
                                relMinY,
                                relMaxY,
                                relMinZ,
                                relMaxZ);
                            drewAny = true;
                        }
                        if (drewAny) return true;
                    }
                }
            }
        }

        // 回退到 TextureRegistry(iconName)
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData ctd)) return false;
        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int mask = ConnectionState.computeMask(blockAccess, (int) x, (int) y, (int) z, face, block, meta, predicate);
        int[] pos = handler.getTilePosition(mask);
        int brightness = block.getMixedBrightnessForBlock(blockAccess, (int) x, (int) y, (int) z);
        FaceRenderer.drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
            icon,
            pos[0],
            pos[1],
            handler.getWidth(),
            handler.getHeight(),
            brightness);
        return true;
    }

    /**
     * 获取单面渲染管线信息（仅查询，不绘制）。用于 Debug HUD。
     */
    public static PipelineInfo getPipelineInfo(IBlockAccess blockAccess, Block block, int x, int y, int z,
        ForgeDirection face) {
        if (blockAccess == null || block == null) {
            return PipelineInfo.vanilla("", "blockAccess=null");
        }
        int meta = blockAccess.getBlockMetadata(x, y, z);
        IIcon icon;
        try {
            icon = block.getIcon(face.ordinal(), meta);
        } catch (Throwable t) {
            return PipelineInfo.vanilla("", "getIcon failed");
        }
        if (icon == null) {
            return PipelineInfo.vanilla("", "icon=null");
        }
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        String blockId = getBlockId(block);

        // Model 分支
        if (blockId != null) {
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (modelId == null) {
                // 继续往下，但记录 skip
            } else {
                ModelData modelData = ModelRegistry.getInstance()
                    .get(modelId);
                if (modelData != null) {
                    List<ModelElement> elements = getElementsWithFace(modelData, face);
                    if (!elements.isEmpty()) {
                        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
                        ModelFace firstFace = elements.get(0)
                            .getFace(face);
                        if (firstFace != null && firstFace.getConnectionKey() != null) {
                            ConnectionPredicate p = PredicateRegistry
                                .getPredicate(firstFace.getConnectionKey(), modelData.getConnections());
                            if (p != null) predicate = p;
                        }
                        int mask = ConnectionState.computeMask(blockAccess, x, y, z, face, block, meta, predicate);
                        String domain = modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':'))
                            : "minecraft";
                        for (ModelElement el : elements) {
                            ModelFace faceData = el.getFace(face);
                            if (faceData == null) continue;
                            String textureKey = faceData.getTextureKey();
                            if (textureKey == null) continue;
                            String texturePath = resolveTexturePath(textureKey, modelData.getTextures());
                            if (texturePath == null) continue;
                            String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                            TextureTypeData data = getConnectingData(textureLookupKey);
                            if (data instanceof ConnectingTextureData texData) {
                                return PipelineInfo.model(iconName, modelId, textureKey, texData.getLayout(), mask);
                            }
                        }
                        return PipelineInfo.vanilla(iconName, "no ConnectingTextureData (Model)");
                    }
                }
            }
        }

        // TextureRegistry 分支
        TextureTypeData data = getConnectingData(iconName);
        if (data instanceof ConnectingTextureData ctd) {
            ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
            int mask = ConnectionState.computeMask(blockAccess, x, y, z, face, block, meta, predicate);
            return PipelineInfo.textureRegistry(iconName, ctd.getLayout(), mask);
        }

        // Legacy
        if (Textures.contain(iconName)) {
            return PipelineInfo.legacy(iconName);
        }

        // Vanilla
        return PipelineInfo.vanilla(iconName, "no Model/TexReg/Legacy");
    }

    private static String getBlockId(Block block) {
        Iterator<?> it = Block.blockRegistry.getKeys()
            .iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if (key instanceof String && Block.blockRegistry.getObject(key) == block) {
                return (String) key;
            }
        }
        return null;
    }

    private static List<ModelElement> getElementsWithFace(ModelData modelData, ForgeDirection face) {
        List<ModelElement> out = new ArrayList<>();
        for (ModelElement el : modelData.getElements()) {
            if (el.getFace(face) != null) out.add(el);
        }
        return out;
    }

    private static String resolveTexturePath(String key, Map<String, String> textures) {
        if (key == null || textures == null) return null;
        String lookupKey = key.startsWith("#") ? key.substring(1)
            .trim() : key;
        String v = textures.get(lookupKey);
        if (v != null && v.startsWith("#")) return resolveTexturePath(
            v.substring(1)
                .trim(),
            textures);
        return v;
    }

    /**
     * 从 TextureRegistry 查找 ConnectingTextureData。使用 TextureKeyNormalizer 多键回退，
     * 兼容 1.7.10 短名（cobblestone）、模型路径（minecraft:block/cobblestone）等格式。
     */
    private static TextureTypeData getConnectingData(String key) {
        return TextureRegistry.getInstance()
            .get(key);
    }

    /**
     * 物品渲染通道：blockAccess 为 null 时使用。以无连接（默认格 0,0）渲染连接纹理。
     * 用于手持、背包 GUI、物品栏等场景，避免显示整张连接图。
     */
    public static boolean tryRenderItemFace(RenderBlocks renderBlocks, Block block, double x, double y, double z,
        IIcon icon, ForgeDirection face) {
        if (icon == null) return false;
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData ctd)) return false;

        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int tileX = 0;
        int tileY = 0;
        int brightness = 15728880;
        FaceRenderer.drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
            icon,
            tileX,
            tileY,
            handler.getWidth(),
            handler.getHeight(),
            brightness);
        return true;
    }

}

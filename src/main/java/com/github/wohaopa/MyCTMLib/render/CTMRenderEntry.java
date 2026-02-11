package com.github.wohaopa.MyCTMLib.render;

import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 新管线渲染入口：在 MixinRenderBlocks 六面 HEAD 处调用。
 * 先查 TextureRegistry（iconName）；若无则查 BlockStateRegistry → ModelRegistry 得该面纹理与谓词，再查 TextureRegistry 取 layout，算连接并绘制。
 */
@SideOnly(Side.CLIENT)
public final class CTMRenderEntry {

    /**
     * 尝试用新管线渲染该面。若应由新管线处理则绘制并返回 true，否则返回 false。
     */
    public static boolean tryRender(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon icon, ForgeDirection face) {
        if (blockAccess == null || icon == null) return false;
        String iconName = normalizeIconName(icon.getIconName());
        int meta = blockAccess.getBlockMetadata((int) x, (int) y, (int) z);
        boolean traceTarget = MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(iconName);

        TextureTypeData data = getConnectingData(iconName);
        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
        ConnectingTextureData ctd = null;
        IIcon useIcon = icon;

        if (data instanceof ConnectingTextureData) {
            ctd = (ConnectingTextureData) data;
            if (traceTarget) {
                MyCTMLib.LOG.info("[CTMLibFusion] tryRender branch=TextureRegistry iconName={}", iconName);
                logTryRenderTable(iconName, getBlockId(block), true, "TextureRegistry", null, "draw");
            }
        } else {
            String blockId = getBlockId(block);
            if (!traceTarget && blockId != null && MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(blockId)) {
                traceTarget = true;
            }
            if (blockId == null) {
                if (traceTarget) {
                    MyCTMLib.LOG.info("[CTMLibFusion] tryRender skip: blockId=null for block={}", block);
                    logTryRenderTable(iconName, null, false, "skip", null, "skip:blockId=null");
                }
                return false;
            }
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (modelId == null) {
                if (traceTarget) {
                    MyCTMLib.LOG.info("[CTMLibFusion] tryRender skip: modelId=null blockId={} meta={}", blockId, meta);
                    logTryRenderTable(iconName, blockId, false, "skip", null, "skip:modelId=null");
                }
                return false;
            }
            ModelData modelData = ModelRegistry.getInstance()
                .get(modelId);
            if (modelData == null) {
                if (traceTarget) {
                    MyCTMLib.LOG.info("[CTMLibFusion] tryRender skip: modelData=null modelId={}", modelId);
                    logTryRenderTable(iconName, blockId, false, "skip", modelId, "skip:modelData=null");
                }
                return false;
            }
            ModelFace faceData = getFaceForDirection(modelData, face);
            if (faceData == null) return false;
            String textureKey = faceData.getTextureKey();
            if (textureKey == null) return false;
            String texturePath = resolveTexturePath(textureKey, modelData.getTextures());
            if (texturePath == null) return false;
            String domain = modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':')) : "minecraft";
            String fullIconPath = toBlockIconPath(domain, texturePath);
            data = getConnectingData(fullIconPath);
            if (!(data instanceof ConnectingTextureData)) {
                if (traceTarget) {
                    MyCTMLib.LOG.info(
                        "[CTMLibFusion] tryRender branch=BlockState blockId={} modelId={} fullIconPath={} ctmlibFound={}",
                        blockId,
                        modelId,
                        fullIconPath,
                        data != null);
                    logTryRenderTable(iconName, blockId, false, "BlockState", modelId, "skip:ctmlibNotFound");
                }
                return false;
            }
            ctd = (ConnectingTextureData) data;
            predicate = faceData.getConnectionKey() != null
                ? PredicateRegistry.getPredicate(faceData.getConnectionKey(), modelData.getConnections())
                : PredicateRegistry.defaultPredicate();
            if (predicate == null) predicate = PredicateRegistry.defaultPredicate();
            useIcon = icon;
            if (traceTarget) {
                MyCTMLib.LOG.info(
                    "[CTMLibFusion] tryRender branch=BlockState blockId={} modelId={} fullIconPath={}",
                    blockId,
                    modelId,
                    fullIconPath);
                logTryRenderTable(iconName, blockId, false, "BlockState", modelId, "draw");
            }
        }

        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int mask = ConnectionState.computeMask(blockAccess, (int) x, (int) y, (int) z, face, block, meta, predicate);
        int[] pos = handler.getTilePosition(mask);
        int tileX = pos[0], tileY = pos[1];
        int brightness = block.getMixedBrightnessForBlock(blockAccess, (int) x, (int) y, (int) z);
        Tessellator.instance.setBrightness(brightness);
        Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        FaceRenderer
            .drawFace(renderBlocks, x, y, z, face, useIcon, tileX, tileY, handler.getWidth(), handler.getHeight());
        return true;
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

    private static ModelFace getFaceForDirection(ModelData modelData, ForgeDirection face) {
        for (ModelElement el : modelData.getElements()) {
            ModelFace f = el.getFace(face);
            if (f != null) return f;
        }
        return null;
    }

    private static String resolveTexturePath(String key, Map<String, String> textures) {
        if (key == null || textures == null) return null;
        String v = textures.get(key);
        if (v != null && v.startsWith("#")) return resolveTexturePath(
            v.substring(1)
                .trim(),
            textures);
        return v;
    }

    private static String toBlockIconPath(String domain, String path) {
        if (path == null) return null;
        if (path.contains(":")) return path;
        String p = path.replace("block/", "blocks/");
        return domain + ":" + p;
    }

    /**
     * 从 TextureRegistry 查找 ConnectingTextureData。1.7.10 注册时 textureName 可能为短名（如 "stone"），
     * 查找时可能为 "minecraft:stone"，故先查 key 再查 path 部分。
     */
    private static TextureTypeData getConnectingData(String key) {
        TextureTypeData data = TextureRegistry.getInstance()
            .get(key);
        if (data == null && key != null && key.indexOf(':') >= 0) {
            data = TextureRegistry.getInstance()
                .get(key.substring(key.indexOf(':') + 1));
        }
        return data;
    }

    /** 仅对 stone/cobblestone 打表：一行汇总 icon / blockId / ctdFromIcon / branch / modelId / result */
    private static void logTryRenderTable(String iconName, String blockId, boolean ctdFromIcon, String branch,
        String modelId, String result) {
        MyCTMLib.LOG.info(
            "[CTMLibFusion] tryRender | icon={} blockId={} ctdFromIcon={} branch={} modelId={} result={}",
            iconName,
            blockId != null ? blockId : "-",
            ctdFromIcon,
            branch,
            modelId != null ? modelId : "-",
            result);
    }

    /** 与 MixinRenderBlocks 中 iconName 处理一致：第二个冒号及之后替换为 & */
    private static String normalizeIconName(String name) {
        if (name == null) return "";
        int first = name.indexOf(':');
        int second = name.indexOf(':', first + 1);
        if (second != -1) {
            return name.substring(0, second) + "&"
                + name.substring(second + 1)
                    .replace(":", "&");
        }
        return name;
    }
}

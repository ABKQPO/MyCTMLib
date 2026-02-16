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

import com.github.wohaopa.MyCTMLib.MyCTMLib;
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
        String iconName = normalizeIconName(icon.getIconName());
        int meta = blockAccess.getBlockMetadata((int) x, (int) y, (int) z);
        boolean traceTarget = MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(iconName);
        String blockId = getBlockId(block);
        if (!traceTarget && blockId != null && MyCTMLib.debugMode && MyCTMLib.isFusionTraceTarget(blockId)) {
            traceTarget = true;
        }

        // 优先 Model 分支：blockId + meta → BlockStateRegistry → ModelRegistry
        if (blockId != null) {
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (traceTarget && modelId == null) {
                MyCTMLib.LOG.info(
                    "[CTMLibFusion] tryRender Model skip | blockId={} meta={} modelId=null (BlockStateRegistry)",
                    blockId,
                    meta);
            }
            if (modelId != null) {
                ModelData modelData = ModelRegistry.getInstance()
                    .get(modelId);
                if (traceTarget && modelData == null) {
                    MyCTMLib.LOG.info(
                        "[CTMLibFusion] tryRender Model skip | blockId={} modelId={} modelData=null (ModelRegistry)",
                        blockId,
                        modelId);
                }
                if (modelData != null) {
                    List<ModelElement> elements = getElementsWithFace(modelData, face);
                    if (traceTarget && elements.isEmpty()) {
                        MyCTMLib.LOG.info(
                            "[CTMLibFusion] tryRender Model skip | blockId={} modelId={} face={} elements=0 (no face)",
                            blockId,
                            modelId,
                            face);
                    }
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
                        int skippedCount = 0;
                        for (ModelElement el : elements) {
                            ModelFace faceData = el.getFace(face);
                            if (faceData == null) {
                                if (traceTarget) skippedCount++;
                                continue;
                            }
                            String textureKey = faceData.getTextureKey();
                            if (textureKey == null) {
                                if (traceTarget) {
                                    MyCTMLib.LOG.info(
                                        "[CTMLibFusion] tryRender Model element skip | textureKey=null face={}",
                                        face);
                                    skippedCount++;
                                }
                                continue;
                            }
                            String texturePath = resolveTexturePath(textureKey, modelData.getTextures());
                            if (texturePath == null) {
                                if (traceTarget) {
                                    MyCTMLib.LOG.info(
                                        "[CTMLibFusion] tryRender Model element skip | textureKey={} texturePath=null (unresolved)",
                                        textureKey);
                                    skippedCount++;
                                }
                                continue;
                            }
                            String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                            TextureTypeData data = getConnectingData(textureLookupKey);
                            if (!(data instanceof ConnectingTextureData texData)) {
                                if (traceTarget) {
                                    String dataType = data != null ? data.getClass()
                                        .getSimpleName() : "null";
                                    MyCTMLib.LOG.info(
                                        "[CTMLibFusion] tryRender Model element skip | textureKey={} texturePath={} textureLookupKey={} TextureRegistry data={} (need ConnectingTextureData)",
                                        textureKey,
                                        texturePath,
                                        textureLookupKey,
                                        dataType);
                                    skippedCount++;
                                }
                                continue;
                            }
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
                        if (drewAny) {
                            if (traceTarget) {
                                MyCTMLib.LOG.info(
                                    "[CTMLibFusion] tryRender branch=Model blockId={} modelId={} face={} elements={} mask={}",
                                    blockId,
                                    modelId,
                                    face,
                                    elements.size(),
                                    mask);
                                logTryRenderTable(iconName, blockId, false, "Model", modelId, "draw");
                            }
                            return true;
                        }
                        if (traceTarget) {
                            MyCTMLib.LOG.info(
                                "[CTMLibFusion] tryRender Model skip | blockId={} modelId={} face={} elements={} drewAny=false skipped={} (all elements skipped)",
                                blockId,
                                modelId,
                                face,
                                elements.size(),
                                skippedCount);
                        }
                    }
                }
            }
        } else if (traceTarget) {
            MyCTMLib.LOG.info("[CTMLibFusion] tryRender Model skip | blockId=null (block not in registry)");
        }

        // 回退到 TextureRegistry(iconName)
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData ctd)) {
            if (traceTarget) {
                String dataType = data != null ? data.getClass()
                    .getSimpleName() : "null";
                MyCTMLib.LOG.info(
                    "[CTMLibFusion] tryRender TextureRegistry skip | iconName={} data={} (need ConnectingTextureData), fallback to vanilla",
                    iconName,
                    dataType);
            }
            return false;
        }
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
        if (traceTarget) {
            MyCTMLib.LOG.info(
                "[CTMLibFusion] tryRender branch=TextureRegistry iconName={} face={} mask={}",
                iconName,
                face,
                mask);
            logTryRenderTable(iconName, blockId, true, "TextureRegistry", null, "draw");
        }
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

    /** 仅对 stone/cobblestone 打表：一行汇总 icon / blockId / ctdFromIcon / branch / modelId / result / face */
    private static void logTryRenderTable(String iconName, String blockId, boolean ctdFromIcon, String branch,
        String modelId, String result) {
        MyCTMLib.LOG.info(
            "[CTMLibFusion] tryRender OK | icon={} blockId={} ctdFromIcon={} branch={} modelId={} result={}",
            iconName,
            blockId != null ? blockId : "-",
            ctdFromIcon,
            branch,
            modelId != null ? modelId : "-",
            result);
    }

    /**
     * 物品渲染通道：blockAccess 为 null 时使用。以无连接（默认格 0,0）渲染连接纹理。
     * 用于手持、背包 GUI、物品栏等场景，避免显示整张连接图。
     */
    public static boolean tryRenderItemFace(RenderBlocks renderBlocks, Block block, double x, double y, double z,
        IIcon icon, ForgeDirection face) {
        if (icon == null) return false;
        String iconName = normalizeIconName(icon.getIconName());
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

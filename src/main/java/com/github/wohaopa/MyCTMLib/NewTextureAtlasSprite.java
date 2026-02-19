package com.github.wohaopa.MyCTMLib;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 用于 ctmlib 连接纹理的非方形图（如 128×96）进图集。
 * 通过 hasCustomLoader + load() 绕过原版 loadSprite 的 "broken aspect ratio" 检查。
 */
@SideOnly(Side.CLIENT)
public class NewTextureAtlasSprite extends TextureAtlasSprite {

    public NewTextureAtlasSprite(String name) {
        super(name);
    }

    /**
     * 仅当该纹理在 TextureRegistry 中为 ConnectingTextureData 时使用自定义加载（与 CTMRenderEntry 查表规则一致）。
     */
    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        String key = toTextureKey(location);
        TextureTypeData data = getConnectingData(key);
        return data instanceof ConnectingTextureData;
    }

    /**
     * 自行加载 PNG（允许非方形），填好宽高与 frame 数据，返回 false 以参与 stitch。
     *
     * <h2>资源路径构建说明</h2>
     *
     * <h3>调用流程</h3>
     * <ol>
     *   <li>{@code TextureMap.loadTextureAtlas()} 从 {@code mapRegisteredSprites} 获取 entry</li>
     *   <li>{@code entry.getKey()} 是 textureName，如 {@code "gregtech:iconsets/MACHINE_CASING_LASER"}</li>
     *   <li>构建 {@code ResourceLocation resourcelocation = new ResourceLocation(textureName)}</li>
     *   <li>调用 {@code hasCustomLoader(manager, resourcelocation)} 和 {@code load(manager, resourcelocation)}</li>
     *   <li><strong>注意</strong>：传入的是原始的 {@code resourcelocation}，不是经 {@code completeResourceLocation()} 构建的完整路径</li>
     * </ol>
     *
     * <h3>location 参数格式</h3>
     * <pre>
     * location.getResourceDomain() = "gregtech"
     * location.getResourcePath()  = "iconsets/MACHINE_CASING_LASER"  // 不含 "blocks/" 前缀
     * </pre>
     *
     * <h3>实际文件路径</h3>
     * <pre>
     * assets/gregtech/textures/blocks/iconsets/MACHINE_CASING_LASER.png
     * └──────────────────────────────────────────────────────────────┘
     *                         ↓
     *              "textures/blocks/" + path + ".png"
     * </pre>
     *
     * <h3>常见 path 格式示例</h3>
     * <table border="1">
     *   <tr><th>textureName</th><th>location.getResourcePath()</th><th>构建的完整路径</th></tr>
     *   <tr><td>{@code gregtech:iconsets/MACHINE_CASING_LASER}</td>
     *       <td>{@code iconsets/MACHINE_CASING_LASER}</td>
     *       <td>{@code textures/blocks/iconsets/MACHINE_CASING_LASER.png}</td></tr>
     *   <tr><td>{@code gregtech:materialicons/SHINY/wire}</td>
     *       <td>{@code materialicons/SHINY/wire}</td>
     *       <td>{@code textures/blocks/materialicons/SHINY/wire.png}</td></tr>
     *   <tr><td>{@code gregtech:basicmachines/mixer/OVERLAY}</td>
     *       <td>{@code basicmachines/mixer/OVERLAY}</td>
     *       <td>{@code textures/blocks/basicmachines/mixer/OVERLAY.png}</td></tr>
     * </table>
     *
     * <h3>关于 items 图集</h3>
     * <p>
     * 当前 {@code NewTextureAtlasSprite} 仅用于 <strong>blocks 图集</strong>。
     * items 图集在 {@code MixinTextureMap.onRegisterIcon()} 第 95-97 行已提前返回：
     * </p>
     * <pre>{@code
     * if (basePath.contains("textures\\items") || basePath.contains("textures/items")) {
     *     return;  // items 图集不处理
     * }
     * }</pre>
     * <p>
     * 若未来需要支持 items 图集，需在此处判断图集类型，并构建对应的路径：
     * </p>
     * <pre>{@code
     * // 伪代码示例
     * String basePath = isItemsAtlas ? "textures/items/" : "textures/blocks/";
     * String resourcePath = basePath + path + ".png";
     * }</pre>
     */
    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        resetSprite();
        String path = location.getResourcePath();
        // NewTextureAtlasSprite 当前仅用于 blocks 图集，直接构建 "textures/blocks/xxx.png"
        String resourcePath = "textures/blocks/" + path + ".png";
        ResourceLocation fullLocation = new ResourceLocation(
            location.getResourceDomain(),
            resourcePath);
        try {
            IResource resource = manager.getResource(fullLocation);
            try (InputStream in = resource.getInputStream()) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return true;
                int w = img.getWidth();
                int h = img.getHeight();
                setIconWidth(w);
                setIconHeight(h);
                int[] pixels = new int[w * h];
                img.getRGB(0, 0, w, h, pixels, 0, w);
                int[][] oneFrame = new int[][] { pixels };
                java.util.List<int[][]> frameList = Collections.singletonList(oneFrame);
                setFramesTextureData(frameList);
                return false;
            }
        } catch (IOException e) {
            return true;
        }
    }

    private static String toTextureKey(ResourceLocation location) {
        if (location == null) return "";
        String domain = location.getResourceDomain();
        String path = location.getResourcePath();
        if (domain == null || domain.isEmpty()) return path;
        return domain + ":" + path;
    }

    private static TextureTypeData getConnectingData(String key) {
        TextureTypeData data = TextureRegistry.getInstance()
            .get(key);
        if (data == null && key != null && key.indexOf(':') >= 0) {
            data = TextureRegistry.getInstance()
                .get(key.substring(key.indexOf(':') + 1));
        }
        return data;
    }
}

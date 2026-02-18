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
     */
    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        resetSprite();
        String path = location.getResourcePath();
        String resourcePath;
        if (path.startsWith("textures/")) {
            resourcePath = path.endsWith(".png") ? path : path + ".png";
        } else if (path.startsWith("blocks/") || path.startsWith("items/") || path.startsWith("iconsets/")) {
            resourcePath = "textures/" + path + ".png";
        } else {
            resourcePath = "textures/blocks/" + path + ".png";
        }
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

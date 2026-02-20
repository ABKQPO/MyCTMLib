package com.github.wohaopa.MyCTMLib.texture;

/**
 * 纹理路径的目标类型。
 * <p>
 * 明确定义纹理路径字符串的去处，便于正确处理和转换。
 */
public enum TexturePathDestination {

    /**
     * TextureRegistry 的键。
     * <p>
     * 格式：{@code <domain>:<category>/<name>}
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "gregtech:blocks/iconsets/MACHINE_CASING_LASER"} - 纹理注册名</li>
     *   <li>{@code "minecraft:blocks/stone"} - Minecraft 原生</li>
     *   <li>{@code "ic2:blocks/blockAlloyGlass&5"} - IC2</li>
     * </ul>
     * <p>
     * 用途：在 TextureRegistry 中查找 ConnectingTextureData
     */
    TEXTURE_REGISTRY_KEY,

    /**
     * ResourceLocation 构造函数的参数。
     * <p>
     * 格式：{@code (domain, path)}，其中 path 不包含 {@code textures/} 前缀
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code new ResourceLocation("gregtech", "iconsets/MACHINE_CASING_LASER")} - 原始路径</li>
     *   <li>{@code new ResourceLocation("minecraft", "textures/blocks/stone.png")} - 完整路径（用于 ResourceManager）</li>
     * </ul>
     * <p>
     * 用途：创建 ResourceLocation 对象
     */
    RESOURCE_LOCATION,

    /**
     * ResourceManager.getResource() 的参数。
     * <p>
     * 格式：完整的 ResourceLocation，包含 {@code textures/} 前缀
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code new ResourceLocation("gregtech", "textures/blocks/iconsets/xxx.png")}</li>
     *   <li>{@code new ResourceLocation("minecraft", "textures/blocks/stone.png")}</li>
     * </ul>
     * <p>
     * 用途：从 ResourceManager 加载实际资源文件
     */
    RESOURCE_MANAGER,

    /**
     * mapRegisteredSprites 的键。
     * <p>
     * 格式：同 registerIcon() 参数
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "gregtech:iconsets/MACHINE_CASING_LASER"}</li>
     *   <li>{@code "stone"}</li>
     * </ul>
     * <p>
     * 用途：在 TextureMap 中查找已注册的 TextureAtlasSprite
     */
    MAP_REGISTERED_SPRITES,

    /**
     * 实际文件系统路径。
     * <p>
     * 格式：{@code assets/<domain>/textures/<category>/<path>.png}
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "assets/gregtech/textures/blocks/iconsets/MACHINE_CASING_LASER.png"}</li>
     *   <li>{@code "assets/minecraft/textures/blocks/stone.png"}</li>
     * </ul>
     * <p>
     * 用途：日志、dump 文件、调试信息
     */
    FILE_SYSTEM,

    /**
     * Debug HUD 显示。
     * <p>
     * 格式：人类可读的字符串
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "gregtech:iconsets/MACHINE_CASING_LASER"}</li>
     *   <li>{@code "16x16"}</li>
     * </ul>
     * <p>
     * 用途：DebugOverlayHandler 显示
     */
    DEBUG_DISPLAY
}

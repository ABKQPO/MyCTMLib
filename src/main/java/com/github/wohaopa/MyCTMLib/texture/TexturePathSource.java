package com.github.wohaopa.MyCTMLib.texture;

/**
 * 纹理路径的来源类型。
 * <p>
 * 明确定义纹理路径字符串的来源，便于正确处理和转换。
 */
public enum TexturePathSource {

    /**
     * 模型 JSON 的 textures 对象。
     * <p>
     * 格式：{@code <namespace>:block/<name>} 或 {@code <namespace>:item/<name>}
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "block/stone"} - Minecraft 原生方块</li>
     *   <li>{@code "ic2:block/blockAlloyGlass"} - IC2 方块</li>
     *   <li>{@code "item/diamond"} - Minecraft 原生物品</li>
     * </ul>
     * <p>
     * 特点：必须有 {@code block/} 或 {@code item/} 前缀（Minecraft 官方规范）
     */
    MODEL_TEXTURES,

    /**
     * Block.registerBlockIcons() 或 Item.registerIcons() 的参数。
     * <p>
     * 格式：由 Mod 自行决定，相对于 TextureMap.basePath
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "gregtech:iconsets/MACHINE_CASING_LASER"} - GregTech</li>
     *   <li>{@code "gregtech:materialicons/SHINY/wire"} - GregTech</li>
     *   <li>{@code "ic2:blockAlloyGlass&5"} - IC2</li>
     *   <li>{@code "stone"} - Minecraft 原生</li>
     * </ul>
     * <p>
     * 特点：不包含 {@code textures/blocks/} 前缀，由 TextureMap.completeResourceLocation() 拼接
     */
    REGISTER_ICON,

    /**
     * ResourceLocation.getResourcePath() 返回的路径。
     * <p>
     * 格式：取决于 ResourceLocation 的创建方式
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "iconsets/MACHINE_CASING_LASER"} - 原始路径（来自 registerIcon）</li>
     *   <li>{@code "textures/blocks/iconsets/xxx"} - 完整路径（来自 completeResourceLocation）</li>
     * </ul>
     * <p>
     * 特点：需要区分是原始的还是完整的
     */
    RESOURCE_LOCATION_PATH,

    /**
     * 配置文件中的纹理路径（如 CTM 配置）。
     * <p>
     * 格式：可能是完整路径或简化路径
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "minecraft:textures/blocks/stone.png"} - 完整路径</li>
     *   <li>{@code "gregtech:iconsets/xxx"} - 简化路径</li>
     * </ul>
     */
    CONFIG_FILE,

    /**
     * IC2TextureLoader 处理的纹理名称（来自 myctmlib.json 配置）。
     * <p>
     * 格式：可能是完整路径或简化路径
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "minecraft:textures/blocks/stone.png"} - 完整路径</li>
     *   <li>{@code "gregtech:iconsets/MACHINE_CASING_FUSION"} - 简化路径</li>
     * </ul>
     */
    IC2_CONFIG,

    /**
     * 模型 JSON 中的纹理引用（#key 形式）。
     * <p>
     * 格式：{@code #<key>}，需要解析为实际路径
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code "#all"} - 引用 textures 中的 "all" 键</li>
     *   <li>{@code "#particle"} - 引用 textures 中的 "particle" 键</li>
     * </ul>
     */
    MODEL_TEXTURE_REF
}

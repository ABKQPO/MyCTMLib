package com.github.wohaopa.MyCTMLib.texture;

import net.minecraft.util.ResourceLocation;

/**
 * 纹理路径转换工具。
 * <p>
 * 提供类型安全的路径转换方法，明确定义来源和去处，避免字符串格式混淆。
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 从模型 JSON textures 转换为 ResourceLocation（用于 ResourceManager）
 * String modelTexturePath = "block/stone";
 * ResourceLocation resLoc = TexturePathConverter.convert(
 *     TexturePathSource.MODEL_TEXTURES,
 *     TexturePathDestination.RESOURCE_MANAGER,
 *     modelTexturePath,
 *     "minecraft"
 * );
 * // 结果：ResourceLocation("minecraft", "textures/blocks/stone.png")
 *
 * // 从 registerIcon 参数转换为 TextureRegistry 键
 * String registerIconPath = "gregtech:iconsets/MACHINE_CASING_LASER";
 * String registryKey = TexturePathConverter.convert(
 *     TexturePathSource.REGISTER_ICON,
 *     TexturePathDestination.TEXTURE_REGISTRY_KEY,
 *     registerIconPath,
 *     null  // domain 已包含在路径中
 * );
 * // 结果："gregtech:iconsets/MACHINE_CASING_LASER"
 * }</pre>
 */
public final class TexturePathConverter {

    private TexturePathConverter() {}

    /**
     * 转换纹理路径。
     *
     * @param source      来源类型
     * @param destination 目标类型
     * @param path        原始路径字符串
     * @param domain      命名空间（可选，如果路径中已包含则传 null）
     * @return 转换后的路径字符串或 ResourceLocation
     * @throws IllegalArgumentException 如果转换不支持
     */
    public static Object convert(
        TexturePathSource source,
        TexturePathDestination destination,
        String path,
        String domain
    ) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        // 步骤 1：统一转换为规范化的中间格式（TextureRegistry 键格式）
        String canonicalKey = normalizeToCanonical(source, path, domain);

        // 步骤 2：从中间格式转换为目标格式
        return convertFromCanonical(canonicalKey, destination);
    }

    /**
     * 将各种来源的路径规范化为 canonical 格式。
     * <p>
     * Canonical 格式：{@code <domain>:<category>/<name>}
     */
    private static String normalizeToCanonical(TexturePathSource source, String path, String defaultDomain) {
        switch (source) {
            case MODEL_TEXTURES:
                // 模型纹理：必须有 block/ 或 item/ 前缀
                return TextureKeyNormalizer.toCanonicalTextureKey(defaultDomain, path);

            case REGISTER_ICON:
                // registerIcon 参数：可能包含 domain，也可能不包含
                if (path.contains(":")) {
                    return TextureKeyNormalizer.toCanonicalTextureKey(path);
                } else {
                    return TextureKeyNormalizer.toCanonicalTextureKey(defaultDomain, path);
                }

            case RESOURCE_LOCATION_PATH:
                // ResourceLocation.getResourcePath() 返回的路径
                // 需要调用方提供 domain
                return TextureKeyNormalizer.toCanonicalTextureKey(defaultDomain, path);

            case CONFIG_FILE:
            case IC2_CONFIG:
                // 配置文件：可能包含完整路径，需要清理
                String cleaned = cleanConfigPath(path);
                return TextureKeyNormalizer.toCanonicalTextureKey(cleaned);

            case MODEL_TEXTURE_REF:
                // #key 引用，需要解析
                throw new IllegalArgumentException(
                    "MODEL_TEXTURE_REF requires texture map for resolution, use resolveModelTextureRef() instead"
                );

            default:
                throw new IllegalArgumentException("Unknown source type: " + source);
        }
    }

    /**
     * 从 canonical 格式转换为目标格式。
     */
    private static Object convertFromCanonical(String canonicalKey, TexturePathDestination destination) {
        switch (destination) {
            case TEXTURE_REGISTRY_KEY:
                // 直接返回 canonical 格式
                return canonicalKey;

            case RESOURCE_LOCATION:
                // 转换为 ResourceLocation（原始路径，不含 textures/ 前缀）
                return createResourceLocation(canonicalKey, false);

            case RESOURCE_MANAGER:
                // 转换为 ResourceLocation（完整路径，含 textures/ 前缀）
                return createResourceLocation(canonicalKey, true);

            case MAP_REGISTERED_SPRITES:
                // 同 registerIcon 参数
                return canonicalKey;

            case FILE_SYSTEM:
                // 转换为完整文件路径
                return canonicalToFileSystemPath(canonicalKey);

            case DEBUG_DISPLAY:
                // 人类可读格式（同 canonical）
                return canonicalKey;

            default:
                throw new IllegalArgumentException("Unknown destination type: " + destination);
        }
    }

    /**
     * 清理配置文件中的路径。
     */
    private static String cleanConfigPath(String path) {
        // 移除可能的前缀和后缀
        String cleaned = path
            .replace("minecraft:", "")
            .replace("textures/blocks/", "")
            .replace("textures/items/", "")
            .replace(".png", "");
        return cleaned.trim();
    }

    /**
     * 从 canonical 格式创建 ResourceLocation。
     *
     * @param canonicalKey canonical 格式
     * @param fullPath     是否包含 textures/ 前缀
     * @return ResourceLocation
     */
    private static ResourceLocation createResourceLocation(String canonicalKey, boolean fullPath) {
        int colon = canonicalKey.indexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException("Invalid canonical key: " + canonicalKey);
        }

        String domain = canonicalKey.substring(0, colon);
        String pathPart = canonicalKey.substring(colon + 1);

        String resourcePath;
        if (fullPath) {
            // 完整路径：textures/blocks/xxx.png 或 textures/items/xxx.png
            resourcePath = "textures/" + pathPart + ".png";
        } else {
            // 原始路径：blocks/xxx 或 iconsets/xxx
            resourcePath = pathPart;
        }

        return new ResourceLocation(domain, resourcePath);
    }

    /**
     * 从 canonical 格式转换为文件系统路径。
     */
    private static String canonicalToFileSystemPath(String canonicalKey) {
        int colon = canonicalKey.indexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException("Invalid canonical key: " + canonicalKey);
        }

        String domain = canonicalKey.substring(0, colon);
        String pathPart = canonicalKey.substring(colon + 1);

        return "assets/" + domain + "/textures/" + pathPart + ".png";
    }

    /**
     * 解析模型纹理引用（#key 形式）。
     *
     * @param textureRef   纹理引用，如 "#all"
     * @param textureMap   模型的 textures 对象
     * @return 解析后的路径（MODEL_TEXTURES 格式）
     */
    public static String resolveModelTextureRef(String textureRef, java.util.Map<String, String> textureMap) {
        if (textureRef == null || !textureRef.startsWith("#")) {
            return textureRef;  // 不是引用，直接返回
        }

        String key = textureRef.substring(1).trim();
        String resolved = textureMap.get(key);

        if (resolved == null) {
            // 尝试不带 # 的键
            resolved = textureMap.get(textureRef);
        }

        if (resolved == null) {
            throw new IllegalArgumentException("Cannot resolve texture reference: " + textureRef);
        }

        // 递归解析（处理 #key -> #all -> stone 这样的链式引用）
        if (resolved.startsWith("#")) {
            return resolveModelTextureRef(resolved, textureMap);
        }

        return resolved;
    }

    // ========================================================================
    // 便捷方法：常用转换的快捷方式
    // ========================================================================

    /**
     * 便捷方法：从模型纹理路径转换为 ResourceLocation（用于 ResourceManager）。
     */
    public static ResourceLocation modelToResourceLocation(String modelTexturePath, String domain) {
        return (ResourceLocation) convert(
            TexturePathSource.MODEL_TEXTURES,
            TexturePathDestination.RESOURCE_MANAGER,
            modelTexturePath,
            domain
        );
    }

    /**
     * 便捷方法：从 registerIcon 参数转换为 TextureRegistry 键。
     */
    public static String registerIconToRegistryKey(String registerIconPath) {
        return (String) convert(
            TexturePathSource.REGISTER_ICON,
            TexturePathDestination.TEXTURE_REGISTRY_KEY,
            registerIconPath,
            null
        );
    }

    /**
     * 便捷方法：从 registerIcon 参数转换为 ResourceManager ResourceLocation。
     */
    public static ResourceLocation registerIconToResourceLocation(String registerIconPath) {
        return (ResourceLocation) convert(
            TexturePathSource.REGISTER_ICON,
            TexturePathDestination.RESOURCE_MANAGER,
            registerIconPath,
            null
        );
    }

    /**
     * 便捷方法：从 canonical 键转换为文件系统路径。
     */
    public static String toFileSystemPath(String canonicalKey) {
        return (String) convert(
            TexturePathSource.REGISTER_ICON,  // canonical 键与 registerIcon 格式相同
            TexturePathDestination.FILE_SYSTEM,
            canonicalKey,
            null
        );
    }
}

package com.github.wohaopa.MyCTMLib;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * CTM配置解析器
 * 负责解析myctmlib.json配置文件中的各种配置项
 */
public class CTMConfig {
    
    private final JsonObject ctmObj;
    
    public CTMConfig(JsonObject ctmObj) {
        this.ctmObj = ctmObj;
    }
    
    /**
     * 获取连接纹理配置
     */
    public ConnectionConfig getConnection() {
        JsonPrimitive connectionPrimitive = ctmObj.getAsJsonPrimitive("connection");
        if (connectionPrimitive == null) {
            return null;
        }
        
        String connectTexture = connectionPrimitive.getAsString();
        if (connectTexture == null || connectTexture.isEmpty()) {
            return null;
        }
        
        return new ConnectionConfig(connectTexture);
    }
    
    /**
     * 获取备用纹理配置
     */
    public AltConfig getAlt() {
        JsonPrimitive altPrimitive = ctmObj.getAsJsonPrimitive("alt");
        if (altPrimitive == null) {
            return null;
        }
        
        String altTexture = altPrimitive.getAsString();
        if (altTexture == null || altTexture.isEmpty()) {
            return null;
        }
        
        return new AltConfig(altTexture);
    }
    
    /**
     * 获取随机纹理配置
     */
    public RandomConfig getRandom() {
        if (!ctmObj.has("random")) {
            return null;
        }
        
        JsonElement randomElement = ctmObj.get("random");
        if (!randomElement.isJsonArray()) {
            return null;
        }
        
        return new RandomConfig(randomElement.getAsJsonArray());
    }
    
    /**
     * 获取等价纹理配置
     */
    public EquivalentsConfig getEquivalents() {
        if (!ctmObj.has("equivalents")) {
            return null;
        }
        
        JsonArray arr = ctmObj.getAsJsonArray("equivalents");
        return new EquivalentsConfig(arr);
    }
    
    /**
     * 连接纹理配置
     */
    public static class ConnectionConfig {
        private final String originalTexture;
        private final String processedTexture;
        
        public ConnectionConfig(String originalTexture) {
            this.originalTexture = originalTexture;
            this.processedTexture = processTextureName(originalTexture);
        }
        
        public String getOriginalTexture() {
            return originalTexture;
        }
        
        public String getProcessedTexture() {
            return processedTexture;
        }
        
        public boolean isGregTechCasing() {
            return processedTexture.startsWith("gregtech:iconsets/MACHINE_CASING_FUSION_")
                && originalTexture.endsWith("_ctm");
        }
        
        public boolean isMiscUtilsCasing() {
            return processedTexture.startsWith("miscutils:iconsets/MACHINE_CASING_FUSION_")
                && originalTexture.endsWith("_ctm");
        }
        
        public boolean isBoronSilicateGlass() {
            return originalTexture.contains("BoronSilicateGlass") 
                && processedTexture.endsWith("_ctm");
        }
        
        private String processTextureName(String texture) {
            return texture.replace("minecraft:", "")
                .replace("textures/blocks/", "")
                .replace(".png", "");
        }
    }
    
    /**
     * 备用纹理配置
     */
    public static class AltConfig {
        private final String originalTexture;
        private final String processedTexture;
        
        public AltConfig(String originalTexture) {
            this.originalTexture = originalTexture;
            this.processedTexture = processTextureName(originalTexture);
        }
        
        public String getOriginalTexture() {
            return originalTexture;
        }
        
        public String getProcessedTexture() {
            return processedTexture;
        }
        
        private String processTextureName(String texture) {
            return texture.replace("minecraft:", "")
                .replace("textures/blocks/", "")
                .replace(".png", "");
        }
    }
    
    /**
     * 随机纹理配置
     */
    public static class RandomConfig {
        private final List<String> originalTextures;
        private final List<String> processedTextures;
        
        public RandomConfig(JsonArray randomArray) {
            this.originalTextures = new ArrayList<>();
            this.processedTextures = new ArrayList<>();
            
            // 处理原始纹理列表
            for (JsonElement element : randomArray) {
                String originalTexture = element.getAsString();
                String processedTexture = processTextureName(originalTexture);
                
                originalTextures.add(originalTexture);
                processedTextures.add(processedTexture);
            }
        }
        
        public List<String> getOriginalTextures() {
            return originalTextures;
        }
        
        public List<String> getProcessedTextures() {
            return processedTextures;
        }
        
        private String processTextureName(String texture) {
            return texture.replace("minecraft:", "")
                .replace("textures/blocks/", "")
                .replace(".png", "");
        }
    }
    
    /**
     * 等价纹理配置
     */
    public static class EquivalentsConfig {
        private final List<String> equivalents;
        
        public EquivalentsConfig(JsonArray arr) {
            this.equivalents = new ArrayList<>();
            
            for (JsonElement el : arr) {
                String eq = el.getAsString()
                    .replace("minecraft:", "")
                    .replace("textures/blocks/", "")
                    .replace(".png", "");
                equivalents.add(eq);
            }
        }
        
        public List<String> getEquivalents() {
            return equivalents;
        }
        
        public String[] getEquivalentsArray() {
            return equivalents.toArray(new String[equivalents.size()]);
        }
        
        public boolean hasEquivalents() {
            return !equivalents.isEmpty();
        }
    }
}

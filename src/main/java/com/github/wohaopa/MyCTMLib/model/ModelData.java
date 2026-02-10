package com.github.wohaopa.MyCTMLib.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 模型数据：elements、textures、connections（type 为 connection 时）。
 * 由 ModelParser 解析 JSON 产出。
 */
public class ModelData {

    private final String type;
    private final List<ModelElement> elements;
    private final Map<String, String> textures;
    /** 谓词键 → 谓词 JSON 或引用名（如 "#blue"）。具体谓词由 PredicateRegistry 后续解析。 */
    private final Map<String, Object> connections;

    public ModelData(String type, List<ModelElement> elements, Map<String, String> textures,
        Map<String, Object> connections) {
        this.type = type;
        this.elements = elements != null ? Collections.unmodifiableList(elements) : Collections.emptyList();
        this.textures = textures != null ? Collections.unmodifiableMap(textures) : Collections.emptyMap();
        this.connections = connections != null ? Collections.unmodifiableMap(connections) : Collections.emptyMap();
    }

    public String getType() {
        return type;
    }

    public List<ModelElement> getElements() {
        return elements;
    }

    public Map<String, String> getTextures() {
        return textures;
    }

    public Map<String, Object> getConnections() {
        return connections;
    }
}

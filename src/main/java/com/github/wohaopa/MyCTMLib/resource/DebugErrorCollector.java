package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Debug 模式下收集注册/加载流程中的解析、反序列化等异常，序列化后写入 config/ctmlib_debug_errors.json。
 */
@SideOnly(Side.CLIENT)
public class DebugErrorCollector {

    private static final DebugErrorCollector INSTANCE = new DebugErrorCollector();

    private final List<JsonObject> entries = new ArrayList<>();

    public static DebugErrorCollector getInstance() {
        return INSTANCE;
    }

    public synchronized void add(String stage, String location, Throwable t) {
        add(stage, location, null, t);
    }

    /**
     * 收集预载阶段异常。
     *
     * @param stage              阶段：blockstate / model / texture_prefill
     * @param location           逻辑位置（如 blockId、modelId、lookupKey）
     * @param attemptedResourcePath 尝试加载的资源路径，如 assets/domain/path（文件未找到时必填）
     * @param t                  异常
     */
    public synchronized void add(String stage, String location, String attemptedResourcePath, Throwable t) {
        if (t == null) return;
        JsonArray stackTrace = new JsonArray();
        for (StackTraceElement el : t.getStackTrace()) {
            stackTrace.add(new JsonPrimitive(el.toString()));
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("stage", stage);
        obj.addProperty("location", location);
        if (attemptedResourcePath != null && !attemptedResourcePath.isEmpty()) {
            obj.addProperty("attemptedPath", attemptedResourcePath);
        }
        obj.addProperty("exceptionClass", t.getClass()
            .getName());
        obj.addProperty("message", t.getMessage() != null ? t.getMessage() : "");
        obj.add("stackTrace", stackTrace);
        entries.add(obj);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void flushToFile(File file) {
        if (entries.isEmpty()) return;
        try {
            JsonObject root = new JsonObject();
            root.addProperty("timestamp", Instant.now()
                .toString());
            JsonArray arr = new JsonArray();
            for (JsonObject e : entries) {
                arr.add(e);
            }
            root.add("entries", arr);
            file.getParentFile()
                .mkdirs();
            try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, w);
            }
        } catch (Exception e) {
            com.github.wohaopa.MyCTMLib.MyCTMLib.LOG.warn("DebugErrorCollector flush failed", e);
        }
    }
}
